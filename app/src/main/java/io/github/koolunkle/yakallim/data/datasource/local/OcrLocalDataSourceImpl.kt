package io.github.koolunkle.yakallim.data.datasource.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OcrLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : OcrLocalDataSource {

    private val activeJobIdKey = stringPreferencesKey("active_ocr_job_id")
    private val lastResultKey = stringPreferencesKey("last_ocr_result")
    private fun cancelledJobKey(jobId: String) = booleanPreferencesKey("cancelled_job_$jobId")

    override suspend fun getPendingJobId(): String? {
        return dataStore.data.map { preferences ->
            preferences[activeJobIdKey]
        }.first()
    }

    override suspend fun savePendingJobId(jobId: String) {
        dataStore.edit { preferences ->
            preferences[activeJobIdKey] = jobId
        }
    }

    override suspend fun clearPendingJobId() {
        dataStore.edit { preferences ->
            preferences.remove(activeJobIdKey)
        }
    }

    override suspend fun isAnalysisCancelled(jobId: String): Boolean {
        return dataStore.data.map { preferences ->
            preferences[cancelledJobKey(jobId)] ?: false
        }.first()
    }

    override suspend fun setAnalysisCancelled(jobId: String) {
        dataStore.edit { preferences ->
            preferences[cancelledJobKey(jobId)] = true
        }
    }

    override suspend fun removeAnalysisCancelled(jobId: String) {
        dataStore.edit { preferences ->
            preferences.remove(cancelledJobKey(jobId))
        }
    }

    override suspend fun saveLastPrescriptionJson(json: String) {
        dataStore.edit { preferences ->
            preferences[lastResultKey] = json
        }
    }

    override suspend fun getLastPrescriptionJson(): String? {
        return dataStore.data.map { preferences ->
            preferences[lastResultKey]
        }.first()
    }

    override suspend fun clearLastPrescriptionJson() {
        dataStore.edit { preferences ->
            preferences.remove(lastResultKey)
        }
    }
}
