package com.example.primera.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.primera.feature.auth.ui.AuthScreen
import com.example.primera.feature.auth.ui.AuthViewModel
import com.example.primera.feature.dashboard.ui.DashboardScreen
import com.example.primera.feature.dashboard.ui.DashboardViewModel
import com.example.primera.core.theme.*

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
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                PrimeraBottomNav(
                    currentRoute          = currentRoute ?: Routes.DASHBOARD,
                    onDestinationSelected = { dest ->
                        navController.navigate(dest.route) {
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
                val authViewModel: AuthViewModel = hiltViewModel()
                AuthScreen(
                    viewModel = authViewModel,
                    onNavigateToDashboard = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.AUTH_SCREEN) { inclusive = true }
                        }
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Routes.FORGOT_PW)
                    }
                )
            }

            composable(Routes.DASHBOARD) {
                val authViewModel: AuthViewModel = hiltViewModel()
                val dashboardViewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onLogout = { 
                        authViewModel.logout()
                        navController.navigate(Routes.AUTH_SCREEN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
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
