package cit.edu.primera.feature.newseducation.ui

import androidx.lifecycle.ViewModel
import cit.edu.primera.feature.newseducation.data.NewsEducationRepository
import cit.edu.primera.feature.newseducation.domain.EducationalResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NewsEducationViewModel(
    private val repository: NewsEducationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsEducationUiState(isLoading = true))
    val uiState: StateFlow<NewsEducationUiState> = _uiState.asStateFlow()

    init {
        loadResources()
    }

    fun loadResources() {
        val resources = repository.getEducationalResources()
        _uiState.update { currentState ->
            currentState.copy(
                allResources = resources,
                filteredResources = filterResources(resources, currentState.searchQuery),
                isLoading = false
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                filteredResources = filterResources(currentState.allResources, query)
            )
        }
    }

    fun onClearSearch() {
        onSearchQueryChanged("")
    }

    private fun filterResources(
        resources: List<EducationalResource>,
        query: String
    ): List<EducationalResource> {
        if (query.isBlank()) return resources
        val trimmed = query.trim().lowercase()
        return resources.filter { resource ->
            resource.title.lowercase().contains(trimmed) ||
                    resource.description.lowercase().contains(trimmed) ||
                    resource.sourceName.lowercase().contains(trimmed) ||
                    resource.category.lowercase().contains(trimmed) ||
                    resource.url.lowercase().contains(trimmed) ||
                    resource.tags.any { it.lowercase().contains(trimmed) }
        }
    }
}
