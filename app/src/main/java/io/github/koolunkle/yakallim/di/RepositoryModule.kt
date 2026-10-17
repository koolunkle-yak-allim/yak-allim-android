package io.github.koolunkle.yakallim.di

import android.content.Context
import io.github.koolunkle.yakallim.data.datasource.local.OcrLocalDataSource
import io.github.koolunkle.yakallim.data.datasource.remote.OcrRemoteDataSource
import io.github.koolunkle.yakallim.data.repository.OcrRepositoryImpl
import io.github.koolunkle.yakallim.domain.image.ImageProcessor
import io.github.koolunkle.yakallim.domain.notification.PushTokenProvider
import io.github.koolunkle.yakallim.domain.repository.OcrRepository
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
