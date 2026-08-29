package com.example.primera.feature.smartwatchconnection.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primera.core.data.PreferenceRepository
import com.example.primera.feature.smartwatchconnection.data.HealthConnectManager
import androidx.health.connect.client.HealthConnectClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

class SmartwatchViewModel(
    private val healthConnectManager: HealthConnectManager,
    private val preferenceRepository: PreferenceRepository,
    private val dashboardRepository: com.example.primera.feature.dashboard.data.DashboardRepository,
    private val networkMonitor: com.example.primera.core.util.NetworkMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartwatchUiState())
    val uiState: StateFlow<SmartwatchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOnline = isOnline) }
            }
        }
    }

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
                preferenceRepository.setWatchSyncEnabled(userId, enabled = true)
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
                kotlinx.coroutines.delay(500.milliseconds)

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

                // Calculate trends
                val hrTrend = calculateTrend(bpmHistory)
                val sleepTrend = calculateTrend(sleepHistory)
                val spO2Trend = calculateTrend(historicalData.values.map { it.spO2?.toFloat() ?: 0f })

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        smartwatchHealth = smartwatchHealth,
                        hasPermissions = true,
                        bpmHistory = bpmHistory,
                        sleepHistory = sleepHistory,
                        historyLabels = labels,
                        isDataVisible = true,
                        message = "Success! Data synced for current user.",
                        hrTrendText = hrTrend.first,
                        hrTrendColor = hrTrend.second,
                        sleepTrendText = sleepTrend.first,
                        sleepTrendColor = sleepTrend.second,
                        spO2TrendText = spO2Trend.first,
                        spO2TrendColor = spO2Trend.second,
                        stepsTrendText = "Goal: 8,000"
                    )
                }

                // Update main dashboard data fields as well
                dashboardRepository.updateHealthData(
                    steps = smartwatchHealth.steps,
                    heartRate = smartwatchHealth.currentHeartRate ?: 0L,
                    sleepHours = smartwatchHealth.sleepMinutes / 60,
                    sleepMinutes = smartwatchHealth.sleepMinutes % 60,
                    spO2 = smartwatchHealth.spO2?.toLong()
                )

                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                preferenceRepository.setWatchSyncEnabled(userId, enabled = true)

            } catch (e: Exception) {
                Log.e("SmartwatchViewModel", "Sync failed", e)
                
                val rawMessage = e.message ?: ""
                val errorMessage = when {
                    rawMessage.contains("not responding", ignoreCase = true) -> 
                        "Health Connect service didn't respond in time. This is common on Huawei devices. Please open the Health Connect app, then try syncing again."
                    rawMessage.contains("permission", ignoreCase = true) -> 
                        "Sync failed: Permissions not granted. Please ensure Primera is allowed in the Health Connect app."
                    (e is java.net.SocketTimeoutException || rawMessage.contains("timeout")) ->
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

    private fun calculateTrend(history: List<Float>): Pair<String, Long> {
        if (history.size < 2) return Pair("No baseline", 0xFF757575)
        
        val current = history.last()
        val previous = history.dropLast(1).average().toFloat()
        
        if (previous == 0f) return Pair("New data", 0xFF757575)
        
        val diff = ((current - previous) / previous) * 100
        val color = if (diff >= 0) 0xFFA1D386 else 0xFFF28B82 // Green if up, Red if down (can be context specific)
        
        val arrow = if (diff >= 0) "▲" else "▼"
        val text = "$arrow ${abs(diff).toInt()}% vs baseline"
        
        return Pair(text, color)
    }

    fun onBackToSources() {
        _uiState.value = _uiState.value.copy(isDataVisible = false)
    }

    fun onOpenHealthConnect() {
        healthConnectManager.openHealthConnect()
    }
}
