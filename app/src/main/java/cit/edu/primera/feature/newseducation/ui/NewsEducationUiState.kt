package cit.edu.primera.feature.newseducation.ui

import cit.edu.primera.feature.newseducation.domain.EducationalResource

/**
 * UI State for the News & Education screen.
 */
data class NewsEducationUiState(
    val searchQuery: String = "",
    val allResources: List<EducationalResource> = emptyList(),
    val filteredResources: List<EducationalResource> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
