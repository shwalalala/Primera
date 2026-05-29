package com.example.primera.feature.smartwatchconnection.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SmartwatchScreen(
    uiState: SmartwatchUiState,
    onRequestPermissions: () -> Unit,
    onReadAndSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Smartwatch Health Data")

        Text(text = uiState.message)

        Button(
            onClick = onRequestPermissions
        ) {
            Text("Allow Health Connect Permissions")
        }

        Button(
            onClick = onReadAndSave
        ) {
            Text("Read and Save Health Data")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.smartwatchHealth?.let { health ->
            Text("Date: ${health.date}")
            Text("Steps: ${health.steps}")
            Text("Current Heart Rate: ${health.currentHeartRate ?: "No data"} bpm")
            Text("Average Heart Rate: ${health.averageHeartRate ?: "No data"} bpm")
            Text("Min Heart Rate: ${health.minimumHeartRate ?: "No data"} bpm")
            Text("Max Heart Rate: ${health.maximumHeartRate ?: "No data"} bpm")
            Text("SpO₂: ${health.spO2 ?: "No data"}%")
            Text("Sleep: ${health.sleepMinutes} minutes")
        }
    }
}