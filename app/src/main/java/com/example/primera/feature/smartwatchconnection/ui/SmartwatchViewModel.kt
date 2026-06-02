package com.example.primera.feature.smartwatchconnection.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primera.core.data.PreferenceRepository
import com.example.primera.feature.smartwatchconnection.data.HealthConnectManager
import com.example.primera.feature.smartwatchconnection.data.HealthRepository
import androidx.health.connect.client.HealthConnectClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class SmartwatchViewModel(
    private val healthConnectManager: HealthConnectManager,
    private val preferenceRepository: PreferenceRepository,
    private val dashboardRepository: com.example.primera.feature.dashboard.data.DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartwatchUiState())
    val uiState: StateFlow<SmartwatchUiState> = _uiState.asStateFlow()

    val permissions = healthConnectManager.permissions

    fun checkHealthConnectStatus() {
        viewModelScope.launch {
            try {
                Log.d("SmartwatchViewModel", "Checking Health Connect status...")
                val status = healthConnectManager.getAvailabilityStatus()
                val isInstalled = healthConnectManager.isPackageInstalled()
                
                Log.d("SmartwatchViewModel", "Status: $status, Package installed: $isInstalled")

                _uiState.value = _uiState.value.copy(isPackageInstalled = isInstalled)

                when {
                    status == HealthConnectClient.SDK_AVAILABLE -> {
                        try {
                            val granted = healthConnectManager.hasAllPermissions()
                            _uiState.value = _uiState.value.copy(
                                hasPermissions = granted,
                                message = if (granted) {
                                    "Permissions granted. You can now sync your data."
                                } else {
                                    "Please allow Health Connect permissions to sync your data."
                                }
                            )
                        } catch (e: Exception) {
                            Log.e("SmartwatchViewModel", "Permission check failed, but SDK is available", e)
                            _uiState.value = _uiState.value.copy(
                                hasPermissions = true,
                                message = "System communication is slow, but you can try syncing now."
                            )
                        }
                    }
                    isInstalled -> {
                        _uiState.value = _uiState.value.copy(
                            message = "Health Connect is installed but not active. Please open the Health Connect app manually to initialize it."
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            message = "Health Connect is not installed. Please install it from the Play Store to sync your data."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SmartwatchViewModel", "Error checking Health Connect status", e)
                _uiState.value = _uiState.value.copy(
                    message = "Error: ${e.message}. Please ensure Health Connect is updated."
                )
            }
        }
    }

    fun onPermissionResult(grantedPermissions: Set<String>) {
        Log.d("SmartwatchViewModel", "Permission result received: $grantedPermissions")
        val granted = grantedPermissions.containsAll(permissions)

        _uiState.value = _uiState.value.copy(
            hasPermissions = granted,
            message = if (granted) {
                "Permissions granted. You can now sync."
            } else {
                "Permissions denied. Please allow permissions in Health Connect settings."
            }
        )
        
        if (granted) {
            viewModelScope.launch {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                preferenceRepository.setWatchSyncEnabled(userId, true)
            }
        }
    }

    fun onPermissionError(e: Exception) {
        Log.e("SmartwatchViewModel", "Permission request failed", e)
        _uiState.value = _uiState.value.copy(
            hasPermissions = true, 
            message = "Standard permission popup blocked by device. Please allow Primera manually in Health Connect app, then click Sync."
        )
    }

    fun readAndSaveSmartwatchHealth() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    message = "Initializing connection..."
                )

                // Small delay to allow the system/sandbox to warm up
                kotlinx.coroutines.delay(500)

                // Attempt read
                _uiState.value = _uiState.value.copy(message = "Syncing with Health Connect...")
                val smartwatchHealth = healthConnectManager.readTodaySmartwatchHealth()
                
                _uiState.value = _uiState.value.copy(message = "Reading historical data...")
                val historicalData = healthConnectManager.readPastWeekSmartwatchHealth()
                val sortedDates = historicalData.keys.sorted()
                val bpmHistory = sortedDates.map { historicalData[it]?.averageHeartRate?.toFloat() ?: 0f }
                val sleepHistory = sortedDates.map { (historicalData[it]?.sleepMinutes ?: 0L).toFloat() / 60f }
                val labels = sortedDates.map { "${it.monthValue}.${it.dayOfMonth}" }

                // Save to repository (Firestore)
                dashboardRepository.saveHistoricalRecord(smartwatchHealth)

                // Update main dashboard data fields as well
                dashboardRepository.updateHealthData(
                    steps = smartwatchHealth.steps,
                    heartRate = smartwatchHealth.currentHeartRate ?: 0L,
                    sleepHours = smartwatchHealth.sleepMinutes / 60,
                    sleepMinutes = smartwatchHealth.sleepMinutes % 60,
                    spO2 = smartwatchHealth.spO2?.toLong()
                )

                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                preferenceRepository.setWatchSyncEnabled(userId, true)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    smartwatchHealth = smartwatchHealth,
                    hasPermissions = true,
                    bpmHistory = bpmHistory,
                    sleepHistory = sleepHistory,
                    historyLabels = labels,
                    isDataVisible = true,
                    message = "Success! Data synced for current user."
                )

            } catch (e: Exception) {
                Log.e("SmartwatchViewModel", "Sync failed", e)
                
                val rawMessage = e.message ?: ""
                val errorMessage = when {
                    rawMessage.contains("not responding", ignoreCase = true) -> 
                        "Health Connect service didn't respond in time. This is common on Huawei devices. Please open the Health Connect app, then try syncing again."
                    rawMessage.contains("permission", ignoreCase = true) -> 
                        "Sync failed: Permissions not granted. Please ensure Primera is allowed in the Health Connect app."
                    e is java.net.SocketTimeoutException || rawMessage.contains("timeout") ->
                        "Connection timed out. Please try one more time."
                    else -> "Sync failed: ${e.localizedMessage ?: "Unknown error"}. Try opening Health Connect first."
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = errorMessage
                )
            }
        }
    }

    fun onBackToSources() {
        _uiState.value = _uiState.value.copy(isDataVisible = false)
    }

    fun onOpenHealthConnect() {
        healthConnectManager.openHealthConnect()
    }
}
