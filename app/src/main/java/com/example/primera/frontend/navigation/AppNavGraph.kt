package com.example.primera.frontend.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.primera.frontend.common.components.AuthTab
import com.example.primera.frontend.common.theme.*
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
                is AuthEffect.NavigateToLogin -> {
                    navController.navigate(Routes.AUTH_SCREEN) {
                        popUpTo(0) { inclusive = true }
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
            modifier         = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = if (currentRoute in bottomNavRoutes) innerPadding.calculateBottomPadding() else 0.dp
            )
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
                DashboardScreen(onLogout = { authViewModel.logout() })
            }

            composable(Routes.DEVICE) {
                PlaceholderScreen("Device Screen")
            }

            composable(Routes.INSIGHT) {
                PlaceholderScreen("Insights Screen")
            }

            composable(Routes.CHECKIN) {
                PlaceholderScreen("Check-In Screen")
            }

            composable(Routes.FORGOT_PW) {
                PlaceholderScreen("Forgot Password Screen")
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Coming soon...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
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