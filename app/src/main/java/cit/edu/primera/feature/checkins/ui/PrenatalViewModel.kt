
package cit.edu.primera.feature.checkins.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import cit.edu.primera.feature.checkins.data.PrenatalDataSource
import cit.edu.primera.feature.checkins.data.PrenatalRecordDto

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ==================================================
// PRENATAL UI STATE
// ==================================================

data class PrenatalUiState(
    val records: List<PrenatalRecordDto> = emptyList(),

    val isLoading: Boolean = true,
    val isSaving: Boolean = false,

    val errorMessage: String? = null,
    val successMessage: String? = null
)

// ==================================================
// PRENATAL VIEWMODEL
// ==================================================

class PrenatalViewModel(
    private val dataSource: PrenatalDataSource =
        PrenatalDataSource()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PrenatalUiState()
    )

    val uiState: StateFlow<PrenatalUiState> =
        _uiState.asStateFlow()

    init {
        observePrenatalRecords()
    }

    // ---------------------------------------------
    // OBSERVE SAVED PRENATAL RECORDS
    // ---------------------------------------------

    private fun observePrenatalRecords() {
        viewModelScope.launch {

            dataSource.observeRecords()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage =
                                error.message
                                    ?: "Unable to load prenatal records."
                        )
                    }
                }
                .collect { records ->
                    _uiState.update {
                        it.copy(
                            records = records,
                            isLoading = false
                        )
                    }
                }
        }
    }

    // ---------------------------------------------
    // CALENDAR DATE INDICATORS
    // ---------------------------------------------

    fun getScheduledDates(): Set<String> {
        return _uiState.value.records
            .filter {
                it.status == "scheduled"
            }
            .map {
                it.visitDate
            }
            .toSet()
    }

    fun getCompletedDates(): Set<String> {
        return _uiState.value.records
            .filter {
                it.status == "completed"
            }
            .map {
                it.visitDate
            }
            .toSet()
    }

    // ---------------------------------------------
    // RECORDS FOR SELECTED DATE
    // ---------------------------------------------

    fun getRecordsForDate(
        date: String
    ): List<PrenatalRecordDto> {
        return _uiState.value.records
            .filter {
                it.visitDate == date &&
                        it.status != "cancelled"
            }
            .sortedWith(
                compareBy<PrenatalRecordDto> {
                    it.status != "scheduled"
                }.thenBy {
                    it.details["time"].orEmpty()
                }
            )
    }

    // ---------------------------------------------
    // CREATE APPOINTMENT
    // ---------------------------------------------

    fun saveAppointment(
        visitDate: String,
        details: Map<String, String>
    ) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {

            startSaving()

            val record = PrenatalRecordDto(
                visitDate = visitDate,
                status = "scheduled",
                details = details
            )

            val result = dataSource.createRecord(
                record
            )

            finishSaving(
                result = result.map { Unit },
                successMessage =
                    "Prenatal appointment scheduled successfully."
            )
        }
    }

    // ---------------------------------------------
    // SAVE PRENATAL RESULTS
    // ---------------------------------------------

    fun savePrenatalResults(
        visitDate: String,
        details: Map<String, String>,
        scheduledRecordId: String? = null
    ) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {

            startSaving()

            // If a specific scheduled appointment was
            // selected, attach results to that record.
            val selectedAppointment =
                scheduledRecordId?.let { id ->

                    _uiState.value.records
                        .firstOrNull {
                            it.id == id &&
                                    it.visitDate == visitDate &&
                                    it.status == "scheduled"
                        }
                }

            // Otherwise, attach automatically only if
            // exactly one scheduled appointment exists
            // for this date.
            val matchingAppointment =
                if (scheduledRecordId != null) {
                    selectedAppointment
                } else {
                    _uiState.value.records
                        .filter {
                            it.visitDate == visitDate &&
                                    it.status == "scheduled"
                        }
                        .singleOrNull()
                }

            val result: Result<Unit> =
                if (matchingAppointment != null) {

                    dataSource.updateRecord(
                        matchingAppointment.copy(
                            status = "completed",
                            details =
                                matchingAppointment.details + details
                        )
                    )

                } else {

                    dataSource.createRecord(
                        PrenatalRecordDto(
                            visitDate = visitDate,
                            status = "completed",
                            details = details
                        )
                    ).map { Unit }
                }

            finishSaving(
                result = result,
                successMessage =
                    "Prenatal results saved successfully."
            )
        }
    }

    // ---------------------------------------------
    // UPDATE EXISTING RECORD
    // ---------------------------------------------

    fun updateRecord(
        record: PrenatalRecordDto
    ) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {

            startSaving()

            val result = dataSource.updateRecord(
                record
            )

            finishSaving(
                result = result,
                successMessage =
                    "Prenatal record updated successfully."
            )
        }
    }

    // ---------------------------------------------
    // CANCEL SCHEDULED APPOINTMENT
    // ---------------------------------------------

    fun cancelAppointment(
        recordId: String
    ) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {

            startSaving()

            val result =
                dataSource.cancelAppointment(
                    recordId
                )

            finishSaving(
                result = result,
                successMessage =
                    "Appointment cancelled successfully."
            )
        }
    }

    // ---------------------------------------------
    // DELETE PRENATAL RECORD
    // ---------------------------------------------

    fun deleteRecord(
        recordId: String
    ) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {

            startSaving()

            val result =
                dataSource.deleteRecord(
                    recordId
                )

            finishSaving(
                result = result,
                successMessage =
                    "Prenatal record deleted successfully."
            )
        }
    }

    // ---------------------------------------------
    // RECORD LOOKUP
    // ---------------------------------------------

    fun getRecordById(
        recordId: String
    ): PrenatalRecordDto? {

        return _uiState.value.records
            .firstOrNull {
                it.id == recordId
            }
    }

    // ---------------------------------------------
    // OPERATION STATE HANDLING
    // ---------------------------------------------

    private fun startSaving() {

        _uiState.update {
            it.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private fun finishSaving(
        result: Result<Unit>,
        successMessage: String
    ) {

        result.fold(
            onSuccess = {
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        errorMessage = null,
                        successMessage = successMessage
                    )
                }
            },
            onFailure = { error ->
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        errorMessage =
                            error.message
                                ?: "An unexpected error occurred.",
                        successMessage = null
                    )
                }
            }
        )
    }

    // ---------------------------------------------
    // CLEAR MESSAGES
    // ---------------------------------------------

    fun clearMessages() {

        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }
}
