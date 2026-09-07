package cit.edu.primera.feature.dashboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.primera.core.theme.ErrorRed
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary

@Composable
fun DashboardGreeting(
    userName: String,
    timeOfDay: String,
    trimesterText: String,
    modifier: Modifier = Modifier,
    inaccuracyWarning: String? = null
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
        
        if (inaccuracyWarning != null) {
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.Surface(
                color = ErrorRed.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, ErrorRed.copy(alpha = 0.5f))
            ) {
                Text(
                    text = inaccuracyWarning,
                    fontSize = 12.sp,
                    color = ErrorRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GreetingHeaderPreview() {
    PrimeraTheme {
        DashboardGreeting(
            userName = "Sarah",
            timeOfDay = "Good morning",
            trimesterText = "You're in your third trimester"
        )
    }
}

