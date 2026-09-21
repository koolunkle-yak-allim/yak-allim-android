package com.example.yakallim.di

import com.example.yakallim.data.infrastructure.alarm.AlarmDispatcherImpl
import com.example.yakallim.data.infrastructure.alarm.AlarmSchedulerImpl
import com.example.yakallim.data.infrastructure.fcm.FcmPushNotificationObserverImpl
import com.example.yakallim.data.infrastructure.fcm.FcmPushTokenProviderImpl
import com.example.yakallim.data.infrastructure.image.ImageProcessorImpl
import com.example.yakallim.domain.alarm.AlarmDispatcher
import com.example.yakallim.domain.alarm.AlarmScheduler
import com.example.yakallim.domain.image.ImageProcessor
import com.example.yakallim.domain.notification.PushNotificationObserver
import com.example.yakallim.domain.notification.PushTokenProvider
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
