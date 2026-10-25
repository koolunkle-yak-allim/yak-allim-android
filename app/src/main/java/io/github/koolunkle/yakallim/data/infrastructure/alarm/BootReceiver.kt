package io.github.koolunkle.yakallim.data.infrastructure.alarm

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

        private val RESCHEDULE_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
        )
    }
}
