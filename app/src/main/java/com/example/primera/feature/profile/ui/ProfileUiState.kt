package com.example.primera.feature.profile.ui

import java.util.Date

data class ProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val middleName: String = "",
    val email: String = "",
    val birthday: Date? = null,
    val weightKg: Int = 0,
    val heightCm: Int = 0,
    val lmpDate: Date? = null,
    val eddDate: Date? = null,
    val isFirstPregnancy: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
