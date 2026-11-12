package cit.edu.primera.feature.vaccination.ui

import cit.edu.primera.feature.vaccination.domain.VaccinationSourceInfo
import cit.edu.primera.feature.vaccination.domain.VaccineDetail
import cit.edu.primera.feature.vaccination.domain.VaccineScheduleRow

data class VaccinationUiState(
    val vaccines: List<VaccineDetail> = emptyList(),
    val scheduleRows: List<VaccineScheduleRow> = emptyList(),
    val sourceInfo: VaccinationSourceInfo = VaccinationSourceInfo(),
    val showSourceDialog: Boolean = false,
    val isLoading: Boolean = false,
)
