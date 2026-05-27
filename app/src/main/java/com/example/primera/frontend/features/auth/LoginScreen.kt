package com.example.primera.frontend.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Devices
import com.example.primera.frontend.common.components.*
import com.example.primera.frontend.common.theme.*

@Composable
fun LoginScreen(
    state: AuthState,
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
            .background(BackgroundCream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            ArchedAuthCard(
                logo = { PrimeraLogoBubble(it) }
            ) {
                Text(
                    text = "Get Started now",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Create an account or log in to explore\nabout our app",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(32.dp))

                AuthTabToggle(
                    selectedTab = AuthTab.LOGIN,
                    onTabSelected = onTabSelected
                )

                Spacer(Modifier.height(32.dp))

                LabeledField(
                    label = "Email",
                    value = state.email,
                    onValueChange = onEmailChange,
                    placeholder = "Enter your email",
                    keyboardType = KeyboardType.Email,
                    isError = state.emailError != null,
                    errorMessage = state.emailError
                )

                Spacer(Modifier.height(18.dp))

                LabeledField(
                    label = "Password",
                    value = state.password,
                    onValueChange = onPasswordChange,
                    placeholder = "Enter your password",
                    isPassword = true,
                    isError = state.passwordError != null,
                    errorMessage = state.passwordError
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = state.rememberMe,
                        onCheckedChange = onRememberMeToggle,
                        colors = CheckboxDefaults.colors(
                            checkedColor = PrimeraViolet,
                            uncheckedColor = CheckboxBorder,
                            checkmarkColor = SurfaceWhite
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Remember me",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "Forgot Password ?",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextLink,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clickable { onForgotPasswordClicked() }
                    )
                }

                Spacer(Modifier.height(32.dp))

                PrimeraGradientButton(
                    text = "Log In",
                    onClick = onLoginClicked,
                    isLoading = state.isLoading
                )

                Spacer(Modifier.height(32.dp))

                OrDivider()

                Spacer(Modifier.height(24.dp))

                SocialAuthRow()

                Spacer(Modifier.height(16.dp))
            }
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
            state = AuthState(activeTab = AuthTab.LOGIN),
            onEmailChange = {},
            onPasswordChange = {},
            onRememberMeToggle = {},
            onForgotPasswordClicked = {},
            onLoginClicked = {},
            onTabSelected = {}
        )
    }
}
