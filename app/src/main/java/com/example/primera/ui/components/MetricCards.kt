package com.example.primera.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.R
import com.example.primera.core.theme.DashboardCardBorder
import com.example.primera.core.theme.DashboardLogBorder
import com.example.primera.core.theme.HeartRateBg
import com.example.primera.core.theme.PrimeraTheme
import com.example.primera.core.theme.PrimeraViolet
import com.example.primera.core.theme.StepsBg
import com.example.primera.core.theme.SurfaceWhite
import com.example.primera.core.theme.TextPrimary
import com.example.primera.core.theme.TextSecondary
import com.example.primera.core.theme.TrendGreen
import com.example.primera.core.theme.TrendRed

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.heightIn(min = 110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, DashboardCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) { content() }
    }
}

@Composable
fun HealthStatCard(
    title: String,
    value: String,
    unit: String,
    icon: Painter,
    iconBgColor: Color,
    modifier: Modifier = Modifier,
    trendText: String? = null,
    trendColor: Color = TextSecondary
) {
    StatCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconBgColor)
            ) {
                Image(
                    painter = icon,
                    contentDescription = title,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = unit,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
        if (trendText != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = trendText,
                fontSize = 11.sp,
                color = trendColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun InputManuallyCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .heightIn(min = 110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, DashboardCardBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "+",
                fontSize = 32.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Light
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Input Manually",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LogCard(
    category: String,
    time: String,
    description: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, DashboardLogBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = time.ifBlank { "—" },
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = description.ifBlank { "No description." },
                fontSize = 13.sp,
                color = TextPrimary,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WellnessGoalCard(
    label: String,
    emoji: String,
    current: Float,
    target: Float,
    unit: String,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0f) (current / target).coerceIn(0f, 1f) else 0f
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, DashboardLogBorder)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(barColor.copy(alpha = 0.1f))
                    ) {
                        Text(emoji, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "$current / $target $unit",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = barColor,
                trackColor = DashboardLogBorder
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MetricCardsPreview() {
    PrimeraTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HealthStatCard(
                title = "Heart Rate",
                value = "72",
                unit = "bpm",
                icon = painterResource(R.drawable.heart),
                iconBgColor = HeartRateBg,
                trendText = "▲ 5% vs last wk",
                trendColor = TrendGreen
            )
            InputManuallyCard(onClick = {})
            LogCard(
                category = "Back Pain",
                time = "8:32 AM",
                description = "Felt mild lower back pain after walking this morning.",
                accentColor = TrendRed
            )
            WellnessGoalCard(
                label = "Hydration",
                emoji = "💧",
                current = 1.8f,
                target = 2.5f,
                unit = "L",
                barColor = PrimeraViolet
            )
        }
    }
}


