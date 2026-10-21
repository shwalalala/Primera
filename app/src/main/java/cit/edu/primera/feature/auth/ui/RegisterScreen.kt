package cit.edu.primera.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import cit.edu.primera.core.theme.TrendGreen
import cit.edu.primera.core.utils.clickableNoRipple
import cit.edu.primera.ui.components.PrimeraCheckbox
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
    onAgreedToTermsToggle: (Boolean) -> Unit,
    onTermsClicked: () -> Unit,
    onDismissTermsDialog: () -> Unit = {},
    onSignUpClicked: () -> Unit,
    onTabSelected: (AuthTab) -> Unit,
    onSocialLoginClicked: (String) -> Unit,
    onFacebookLoginClicked: () -> Unit,
    onGoogleLoginClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        (BackgroundCream),
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            PrimeraLogoBubble()
            Spacer(Modifier.height(40.dp))
            
            AuthTabToggle(
                selectedTab = state.activeTab,
                onTabSelected = onTabSelected
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary
            )
            Text(
                text = "Sign up to start your journey",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(32.dp))
            
            PrimeraTextField(
                value = state.username,
                onValueChange = onUsernameChange,
                placeholder = "Username",
                isError = state.usernameError != null,
                errorMessage = state.usernameError
            )
            
            Spacer(Modifier.height(16.dp))
            
            PrimeraTextField(
                value = state.email,
                onValueChange = onEmailChange,
                placeholder = "Email",
                isError = state.emailError != null,
                errorMessage = state.emailError
            )
            
            Spacer(Modifier.height(16.dp))
            
            PrimeraTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                placeholder = "Password",
                isPassword = true,
                isError = state.passwordError != null,
                errorMessage = state.passwordError
            )

            if (state.password.isNotEmpty()) {
                PasswordStrengthBar(strength = state.passwordStrength)
            }

            if (state.passwordError == null) {
                Text(
                    text = "Password must be at least 8 characters with letters & numbers",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(Modifier.height(16.dp))

            PrimeraTextField(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = "Confirm Password",
                isPassword = true,
                isError = state.confirmPasswordError != null,
                errorMessage = state.confirmPasswordError
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrimeraCheckbox(
                    checked = state.agreedToTerms,
                    onCheckedChange = onAgreedToTermsToggle,
                    label = "I agree to the "
                )
                Text(
                    text = "Terms & Conditions",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimeraViolet,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickableNoRipple { onTermsClicked() }
                )
            }
            
            if (state.termsError != null) {
                Text(
                    text = state.termsError,
                    color = ErrorRed,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            PrimeraGradientButton(
                text = "Sign Up",
                onClick = onSignUpClicked,
                isLoading = state.isLoading
            )
            
            if (state.errorMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = state.errorMessage,
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            OrDivider()
            
            Spacer(Modifier.height(24.dp))
            
            SocialAuthRow(
                onGoogleClick = onGoogleLoginClicked,
                onFacebookClick = onFacebookLoginClicked,
                onAppleClick = { onSocialLoginClicked("Apple") },
                onPhoneClick = { onSocialLoginClicked("Phone") }
            )
            
            Spacer(Modifier.height(40.dp))
        }

        if (state.showTermsDialog) {
            TermsAndConditionsDialog(
                onDismiss = onDismissTermsDialog,
                onAccept = { onAgreedToTermsToggle(true) }
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
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.LightGray.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(flexWeight)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Password Strength: ${strength.label}",
            style = MaterialTheme.typography.labelSmall,
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

@Preview(name = "Compact Phone", device = Devices.PHONE)
@Preview(name = "Small Phone", widthDp = 360, heightDp = 640)
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
            onSignUpClicked = {},
            onTabSelected = {},
            onSocialLoginClicked = {},
            onFacebookLoginClicked = {}
        )
    }
}
