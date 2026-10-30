package cit.edu.primera.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.CheckboxBorder
import cit.edu.primera.core.theme.ErrorRed
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.SurfaceWhite
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.core.theme.TrendGreen
import cit.edu.primera.core.utils.clickableNoRipple
import cit.edu.primera.ui.components.PrimeraGradientButton
import cit.edu.primera.ui.components.PrimeraLogoBubble
import cit.edu.primera.ui.components.PrimeraTextField

@Composable
fun RegisterScreen(
    state: AuthUiState,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit = {},
    onAgreedToTermsToggle: (Boolean) -> Unit = {},
    onTermsClicked: () -> Unit = {},
    onDismissTermsDialog: () -> Unit = {},
    onSignUpClicked: () -> Unit = {},
    onTabSelected: (AuthTab) -> Unit = {},
    onSocialLoginClicked: (String) -> Unit = {},
    onFacebookLoginClicked: () -> Unit = {},
    onGoogleLoginClicked: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
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
                selectedTab = AuthTab.SIGNUP,
                onTabSelected = onTabSelected,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Sign up to start your journey",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            PrimeraTextField(
                value = state.username,
                onValueChange = onUsernameChange,
                placeholder = "Username",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                isError = state.usernameError != null,
                errorMessage = state.usernameError
            )

            Spacer(Modifier.height(12.dp))

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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                isError = state.passwordError != null,
                errorMessage = state.passwordError
            )

            if (state.password.isNotEmpty()) {
                PasswordStrengthBar(strength = state.passwordStrength)
            }

            if (state.passwordError == null) {
                Text(
                    text = "Password must be at least 8 characters with letters & numbers",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 2.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            PrimeraTextField(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = "Confirm Password",
                isPassword = true,
                keyboardType = KeyboardType.Password,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSignUpClicked() }),
                isError = state.confirmPasswordError != null,
                errorMessage = state.confirmPasswordError
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = state.agreedToTerms,
                        onValueChange = onAgreedToTermsToggle,
                        role = Role.Checkbox
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.agreedToTerms,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimeraViolet,
                        uncheckedColor = CheckboxBorder,
                        checkmarkColor = SurfaceWhite
                    )
                )
                Text(
                    text = "I agree to the ",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextSecondary
                )
                Text(
                    text = "Terms & Conditions",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PrimeraViolet,
                    modifier = Modifier.clickableNoRipple { onTermsClicked() }
                )
            }

            if (state.termsError != null) {
                Text(
                    text = state.termsError,
                    color = ErrorRed,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            PrimeraGradientButton(
                text = if (state.cooldownSeconds > 0) "Wait (${state.cooldownSeconds}s)" else "Sign Up",
                onClick = onSignUpClicked,
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
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = TextSecondary
                )
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PrimeraViolet,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }

        if (state.showTermsDialog) {
            TermsAndConditionsDialog(
                onDismiss = onDismissTermsDialog,
                onAccept = { onAgreedToTermsToggle(true) }
            )
        }

        if (state.showAccountLinkingDialog) {
            AccountLinkingDialog(
                providerName = state.pendingSocialProvider,
                onDismiss = onDismissAccountLinkingDialog
            )
        }
    }
}

@Composable
fun PasswordStrengthBar(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    val (barColor, flexWeight) = when (strength) {
        PasswordStrength.NONE -> Pair(Color.Transparent, 0.0f)
        PasswordStrength.WEAK -> Pair(ErrorRed, 0.33f)
        PasswordStrength.FAIR -> Pair(Color(0xFFFFB300), 0.66f)
        PasswordStrength.STRONG -> Pair(TrendGreen, 1.0f)
    }

    if (strength == PasswordStrength.NONE) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.LightGray.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(flexWeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Password Strength: ${strength.label}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = barColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TermsAndConditionsDialog(
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Terms & Conditions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Welcome to Primera. By using this application, you agree to comply with and be bound by the following terms and conditions:\n\n" +
                            "1. Privacy & Data Protection: Your health log data is securely stored and processed in accordance with medical data confidentiality guidelines.\n\n" +
                            "2. Medical Disclaimer: Primera is an educational and self-tracking tool. It is NOT a substitute for professional medical advice, diagnosis, or treatment.\n\n" +
                            "3. User Responsibilities: You are responsible for keeping your login credentials confidential.\n\n" +
                            "4. Terms Updates: We reserve the right to modify these terms as needed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onAccept()
                onDismiss()
            }) {
                Text("I Agree", color = PrimeraViolet, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640)
@Preview(name = "Compact Phone", device = Devices.PHONE)
@Preview(name = "Large Phone", widthDp = 480, heightDp = 960)
@Composable
private fun RegisterScreenPreview() {
    PrimeraTheme {
        RegisterScreen(
            state = AuthUiState(activeTab = AuthTab.SIGNUP),
            onUsernameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onAgreedToTermsToggle = {},
            onTermsClicked = {},
            onSignUpClicked = {}
        )
    }
}
