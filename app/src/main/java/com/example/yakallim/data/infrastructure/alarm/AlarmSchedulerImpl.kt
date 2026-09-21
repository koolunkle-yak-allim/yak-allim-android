package com.example.yakallim.data.infrastructure.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.yakallim.data.datasource.local.preference.AlarmPreference
import com.example.yakallim.domain.alarm.AlarmScheduler
import com.example.yakallim.domain.alarm.AlarmTimeCalculator
import com.example.yakallim.domain.model.Alarm
import com.example.yakallim.domain.model.AlarmSpec
import com.example.yakallim.util.alarmManager
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
    private val zone: ZoneId = ZoneId.systemDefault()

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
            startEpochMillis = System.currentTimeMillis()
        )

        armFutureTriggers(spec)

        alarmPreference.saveAlarmSpecJson(medicineName, json.encodeToString(spec))
        alarmPreference.addActiveAlarmMedicine(medicineName)
    }

    override suspend fun cancel(medicineName: String) {
        val spec = loadSpec(medicineName) ?: return

        AlarmTimeCalculator.allTriggers(spec, zone).forEach { trigger ->
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                setPackage(context.packageName)
            }
            val requestCode = AlarmTimeCalculator.requestCode(medicineName, trigger.day, trigger.timeIndex)
            val pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }

        alarmPreference.removeAlarmSpecJson(medicineName)
        alarmPreference.removeActiveAlarmMedicine(medicineName)
    }

    override suspend fun getActiveAlarm(): Set<String> {
        return alarmPreference.getActiveAlarmMedicines()
    }

    override suspend fun getDetailAlarm(medicineName: String): Alarm? {
        val spec = loadSpec(medicineName) ?: return null
        return Alarm(spec.alarmTimes, spec.soundUri)
    }

    /** 재부팅 등으로 예약이 모두 사라졌을 때, 저장된 모든 활성 알람을 남은 미래 시각 기준으로 다시 건다. */
    override suspend fun rescheduleAll() {
        alarmPreference.getActiveAlarmMedicines().forEach { medicineName ->
            loadSpec(medicineName)?.let { spec -> armFutureTriggers(spec) }
        }
    }

    private suspend fun loadSpec(medicineName: String): AlarmSpec? {
        val specJson = alarmPreference.getAlarmSpecJson(medicineName) ?: return null
        return runCatching { json.decodeFromString<AlarmSpec>(specJson) }.getOrNull()
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
