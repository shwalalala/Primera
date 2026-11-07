package cit.edu.primera.feature.dashboard.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cit.edu.primera.core.di.ViewModelProvider
import cit.edu.primera.core.navigation.Routes
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.ErrorRed
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.ui.components.AppSidebar
import cit.edu.primera.ui.components.FullScreenLoadingOverlay
import cit.edu.primera.ui.components.OfflineBanner
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogClick: (DashboardLogUiItem) -> Unit,
    onViewAllLogs: () -> Unit,
    onAddLog: () -> Unit,
    onInputManually: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onPhotoUriSelected(it.toString()) }
    }

    when (val state = uiState) {
        is DashboardUiState.Loading -> FullScreenLoadingOverlay()
        is DashboardUiState.Error -> ErrorContent(state.message)
        is DashboardUiState.Success -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                scrimColor = Color.Black.copy(alpha = 0.6f),
                drawerContent = {
                    AppSidebar(
                        userName = state.data.userName,
                        email = state.data.email,
                        photoUri = state.data.photoUri,
                        currentRoute = Routes.DASHBOARD,
                        onNavigate = { route ->
                            coroutineScope.launch { drawerState.close() }
                            onNavigate(route)
                        },
                        onClose = {
                            coroutineScope.launch { drawerState.close() }
                        },
                        onEditProfilePicture = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onNotificationClick = {
                            coroutineScope.launch { drawerState.close() }
                        },
                        onLogoutClick = {
                            coroutineScope.launch { drawerState.close() }
                            onLogout()
                        }
                    )
                }
            ) {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    BackgroundCream,
                                    PrimeraLilac.copy(alpha = 0.45f)
                                )
                            )
                        )
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        DashboardTopBar(
                            onOpenDrawer = {
                                coroutineScope.launch { drawerState.open() }
                            },
                            onPrintQrClick = { viewModel.onExportSummaryPdf(context) }
                        )

                        if (!isOnline) {
                            OfflineBanner()
                        }

                        DashboardContent(
                            state = state.data,
                            onInputManually = onInputManually,
                            onSyncWatch = { viewModel.onSyncWatch() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    state: DashboardUiModel,
    onInputManually: () -> Unit,
    onSyncWatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        WeekCalendarRow(state.weekDays)
        Spacer(Modifier.height(8.dp))
        
        // Safety warning banner at the very top of Dashboard
        state.inaccuracyWarning?.let { warning ->
            UrgentWarningBanner(message = warning)
            Spacer(Modifier.height(8.dp))
        }

        DashboardGreeting(
            userName = state.userName,
            timeOfDay = state.timeOfDay,
            trimesterText = "You're in your ${state.trimester}",
            inaccuracyWarning = null
        )
        Spacer(Modifier.height(4.dp))
        BabyRingCard(
            weekNumber = state.weekNumber,
            dayNumber = state.dayNumber,
            daysLeft = state.daysLeft,
            babySize = state.babySize,
            babyEmoji = state.babyEmoji,
            babyIllustration = state.babyIllustration
        )
        Spacer(Modifier.height(20.dp))
        StatsGrid(
            state = state,
            onInputManually = onInputManually,
            onSyncWatch = onSyncWatch
        )
        Spacer(Modifier.height(24.dp))
        cit.edu.primera.ui.components.MonitoringDisclaimerFooter()
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun ErrorContent(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = ErrorRed)
    }
}
@Preview(name = "Compact Phone", device = Devices.PHONE)
@Preview(name = "Small Phone", widthDp = 360, heightDp = 640)
@Preview(name = "Large Phone", widthDp = 480, heightDp = 1560)
@Composable
private fun DashboardScreenPreview() {
    PrimeraTheme {
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
                )
        ) {
            DashboardContent(
                state = DashboardUiModel(
                    userName = "Sarah",
                    timeOfDay = "Good morning",
                    trimester = "Third Trimester",
                    weekNumber = 28,
                    dayNumber = 3,
                    daysLeft = 88,
                    babySize = "Large eggplant",
                    babyEmoji = "🍆",
                    heartRateBpm = 0,
                    heartRateTrendingUp = false,
                    heartRateVsLastWeek = 0,
                    steps = 0,
                    stepsGoal = 8000,
                    sleepHours = 7,
                    sleepMinutes = 45,
                    sleepQuality = "Good quality",
                    spO2 = 98,
                    isWatchSynced = true,
                    isGestationalAgeTentative = false,
                    inaccuracyWarning = null,
                    recentLogs = emptyList(),
                    weekDays = listOf(
                        DashboardWeekDayItem("S", 22, false, "Oct"),
                        DashboardWeekDayItem("M", 23, true, "Oct"),
                        DashboardWeekDayItem("T", 24, false, "Oct"),
                        DashboardWeekDayItem("W", 25, false, "Oct"),
                        DashboardWeekDayItem("T", 26, false, "Oct"),
                        DashboardWeekDayItem("F", 27, false, "Oct"),
                        DashboardWeekDayItem("S", 28, false, "Oct")
                    )
                ),
                onInputManually = {},
                onSyncWatch = {}
            )
        }
    }
}