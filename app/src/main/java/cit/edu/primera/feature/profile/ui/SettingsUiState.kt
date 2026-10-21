package cit.edu.primera.feature.profile.ui

data class SettingsUiState(
    val hasPasswordAuthProvider: Boolean = false,
    val email: String = "",
    val newEmail: String = "",
    val emailCurrentPassword: String = "",
    val passwordCurrentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
