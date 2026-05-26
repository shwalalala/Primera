package com.example.primera.frontend.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.primera.frontend.features.auth.*
import com.example.primera.frontend.common.components.AuthTab

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.AUTH_SCREEN
) {
    val authViewModel: AuthViewModel = viewModel()

    LaunchedEffect(Unit) {
        authViewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.NavigateToDashboard -> {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.AUTH_SCREEN) { inclusive = true }
                    }
                }
                is AuthEffect.NavigateToForgotPassword -> {
                    navController.navigate(Routes.FORGOT_PW)
                }
                is AuthEffect.ShowSnackbar -> { /* Handle snackbar */ }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.AUTH_SCREEN) {
            val state by authViewModel.state.collectAsState()
            AuthScreenHost(
                state = state,
                authViewModel = authViewModel,
                onTermsClicked = { navController.navigate(Routes.FORGOT_PW) }
            )
        }

        composable(Routes.DASHBOARD) {
            /* Dashboard Screen placeholder */
        }

        composable(Routes.FORGOT_PW) {
            /* Forgot Password Screen placeholder */
        }
    }
}

@Composable
private fun AuthScreenHost(
    state: AuthState,
    authViewModel: AuthViewModel,
    onTermsClicked: () -> Unit
) {
    when (state.activeTab) {
        AuthTab.LOGIN -> LoginScreen(
            state = state,
            onEmailChange = authViewModel::onEmailChange,
            onPasswordChange = authViewModel::onPasswordChange,
            onRememberMeToggle = authViewModel::onRememberMeToggle,
            onForgotPasswordClicked = authViewModel::onForgotPasswordClicked,
            onLoginClicked = authViewModel::onLoginClicked,
            onTabSelected = authViewModel::onTabSelected
        )
        AuthTab.SIGNUP -> RegisterScreen(
            state = state,
            onFullNameChange = authViewModel::onFullNameChange,
            onEmailChange = authViewModel::onEmailChange,
            onPasswordChange = authViewModel::onPasswordChange,
            onAgreedToTermsToggle = authViewModel::onAgreedToTermsToggle,
            onTermsClicked = onTermsClicked,
            onSignUpClicked = authViewModel::onSignUpClicked,
            onTabSelected = authViewModel::onTabSelected
        )
    }
}
