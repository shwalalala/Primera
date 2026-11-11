
package cit.edu.primera.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cit.edu.primera.core.di.ViewModelProvider
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.feature.auth.ui.AuthEffect
import cit.edu.primera.feature.auth.ui.AuthTab
import cit.edu.primera.feature.auth.ui.AuthViewModel
import cit.edu.primera.feature.auth.ui.ForgotPasswordScreen
import cit.edu.primera.feature.auth.ui.LoginScreen
import cit.edu.primera.feature.auth.ui.RegisterScreen
import cit.edu.primera.feature.auth.ui.rememberFacebookLoginLauncher
import cit.edu.primera.feature.auth.ui.rememberGoogleLoginLauncher
import cit.edu.primera.feature.checkins.ui.CheckinPreviewScreen
import cit.edu.primera.feature.checkins.ui.CheckinsOverviewScreen
import cit.edu.primera.feature.checkins.ui.CheckinsViewModel
import cit.edu.primera.feature.checkins.ui.DailyCheckinScreen
import cit.edu.primera.feature.checkins.ui.PrenatalCheckupScreen
import cit.edu.primera.feature.dashboard.ui.DashboardScreen
import cit.edu.primera.feature.dashboard.ui.DashboardUiState
import cit.edu.primera.feature.dashboard.ui.DashboardViewModel
import cit.edu.primera.feature.dashboard.ui.NewsEducationScreen
import cit.edu.primera.feature.insights.ui.InsightsScreen
import cit.edu.primera.feature.onboarding.ui.OnboardingHostScreen
import cit.edu.primera.feature.profile.ui.ProfileScreen
import cit.edu.primera.feature.profile.ui.SettingsScreen
import cit.edu.primera.feature.smartwatchconnection.ui.SmartwatchRoute
import cit.edu.primera.feature.splash.ui.SplashScreen
import cit.edu.primera.feature.transcription.ui.TranscriptionScreen
import cit.edu.primera.feature.transcription.ui.TranscriptionViewModel
import cit.edu.primera.feature.welcome.ui.WelcomeScreen
import cit.edu.primera.ui.components.AppSidebar
import cit.edu.primera.ui.components.CoachmarkTooltip
import kotlinx.coroutines.launch

private const val PRENATAL_CHECKUP_ROUTE = "prenatal_checkup"
private const val PRENATAL_CHECKUP_PATTERN =
    "$PRENATAL_CHECKUP_ROUTE?recordId={recordId}"

private val bottomNavRoutes = setOf(
    Routes.DASHBOARD,
    Routes.DEVICE,
    Routes.INSIGHT,
    Routes.CHECKIN,
    Routes.DAILY_CHECKIN,
    Routes.CHECKIN_PREVIEW,
    PRENATAL_CHECKUP_PATTERN
)

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH,
    pendingDestinationRoute: String? = null,
    onPendingRouteHandled: () -> Unit = {}
) {
    val authViewModel: AuthViewModel =
        viewModel(factory = ViewModelProvider.Factory)

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val drawerState =
        rememberDrawerState(initialValue = DrawerValue.Closed)

    val coroutineScope = rememberCoroutineScope()

    val dashboardViewModel: DashboardViewModel =
        viewModel(factory = ViewModelProvider.Factory)

    val dashboardUiState by
    dashboardViewModel.uiState.collectAsStateWithLifecycle()

    val showMicCoachmark by
    dashboardViewModel.showMicCoachmark.collectAsStateWithLifecycle()

    val sidebarUserData =
        (dashboardUiState as? DashboardUiState.Success)?.data

    var showSummaryQrDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(pendingDestinationRoute, currentRoute) {
        if (
            !pendingDestinationRoute.isNullOrBlank() &&
            currentRoute != null &&
            currentRoute != Routes.SPLASH
        ) {
            val target = pendingDestinationRoute
            onPendingRouteHandled()

            val currentUser =
                com.google.firebase.auth.FirebaseAuth
                    .getInstance().currentUser

            if (
                currentUser != null ||
                target == Routes.LOGIN ||
                target == Routes.WELCOME
            ) {
                navController.navigate(target) {
                    launchSingleTop = true
                }
            } else {
                navController.navigate(Routes.LOGIN) {
                    launchSingleTop = true
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.NavigateToDashboard -> {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }

                is AuthEffect.NavigateToOnboarding -> {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }

                is AuthEffect.NavigateToLogin -> {
                    val target = currentRoute ?: Routes.LOGIN

                    navController.navigate(Routes.LOGIN) {
                        popUpTo(target) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }

                is AuthEffect.NavigateToRegister -> {
                    val target = currentRoute ?: Routes.REGISTER

                    navController.navigate(Routes.REGISTER) {
                        popUpTo(target) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }

                is AuthEffect.NavigateToForgotPassword -> {
                    navController.navigate(Routes.FORGOT_PW)
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute in bottomNavRoutes,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        drawerContent = {
            val context =
                androidx.compose.ui.platform.LocalContext.current

            val currentUserId =
                com.google.firebase.auth.FirebaseAuth
                    .getInstance().currentUser?.uid ?: ""

            val appContainer =
                (context.applicationContext as?
                        cit.edu.primera.PrimeraApplication)?.container

            val localPhoto =
                appContainer?.preferenceRepository
                    ?.getProfilePictureUri(currentUserId)

            val unifiedPhotoUri =
                sidebarUserData?.photoUri
                    ?.takeIf { it.isNotBlank() } ?: localPhoto

            AppSidebar(
                userName = sidebarUserData?.userName ?: "",
                email = sidebarUserData?.email ?: "",
                photoUri = unifiedPhotoUri,
                currentRoute = currentRoute ?: Routes.DASHBOARD,
                onNavigate = { route ->
                    coroutineScope.launch {
                        drawerState.close()
                    }

                    navController.navigate(route) {
                        if (route != Routes.DASHBOARD) {
                            launchSingleTop = true
                        }
                    }
                },
                onClose = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                },
                onEditProfilePicture = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                    navController.navigate(Routes.PROFILE)
                },
                onNotificationClick = {
                    coroutineScope.launch {
                        drawerState.close()
                    }

                    navController.navigate(Routes.NOTIFICATIONS) {
                        launchSingleTop = true
                    }
                },
                onLogoutClick = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                    authViewModel.logout()
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (currentRoute in bottomNavRoutes) {
                        PrimeraBottomNav(
                            currentRoute =
                                currentRoute ?: Routes.DASHBOARD,
                            onDestinationSelected = { dest ->
                                if (
                                    dest == NavDestination.CHECKIN &&
                                    (
                                            currentRoute == Routes.DAILY_CHECKIN ||
                                                    currentRoute == Routes.CHECKIN_PREVIEW ||
                                                    currentRoute == Routes.CHECKIN ||
                                                    currentRoute == PRENATAL_CHECKUP_PATTERN
                                            )
                                ) {
                                    navController.navigate(Routes.CHECKIN) {
                                        popUpTo(Routes.CHECKIN) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.navigate(dest.route) {
                                        popUpTo(Routes.DASHBOARD) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            onFabClicked = {
                                if (showMicCoachmark) {
                                    dashboardViewModel
                                        .dismissMicCoachmark()
                                }

                                navController.navigate(
                                    Routes.TRANSCRIPTION
                                )
                            }
                        )
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = if (currentRoute in bottomNavRoutes) {
                            innerPadding.calculateBottomPadding()
                        } else {
                            0.dp
                        }
                    )
                ) {
                    composable(Routes.SPLASH) {
                        SplashScreen(
                            onTimeout = {
                                navController.navigate(Routes.WELCOME) {
                                    popUpTo(Routes.SPLASH) {
                                        inclusive = true
                                    }
                                }
                            },
                            onNavigateToAuth = {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(Routes.SPLASH) {
                                        inclusive = true
                                    }
                                }
                            },
                            onNavigateToDashboard = {
                                if (!pendingDestinationRoute.isNullOrBlank()) {
                                    val target = pendingDestinationRoute
                                    onPendingRouteHandled()

                                    navController.navigate(target) {
                                        popUpTo(Routes.SPLASH) {
                                            inclusive = true
                                        }
                                    }
                                } else {
                                    navController.navigate(Routes.DASHBOARD) {
                                        popUpTo(Routes.SPLASH) {
                                            inclusive = true
                                        }
                                    }
                                }
                            },
                            onNavigateToOnboarding = {
                                navController.navigate(Routes.ONBOARDING) {
                                    popUpTo(Routes.SPLASH) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }

                    composable(Routes.WELCOME) {
                        WelcomeScreen(
                            onNavigateToRegister = {
                                navController.navigate(Routes.REGISTER) {
                                    popUpTo(Routes.WELCOME) {
                                        inclusive = true
                                    }
                                }
                            },
                            onNavigateToLogin = {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(Routes.WELCOME) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }

                    composable(Routes.LOGIN) {
                        val state by
                        authViewModel.uiState.collectAsStateWithLifecycle()

                        val launchFacebook =
                            rememberFacebookLoginLauncher(
                                onAuthComplete =
                                    authViewModel::onFacebookLoginSuccess,
                                onAuthError =
                                    authViewModel::onFacebookLoginError
                            )

                        val launchGoogle =
                            rememberGoogleLoginLauncher(
                                onAuthComplete =
                                    authViewModel::onGoogleLoginSuccess,
                                onAuthError =
                                    authViewModel::onGoogleLoginError
                            )

                        LoginScreen(
                            state = state,
                            onEmailChange = authViewModel::onEmailChange,
                            onPasswordChange = authViewModel::onPasswordChange,
                            onRememberMeToggle =
                                authViewModel::onRememberMeToggle,
                            onForgotPasswordClicked =
                                authViewModel::onForgotPasswordClicked,
                            onLoginClicked =
                                authViewModel::onLoginClicked,
                            onTabSelected = { tab ->
                                if (tab == AuthTab.SIGNUP) {
                                    authViewModel
                                        .onNavigateToRegisterClicked()
                                }
                            },
                            onSocialLoginClicked =
                                authViewModel::onSocialLoginClicked,
                            onFacebookLoginClicked = {
                                launchFacebook()
                            },
                            onGoogleLoginClicked = {
                                launchGoogle()
                            },
                            onNavigateToRegister =
                                authViewModel::onNavigateToRegisterClicked,
                            onDismissAccountLinkingDialog =
                                authViewModel::onDismissAccountLinkingDialog
                        )
                    }

                    composable(Routes.REGISTER) {
                        val state by
                        authViewModel.uiState.collectAsStateWithLifecycle()

                        val launchFacebook =
                            rememberFacebookLoginLauncher(
                                onAuthComplete =
                                    authViewModel::onFacebookLoginSuccess,
                                onAuthError =
                                    authViewModel::onFacebookLoginError
                            )

                        val launchGoogle =
                            rememberGoogleLoginLauncher(
                                onAuthComplete =
                                    authViewModel::onGoogleLoginSuccess,
                                onAuthError =
                                    authViewModel::onGoogleLoginError
                            )

                        RegisterScreen(
                            state = state,
                            onUsernameChange =
                                authViewModel::onUsernameChange,
                            onEmailChange =
                                authViewModel::onEmailChange,
                            onPasswordChange =
                                authViewModel::onPasswordChange,
                            onConfirmPasswordChange =
                                authViewModel::onConfirmPasswordChange,
                            onAgreedToTermsToggle =
                                authViewModel::onAgreedToTermsToggle,
                            onTermsClicked =
                                authViewModel::onTermsClicked,
                            onDismissTermsDialog =
                                authViewModel::onDismissTermsDialog,
                            onSignUpClicked =
                                authViewModel::onSignUpClicked,
                            onTabSelected = { tab ->
                                if (tab == AuthTab.LOGIN) {
                                    authViewModel
                                        .onNavigateToLoginClicked()
                                }
                            },
                            onSocialLoginClicked =
                                authViewModel::onSocialLoginClicked,
                            onFacebookLoginClicked = {
                                launchFacebook()
                            },
                            onGoogleLoginClicked = {
                                launchGoogle()
                            },
                            onNavigateToLogin =
                                authViewModel::onNavigateToLoginClicked,
                            onDismissAccountLinkingDialog =
                                authViewModel::onDismissAccountLinkingDialog
                        )
                    }

                    composable(Routes.ONBOARDING) {
                        OnboardingHostScreen(
                            onOnboardingComplete = {
                                navController.navigate(Routes.DASHBOARD) {
                                    popUpTo(Routes.ONBOARDING) {
                                        inclusive = true
                                    }
                                }
                            },
                            onBack = {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(Routes.ONBOARDING) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }

                    composable(Routes.DASHBOARD) {
                        val checkinsViewModel: CheckinsViewModel =
                            viewModel(
                                factory = ViewModelProvider.Factory
                            )

                        DashboardScreen(
                            onOpenDrawer = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            },
                            onLogout = {
                                authViewModel.logout()
                            },
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    if (route != Routes.DASHBOARD) {
                                        launchSingleTop = true
                                    }
                                }
                            },
                            onLogClick = { log ->
                                checkinsViewModel
                                    .loadCheckinForEdit(log)

                                navController.navigate(
                                    Routes.DAILY_CHECKIN
                                )
                            },
                            onViewAllLogs = {
                                navController.navigate(Routes.CHECKIN)
                            },
                            onAddLog = {
                                checkinsViewModel.prepareNewCheckin()

                                navController.navigate(
                                    Routes.DAILY_CHECKIN
                                )
                            },
                            onInputManually = {
                                checkinsViewModel.prepareNewCheckin()

                                navController.navigate(
                                    Routes.DAILY_CHECKIN
                                )
                            },
                            onProfileClick = {
                                navController.navigate(Routes.PROFILE)
                            },
                            viewModel = dashboardViewModel
                        )
                    }

                    composable(Routes.DEVICE) {
                        SmartwatchRoute(
                            onOpenDrawer = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            },
                            onPrintQrClick = {
                                showSummaryQrDialog = true
                            }
                        )
                    }

                    composable(Routes.INSIGHT) {
                        InsightsScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onOpenDrawer = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            }
                        )
                    }

                    composable(Routes.CHECKIN) {
                        val parentEntry = remember(it) {
                            try {
                                navController.getBackStackEntry(
                                    Routes.CHECKIN
                                )
                            } catch (_: Exception) {
                                it
                            }
                        }

                        val checkinsViewModel: CheckinsViewModel =
                            viewModel(
                                parentEntry,
                                factory = ViewModelProvider.Factory
                            )

                        CheckinsOverviewScreen(
                            onNavigateToDailyCheckin = {
                                checkinsViewModel.prepareNewCheckin()

                                navController.navigate(
                                    Routes.DAILY_CHECKIN
                                )
                            },
                            onLogClick = { log ->
                                checkinsViewModel
                                    .loadCheckinForEdit(log)

                                navController.navigate(
                                    Routes.DAILY_CHECKIN
                                )
                            },
                            onOpenDrawer = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            },
                            onPrintQrClick = {
                                showSummaryQrDialog = true
                            },
                            onNavigateToPrenatalCheckup = {
                                navController.navigate(PRENATAL_CHECKUP_ROUTE)
                            },
                            onPrenatalRecordClick = { recordId ->
                                navController.navigate(
                                    "$PRENATAL_CHECKUP_ROUTE?recordId=${android.net.Uri.encode(recordId)}"
                                )
                            },
                            viewModel = checkinsViewModel
                        )
                    }

                    // OUR ADDITION:
                    // Prenatal calendar and direct record opening.
                    composable(
                        route = PRENATAL_CHECKUP_PATTERN,
                        arguments = listOf(
                            navArgument("recordId") {
                                type = NavType.StringType
                                defaultValue = ""
                            }
                        )
                    ) { entry ->
                        val selectedRecordId =
                            entry.arguments
                                ?.getString("recordId")
                                ?.takeIf { it.isNotBlank() }

                        PrenatalCheckupScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            selectedRecordId = selectedRecordId
                        )
                    }

                    composable(Routes.DAILY_CHECKIN) {
                        val parentEntry = remember(it) {
                            try {
                                navController.getBackStackEntry(
                                    Routes.CHECKIN
                                )
                            } catch (_: Exception) {
                                it
                            }
                        }

                        val checkinsViewModel: CheckinsViewModel =
                            viewModel(
                                parentEntry,
                                factory = ViewModelProvider.Factory
                            )

                        DailyCheckinScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onReview = {
                                navController.navigate(
                                    Routes.CHECKIN_PREVIEW
                                )
                            },
                            viewModel = checkinsViewModel
                        )
                    }

                    composable(Routes.CHECKIN_PREVIEW) {
                        val parentEntry = remember(it) {
                            try {
                                navController.getBackStackEntry(
                                    Routes.CHECKIN
                                )
                            } catch (_: Exception) {
                                it
                            }
                        }

                        val checkinsViewModel: CheckinsViewModel =
                            viewModel(
                                parentEntry,
                                factory = ViewModelProvider.Factory
                            )

                        CheckinPreviewScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onNavigateToOverview = {
                                navController.navigate(Routes.CHECKIN) {
                                    popUpTo(Routes.CHECKIN) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            },
                            viewModel = checkinsViewModel
                        )
                    }

                    composable(Routes.FORGOT_PW) {
                        val state by
                        authViewModel.uiState.collectAsStateWithLifecycle()

                        ForgotPasswordScreen(
                            state = state,
                            onEmailChange =
                                authViewModel::onEmailChange,
                            onResetPasswordClicked =
                                authViewModel::onResetPasswordClicked,
                            onBackToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(Routes.TRANSCRIPTION) {
                        val transcriptionViewModel: TranscriptionViewModel =
                            viewModel(
                                factory = ViewModelProvider.Factory
                            )

                        val parentEntry = remember(it) {
                            navController.getBackStackEntry(
                                Routes.DASHBOARD
                            )
                        }

                        val checkinsViewModel: CheckinsViewModel =
                            viewModel(
                                parentEntry,
                                factory = ViewModelProvider.Factory
                            )

                        TranscriptionScreen(
                            viewModel = transcriptionViewModel,
                            onBack = {
                                navController.popBackStack()
                            },
                            onUseInCheckin = { transcribedText ->
                                checkinsViewModel.prepareNewCheckin()

                                checkinsViewModel.applyVoiceInputToCheckin(
                                    transcribedText = transcribedText
                                )

                                navController.navigate(
                                    Routes.DAILY_CHECKIN
                                )
                            }
                        )
                    }

                    composable(Routes.PROFILE) {
                        ProfileScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onNavigateToSettings = {
                                navController.navigate(Routes.SETTINGS)
                            }
                        )
                    }

                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onLogout = {
                                authViewModel.logout()
                            },
                            onOpenReminderSettings = {
                                navController.navigate(
                                    Routes.REMINDER_SETTINGS
                                )
                            }
                        )
                    }

                    composable(Routes.NOTIFICATIONS) {
                        cit.edu.primera.feature.notification.ui.NotificationsScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    launchSingleTop = true
                                }
                            },
                            onOpenSettings = {
                                navController.navigate(
                                    Routes.REMINDER_SETTINGS
                                )
                            }
                        )
                    }

                    composable(Routes.REMINDER_SETTINGS) {
                        cit.edu.primera.feature.notification.ui.ReminderSettingsScreen(
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(Routes.NEWS_EDUCATION) {
                        NewsEducationScreen(
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }

            if (
                currentRoute == Routes.DASHBOARD &&
                showMicCoachmark
            ) {
                CoachmarkTooltip(
                    visible = true,
                    onDismiss = {
                        dashboardViewModel.dismissMicCoachmark()
                    },
                    title = "Voice Check-in",
                    text = "Tap to record your daily check-in thoughts or symptoms with your voice!",
                    primaryActionLabel = "Got it",
                    skipActionLabel = "Skip",
                    iconRes = cit.edu.primera.R.drawable.mic,
                    arrowPosition =
                        cit.edu.primera.ui.components.ArrowPosition.BOTTOM,
                    cardAlignment = Alignment.BottomCenter,
                    targetAlignment = Alignment.BottomCenter,
                    cardModifier = Modifier.padding(
                        bottom = 92.dp,
                        start = 24.dp,
                        end = 24.dp
                    ),
                    targetModifier =
                        Modifier.padding(bottom = 12.dp),
                    onPrimaryAction = {
                        dashboardViewModel.dismissMicCoachmark()
                    },
                    targetContent = {
                        FabButton(
                            onClick = {
                                dashboardViewModel
                                    .dismissMicCoachmark()

                                navController.navigate(
                                    Routes.TRANSCRIPTION
                                )
                            },
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .offset(y = (-14.dp))
                        )
                    }
                )
            }

            if (showSummaryQrDialog) {
                val context =
                    androidx.compose.ui.platform.LocalContext.current

                val summaryQrContent by
                dashboardViewModel.summaryQrContent
                    .collectAsStateWithLifecycle()

                cit.edu.primera.ui.components.SummaryQrDialog(
                    onDismissRequest = {
                        showSummaryQrDialog = false
                    },
                    onSavePdf = {
                        dashboardViewModel.onSaveSummaryPdf(context)
                    },
                    onPrint = {
                        dashboardViewModel.onPrintSummaryPdf(context)
                    },
                    onShare = {
                        dashboardViewModel.onShareSummaryPdf(context)
                    },
                    onRequestPdfUrl = {
                        dashboardViewModel.getSummaryPdfUrl(context)
                    },
                    qrContent = summaryQrContent,
                    patientName = sidebarUserData?.userName
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
                        BackgroundCream,
                        PrimeraLilac.copy(alpha = 0.45f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
