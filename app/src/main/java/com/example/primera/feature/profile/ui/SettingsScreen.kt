package com.example.primera.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.primera.core.di.ViewModelProvider
import com.example.primera.core.theme.*
import com.example.primera.ui.components.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundCream,
                        PrimeraLilac.copy(alpha = 0.45f)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FeatureTopBar(
                title = "Settings",
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = "Account Security",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                
                Spacer(Modifier.height(16.dp))

                // Update Email Section
                SettingsCard(title = "Change Email") {
                    OutlinedTextField(
                        value = state.newEmail,
                        onValueChange = viewModel::onNewEmailChange,
                        label = { Text("New Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))
                    PrimeraGradientButton(
                        text = "Update Email",
                        onClick = viewModel::updateEmail,
                        isLoading = state.isLoading
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Update Password Section
                SettingsCard(title = "Change Password") {
                    OutlinedTextField(
                        value = state.newPassword,
                        onValueChange = viewModel::onNewPasswordChange,
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        label = { Text("Confirm New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))
                    PrimeraGradientButton(
                        text = "Update Password",
                        onClick = viewModel::updatePassword,
                        isLoading = state.isLoading
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Logout Button
                OutlinedButton(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(ErrorRed))
                ) {
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (state.isLoading) {
            FullScreenLoadingOverlay()
        }
        
        // Error/Success handling
        LaunchedEffect(state.error, state.successMessage) {
            if (state.error != null || state.successMessage != null) {
                // In a real app we'd show a snackbar
                viewModel.clearMessages()
            }
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}
