package io.github.koolunkle.yakallim.di

import io.github.koolunkle.yakallim.data.datasource.local.FcmLocalDataSource
import io.github.koolunkle.yakallim.data.datasource.local.FcmLocalDataSourceImpl
import io.github.koolunkle.yakallim.data.datasource.local.OcrLocalDataSource
import io.github.koolunkle.yakallim.data.datasource.local.OcrLocalDataSourceImpl
import io.github.koolunkle.yakallim.data.datasource.remote.OcrRemoteDataSource
import io.github.koolunkle.yakallim.data.datasource.remote.OcrRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindFcmLocalDataSource(
        fcmLocalDataSourceImpl: FcmLocalDataSourceImpl
    ): FcmLocalDataSource

    @Binds
    @Singleton
    abstract fun bindOcrLocalDataSource(
        ocrLocalDataSourceImpl: OcrLocalDataSourceImpl
    ): OcrLocalDataSource

    @Binds
    @Singleton
    abstract fun bindOcrRemoteDataSource(
        ocrRemoteDataSourceImpl: OcrRemoteDataSourceImpl
    ): OcrRemoteDataSource
}
