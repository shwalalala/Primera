package cit.edu.primera.feature.profile.ui

import cit.edu.primera.feature.profile.domain.EmergencyContact
import java.util.Date

data class ProfileUiState(
    val email: String = "",
    val birthday: Date? = null,
    val weightKg: Int = 0,
    val heightCm: Int = 0,
    val lmpDate: Date? = null,
    val eddDate: Date? = null,
    val isFirstPregnancy: Boolean = true,
    
    // Pregnancy Tracking Details
    val isCycleRegular: Boolean? = null,
    val shortestCycleDays: Int? = null,
    val longestCycleDays: Int? = null,
    val hasHadUltrasound: Boolean? = null,
    val positiveTestDate: Date? = null,
    val scanDate: Date? = null,
    val scanWeeks: Int? = null,
    val scanDays: Int? = null,
    val pregnancyHistories: List<cit.edu.primera.feature.onboarding.domain.PregnancyHistory> = emptyList(),
    
    // Emergency Contact (ICE)
    val emergencyContact: EmergencyContact = EmergencyContact(),
    
    // UI State
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val expandedSections: Set<ProfileSection> = setOf(ProfileSection.PERSONAL_INFO),
    val error: String? = null,
    val successMessage: String? = null
)

enum class ProfileSection {
    PERSONAL_INFO,
    PREGNANCY_DETAILS,
    PREGNANCY_HISTORY,
    ICE_CONTACT,
    ACCOUNT_SETTINGS
}
