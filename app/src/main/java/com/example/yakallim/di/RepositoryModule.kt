package com.example.yakallim.di

import android.content.Context
import com.example.yakallim.data.datasource.local.OcrLocalDataSource
import com.example.yakallim.data.datasource.remote.OcrRemoteDataSource
import com.example.yakallim.data.repository.OcrRepositoryImpl
import com.example.yakallim.domain.image.ImageProcessor
import com.example.yakallim.domain.notification.PushTokenProvider
import com.example.yakallim.domain.repository.OcrRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideOcrRepository(
        ocrRemoteDataSource: OcrRemoteDataSource,
        imageProcessor: ImageProcessor,
        pushTokenProvider: PushTokenProvider,
        ocrLocalDataSource: OcrLocalDataSource,
        json: Json,
        @ApplicationContext context: Context
    ): OcrRepository {
        return OcrRepositoryImpl(
            ocrRemoteDataSource,
            imageProcessor,
            pushTokenProvider,
            ocrLocalDataSource,
            json,
            context
        )
    }
}
