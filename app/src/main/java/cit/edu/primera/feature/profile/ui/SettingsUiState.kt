package cit.edu.primera.feature.profile.ui

data class SettingsUiState(
    val email: String = "",
    val newEmail: String = "",
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
