package com.example.primera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.core.theme.PrimeraTheme
import com.example.primera.core.theme.PrimeraViolet
import com.example.primera.core.theme.SurfaceWhite
import com.example.primera.core.theme.TextPrimary
import com.example.primera.core.theme.TextSecondary

@Composable
fun GoalIcon(
    icon: String,
    modifier: Modifier = Modifier,
    tint: Color = PrimeraViolet
) {
    if (icon.startsWith("R.drawable.")) {
        val context = LocalContext.current
        val resName = icon.substringAfter("R.drawable.")
        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
        if (resId != 0) {
            Icon(
                painter = painterResource(id = resId),
                contentDescription = null,
                modifier = modifier,
                tint = tint
            )
        } else {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = modifier,
                tint = tint
            )
        }
    } else {
        Text(
            text = icon,
            fontSize = 20.sp,
            modifier = modifier
        )
    }
}

@Composable
fun InsightTipCard(
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = SurfaceWhite
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun GoalItemCard(
    icon: String,
    title: String,
    currentValue: String,
    targetValue: String,
    progress: Float,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF64B5F6),
    onClick: () -> Unit = {},
    onDelete: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            GoalIcon(icon = icon, tint = accentColor, modifier = Modifier.size(20.dp))
        }
        
        Spacer(Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$currentValue / $targetValue",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp).padding(start = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Goal",
                                tint = Color.Red.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            GoalProgressBar(
                progress = progress,
                color = accentColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CardsPreview() {
    PrimeraTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            InsightTipCard(
                text = "This week, you've completed 5/24 weekly goals. Try focusing on one small goal each day this week.",
                backgroundColor = Color(0xFFFFCC80),
                contentColor = Color(0xFF5D4037)
            )
            
            GoalItemCard(
                icon = "💧",
                title = "Hydration",
                currentValue = "1.8",
                targetValue = "2.5 L",
                progress = 0.72f,
                accentColor = Color(0xFF64B5F6)
            )
        }
    }
}
