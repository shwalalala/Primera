package cit.edu.primera.feature.profile.ui

data class SettingsUiState(
    val hasPasswordAuthProvider: Boolean = false,
    val authProviders: Set<String> = emptySet(),
    val email: String = "",
    val newEmail: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
