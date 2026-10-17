package io.github.koolunkle.yakallim.data.infrastructure.alarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import io.github.koolunkle.yakallim.MainActivity
import io.github.koolunkle.yakallim.R
import io.github.koolunkle.yakallim.domain.alarm.AlarmDispatcher
import io.github.koolunkle.yakallim.util.notificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmDispatcherImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AlarmDispatcher {


    companion object {
        private const val CHANNEL_ID = "MEDICINE_ALARM_CHANNEL"
    }

    override fun notifyAlarm(
        medicineName: String,
        dosagePerTake: String,
        dailyFrequency: Int,
        durationDays: Int,
        soundUri: String?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notificationManager = context.notificationManager ?: return

        val instruction = context.getString(
            R.string.alarm_card_format,
            dailyFrequency,
            durationDays,
            dosagePerTake
        )
        val soundUriObj = soundUri?.toUri()

        val channelId = if (soundUri != null) {
            "${CHANNEL_ID}_${soundUri.hashCode()}"
        } else {
            "${CHANNEL_ID}_DEFAULT"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.alarm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.alarm_channel_desc)
                if (soundUriObj != null) {
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    setSound(soundUriObj, audioAttributes)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = medicineName.hashCode()
        val contentIntent = Intent(context, MainActivity::class.java)
        contentIntent.setPackage(context.packageName)
        contentIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        contentIntent.data = "yakallim://alarm/$notificationId".toUri()

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.alarm_content_title_format, medicineName))
            .setContentText(context.getString(R.string.alarm_content_text))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.alarm_big_text_format, instruction))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .apply {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && soundUriObj != null) {
                    setSound(soundUriObj)
                }
            }
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
