package com.example.primera.frontend.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.primera.frontend.common.components.AuthTab
import com.example.primera.frontend.features.auth.*
import com.example.primera.frontend.features.dashboard.DashboardScreen

// Routes where the bottom nav should be visible
private val bottomNavRoutes = setOf(
    Routes.DASHBOARD,
    Routes.DEVICE,
    Routes.INSIGHT,
    Routes.CHECKIN
)

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.AUTH_SCREEN
) {
    val authViewModel: AuthViewModel = viewModel()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

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

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                PrimeraBottomNav(
                    currentRoute          = currentRoute ?: Routes.DASHBOARD,
                    onDestinationSelected = { dest ->
                        navController.navigate(dest.route) {
                            // Keep the back stack clean — don't stack duplicates
                            popUpTo(Routes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                    onFabClicked = { /* open quick-action / mic sheet */ }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = startDestination,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Routes.AUTH_SCREEN) {
                val state by authViewModel.state.collectAsState()
                AuthScreenHost(
                    state          = state,
                    authViewModel  = authViewModel,
                    onTermsClicked = { navController.navigate(Routes.FORGOT_PW) }
                )
            }

            composable(Routes.DASHBOARD) {
                DashboardScreen()
            }

            composable(Routes.DEVICE) {
                /* DeviceScreen() — placeholder */
            }

            composable(Routes.INSIGHT) {
                /* InsightScreen() — placeholder */
            }

            composable(Routes.CHECKIN) {
                /* CheckInScreen() — placeholder */
            }

            composable(Routes.FORGOT_PW) {
                /* ForgotPasswordScreen() — placeholder */
            }
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
            state                  = state,
            onEmailChange          = authViewModel::onEmailChange,
            onPasswordChange       = authViewModel::onPasswordChange,
            onRememberMeToggle     = authViewModel::onRememberMeToggle,
            onForgotPasswordClicked = authViewModel::onForgotPasswordClicked,
            onLoginClicked         = authViewModel::onLoginClicked,
            onTabSelected          = authViewModel::onTabSelected
        )
        AuthTab.SIGNUP -> RegisterScreen(
            state                  = state,
            onFullNameChange       = authViewModel::onFullNameChange,
            onEmailChange          = authViewModel::onEmailChange,
            onPasswordChange       = authViewModel::onPasswordChange,
            onAgreedToTermsToggle  = authViewModel::onAgreedToTermsToggle,
            onTermsClicked         = onTermsClicked,
            onSignUpClicked        = authViewModel::onSignUpClicked,
            onTabSelected          = authViewModel::onTabSelected
        )
    }
}