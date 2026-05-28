package com.example.primera.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.primera.core.di.ViewModelProvider
import com.example.primera.feature.auth.ui.*
import com.example.primera.feature.checkins.ui.CheckinsOverviewScreen
import com.example.primera.feature.checkins.ui.CheckinsViewModel
import com.example.primera.feature.checkins.ui.DailyCheckinScreen
import com.example.primera.feature.dashboard.ui.DashboardScreen
import com.example.primera.feature.onboarding.ui.OnboardingHostScreen
import com.example.primera.feature.splash.ui.SplashScreen
import com.example.primera.feature.transcription.ui.TranscriptionScreen
import com.example.primera.feature.transcription.ui.TranscriptionViewModel
import com.example.primera.ui.components.AuthTab
import com.example.primera.core.theme.*
import com.example.primera.feature.welcome.ui.WelcomeScreen

// Routes where the bottom nav should be visible
private val bottomNavRoutes = setOf(
    Routes.DASHBOARD,
    Routes.DEVICE,
    Routes.INSIGHT,
    Routes.CHECKIN,
    Routes.DAILY_CHECKIN
)

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH
) {
    val authViewModel: AuthViewModel = viewModel(factory = ViewModelProvider.Factory)
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
                is AuthEffect.NavigateToOnboarding -> {
                    navController.navigate(Routes.ONBOARDING) {
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
                            popUpTo(Routes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                    onFabClicked = { navController.navigate(Routes.TRANSCRIPTION) }
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
            composable(Routes.SPLASH) {
                SplashScreen(
                    onTimeout = {
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToAuth = {
                        navController.navigate(Routes.AUTH_SCREEN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onNavigateToAuth = {
                        navController.navigate(Routes.AUTH_SCREEN) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.AUTH_SCREEN) {
                val state by authViewModel.uiState.collectAsStateWithLifecycle()
                AuthScreenHost(
                    state          = state,
                    authViewModel  = authViewModel,
                    onTermsClicked = { navController.navigate(Routes.FORGOT_PW) }
                )
            }

            composable(Routes.ONBOARDING) {
                OnboardingHostScreen(
                    onOnboardingComplete = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
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
                val checkinsViewModel: CheckinsViewModel = viewModel(factory = ViewModelProvider.Factory)
                CheckinsOverviewScreen(
                    onNavigateToDailyCheckin = { navController.navigate(Routes.DAILY_CHECKIN) },
                    viewModel = checkinsViewModel
                )
            }

            composable(Routes.DAILY_CHECKIN) {
                val checkinsViewModel: CheckinsViewModel = viewModel(factory = ViewModelProvider.Factory)
                DailyCheckinScreen(
                    onBack = { navController.popBackStack() },
                    onReview = { navController.navigate(Routes.CHECKIN) },
                    viewModel = checkinsViewModel
                )
            }

            composable(Routes.FORGOT_PW) {
                PlaceholderScreen("Forgot Password Screen")
            }

            composable(Routes.TRANSCRIPTION) {
                val transcriptionViewModel: TranscriptionViewModel = viewModel(factory = ViewModelProvider.Factory)
                TranscriptionScreen(
                    viewModel = transcriptionViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        (BackgroundCream),
                        PrimeraLilac.copy(alpha = 0.45f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = name,
                style = MaterialTheme.typography.displayMedium,
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
    state: AuthUiState,
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
