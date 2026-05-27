package com.example.primera.frontend.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Devices
import com.example.primera.frontend.common.components.*
import com.example.primera.frontend.common.theme.*

@Composable
fun RegisterScreen(
    state: AuthState,
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
                    selectedTab = AuthTab.SIGNUP,
                    onTabSelected = onTabSelected
                )

                Spacer(Modifier.height(32.dp))

                LabeledField(
                    label = "Full Name",
                    value = state.fullName,
                    onValueChange = onFullNameChange,
                    placeholder = "Enter your full name",
                    isError = state.fullNameError != null,
                    errorMessage = state.fullNameError
                )

                Spacer(Modifier.height(18.dp))

                LabeledField(
                    label = "Email",
                    value = state.email,
                    onValueChange = onEmailChange,
                    placeholder = "Enter a valid email",
                    keyboardType = KeyboardType.Email,
                    isError = state.emailError != null,
                    errorMessage = state.emailError
                )

                Spacer(Modifier.height(18.dp))

                LabeledField(
                    label = "Password",
                    value = state.password,
                    onValueChange = onPasswordChange,
                    placeholder = "Enter valid password (minimum of 8 characters)",
                    isPassword = true,
                    isError = state.passwordError != null,
                    errorMessage = state.passwordError
                )

                Spacer(Modifier.height(16.dp))

                TermsCheckboxRow(
                    checked = state.agreedToTerms,
                    onCheckedChange = onAgreedToTermsToggle,
                    onTermsClicked = onTermsClicked,
                    error = state.termsError
                )

                Spacer(Modifier.height(32.dp))

                PrimeraGradientButton(
                    text = "Sign Up",
                    onClick = onSignUpClicked,
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

@Composable
private fun TermsCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClicked: () -> Unit,
    error: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimeraViolet,
                    uncheckedColor = CheckboxBorder,
                    checkmarkColor = SurfaceWhite
                ),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = buildAnnotatedString {
                    append("I agree to Primera's ")
                    withStyle(
                        SpanStyle(
                            color = TextBlueLink,
                            fontWeight = FontWeight.Normal,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Terms & Conditions.")
                    }
                },
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable { onTermsClicked() }
            )
        }
        if (error != null) {
            Text(
                text = error,
                color = ErrorRed,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 32.dp, top = 2.dp)
            )
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
            state = AuthState(activeTab = AuthTab.SIGNUP),
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
