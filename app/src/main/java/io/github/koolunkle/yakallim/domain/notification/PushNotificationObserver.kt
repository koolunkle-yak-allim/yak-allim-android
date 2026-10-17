package io.github.koolunkle.yakallim.domain.notification

import kotlinx.coroutines.flow.Flow

interface PushNotificationObserver {
    val messages: Flow<PushMessage>

    suspend fun emitMessage(message: PushMessage)
}
