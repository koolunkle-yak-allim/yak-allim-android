package io.github.koolunkle.yakallim.di

import io.github.koolunkle.yakallim.data.infrastructure.alarm.AlarmDispatcherImpl
import io.github.koolunkle.yakallim.data.infrastructure.alarm.AlarmSchedulerImpl
import io.github.koolunkle.yakallim.data.infrastructure.fcm.FcmPushNotificationObserverImpl
import io.github.koolunkle.yakallim.data.infrastructure.fcm.FcmPushTokenProviderImpl
import io.github.koolunkle.yakallim.data.infrastructure.image.ImageProcessorImpl
import io.github.koolunkle.yakallim.domain.alarm.AlarmDispatcher
import io.github.koolunkle.yakallim.domain.alarm.AlarmScheduler
import io.github.koolunkle.yakallim.domain.image.ImageProcessor
import io.github.koolunkle.yakallim.domain.notification.PushNotificationObserver
import io.github.koolunkle.yakallim.domain.notification.PushTokenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InfrastructureModule {

    @Binds
    @Singleton
    abstract fun bindPushNotificationObserver(fcmPushNotificationObserverImpl: FcmPushNotificationObserverImpl): PushNotificationObserver

    @Binds
    @Singleton
    abstract fun bindPushTokenProvider(fcmPushTokenProviderImpl: FcmPushTokenProviderImpl): PushTokenProvider

    @Binds
    @Singleton
    abstract fun bindAlarmDispatcher(
        alarmDispatcherImpl: AlarmDispatcherImpl
    ): AlarmDispatcher

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(
        alarmSchedulerImpl: AlarmSchedulerImpl
    ): AlarmScheduler

    @Binds
    @Singleton
    abstract fun bindImageProcessor(
        imageProcessorImpl: ImageProcessorImpl
    ): ImageProcessor
}
