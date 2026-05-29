package com.example.primera.feature.smartwatchconnection.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.primera.feature.smartwatchconnection.ui.SmartwatchScreen

@Composable
fun SmartwatchRoute(
    viewModel: SmartwatchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = PermissionController.createRequestPermissionResultContract()
        ) { grantedPermissions ->
            viewModel.onPermissionResult(grantedPermissions)
        }

    LaunchedEffect(Unit) {
        viewModel.checkHealthConnectStatus()
    }

    SmartwatchScreen(
        uiState = uiState,
        onRequestPermissions = {
            permissionLauncher.launch(viewModel.permissions)
        },
        onReadAndSave = {
            viewModel.readAndSaveSmartwatchHealth()
        }
    )
}