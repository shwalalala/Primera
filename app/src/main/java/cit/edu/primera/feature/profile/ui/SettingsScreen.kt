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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import cit.edu.primera.core.theme.PrimeraViolet
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
    onOpenReminderSettings: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

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

    var showQrDialog by remember { mutableStateOf(false) }

    if (showQrDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val dashboardViewModel: cit.edu.primera.feature.dashboard.ui.DashboardViewModel =
            viewModel(factory = ViewModelProvider.Factory)
        val summaryQrContent by dashboardViewModel.summaryQrContent.collectAsStateWithLifecycle()
        cit.edu.primera.ui.components.SummaryQrDialog(
            onDismissRequest = { showQrDialog = false },
            onSavePdf = { dashboardViewModel.onSaveSummaryPdf(context) },
            onPrint = { dashboardViewModel.onPrintSummaryPdf(context) },
            onShare = { dashboardViewModel.onShareSummaryPdf(context) },
            onRequestPdfUrl = { dashboardViewModel.getSummaryPdfUrl(context) },
            qrContent = summaryQrContent
        )
    }

    SettingsContent(
        state = state,
        onBack = onBack,
        showLogoutDialog = showLogoutDialog,
        onShowLogoutDialogChange = { showLogoutDialog = it },
        showDeleteAccountDialog = showDeleteAccountDialog,
        onShowDeleteAccountDialogChange = { showDeleteAccountDialog = it },
        onLogout = {
            viewModel.logout()
            onLogout()
        },
        onDeleteAccountConfirm = {
            viewModel.deleteAccount(onLogout)
        },
        onLinkGoogle = launchGoogleLogin,
        onLinkFacebook = launchFacebookLogin,
        onNewEmailChange = viewModel::onNewEmailChange,
        onRequestEmailChange = viewModel::requestEmailChange,
        onRequestPasswordReset = viewModel::sendPasswordResetEmail,
        onShowQrDialog = { showQrDialog = true },
        onOpenReminderSettings = onOpenReminderSettings
    )
}

@Composable
fun SettingsContent(
    state: SettingsUiState,
    onBack: () -> Unit,
    showLogoutDialog: Boolean,
    onShowLogoutDialogChange: (Boolean) -> Unit,
    showDeleteAccountDialog: Boolean = false,
    onShowDeleteAccountDialogChange: (Boolean) -> Unit = {},
    onLogout: () -> Unit,
    onDeleteAccountConfirm: () -> Unit = {},
    onLinkGoogle: () -> Unit,
    onLinkFacebook: () -> Unit,
    onNewEmailChange: (String) -> Unit,
    onRequestEmailChange: () -> Unit,
    onRequestPasswordReset: () -> Unit,
    onShowQrDialog: () -> Unit = {},
    onOpenReminderSettings: () -> Unit = {},
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
                    .padding(20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Account & Security Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Reminder Settings Section
                SettingsCard(title = "Notification & Reminder Preferences") {
                    Text(
                        text = "Customize daily check-in alerts, hydration reminders, and evening wind-down schedules.",
                        fontSize = 13.sp,
                        color = cit.edu.primera.core.theme.TextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onOpenReminderSettings,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            cit.edu.primera.core.theme.MenuButtonBorder
                        )
                    ) {
                        Text(
                            "Manage Reminder Preferences",
                            color = PrimeraViolet,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                SettingsCard(title = "Link Accounts") {
                    Text(
                        text = "Add another sign-in method to access this account.",
                        fontSize = 13.sp,
                        color = cit.edu.primera.core.theme.TextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    if ("google.com" in state.authProviders) {
                        Text(
                            text = "✓ Google account linked",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimeraViolet
                        )
                    } else {
                        OutlinedButton(
                            onClick = onLinkGoogle,
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                cit.edu.primera.core.theme.MenuButtonBorder
                            )
                        ) {
                            Text(
                                "Link Google account",
                                color = PrimeraViolet,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if ("facebook.com" in state.authProviders) {
                        Text(
                            text = "✓ Facebook account linked",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimeraViolet
                        )
                    } else {
                        OutlinedButton(
                            onClick = onLinkFacebook,
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                cit.edu.primera.core.theme.MenuButtonBorder
                            )
                        ) {
                            Text(
                                "Link Facebook account",
                                color = PrimeraViolet,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Request Email Change Section
                SettingsCard(title = "Request Email Change") {
                    Text(
                        text = "Enter your desired new email address below. We will send a confirmation verification link to that address.",
                        fontSize = 13.sp,
                        color = cit.edu.primera.core.theme.TextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.newEmail,
                        onValueChange = onNewEmailChange,
                        label = { Text("New Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = cit.edu.primera.core.theme.InputBorder,
                            focusedBorderColor = PrimeraViolet
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    PrimeraGradientButton(
                        text = "Send Email Change Link",
                        onClick = onRequestEmailChange,
                        isLoading = state.isLoading
                    )
                }

                // Request Password Reset Section
                SettingsCard(title = "Reset Password") {
                    Text(
                        text = "To update your password, request a password reset link sent directly to your registered email address.",
                        fontSize = 13.sp,
                        color = cit.edu.primera.core.theme.TextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    PrimeraGradientButton(
                        text = "Request Password Reset Link",
                        onClick = onRequestPasswordReset,
                        isLoading = state.isLoading
                    )
                }

                // Health Summary & QR Code Export Section
                SettingsCard(title = "Health Summary & Sharing") {
                    Text(
                        text = "Generate your automatically updated QR code to share your health summary with care providers, or save as PDF, print, or share.",
                        fontSize = 13.sp,
                        color = cit.edu.primera.core.theme.TextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    cit.edu.primera.ui.components.PrimeraOutlinedButton(
                        text = "View Summary QR & Options",
                        onClick = onShowQrDialog,
                        color = PrimeraViolet
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Logout Button
                cit.edu.primera.ui.components.PrimeraOutlinedButton(
                    text = "Logout",
                    onClick = { onShowLogoutDialogChange(true) },
                    color = ErrorRed
                )

                // Delete Account Button
                OutlinedButton(
                    onClick = { onShowDeleteAccountDialogChange(true) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed),
                ) {
                    Text(
                        text = "Delete Account",
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // Medical & Fetal Size Data Source Attribution Footer
                Text(
                    text = "Fetal growth & week-by-week baby size comparisons referenced from Healthdirect Australia Limited (pregnancybirthbaby.org.au).",
                    fontSize = 11.sp,
                    color = cit.edu.primera.core.theme.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
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

        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { onShowDeleteAccountDialogChange(false) },
                title = { Text("Delete Account?") },
                text = {
                    Text("Are you sure you want to delete your account? All your personal health records, profile details, and account data will be permanently deleted. This action cannot be undone.")
                },
                confirmButton = {
                    OutlinedButton(
                        onClick = {
                            onShowDeleteAccountDialogChange(false)
                            onDeleteAccountConfirm()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Text("Delete Account permanently")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { onShowDeleteAccountDialogChange(false) }) {
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
            onNewEmailChange = {},
            onRequestEmailChange = {},
            onRequestPasswordReset = {}
        )
    }
}
