package com.example.primera.frontend.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.frontend.common.theme.*

@Composable
fun DashboardGreeting(
    userName: String,
    timeOfDay: String,
    trimesterText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello, $userName",
            fontSize = 13.sp,
            color = TextSecondary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = timeOfDay.ifBlank { "Good morning" },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = trimesterText,
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingHeaderPreview() {
    PrimeraTheme {
        DashboardGreeting(
            userName = "Sarah",
            timeOfDay = "Good morning",
            trimesterText = "You're in your third trimester"
        )
    }
}
