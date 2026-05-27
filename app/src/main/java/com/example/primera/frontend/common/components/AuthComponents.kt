package com.example.primera.frontend.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.R
import com.example.primera.frontend.common.theme.*

@Composable
fun ArchedAuthCard(
    logo: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 80.dp)
    ) {
        // 1. Draw the Arch background
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    drawArc(
                        color = SurfaceWhite,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(size.width * 0.2f, -size.width * 0.3f),
                        size = Size(size.width * 0.6f, size.width * 0.6f)
                    )
                }
        )

        // 2. Position the Logo centered in the arch
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            val archRadius = maxWidth * 0.3f
            logo(Modifier.offset(y = -archRadius / 2 - 35.dp))
        }

        // 3. Main Content Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(SurfaceWhite)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

@Composable
fun AuthTabToggle(
    selectedTab: AuthTab,
    onTabSelected: (AuthTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(ToggleTrack),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuthTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (isSelected) SurfaceWhite else Color.Transparent)
                        .then(
                            if (isSelected) Modifier.shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(28.dp)
                            ) else Modifier
                        )
                        .clickable { onTabSelected(tab) }
                ) {
                    Text(
                        text = tab.label,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        fontSize = 15.sp,
                        color = if (isSelected) TextPrimary else TextSecondary
                    )
                }
            }
        }
    }
}

enum class AuthTab(val label: String) {
    LOGIN("Log In"),
    SIGNUP("Sign Up")
}

@Composable
fun OrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
        Text(
            text = "  Or login with  ",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
    }
}

@Composable
fun SocialIconButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(width = 80.dp, height = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .border(BorderStroke(1.dp, InputBorder.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )
    }
}

@Composable
fun SocialAuthRow(
    onGoogleClick: () -> Unit = {},
    onFacebookClick: () -> Unit = {},
    onAppleClick: () -> Unit = {},
    onPhoneClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SocialIconButton(
            icon = painterResource(id = R.drawable.google),
            contentDescription = "Google",
            onClick = onGoogleClick
        )
        SocialIconButton(
            icon = painterResource(id = R.drawable.facebook),
            contentDescription = "Facebook",
            onClick = onFacebookClick
        )
        SocialIconButton(
            icon = painterResource(id = R.drawable.apple),
            contentDescription = "Apple",
            onClick = onAppleClick
        )
        SocialIconButton(
            icon = painterResource(id = R.drawable.mobile),
            contentDescription = "Phone",
            onClick = onPhoneClick
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1FB)
@Composable
fun AuthComponentsPreview() {
    PrimeraTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AuthTabToggle(selectedTab = AuthTab.LOGIN, onTabSelected = {})
            OrDivider()
            SocialAuthRow()
        }
    }
}
