package com.example.yakallim.ui.alarm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yakallim.R
import com.example.yakallim.domain.model.Alarm
import com.example.yakallim.domain.usecase.CancelAlarmUseCase
import com.example.yakallim.domain.usecase.GetActiveAlarmsUseCase
import com.example.yakallim.domain.usecase.GetDetailAlarmUseCase
import com.example.yakallim.domain.usecase.ScheduleAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val getActiveAlarmsUseCase: GetActiveAlarmsUseCase,
    private val getDetailAlarmUseCase: GetDetailAlarmUseCase,
    private val scheduleAlarmUseCase: ScheduleAlarmUseCase,
    private val cancelAlarmUseCase: CancelAlarmUseCase,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AlarmEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AlarmEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            recoverActiveAlarms()
            _uiState.update { it.copy(isInitialized = true) }
        }
    }

    private suspend fun recoverActiveAlarms() {
        val activeAlarms = getActiveAlarmsUseCase()
        val restoredDetails = activeAlarms.associateWith { medicineName ->
            getDetailAlarmUseCase(medicineName) ?: Alarm(times = emptyList(), soundUri = null)
        }
        _uiState.update { state ->
            state.copy(registeredAlarms = state.registeredAlarms + restoredDetails)
        }
    }

    fun registerMedicineAlarm(
        medicineId: String,
        medicineName: String,
        dosagePerTake: String,
        dailyFrequency: Int,
        durationDays: Int,
        alarmTimes: List<String>,
        soundUri: String?
    ) {
        viewModelScope.launch {
            try {
                cancelAlarmUseCase(medicineName)
            } catch (_: Exception) {}

            val isSuccess = try {
                scheduleAlarmUseCase(
                    medicineName,
                    dosagePerTake,
                    dailyFrequency,
                    durationDays,
                    alarmTimes,
                    soundUri
                )
                true
            } catch (_: Exception) {
                false
            }

            if (isSuccess) {
                _uiState.update { state ->
                    state.copy(
                        registeredAlarms = state.registeredAlarms + (medicineName to Alarm(alarmTimes, soundUri))
                    )
                }
                _events.emit(AlarmEvent.Registered(medicineId, dosagePerTake, dailyFrequency, durationDays))
            } else {
                _events.emit(AlarmEvent.Error(context.getString(R.string.error_alarm_registration_failed)))
            }
        }
    }

    fun unregisterMedicineAlarm(medicineName: String) {
        viewModelScope.launch {
            cancelAlarmUseCase(medicineName)
            _uiState.update { it.copy(registeredAlarms = it.registeredAlarms - medicineName) }
        }
    }

    fun clearAllRegisteredAlarms() {
        val alarms = _uiState.value.registeredAlarms.keys
        if (alarms.isNotEmpty()) {
            viewModelScope.launch {
                alarms.forEach { medicineName -> cancelAlarmUseCase(medicineName) }
                _uiState.update { it.copy(registeredAlarms = emptyMap()) }
            }
        }
    }
}
