package com.example.primera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.core.theme.CalendarDayBg
import com.example.primera.core.theme.CalendarDaySelected
import com.example.primera.core.theme.DaysLeftBg
import com.example.primera.core.theme.NauseaBg
import com.example.primera.core.theme.NauseaText
import com.example.primera.core.theme.PrimeraTheme
import com.example.primera.core.theme.PrimeraViolet
import com.example.primera.core.theme.TextPrimary
import com.example.primera.core.theme.TextSecondary

@Composable
fun SymptomChip(
    label: String,
    backgroundColor: Color,
    indicatorColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun DayChip(
    initial: String,
    date: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(40.dp)
            .minimumInteractiveComponentSize()
    ) {
        Text(
            text = initial,
            fontSize = 11.sp,
            color = if (isSelected) CalendarDaySelected else TextSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
        Spacer(Modifier.height(4.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CalendarDayBg)
                .then(
                    if (isSelected) {
                        Modifier.border(1.5.dp, CalendarDaySelected, CircleShape)
                    } else Modifier
                )
        ) {
            Text(
                text = date.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) CalendarDaySelected else TextPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TagsAndChipsPreview() {
    PrimeraTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SymptomChip(
                label = "Mild Nausea",
                backgroundColor = NauseaBg,
                indicatorColor = NauseaText
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DayChip(initial = "M", date = 23, isSelected = true)
                DayChip(initial = "T", date = 24, isSelected = false)
            }
        }
    }
}


