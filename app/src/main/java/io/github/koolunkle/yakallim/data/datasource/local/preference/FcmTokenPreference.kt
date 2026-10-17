package io.github.koolunkle.yakallim.data.datasource.local.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenPreference @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val fcmTokenKey = stringPreferencesKey("fcm_token")

    suspend fun getFcmToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[fcmTokenKey]
        }.first()
    }

    suspend fun saveFcmToken(token: String) {
        dataStore.edit { preferences ->
            preferences[fcmTokenKey] = token
        }
    }
}
