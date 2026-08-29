package cit.edu.primera.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.primera.core.theme.*
import cit.edu.primera.ui.components.PrimeraLogoBubble
import cit.edu.primera.ui.components.PrimeraGradientButton
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            PrimeraLogoBubble()
            Spacer(Modifier.height(40.dp))

            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Enter your email address and we'll send you a link to reset your password.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

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
            
            Spacer(Modifier.height(40.dp))
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
            Spacer(Modifier.height(16.dp))
            Text(
                text = state.errorMessage,
                color = ErrorRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(40.dp))

        PrimeraGradientButton(
            text = "Send Reset Link",
            onClick = onResetPasswordClicked,
            isLoading = state.isLoading
        )

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onBackToLogin) {
            Text(
                text = "Back to Login",
                style = MaterialTheme.typography.labelLarge,
                color = PrimeraViolet,
                fontWeight = FontWeight.Medium
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
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = "Check your email",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            text = "We have sent a password recover instructions to your email.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(32.dp))
        
        PrimeraGradientButton(
            text = "Back to Login",
            onClick = onBackToLogin
        )
    }
}
