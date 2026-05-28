package com.example.primera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.core.theme.MenuButtonBg
import com.example.primera.core.theme.MenuButtonBorder
import com.example.primera.core.theme.PrimeraTheme
import com.example.primera.core.theme.PrimeraViolet
import com.example.primera.core.theme.SurfaceWhite
import com.example.primera.core.theme.TextPrimary
import com.example.primera.core.theme.TextSecondary

@Composable
fun FeatureTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    selectedPeriod: String? = null,
    onPeriodSelected: ((String) -> Unit)? = null
) {
    var showPeriodMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MenuButtonBg)
                .border(1.dp, MenuButtonBorder, CircleShape)
                .minimumInteractiveComponentSize()
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = "Back",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp).padding(start = 4.dp)
            )
        }

        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        if (selectedPeriod != null && onPeriodSelected != null) {
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceWhite)
                        .clickable { showPeriodMenu = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedPeriod,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimeraViolet
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Period",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showPeriodMenu,
                    onDismissRequest = { showPeriodMenu = false },
                    modifier = Modifier.background(SurfaceWhite)
                ) {
                    listOf("Daily", "Weekly", "Monthly", "Yearly").forEach { period ->
                        DropdownMenuItem(
                            text = { Text(period, fontSize = 14.sp) },
                            onClick = {
                                onPeriodSelected(period)
                                showPeriodMenu = false
                            }
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.size(40.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TopBarsPreview() {
    PrimeraTheme {
        FeatureTopBar(title = "Insights", onBack = {})
    }
}
