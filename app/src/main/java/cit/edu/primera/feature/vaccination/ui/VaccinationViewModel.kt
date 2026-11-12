package cit.edu.primera.feature.vaccination.ui

import androidx.lifecycle.ViewModel
import cit.edu.primera.feature.vaccination.data.VaccinationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VaccinationViewModel(
    private val repository: VaccinationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaccinationUiState())
    val uiState: StateFlow<VaccinationUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val vaccines = repository.getVaccineDetails()
        val scheduleRows = repository.getScheduleRows()
        val sourceInfo = repository.getSourceInfo()

        _uiState.update {
            it.copy(
                vaccines = vaccines,
                scheduleRows = scheduleRows,
                sourceInfo = sourceInfo,
                isLoading = false,
            )
        }
    }

    fun onOpenSourceDialog() {
        _uiState.update { it.copy(showSourceDialog = true) }
    }

    fun onDismissSourceDialog() {
        _uiState.update { it.copy(showSourceDialog = false) }
    }
}
