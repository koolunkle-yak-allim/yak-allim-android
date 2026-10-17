package io.github.koolunkle.yakallim.data.infrastructure.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import io.github.koolunkle.yakallim.MainActivity
import io.github.koolunkle.yakallim.R
import io.github.koolunkle.yakallim.data.datasource.local.FcmLocalDataSource
import io.github.koolunkle.yakallim.data.datasource.local.OcrLocalDataSource
import io.github.koolunkle.yakallim.domain.notification.PushNotificationObserver
import io.github.koolunkle.yakallim.domain.notification.PushTokenProvider
import io.github.koolunkle.yakallim.domain.notification.PushMessage
import io.github.koolunkle.yakallim.domain.model.JobStatus
import io.github.koolunkle.yakallim.ui.ocr.OcrExtraSpec
import io.github.koolunkle.yakallim.util.notificationManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FcmMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FcmMessagingService"
        private const val CHANNEL_ID = "OCR_FCM_CHANNEL"
    }

    @Inject
    lateinit var pushNotificationObserver: PushNotificationObserver

    @Inject
    lateinit var pushTokenProvider: PushTokenProvider

    @Inject
    lateinit var fcmLocalDataSource: FcmLocalDataSource

    @Inject
    lateinit var ocrLocalDataSource: OcrLocalDataSource

    private val supervisorJob = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + supervisorJob)

    override fun onRegistered(token: String) {
        super.onRegistered(token)
        coroutineScope.launch {
            fcmLocalDataSource.saveFcmToken(token)
            pushTokenProvider.emitToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val jobId = message.data[FcmPayloadSpec.KEY_JOB_ID]
        val status = message.data[FcmPayloadSpec.KEY_STATUS]

        if (jobId == null) {
            Log.w(TAG, "FCM 수신 에러: 작업 ID 누락")
            return
        }

        coroutineScope.launch {
            if (ocrLocalDataSource.isAnalysisCancelled(jobId)) {
                Log.i(TAG, "FCM 수신 제외: 이미 취소된 작업 (작업 ID: $jobId)")
                ocrLocalDataSource.removeAnalysisCancelled(jobId)
                return@launch
            }

            val isSuccess = status?.equals(FcmPayloadSpec.STATUS_COMPLETED, ignoreCase = true) == true
            val errorMessage = message.data[FcmPayloadSpec.KEY_ERROR]

            pushNotificationObserver.emitMessage(
                PushMessage(
                    jobId = jobId,
                    status = if (isSuccess) JobStatus.COMPLETED else JobStatus.FAILED,
                    errorMessage = errorMessage
                )
            )

            val title = message.notification?.title
                ?: if (isSuccess) getString(R.string.notification_fcm_completed_title) else getString(R.string.notification_fcm_failed_title)

            val body = message.notification?.body
                ?: if (isSuccess) getString(R.string.notification_fcm_completed_body)
                else errorMessage ?: getString(R.string.notification_fcm_failed_body)

            showNotification(title, body, jobId)
        }
    }

    private fun showNotification(title: String, body: String, jobId: String?) {
        val notificationManager = notificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_fcm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_fcm_channel_name)
                enableLights(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.setPackage(packageName)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        // 서버가 보내는 FCM 페이로드 키(FcmPayloadSpec)와 달리, 이 Intent를 읽는 화면의 키를 쓴다.
        intent.putExtra(OcrExtraSpec.KEY_JOB_ID, jobId)
        val pendingIntent = PendingIntent.getActivity(
            this,
            jobId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(jobId?.hashCode() ?: 0, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        supervisorJob.cancel()
    }
}
