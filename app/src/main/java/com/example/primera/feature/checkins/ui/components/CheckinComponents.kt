package com.example.primera.feature.checkins.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.core.theme.*

@Composable
fun CheckinItemChip(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) PrimeraViolet.copy(alpha = 0.2f) else SurfaceWhite)
            .border(
                width = 1.dp,
                color = if (isSelected) PrimeraViolet else DashboardCardBorder,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(BackgroundCream),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 14.sp)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PrimeraViolet else TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckinItemChipPreview() {
    PrimeraTheme {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CheckinItemChip(
                label = "Nausea",
                emoji = "🤢",
                isSelected = true,
                onClick = {}
            )
            CheckinItemChip(
                label = "Happy",
                emoji = "😁",
                isSelected = false,
                onClick = {}
            )
        }
    }
}
