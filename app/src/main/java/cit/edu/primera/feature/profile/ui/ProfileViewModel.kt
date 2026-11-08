package cit.edu.primera.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cit.edu.primera.feature.checkins.data.CheckinsRepository
import cit.edu.primera.feature.dashboard.data.DashboardRepository
import cit.edu.primera.feature.onboarding.data.OnboardingRepository
import cit.edu.primera.feature.onboarding.domain.OnboardingProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class ProfileViewModel(
    private val dashboardRepository: DashboardRepository,
    private val checkinsRepository: CheckinsRepository,
    private val onboardingRepository: OnboardingRepository,
    private val preferenceRepository: cit.edu.primera.core.data.PreferenceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
            
            combine(
                dashboardRepository.observeDashboardData(),
                checkinsRepository.observeUserWeight()
            ) { data, weightData ->
                if (data != null) {
                    val currentLocalPhoto = preferenceRepository.getProfilePictureUri(userId)
                    _uiState.update { currentState ->
                        currentState.copy(
                            email = data.email ?: currentState.email,
                            username = data.userName.ifBlank { currentState.username },
                            photoUri = data.photoUri?.takeIf { u -> u.isNotBlank() }
                                ?: currentLocalPhoto
                                ?: currentState.photoUri,
                            birthday = data.birthday ?: currentState.birthday,
                            weightKg = if (data.weightKg != null && data.weightKg != 0) data.weightKg else (weightData?.weightKg
                                ?: currentState.weightKg),
                            heightCm = if (data.heightCm != null && data.heightCm != 0) data.heightCm else currentState.heightCm,
                            lmpDate = data.lmpDate ?: currentState.lmpDate,
                            eddDate = data.dueDate ?: currentState.eddDate,
                            isFirstPregnancy = data.isFirstPregnancy
                                ?: currentState.isFirstPregnancy,
                            isCycleRegular = data.isCycleRegular ?: currentState.isCycleRegular,
                            shortestCycleDays = data.shortestCycleDays
                                ?: currentState.shortestCycleDays,
                            longestCycleDays = data.longestCycleDays
                                ?: currentState.longestCycleDays,
                            hasHadUltrasound = data.hasHadUltrasound
                                ?: currentState.hasHadUltrasound,
                            positiveTestDate = data.positiveTestDate
                                ?: currentState.positiveTestDate,
                            scanDate = data.scanDate ?: currentState.scanDate,
                            scanWeeks = data.scanWeeks ?: currentState.scanWeeks,
                            scanDays = data.scanDays ?: currentState.scanDays,
                            pregnancyHistories = if (data.pregnancyHistories.isNotEmpty()) data.pregnancyHistories else currentState.pregnancyHistories,
                            emergencyContact = cit.edu.primera.feature.profile.domain.EmergencyContact(
                                name = data.iceName ?: currentState.emergencyContact.name,
                                relationship = data.iceRelationship
                                    ?: currentState.emergencyContact.relationship,
                                primaryPhone = data.icePrimaryPhone
                                    ?: currentState.emergencyContact.primaryPhone,
                                secondaryPhone = data.iceSecondaryPhone
                                    ?: currentState.emergencyContact.secondaryPhone
                            ),
                            isLoading = false
                        )
                    }
                }
            }.collect {}
        }
    }

    fun onWeightChange(weight: Int) = _uiState.update { it.copy(weightKg = weight) }
    fun onHeightChange(height: Int) = _uiState.update { it.copy(heightCm = height) }
    fun onEddDateChange(date: Date) = _uiState.update { it.copy(eddDate = date) }
    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email) }
    fun onBirthdayChange(date: Date) = _uiState.update { it.copy(birthday = date) }
    fun onIsCycleRegularChange(isRegular: Boolean) = _uiState.update { it.copy(isCycleRegular = isRegular) }
    fun onShortestCycleChange(days: Int) = _uiState.update { it.copy(shortestCycleDays = days) }
    fun onLongestCycleChange(days: Int) = _uiState.update { it.copy(longestCycleDays = days) }
    fun onHasHadUltrasoundChange(hasHad: Boolean) {
        _uiState.update { it.copy(hasHadUltrasound = hasHad, error = null) }
        if (hasHad) {
            if (_uiState.value.scanDate != null) {
                calculateEddFromUltrasound()
            }
        } else {
            _uiState.update { it.copy(scanDate = null, scanWeeks = null, scanDays = null) }
            calculateEddFromLmp()
        }
    }

    fun onScanDateChange(date: Date) {
        _uiState.update { it.copy(scanDate = date, error = null) }
        calculateEddFromUltrasound()
    }

    fun onScanWeeksChange(weeks: Int) {
        _uiState.update { it.copy(scanWeeks = weeks, error = null) }
        if (_uiState.value.scanDate != null) {
            calculateEddFromUltrasound()
        }
    }

    fun onScanDaysChange(days: Int) {
        _uiState.update { it.copy(scanDays = days, error = null) }
        if (_uiState.value.scanDate != null) {
            calculateEddFromUltrasound()
        }
    }

    fun onPositiveTestDateChange(date: Date) = _uiState.update { it.copy(positiveTestDate = date) }

    private fun calculateEddFromUltrasound() {
        val s = _uiState.value
        val resolved = cit.edu.primera.core.util.PregnancyCalculator.calculateEdd(
            scanDate = s.scanDate,
            scanWeeks = s.scanWeeks,
            scanDays = s.scanDays,
            lmpDate = s.lmpDate,
            positiveTestDate = s.positiveTestDate
        )
        if (resolved != null) {
            _uiState.update { it.copy(eddDate = resolved, error = null) }
        }
    }

    fun onHistoryDeliveryDateChange(index: Int, date: Date) {
        _uiState.update { state ->
            val newList = state.pregnancyHistories.toMutableList()
            if (index in newList.indices) {
                newList[index] = newList[index].copy(deliveryDate = date)
            }
            state.copy(pregnancyHistories = newList)
        }
    }

    fun onHistoryDeliveryTypeChange(index: Int, type: String) {
        _uiState.update { state ->
            val newList = state.pregnancyHistories.toMutableList()
            if (index in newList.indices) {
                newList[index] = newList[index].copy(deliveryType = type)
            }
            state.copy(pregnancyHistories = newList)
        }
    }

    fun onHistoryChildrenChange(index: Int, count: String) {
        _uiState.update { state ->
            val newList = state.pregnancyHistories.toMutableList()
            if (index in newList.indices) {
                newList[index] = newList[index].copy(childrenDelivered = count)
            }
            state.copy(pregnancyHistories = newList)
        }
    }

    fun onLmpDateChange(date: Date) {
        _uiState.update { it.copy(lmpDate = date) }
        calculateEddFromLmp()
    }

    private fun calculateEddFromLmp() {
        val s = _uiState.value
        val resolved = cit.edu.primera.core.util.PregnancyCalculator.calculateEdd(
            eddDate = if (s.hasHadUltrasound == true && s.scanDate != null) s.eddDate else null,
            scanDate = if (s.hasHadUltrasound == true) s.scanDate else null,
            scanWeeks = s.scanWeeks,
            scanDays = s.scanDays,
            lmpDate = s.lmpDate,
            positiveTestDate = s.positiveTestDate
        )
        if (resolved != null) {
            _uiState.update { it.copy(eddDate = resolved) }
        }
    }

    fun onHistoryLivingChildrenChange(index: Int, isLiving: Boolean) {
        _uiState.update { state ->
            val newList = state.pregnancyHistories.toMutableList()
            if (index in newList.indices) {
                newList[index] = newList[index].copy(areChildrenLiving = isLiving)
            }
            state.copy(pregnancyHistories = newList)
        }
    }

    fun onHistoryStructuredComplicationToggle(index: Int, type: String) {
        _uiState.update { state ->
            val newList = state.pregnancyHistories.toMutableList()
            if (index in newList.indices) {
                val current = newList[index]
                val updated = when (type) {
                    "GDM" -> current.copy(hasGdm = !current.hasGdm)
                    "Diabetes" -> current.copy(hasDiabetes = !current.hasDiabetes)
                    "Pre-eclampsia" -> current.copy(hasPreeclampsia = !current.hasPreeclampsia)
                    "Preterm delivery" -> current.copy(hasPretermDelivery = !current.hasPretermDelivery)
                    else -> current
                }
                newList[index] = updated
            }
            state.copy(pregnancyHistories = newList)
        }
    }

    fun onHistoryAdditionalComplicationsChange(index: Int, text: String) {
        _uiState.update { state ->
            val newList = state.pregnancyHistories.toMutableList()
            if (index in newList.indices) {
                newList[index] = newList[index].copy(additionalComplications = text)
            }
            state.copy(pregnancyHistories = newList)
        }
    }

    fun onHistoryComplicationsChange(index: Int, complication: String) {
        _uiState.update { state ->
            val newList = state.pregnancyHistories.toMutableList()
            if (index in newList.indices) {
                val current = newList[index]
                val newComps = if (current.complications.contains(complication)) {
                    current.complications - complication
                } else {
                    current.complications + complication
                }
                newList[index] = current.copy(complications = newComps)
            }
            state.copy(pregnancyHistories = newList)
        }
    }

    // ICE Contact
    fun onIceNameChange(name: String) = _uiState.update { 
        it.copy(emergencyContact = it.emergencyContact.copy(name = name)) 
    }
    fun onIceRelationshipChange(rel: String) = _uiState.update { 
        it.copy(emergencyContact = it.emergencyContact.copy(relationship = rel)) 
    }
    fun onIcePrimaryPhoneChange(phone: String) = _uiState.update { 
        it.copy(emergencyContact = it.emergencyContact.copy(primaryPhone = phone)) 
    }
    fun onIceSecondaryPhoneChange(phone: String) = _uiState.update { 
        it.copy(emergencyContact = it.emergencyContact.copy(secondaryPhone = phone)) 
    }

    fun onUsernameChange(username: String) = _uiState.update { it.copy(username = username) }

    private fun openUriInputStream(
        context: android.content.Context,
        uri: android.net.Uri
    ): java.io.InputStream? {
        return try {
            if (uri.scheme == "file" && uri.path != null) {
                java.io.File(uri.path!!).inputStream()
            } else {
                context.contentResolver.openInputStream(uri)
                    ?: uri.path?.let { java.io.File(it).inputStream() }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun onPhotoSelected(context: android.content.Context, uri: android.net.Uri) {
        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: ""

            // 1. Validate image format
            val uriString = uri.toString().lowercase()
            val isAllowedFormat = mimeType.startsWith("image/", ignoreCase = true) ||
                    mimeType.isEmpty() ||
                    uriString.endsWith(".png") ||
                    uriString.endsWith(".jpg") ||
                    uriString.endsWith(".jpeg") ||
                    uriString.endsWith(".webp") ||
                    uriString.endsWith(".heic") ||
                    uriString.endsWith(".heif")

            if (!isAllowedFormat) {
                _uiState.update { it.copy(error = "Only PNG, JPG, JPEG, WEBP, and HEIC images are allowed.") }
                return
            }

            // 2. Validate max file size <= 100 MB
            var fileSize = 0L
            try {
                contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                    fileSize = afd.length
                }
            } catch (_: Exception) {
            }

            val maxSizeBytes = 100L * 1024L * 1024L // 100 MB
            if (fileSize > maxSizeBytes && fileSize > 0) {
                _uiState.update { it.copy(error = "File size exceeds limit: Maximum allowed image size is 100 MB.") }
                return
            }

            // 3. Open Crop & Confirmation Dialog
            _uiState.update { it.copy(pendingPhotoUri = uri, showCropDialog = true, error = null) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to process image: ${e.message}") }
        }
    }

    fun confirmPhotoSelection(context: android.content.Context, uri: android.net.Uri) {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // 1. Immediately show loading overlay & dismiss dialog
        _uiState.update {
            it.copy(
                isSaving = true,
                showCropDialog = false,
                pendingPhotoUri = null,
                error = null
            )
        }

        // 2. Save locally first so UI updates immediately & upload to Storage in background
        viewModelScope.launch {
            val localUri = savePhotoLocally(context, uri, userId)
            preferenceRepository.setProfilePictureUri(userId, localUri)
            dashboardRepository.updatePhotoUri(localUri)

            _uiState.update {
                it.copy(
                    photoUri = localUri,
                    isSaving = false,
                    error = null,
                    successMessage = "Profile picture updated successfully"
                )
            }

            try {
                val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                    .child("profile_pictures/${if (userId.isBlank()) "guest" else userId}.png")

                val inputStream = openUriInputStream(context, uri)
                if (inputStream != null) {
                    val bytes = inputStream.use { it.readBytes() }
                    storageRef.putBytes(bytes).await()
                    val downloadUrl = storageRef.downloadUrl.await().toString()

                    // Upload successful! Save Firebase Storage HTTPS URL
                    preferenceRepository.setProfilePictureUri(userId, downloadUrl)
                    dashboardRepository.updatePhotoUri(downloadUrl)

                    _uiState.update {
                        it.copy(
                            photoUri = downloadUrl,
                            error = null
                        )
                    }
                }
            } catch (_: Exception) {
                // Background upload exception ignored as localUri is already saved and active
            }
        }
    }

    private fun savePhotoLocally(
        context: android.content.Context,
        uri: android.net.Uri,
        userId: String
    ): String {
        val timestamp = System.currentTimeMillis()
        val file = java.io.File(
            context.filesDir,
            "profile_avatar_${if (userId.isBlank()) "guest" else userId}.png"
        )
        try {
            openUriInputStream(context, uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return "${android.net.Uri.fromFile(file)}?t=$timestamp"
        } catch (_: Exception) {
            return if (file.exists()) {
                "${android.net.Uri.fromFile(file)}?t=$timestamp"
            } else {
                "$uri?t=$timestamp"
            }
        }
    }

    fun dismissCropDialog() {
        _uiState.update { it.copy(showCropDialog = false, pendingPhotoUri = null) }
    }

    // UI State
    fun toggleSection(section: ProfileSection) {
        _uiState.update { state ->
            val newSections = if (state.expandedSections.contains(section)) {
                state.expandedSections - section
            } else {
                state.expandedSections + section
            }
            state.copy(expandedSections = newSections)
        }
    }

    fun toggleEdit() = _uiState.update { it.copy(isEditing = !it.isEditing) }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val currentUiState = _uiState.value
            val currentUserEmail =
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
            val emailToSave = currentUiState.email.ifBlank { currentUserEmail ?: "" }

            val resolvedEdd = cit.edu.primera.core.util.PregnancyCalculator.calculateEdd(
                eddDate = currentUiState.eddDate,
                scanDate = currentUiState.scanDate,
                scanWeeks = currentUiState.scanWeeks,
                scanDays = currentUiState.scanDays,
                lmpDate = currentUiState.lmpDate,
                positiveTestDate = currentUiState.positiveTestDate
            )

            val profile = OnboardingProfile(
                email = emailToSave,
                birthday = currentUiState.birthday ?: Date(),
                weightKg = currentUiState.weightKg,
                heightCm = currentUiState.heightCm,
                isCycleRegular = currentUiState.isCycleRegular,
                shortestCycleDays = currentUiState.shortestCycleDays,
                longestCycleDays = currentUiState.longestCycleDays,
                hasHadUltrasound = currentUiState.hasHadUltrasound,
                positiveTestDate = currentUiState.positiveTestDate,
                lmpDate = currentUiState.lmpDate,
                eddDate = resolvedEdd,
                isFirstPregnancy = currentUiState.isFirstPregnancy,
                scanDate = currentUiState.scanDate,
                scanWeeks = currentUiState.scanWeeks,
                scanDays = currentUiState.scanDays,
                iceName = currentUiState.emergencyContact.name,
                iceRelationship = currentUiState.emergencyContact.relationship,
                icePrimaryPhone = currentUiState.emergencyContact.primaryPhone,
                iceSecondaryPhone = currentUiState.emergencyContact.secondaryPhone,
                pregnancyHistories = currentUiState.pregnancyHistories
            )

            onboardingRepository.saveProfile(profile).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false, isEditing = false, successMessage = "Profile updated successfully") }
                }
            ) { error ->
                _uiState.update { it.copy(isSaving = false, error = error.message ?: "Failed to save profile") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
