package io.github.koolunkle.yakallim.domain.notification

import kotlinx.coroutines.flow.Flow

interface PushTokenProvider {
    val fcmToken: Flow<String>

    suspend fun getFcmToken(): String?
    suspend fun emitToken(token: String)
    fun registerFcmToken()
}
