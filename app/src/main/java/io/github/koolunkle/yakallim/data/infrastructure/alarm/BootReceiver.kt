package io.github.koolunkle.yakallim.data.infrastructure.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.github.koolunkle.yakallim.domain.alarm.AlarmScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 저장된 [io.github.koolunkle.yakallim.domain.model.AlarmSpec] 기준으로 알람을 다시 등록한다.
 * 재부팅하면 예약이 사라지고, 시간대나 시각이 바뀌면 "아침 8시" 같은 복용 시각의 실제 발생 시점이 달라지기 때문이다.
 * 정확한 알람 권한을 (다시) 받았을 때도 다시 건다. 권한을 끄면 시스템이 정확한 알람을 모두 지우고, 권한이 없을 때
 * 건 알람은 몇 분씩 늦을 수 있는 일반 알람이라, 권한을 받은 시점에 정확한 알람으로 바꿔 둔다.
 */
class BootReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootReceiverEntryPoint {
        fun alarmScheduler(): AlarmScheduler
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in RESCHEDULE_ACTIONS) return

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            BootReceiverEntryPoint::class.java
        )
        val alarmScheduler = entryPoint.alarmScheduler()

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                alarmScheduler.rescheduleAll()
            } catch (e: Exception) {
                Log.e(TAG, "알람 재등록 실패: ${intent.action}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"

        // 권한 변경 액션은 API 31부터 오지만, 문자열을 비교만 하므로 더 낮은 버전에서도 안전하다.
        @SuppressLint("InlinedApi")
        private val RESCHEDULE_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
        )
    }
}
