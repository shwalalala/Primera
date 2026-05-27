package com.example.primera.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.core.theme.*

@Composable
fun TrimesterBadge(
    weekNumber: Int,
    dayNumber: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Week $weekNumber, Day $dayNumber",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatusBadgesPreview() {
    PrimeraTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            TrimesterBadge(weekNumber = 28, dayNumber = 3)
        }
    }
}
