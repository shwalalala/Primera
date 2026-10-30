package cit.edu.primera.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.ErrorRed
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.core.utils.clickableNoRipple
import cit.edu.primera.ui.components.PrimeraCheckbox
import cit.edu.primera.ui.components.PrimeraGradientButton
import cit.edu.primera.ui.components.PrimeraLogoBubble
import cit.edu.primera.ui.components.PrimeraTextField

@Composable
fun LoginScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeToggle: (Boolean) -> Unit,
    onForgotPasswordClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    onTabSelected: (AuthTab) -> Unit,
    onSocialLoginClicked: (String) -> Unit,
    onFacebookLoginClicked: () -> Unit,
    onGoogleLoginClicked: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onDismissAccountLinkingDialog: () -> Unit = {},
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            PrimeraLogoBubble(size = 76.dp)
            Spacer(Modifier.height(14.dp))

            AuthTabToggle(
                selectedTab = AuthTab.LOGIN,
                onTabSelected = onTabSelected,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Please enter your account details",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            PrimeraTextField(
                value = state.email,
                onValueChange = onEmailChange,
                placeholder = "Email",
                keyboardType = KeyboardType.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                isError = state.emailError != null,
                errorMessage = state.emailError
            )

            Spacer(Modifier.height(12.dp))

            PrimeraTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                placeholder = "Password",
                isPassword = true,
                keyboardType = KeyboardType.Password,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onLoginClicked() }),
                isError = state.passwordError != null,
                errorMessage = state.passwordError
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PrimeraCheckbox(
                    checked = state.rememberMe,
                    onCheckedChange = onRememberMeToggle,
                    label = "Remember me"
                )
                Text(
                    text = "Forgot password?",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = PrimeraViolet,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickableNoRipple { onForgotPasswordClicked() }
                )
            }

            Spacer(Modifier.height(20.dp))

            PrimeraGradientButton(
                text = if (state.cooldownSeconds > 0) "Wait (${state.cooldownSeconds}s)" else "Login",
                onClick = onLoginClicked,
                isLoading = state.isLoading
            )

            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.errorMessage,
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))

            OrDivider()

            Spacer(Modifier.height(16.dp))

            SocialAuthRow(
                onGoogleClick = onGoogleLoginClicked,
                onFacebookClick = onFacebookLoginClicked
            )

            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = TextSecondary
                )
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PrimeraViolet,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }

        if (state.showAccountLinkingDialog) {
            AccountLinkingDialog(
                providerName = state.pendingSocialProvider,
                onDismiss = onDismissAccountLinkingDialog
            )
        }
    }
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640)
@Preview(name = "Compact Phone", device = Devices.PHONE)
@Preview(name = "Large Phone", widthDp = 480, heightDp = 960)
@Composable
private fun LoginScreenPreview() {
    PrimeraTheme {
        LoginScreen(
            state = AuthUiState(activeTab = AuthTab.LOGIN),
            onEmailChange = {},
            onPasswordChange = {},
            onRememberMeToggle = {},
            onForgotPasswordClicked = {},
            onLoginClicked = {},
            onTabSelected = {},
            onSocialLoginClicked = {},
            onFacebookLoginClicked = {}
        )
    }
}
