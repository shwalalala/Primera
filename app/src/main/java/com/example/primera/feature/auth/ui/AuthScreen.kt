package com.example.primera.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.primera.ui.components.*
import com.example.primera.core.theme.*

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.NavigateToDashboard -> onNavigateToDashboard()
                is AuthEffect.NavigateToForgotPassword -> onNavigateToForgotPassword()
                is AuthEffect.NavigateToLogin -> viewModel.onTabSelected(AuthTab.LOGIN)
                is AuthEffect.ShowSnackbar -> { /* Handle snackbar */ }
            }
        }
    }

    if (state.activeTab == AuthTab.LOGIN) {
        LoginScreenContent(
            state = state,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onRememberMeToggle = viewModel::onRememberMeToggle,
            onForgotPasswordClicked = viewModel::onForgotPasswordClicked,
            onLoginClicked = viewModel::onLoginClicked,
            onTabSelected = viewModel::onTabSelected,
            modifier = modifier
        )
    } else {
        RegisterScreenContent(
            state = state,
            onFullNameChange = viewModel::onFullNameChange,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onAgreedToTermsToggle = viewModel::onAgreedToTermsToggle,
            onTermsClicked = { /* Handle terms */ },
            onSignUpClicked = viewModel::onSignUpClicked,
            onTabSelected = viewModel::onTabSelected,
            modifier = modifier
        )
    }
}

@Composable
fun LoginScreenContent(
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
                    text = "Create an account or log in to explore\\nabout our app",
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

@Composable
fun RegisterScreenContent(
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
                    text = "Create an account or log in to explore\\nabout our app",
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
