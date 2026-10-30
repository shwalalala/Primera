package cit.edu.primera.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cit.edu.primera.core.di.ViewModelProvider
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.ErrorRed
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.SurfaceWhite
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.feature.auth.ui.rememberFacebookLoginLauncher
import cit.edu.primera.feature.auth.ui.rememberGoogleLoginLauncher
import cit.edu.primera.ui.components.FeatureTopBar
import cit.edu.primera.ui.components.FullScreenLoadingOverlay
import cit.edu.primera.ui.components.PrimeraGradientButton

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val launchFacebookLogin = rememberFacebookLoginLauncher(
        onAuthComplete = viewModel::linkFacebookAccount,
        onAuthError = { error ->
            viewModel.showError(error.message ?: "Facebook sign-in failed")
        }
    )
    val launchGoogleLogin = rememberGoogleLoginLauncher(
        onAuthComplete = viewModel::linkGoogleAccount,
        onAuthError = { error ->
            viewModel.showError(error.message ?: "Google sign-in failed")
        }
    )

    SettingsContent(
        state = state,
        onBack = onBack,
        showLogoutDialog = showLogoutDialog,
        onShowLogoutDialogChange = { showLogoutDialog = it },
        onLogout = {
            viewModel.logout()
            onLogout()
        },
        onLinkGoogle = launchGoogleLogin,
        onLinkFacebook = launchFacebookLogin,
        onEmailCurrentPasswordChange = viewModel::onEmailCurrentPasswordChange,
        onNewEmailChange = viewModel::onNewEmailChange,
        onUpdateEmail = viewModel::updateEmail,
        onPasswordCurrentPasswordChange = viewModel::onPasswordCurrentPasswordChange,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onUpdatePassword = viewModel::updatePassword
    )
}

@Composable
fun SettingsContent(
    state: SettingsUiState,
    onBack: () -> Unit,
    showLogoutDialog: Boolean,
    onShowLogoutDialogChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onLinkGoogle: () -> Unit,
    onLinkFacebook: () -> Unit,
    onEmailCurrentPasswordChange: (String) -> Unit,
    onNewEmailChange: (String) -> Unit,
    onUpdateEmail: () -> Unit,
    onPasswordCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onUpdatePassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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

                SettingsCard(title = "Link Account") {
                    Text(
                        text = "Add another sign-in method to access this account.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(12.dp))
                    if ("google.com" in state.authProviders) {
                        Text("Google account linked", color = MaterialTheme.colorScheme.primary)
                    } else {
                        OutlinedButton(
                            onClick = onLinkGoogle,
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Link Google account")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if ("facebook.com" in state.authProviders) {
                        Text("Facebook account linked", color = MaterialTheme.colorScheme.primary)
                    } else {
                        OutlinedButton(
                            onClick = onLinkFacebook,
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Link Facebook account")
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Update Email Section
                SettingsCard(title = "Change Email") {
                    if (state.hasPasswordAuthProvider) {
                        OutlinedTextField(
                            value = state.emailCurrentPassword,
                            onValueChange = onEmailCurrentPasswordChange,
                            label = { Text("Current Password") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isLoading,
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    OutlinedTextField(
                        value = state.newEmail,
                        onValueChange = onNewEmailChange,
                        label = { Text("New Email") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))
                    PrimeraGradientButton(
                        text = "Update Email",
                        onClick = onUpdateEmail,
                        isLoading = state.isLoading
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Update Password Section
                SettingsCard(title = "Change Password") {
                    if (state.hasPasswordAuthProvider) {
                        OutlinedTextField(
                            value = state.passwordCurrentPassword,
                            onValueChange = onPasswordCurrentPasswordChange,
                            label = { Text("Current Password") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isLoading,
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    OutlinedTextField(
                        value = state.newPassword,
                        onValueChange = onNewPasswordChange,
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = { Text("Confirm New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))
                    PrimeraGradientButton(
                        text = "Update Password",
                        onClick = onUpdatePassword,
                        isLoading = state.isLoading
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Logout Button
                cit.edu.primera.ui.components.PrimeraOutlinedButton(
                    text = "Logout",
                    onClick = { onShowLogoutDialogChange(true) },
                    color = ErrorRed
                )
            }
        }

        if (state.isLoading) {
            FullScreenLoadingOverlay()
        }

        state.error?.let { error ->
            Text(
                text = error,
                color = ErrorRed,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        state.successMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { onShowLogoutDialogChange(false) },
                title = { Text("Log out?") },
                text = {
                    Text("You will be signed out and local app data will be cleared from this device.")
                },
                confirmButton = {
                    OutlinedButton(
                        onClick = {
                            onShowLogoutDialogChange(false)
                            onLogout()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Text("Log out")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { onShowLogoutDialogChange(false) }) {
                        Text("Cancel")
                    }
                }
            )
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

@Preview(name = "Compact Phone", device = Devices.PHONE)
@Preview(name = "Small Phone", widthDp = 360, heightDp = 640)
@Preview(name = "Large Phone", widthDp = 480, heightDp = 960)
@Composable
private fun SettingsScreenPreview() {
    PrimeraTheme {
        SettingsContent(
            state = SettingsUiState(
                newEmail = "sarah.miller@example.com",
                authProviders = setOf("password", "google.com")
            ),
            onBack = {},
            showLogoutDialog = false,
            onShowLogoutDialogChange = {},
            onLogout = {},
            onLinkGoogle = {},
            onLinkFacebook = {},
            onEmailCurrentPasswordChange = {},
            onNewEmailChange = {},
            onUpdateEmail = {},
            onPasswordCurrentPasswordChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onUpdatePassword = {}
        )
    }
}
