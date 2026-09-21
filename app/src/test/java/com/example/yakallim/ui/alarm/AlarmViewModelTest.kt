package com.example.yakallim.ui.alarm

import android.content.Context
import app.cash.turbine.test
import com.example.yakallim.domain.model.Alarm
import com.example.yakallim.domain.usecase.CancelAlarmUseCase
import com.example.yakallim.domain.usecase.GetActiveAlarmsUseCase
import com.example.yakallim.domain.usecase.GetDetailAlarmUseCase
import com.example.yakallim.domain.usecase.ScheduleAlarmUseCase
import com.example.yakallim.testutil.FakeAlarmScheduler
import com.example.yakallim.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeAlarmScheduler: FakeAlarmScheduler

    @Before
    fun setUp() {
        fakeAlarmScheduler = FakeAlarmScheduler()
    }

    private fun createViewModel(): AlarmViewModel {
        val context: Context = mock {
            on { getString(any()) } doReturn "알람 등록에 실패했습니다."
        }
        return AlarmViewModel(
            getActiveAlarmsUseCase = GetActiveAlarmsUseCase(fakeAlarmScheduler),
            getDetailAlarmUseCase = GetDetailAlarmUseCase(fakeAlarmScheduler),
            scheduleAlarmUseCase = ScheduleAlarmUseCase(fakeAlarmScheduler),
            cancelAlarmUseCase = CancelAlarmUseCase(fakeAlarmScheduler),
            context = context
        )
    }

    @Test
    fun init_recoversPreviouslyActiveAlarmsFromScheduler() = runTest {
        fakeAlarmScheduler.activeAlarms = setOf("타이레놀정")
        fakeAlarmScheduler.detailAlarms = mapOf("타이레놀정" to Alarm(times = listOf("09:00"), soundUri = null))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isInitialized)
        assertEquals(listOf("09:00"), state.registeredAlarms["타이레놀정"]?.times)
    }

    @Test
    fun registerMedicineAlarm_whenSchedulingSucceeds_addsToRegisteredAlarmsAndEmitsRegisteredEvent() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.registerMedicineAlarm(
                medicineId = "medicine-1",
                medicineName = "타이레놀정",
                dosagePerTake = "1정",
                dailyFrequency = 3,
                durationDays = 5,
                alarmTimes = listOf("09:00"),
                soundUri = null
            )

            val event = awaitItem()
            assertTrue(event is AlarmEvent.Registered)
            val registered = event as AlarmEvent.Registered
            assertEquals("medicine-1", registered.medicineId)
            assertEquals("1정", registered.dosagePerTake)
            assertEquals(3, registered.dailyFrequency)
            assertEquals(5, registered.durationDays)
        }

        val state = viewModel.uiState.value
        assertTrue(state.registeredAlarms.containsKey("타이레놀정"))
        assertEquals(listOf("타이레놀정"), fakeAlarmScheduler.scheduledCalls)
    }

    @Test
    fun registerMedicineAlarm_whenSchedulingFails_emitsErrorEventAndDoesNotRegister() = runTest {
        fakeAlarmScheduler.scheduleShouldThrow = true
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.registerMedicineAlarm(
                medicineId = "medicine-1",
                medicineName = "타이레놀정",
                dosagePerTake = "1정",
                dailyFrequency = 3,
                durationDays = 5,
                alarmTimes = listOf("09:00"),
                soundUri = null
            )

            assertTrue(awaitItem() is AlarmEvent.Error)
        }

        assertTrue(viewModel.uiState.value.registeredAlarms.isEmpty())
    }

    @Test
    fun unregisterMedicineAlarm_removesFromRegisteredAlarmsAndCancelsInScheduler() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.registerMedicineAlarm(
            medicineId = "medicine-1",
            medicineName = "타이레놀정",
            dosagePerTake = "1정",
            dailyFrequency = 3,
            durationDays = 5,
            alarmTimes = listOf("09:00"),
            soundUri = null
        )
        advanceUntilIdle()

        viewModel.unregisterMedicineAlarm("타이레놀정")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.registeredAlarms.containsKey("타이레놀정"))
        assertTrue(fakeAlarmScheduler.cancelledCalls.contains("타이레놀정"))
    }

    @Test
    fun clearAllRegisteredAlarms_cancelsEveryRegisteredAlarmAndClearsState() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.registerMedicineAlarm(
            medicineId = "medicine-1",
            medicineName = "타이레놀정",
            dosagePerTake = "1정",
            dailyFrequency = 3,
            durationDays = 5,
            alarmTimes = listOf("09:00"),
            soundUri = null
        )
        viewModel.registerMedicineAlarm(
            medicineId = "medicine-2",
            medicineName = "게보린정",
            dosagePerTake = "1정",
            dailyFrequency = 2,
            durationDays = 3,
            alarmTimes = listOf("08:00"),
            soundUri = null
        )
        advanceUntilIdle()

        viewModel.clearAllRegisteredAlarms()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.registeredAlarms.isEmpty())
        assertTrue(fakeAlarmScheduler.cancelledCalls.containsAll(listOf("타이레놀정", "게보린정")))
    }
}
