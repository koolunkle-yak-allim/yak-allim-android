package io.github.koolunkle.yakallim.data.datasource.local

interface FcmLocalDataSource {
    suspend fun getFcmToken(): String?
    suspend fun saveFcmToken(token: String)
}
