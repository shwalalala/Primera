package com.example.primera.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.core.utils.clickableNoRipple
import com.example.primera.core.theme.*
import com.example.primera.ui.components.*

@Composable
fun LoginScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeToggle: (Boolean) -> Unit,
    onForgotPasswordClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    onTabSelected: (AuthTab) -> Unit,
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
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary
            )
            Text(
                text = "Please enter your account details",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(32.dp))
            
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
            
            Spacer(Modifier.height(12.dp))
            
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
                    style = MaterialTheme.typography.labelSmall,
                    color = TextHint,
                    modifier = Modifier.clickableNoRipple { onForgotPasswordClicked() }
                )
            }
            
            Spacer(Modifier.height(40.dp))
            
            PrimeraGradientButton(
                text = "Login",
                onClick = onLoginClicked,
                isLoading = state.isLoading
            )
            
            if (state.errorMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(text = state.errorMessage, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(Modifier.height(24.dp))
            
            OrDivider()
            
            Spacer(Modifier.height(24.dp))
            
            SocialAuthRow()
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Preview(name = "Compact Phone", device = Devices.PHONE)
@Preview(name = "Small Phone", widthDp = 360, heightDp = 640)
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
            onTabSelected = {}
        )
    }
}
