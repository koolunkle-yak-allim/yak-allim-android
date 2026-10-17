package io.github.koolunkle.yakallim.data.infrastructure.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.koolunkle.yakallim.domain.alarm.AlarmDispatcher
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class AlarmReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AlarmReceiverEntryPoint {
        fun alarmDispatcher(): AlarmDispatcher
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(AlarmExtraSpec.KEY_MEDICINE_NAME) ?: return
        val dosage = intent.getStringExtra(AlarmExtraSpec.KEY_DOSAGE_PER_TAKE) ?: "1"
        val frequency = intent.getIntExtra(AlarmExtraSpec.KEY_DAILY_FREQUENCY, 1)
        val duration = intent.getIntExtra(AlarmExtraSpec.KEY_DURATION_DAYS, 1)
        val soundUri = AlarmExtraSpec.readSoundUri(intent)

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AlarmReceiverEntryPoint::class.java
        )
        val dispatcher = entryPoint.alarmDispatcher()

        dispatcher.notifyAlarm(
            medicineName = title,
            dosagePerTake = dosage,
            dailyFrequency = frequency,
            durationDays = duration,
            soundUri = soundUri
        )
    }
}