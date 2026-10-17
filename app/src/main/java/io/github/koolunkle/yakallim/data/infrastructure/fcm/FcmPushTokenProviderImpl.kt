package io.github.koolunkle.yakallim.data.infrastructure.fcm

import android.util.Log
import io.github.koolunkle.yakallim.data.datasource.local.FcmLocalDataSource
import io.github.koolunkle.yakallim.domain.notification.PushTokenProvider
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmPushTokenProviderImpl @Inject constructor(
    private val fcmLocalDataSource: FcmLocalDataSource
) : PushTokenProvider {

    private val _fcmToken = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val fcmToken: Flow<String> = _fcmToken

    override suspend fun getFcmToken(): String? {
        val cachedToken = fcmLocalDataSource.getFcmToken()
        if (!cachedToken.isNullOrBlank()) {
            return cachedToken
        }

        registerFcmToken()

        return withTimeoutOrNull(5000L) { fcmToken.first() } ?: run {
            Log.w("FcmPushTokenProviderImpl", "FCM 토큰 등록 시간 초과")
            null
        }
    }

    override suspend fun emitToken(token: String) {
        _fcmToken.emit(token)
    }

    override fun registerFcmToken() {
        try {
            FirebaseMessaging.getInstance().register()
        } catch (e: Exception) {
            Log.e("FcmPushTokenProviderImpl", "FCM 토큰 등록 요청 실패", e)
        }
    }
}
