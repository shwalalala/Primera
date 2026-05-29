package com.example.primera.feature.smartwatchconnection.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.primera.feature.smartwatchconnection.data.HealthConnectManager
import com.example.primera.feature.smartwatchconnection.data.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SmartwatchViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val healthConnectManager =
        HealthConnectManager(application.applicationContext)

    private val healthRepository = HealthRepository()

    private val _uiState = MutableStateFlow(SmartwatchUiState())
    val uiState: StateFlow<SmartwatchUiState> = _uiState.asStateFlow()

    val permissions = healthConnectManager.permissions

    fun checkHealthConnectStatus() {
        viewModelScope.launch {
            if (!healthConnectManager.isAvailable()) {
                _uiState.value = _uiState.value.copy(
                    message = "Health Connect is not available on this device."
                )
                return@launch
            }

            val granted = healthConnectManager.hasAllPermissions()

            _uiState.value = _uiState.value.copy(
                hasPermissions = granted,
                message = if (granted) {
                    "Permissions granted. You can now read smartwatch health data."
                } else {
                    "Health Connect is available. Please allow permissions."
                }
            )
        }
    }

    fun onPermissionResult(grantedPermissions: Set<String>) {
        val granted = grantedPermissions.containsAll(permissions)

        _uiState.value = _uiState.value.copy(
            hasPermissions = granted,
            message = if (granted) {
                "Permissions granted."
            } else {
                "Permissions denied. Please allow permissions first."
            }
        )
    }

    fun readAndSaveSmartwatchHealth() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    message = "Reading smartwatch health data..."
                )

                if (!healthConnectManager.isAvailable()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Health Connect is not available."
                    )
                    return@launch
                }

                if (!healthConnectManager.hasAllPermissions()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hasPermissions = false,
                        message = "Please grant permissions first."
                    )
                    return@launch
                }

                val smartwatchHealth =
                    healthConnectManager.readTodaySmartwatchHealth()

                healthRepository.saveSmartwatchHealth(
                    smartwatchHealth = smartwatchHealth
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    smartwatchHealth = smartwatchHealth,
                    message = "Smartwatch health data saved to Firebase."
                )

            } catch (e: Exception) {
                Log.e("MyTag", "Error during operation", e)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Error: ${e.message}"
                )
            }
        }
    }
}