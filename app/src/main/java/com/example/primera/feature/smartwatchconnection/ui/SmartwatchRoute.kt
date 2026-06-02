package com.example.primera.feature.smartwatchconnection.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.primera.core.di.ViewModelProvider
import com.example.primera.feature.smartwatchconnection.ui.SmartwatchScreen

@Composable
fun SmartwatchRoute(
    viewModel: SmartwatchViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkHealthConnectStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = PermissionController.createRequestPermissionResultContract()
        ) { grantedPermissions ->
            viewModel.onPermissionResult(grantedPermissions)
        }

    SmartwatchScreen(
        uiState = uiState,
        onRequestPermissions = {
            try {
                permissionLauncher.launch(viewModel.permissions)
            } catch (e: Exception) {
                viewModel.onPermissionError(e)
            }
        },
        onReadAndSave = {
            viewModel.readAndSaveSmartwatchHealth()
        },
        onBackToSources = {
            viewModel.onBackToSources()
        },
        onOpenHealthConnect = {
            viewModel.onOpenHealthConnect()
        }
    )
}