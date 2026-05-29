package com.example.primera.feature.smartwatchconnection.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.example.primera.R
import com.example.primera.core.theme.*
import com.example.primera.ui.components.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLocale

@Composable
fun SmartwatchScreen(
    uiState: SmartwatchUiState,
    onRequestPermissions: () -> Unit,
    onReadAndSave: () -> Unit,
    onBackToSources: () -> Unit
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
                    onConnectAndSync = onReadAndSave
                )
            }
        }
    }
}

@Composable
private fun ConnectDeviceContent(
    uiState: SmartwatchUiState,
    onRequestPermissions: () -> Unit,
    onConnectAndSync: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
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

        Spacer(Modifier.height(56.dp))

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

        Spacer(Modifier.height(48.dp))

        Text(
            "Connect your health data source",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            lineHeight = 32.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )

        Spacer(Modifier.height(48.dp))

        // Large rounded surface at the bottom
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = ArchFill.copy(alpha = 0.5f),
            shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
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
                    PrimeraGradientButton(
                        text = "Review and accept permissions",
                        onClick = onRequestPermissions,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(24.dp))

                PrimeraGradientButton(
                    text = "Connect and Sync",
                    onClick = onConnectAndSync,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SourceCard(
    title: String,
    subtitle: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, DashboardCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundCream),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary
            )
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
                        trendText = "▲ 2% vs last wk", // Hardcoded placeholder as in design
                        trendColor = TrendGreen,
                        modifier = Modifier.weight(1f)
                    )
                    HealthStatCard(
                        title = "Steps",
                        value = "%,d".format(health.steps),
                        unit = "steps",
                        icon = painterResource(R.drawable.steps),
                        iconBgColor = StepsBg,
                        trendText = "Goal: 8,000",
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
                        trendText = "Good quality",
                        trendColor = TrendGreen,
                        modifier = Modifier.weight(1f)
                    )
                    HealthStatCard(
                        title = "SpO₂",
                        value = health.spO2?.toInt()?.toString() ?: "--",
                        unit = "%",
                        icon = painterResource(R.drawable.sp02),
                        iconBgColor = Color(0xFFFFEBEE),
                        trendText = "Good quality",
                        trendColor = TrendGreen,
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
                text = "Sync Now",
                onClick = onSyncNow,
                isLoading = uiState.isLoading
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
            onBackToSources = {}
        )
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
            onBackToSources = {}
        )
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
            onBackToSources = {}
        )
    }
}
