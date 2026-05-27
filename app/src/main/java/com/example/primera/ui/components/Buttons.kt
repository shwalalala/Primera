package com.example.primera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.primera.core.theme.PrimeraLogoStart
import com.example.primera.core.theme.PrimeraTheme
import com.example.primera.core.theme.PrimeraViolet
import com.example.primera.core.theme.SurfaceWhite

@Composable
fun PrimeraGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val gradient = Brush.linearGradient(
        colors = listOf(PrimeraLogoStart, PrimeraViolet),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, 0f)
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 54.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = PrimeraViolet.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                if (enabled) gradient else Brush.linearGradient(listOf(Color.Gray, Color.Gray))
            )
            .clickable(enabled = enabled && !isLoading, onClick = onClick)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = SurfaceWhite,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = SurfaceWhite
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonsPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PrimeraGradientButton(text = "Log In", onClick = {})
        }
    }
}

