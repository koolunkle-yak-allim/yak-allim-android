package io.github.koolunkle.yakallim.data.datasource.local

import io.github.koolunkle.yakallim.data.datasource.local.preference.FcmTokenPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmLocalDataSourceImpl @Inject constructor(
    private val fcmTokenPreference: FcmTokenPreference
) : FcmLocalDataSource {

    override suspend fun getFcmToken(): String? {
        return fcmTokenPreference.getFcmToken()
    }

    override suspend fun saveFcmToken(token: String) {
        fcmTokenPreference.saveFcmToken(token)
    }
}
