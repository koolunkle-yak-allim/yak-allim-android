package com.example.yakallim.data.datasource.local.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmPreference @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val activeAlarmMedicinesKey = stringSetPreferencesKey("active_alarm_medicines")

    private fun alarmSpecKey(medicineName: String) =
        stringPreferencesKey("alarm_spec_${medicineName}")

    suspend fun addActiveAlarmMedicine(medicineName: String) = withContext(Dispatchers.IO) {
        dataStore.edit { prefs ->
            val current = prefs[activeAlarmMedicinesKey] ?: emptySet()
            prefs[activeAlarmMedicinesKey] = current + medicineName
        }
    }

    suspend fun removeActiveAlarmMedicine(medicineName: String) = withContext(Dispatchers.IO) {
        dataStore.edit { prefs ->
            val current = prefs[activeAlarmMedicinesKey] ?: emptySet()
            prefs[activeAlarmMedicinesKey] = current - medicineName
        }
    }

    suspend fun getActiveAlarmMedicines(): Set<String> = withContext(Dispatchers.IO) {
        dataStore.data.map { prefs ->
            prefs[activeAlarmMedicinesKey] ?: emptySet()
        }.first()
    }

    /** 재부팅 후 재등록에 필요한 전체 정보(복용량·횟수·기간·시작 시각 포함)를 JSON으로 저장한다. */
    suspend fun saveAlarmSpecJson(medicineName: String, json: String) = withContext(Dispatchers.IO) {
        dataStore.edit { prefs ->
            prefs[alarmSpecKey(medicineName)] = json
        }
    }

    suspend fun getAlarmSpecJson(medicineName: String): String? = withContext(Dispatchers.IO) {
        dataStore.data.map { prefs ->
            prefs[alarmSpecKey(medicineName)]
        }.first()
    }

    suspend fun removeAlarmSpecJson(medicineName: String) = withContext(Dispatchers.IO) {
        dataStore.edit { prefs ->
            prefs.remove(alarmSpecKey(medicineName))
        }
    }
}
