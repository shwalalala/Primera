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
fun RegisterScreen(
    state: AuthUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAgreedToTermsToggle: (Boolean) -> Unit,
    onTermsClicked: () -> Unit,
    onSignUpClicked: () -> Unit,
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
                value = state.fullName,
                onValueChange = onFullNameChange,
                placeholder = "Full Name",
                isError = state.fullNameError != null,
                errorMessage = state.fullNameError
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
            
            Spacer(Modifier.height(12.dp))
            
            PrimeraCheckbox(
                checked = state.agreedToTerms,
                onCheckedChange = onAgreedToTermsToggle,
                label = "I agree to the Terms & Conditions"
            )
            
            if (state.termsError != null) {
                Text(
                    text = state.termsError,
                    color = ErrorRed,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            
            Spacer(Modifier.height(40.dp))
            
            PrimeraGradientButton(
                text = "Sign Up",
                onClick = onSignUpClicked,
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
private fun RegisterScreenPreview() {
    PrimeraTheme {
        RegisterScreen(
            state = AuthUiState(activeTab = AuthTab.SIGNUP),
            onFullNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onAgreedToTermsToggle = {},
            onTermsClicked = {},
            onSignUpClicked = {},
            onTabSelected = {}
        )
    }
}
