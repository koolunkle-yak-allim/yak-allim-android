package io.github.koolunkle.yakallim.data.infrastructure.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import io.github.koolunkle.yakallim.data.datasource.local.preference.AlarmPreference
import io.github.koolunkle.yakallim.domain.alarm.AlarmScheduler
import io.github.koolunkle.yakallim.domain.alarm.AlarmTimeCalculator
import io.github.koolunkle.yakallim.domain.model.Alarm
import io.github.koolunkle.yakallim.domain.model.AlarmSpec
import io.github.koolunkle.yakallim.util.alarmManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmSchedulerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val alarmPreference: AlarmPreference,
    private val json: Json
) : AlarmScheduler {

    private val alarmManager = context.alarmManager
        ?: throw IllegalStateException("AlarmManager service is not available")
    // 사용자가 시간대를 바꿀 수 있으므로 매번 기기의 현재 시간대를 읽는다.
    private val zone: ZoneId get() = ZoneId.systemDefault()

    override suspend fun schedule(
        medicineName: String,
        dosagePerTake: String,
        dailyFrequency: Int,
        durationDays: Int,
        alarmTimes: List<String>,
        soundUri: String?
    ) {
        if (dailyFrequency <= 0 || durationDays <= 0 || alarmTimes.isEmpty()) return

        val spec = AlarmSpec(
            medicineName = medicineName,
            dosagePerTake = dosagePerTake,
            dailyFrequency = dailyFrequency,
            durationDays = durationDays,
            alarmTimes = alarmTimes,
            soundUri = soundUri,
            startEpochMillis = System.currentTimeMillis(),
            startZoneId = zone.id
        )

        armFutureTriggers(spec)

        alarmPreference.saveAlarmSpecJson(medicineName, json.encodeToString(spec))
        alarmPreference.addActiveAlarmMedicine(medicineName)
    }

    override suspend fun cancel(medicineName: String) {
        val spec = loadSpec(medicineName) ?: return

        cancelTriggers(spec)

        alarmPreference.removeAlarmSpecJson(medicineName)
        alarmPreference.removeActiveAlarmMedicine(medicineName)
    }

    /** 아직 울릴 알람이 남은 약만 반환한다. 복용 기간이 끝난 약은 이때 저장소에서 정리한다. */
    override suspend fun getActiveAlarm(): Set<String> {
        val now = System.currentTimeMillis()
        return alarmPreference.getActiveAlarmMedicines().filterTo(mutableSetOf()) { medicineName ->
            val spec = loadSpec(medicineName)
            val isActive = spec != null && !AlarmTimeCalculator.isFinished(spec, now, zone)
            if (!isActive) {
                alarmPreference.removeAlarmSpecJson(medicineName)
                alarmPreference.removeActiveAlarmMedicine(medicineName)
            }
            isActive
        }
    }

    override suspend fun getAlarmDetail(medicineName: String): Alarm? {
        val spec = loadSpec(medicineName) ?: return null
        return Alarm(spec.alarmTimes, spec.soundUri)
    }

    /**
     * 저장된 모든 활성 알람을 지금 시간대의 남은 시각으로 다시 건다. 재부팅과 시간대·시각 변경 때 부른다.
     * 시간대가 바뀌면 같은 칸의 발생 시각이 달라져 예전 예약이 남을 수 있으므로, 먼저 모든 칸을 지우고 건다.
     */
    override suspend fun rescheduleAll() {
        getActiveAlarm().forEach { medicineName ->
            loadSpec(medicineName)?.let { spec ->
                cancelTriggers(spec)
                armFutureTriggers(spec)
            }
        }
    }

    private suspend fun loadSpec(medicineName: String): AlarmSpec? {
        val specJson = alarmPreference.getAlarmSpecJson(medicineName) ?: return null
        return runCatching { json.decodeFromString<AlarmSpec>(specJson) }.getOrNull()
    }

    /** 요청 코드는 약품명과 day·시각 칸으로만 정해지므로, 시간대와 상관없이 모든 칸의 예약을 지운다. */
    private fun cancelTriggers(spec: AlarmSpec) {
        AlarmTimeCalculator.allTriggers(spec, zone).forEach { trigger ->
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                setPackage(context.packageName)
            }
            val requestCode = AlarmTimeCalculator.requestCode(spec.medicineName, trigger.day, trigger.timeIndex)
            val pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    private fun armFutureTriggers(spec: AlarmSpec) {
        val now = System.currentTimeMillis()
        AlarmTimeCalculator.triggerTimes(spec, now, zone).forEach { trigger ->
            scheduleExact(trigger.triggerMillis, createPendingIntent(spec, trigger))
        }
    }

    private fun scheduleExact(triggerMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    private fun createPendingIntent(spec: AlarmSpec, trigger: AlarmTimeCalculator.Trigger): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            setPackage(context.packageName)
            putExtra(AlarmExtraSpec.KEY_MEDICINE_NAME, spec.medicineName)
            putExtra(AlarmExtraSpec.KEY_DOSAGE_PER_TAKE, spec.dosagePerTake)
            putExtra(AlarmExtraSpec.KEY_DAILY_FREQUENCY, spec.dailyFrequency)
            putExtra(AlarmExtraSpec.KEY_DURATION_DAYS, spec.durationDays)
            putExtra(AlarmExtraSpec.KEY_SOUND_URI, spec.soundUri)
        }
        val requestCode = AlarmTimeCalculator.requestCode(spec.medicineName, trigger.day, trigger.timeIndex)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
