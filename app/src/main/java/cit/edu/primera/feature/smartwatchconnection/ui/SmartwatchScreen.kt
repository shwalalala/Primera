package cit.edu.primera.feature.smartwatchconnection.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.primera.R
import cit.edu.primera.core.theme.*
import cit.edu.primera.ui.components.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLocale

@Composable
fun SmartwatchScreen(
    uiState: SmartwatchUiState,
    onRequestPermissions: () -> Unit,
    onReadAndSave: () -> Unit,
    onBackToSources: () -> Unit,
    onOpenHealthConnect: () -> Unit,
) {
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
        AnimatedContent(
            targetState = uiState.isDataVisible,
            label = "ScreenTransition"
        ) { isVisible ->
            if (isVisible) {
                HealthDataContent(
                    uiState = uiState,
                    onBack = onBackToSources,
                    onSyncNow = onReadAndSave
                )
            } else {
                ConnectDeviceContent(
                    uiState = uiState,
                    onRequestPermissions = onRequestPermissions,
                    onConnectAndSync = onReadAndSave,
                    onOpenHealthConnect = onOpenHealthConnect
                )
            }
        }
    }
}

@Composable
private fun ConnectDeviceContent(
    uiState: SmartwatchUiState,
    onRequestPermissions: () -> Unit,
    onConnectAndSync: () -> Unit,
    onOpenHealthConnect: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterStart),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                IconButton(onClick = { /* Handle back if needed */ }) {
                    Icon(
                        Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        modifier = Modifier.size(16.dp),
                        tint = TextPrimary
                    )
                }
            }

            Text(
                "Connect a Device",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(Modifier.height(32.dp))

        // Connection Illustration
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.watch),
                contentDescription = "Watch",
                modifier = Modifier.size(72.dp)
            )

            Box(
                modifier = Modifier
                    .weight(3f)
                    .padding(horizontal = 12.dp)
                    .height(2.5.dp)
                    .clip(CircleShape)
                    .background(PrimeraViolet.copy(alpha = 0.6f))
            )

            Image(
                painter = painterResource(R.drawable.phone),
                contentDescription = "Phone",
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "Connect your health data source",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            lineHeight = 30.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )

        Spacer(Modifier.height(32.dp))

        // Large rounded surface at the bottom
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = ArchFill.copy(alpha = 0.5f),
            shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                if (uiState.message.isNotBlank()) {
                    val isError = uiState.message.contains("Error", ignoreCase = true) || 
                                 uiState.message.contains("not available", ignoreCase = true) ||
                                 uiState.message.contains("not support", ignoreCase = true)
                    Text(
                        text = uiState.message,
                        color = if (isError) ErrorRed else TextPrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                if (uiState.hasPermissions) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .background(TrendGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TrendGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Permissions accepted",
                            color = TrendGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    if (uiState.isPackageInstalled) {
                        PrimeraGradientButton(
                            text = "Open Health Connect",
                            onClick = onOpenHealthConnect,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    // Button to open common watch apps
                    SourceAppLauncher(modifier = Modifier.fillMaxWidth())

                    Spacer(Modifier.height(16.dp))

                    PrimeraGradientButton(
                        text = "Review and accept permissions",
                        onClick = onRequestPermissions,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(24.dp))

                PrimeraGradientButton(
                    text = if (uiState.isOnline) "Connect and Sync" else "Offline",
                    onClick = onConnectAndSync,
                    isLoading = uiState.isLoading,
                    enabled = uiState.isOnline && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SourceAppLauncher(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val watchApps = mapOf(
        "OHealth" to "com.heytap.health.international",
        "Huawei Health" to "com.huawei.health",
        "Samsung Health" to "com.sec.android.app.shealth",
        "Zepp / Amazfit" to "com.huami.watch.hmwatchManager",
        "Fitbit" to "com.fitbit.FitbitMobile"
    )

    val installedApp = watchApps.entries.find { (_, pkg) ->
        try {
            context.packageManager.getPackageInfo(pkg, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    installedApp?.let { (name, pkg) ->
        OutlinedButton(
            onClick = {
                context.packageManager.getLaunchIntentForPackage(pkg)?.let { intent ->
                    context.startActivity(intent)
                }
            },
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, PrimeraViolet),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimeraViolet)
        ) {
            Text("Open $name to Refresh Data", fontSize = 14.sp)
        }
    }
}

@Composable
private fun HealthDataContent(
    uiState: SmartwatchUiState,
    onBack: () -> Unit,
    onSyncNow: () -> Unit
) {
    val scrollState = rememberScrollState()
    val sdf = SimpleDateFormat("h:mm a", LocalLocale.current.platformLocale)
    val lastSyncedStr = uiState.smartwatchHealth?.syncedAt?.let { "Last Synced: ${sdf.format(Date(it))}" } ?: "Last Synced: Never"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
    ) {
        if (!uiState.isOnline) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = "You're offline. Sync is unavailable.",
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.weight(0.5f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Health Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    lastSyncedStr,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        // Stats Grid
        uiState.smartwatchHealth?.let { health ->
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HealthStatCard(
                        title = "Heart Rate",
                        value = health.currentHeartRate?.toString() ?: "--",
                        unit = "bpm",
                        icon = painterResource(R.drawable.heart),
                        iconBgColor = HeartRateBg,
                        trendText = uiState.hrTrendText,
                        trendColor = Color(uiState.hrTrendColor),
                        modifier = Modifier.weight(1f)
                    )
                    HealthStatCard(
                        title = "Steps",
                        value = "%,d".format(health.steps),
                        unit = "steps",
                        icon = painterResource(R.drawable.steps),
                        iconBgColor = StepsBg,
                        trendText = uiState.stepsTrendText,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HealthStatCard(
                        title = "Sleep",
                        value = "${health.sleepMinutes / 60}h ${health.sleepMinutes % 60}m",
                        unit = "",
                        icon = painterResource(R.drawable.sleep),
                        iconBgColor = SleepBg,
                        trendText = uiState.sleepTrendText,
                        trendColor = Color(uiState.sleepTrendColor),
                        modifier = Modifier.weight(1f)
                    )
                    HealthStatCard(
                        title = "SpO₂",
                        value = health.spO2?.toInt()?.toString() ?: "--",
                        unit = "%",
                        icon = painterResource(R.drawable.sp02),
                        iconBgColor = Color(0xFFFFEBEE),
                        trendText = uiState.spO2TrendText,
                        trendColor = Color(uiState.spO2TrendColor),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Trends
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(PrimeraLilac.copy(alpha = 0.1f))
                .padding(24.dp)
        ) {
            Text(
                "Health data trend",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(16.dp))

            ChartContainer(
                title = { Text("Bpm", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                dateRange = "March 10 - March 16, 2024",
                onPrevious = {},
                onNext = {}
            ) {
                SimpleBarChart(
                    data = uiState.bpmHistory.ifEmpty { listOf(0f) },
                    labels = uiState.historyLabels,
                    barColor = TrendGreen,
                    highlightedIndex = uiState.bpmHistory.size - 1
                )
            }

            Spacer(Modifier.height(16.dp))

            ChartContainer(
                title = { Text("Sleep", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                dateRange = "March 10 - March 16, 2024",
                onPrevious = {},
                onNext = {}
            ) {
                SimpleBarChart(
                    data = uiState.sleepHistory.ifEmpty { listOf(0f) },
                    labels = uiState.historyLabels,
                    barColor = TrendGreen,
                    highlightedIndex = uiState.sleepHistory.size - 1
                )
            }

            Spacer(Modifier.height(24.dp))

            PrimeraGradientButton(
                text = if (uiState.isOnline) "Sync Now" else "Offline",
                onClick = onSyncNow,
                isLoading = uiState.isLoading,
                enabled = uiState.isOnline && !uiState.isLoading
            )
            
            Spacer(Modifier.height(80.dp)) // Extra space for bottom nav
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SmartwatchScreenPreview() {
    PrimeraTheme {
        SmartwatchScreen(
            uiState = SmartwatchUiState(
                isDataVisible = false,
                isLoading = false,
                hasPermissions = false
            ),
            onRequestPermissions = {},
            onReadAndSave = {},
            onBackToSources = {},
        ) { }
    }
}

@Preview(showBackground = true, name = "Permissions Accepted")
@Composable
private fun SmartwatchScreenPermissionsAcceptedPreview() {
    PrimeraTheme {
        SmartwatchScreen(
            uiState = SmartwatchUiState(
                isDataVisible = false,
                isLoading = false,
                hasPermissions = true
            ),
            onRequestPermissions = {},
            onReadAndSave = {},
            onBackToSources = {},
        ) { }
    }
}

@Preview(showBackground = true)
@Composable
private fun SmartwatchScreenDataPreview() {
    PrimeraTheme {
        SmartwatchScreen(
            uiState = SmartwatchUiState(
                isDataVisible = true,
                isLoading = false
            ),
            onRequestPermissions = {},
            onReadAndSave = {},
            onBackToSources = {},
        ) { }
    }
}
