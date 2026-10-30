package cit.edu.primera.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.ErrorRed
import cit.edu.primera.core.theme.Good
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.ui.components.PrimeraGradientButton
import cit.edu.primera.ui.components.PrimeraLogoBubble
import cit.edu.primera.ui.components.PrimeraTextField

@Composable
fun ForgotPasswordScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onResetPasswordClicked: () -> Unit,
    onBackToLogin: () -> Unit,
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            PrimeraLogoBubble(size = 76.dp)
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Enter your email address and we'll send you a link to reset your password.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            if (state.resetEmailSent) {
                SuccessView(onBackToLogin)
            } else {
                ResetForm(
                    state = state,
                    onEmailChange = onEmailChange,
                    onResetPasswordClicked = onResetPasswordClicked,
                    onBackToLogin = onBackToLogin
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ResetForm(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onResetPasswordClicked: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PrimeraTextField(
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = "Email",
            isError = state.emailError != null,
            errorMessage = state.emailError
        )

        if (state.errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = state.errorMessage,
                color = ErrorRed,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))

        PrimeraGradientButton(
            text = "Send Reset Link",
            onClick = onResetPasswordClicked,
            isLoading = state.isLoading
        )

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onBackToLogin) {
            Text(
                text = "Back to Login",
                style = MaterialTheme.typography.labelLarge,
                color = PrimeraViolet,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SuccessView(onBackToLogin: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Good,
            modifier = Modifier.size(56.dp)
        )

        Spacer(Modifier.height(12.dp))
        
        Text(
            text = "Check your email",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))
        
        Text(
            text = "We have sent password recovery instructions to your email.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))
        
        PrimeraGradientButton(
            text = "Back to Login",
            onClick = onBackToLogin
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordPreview() {
    PrimeraTheme {
        ForgotPasswordScreen(
            state = AuthUiState(),
            onEmailChange = {},
            onResetPasswordClicked = {},
            onBackToLogin = {}
        )
    }
}
