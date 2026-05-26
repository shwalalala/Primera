package com.example.primera.frontend.navigation

import androidx.compose.ui.res.painterResource
import  com.example.primera.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.frontend.common.theme.*

// ---------------------------------------------------------------------------
// Nav destinations — maps 1:1 to the HTML nav items
// ---------------------------------------------------------------------------

enum class NavDestination(
    val label: String,
    val iconRes: Int,
    val route: String
) {
    HOME    ("Home",     R.drawable.home,         Routes.DASHBOARD),
    CHECKIN ("Check-in", R.drawable.log,          Routes.CHECKIN),
    INSIGHT ("Insight",  R.drawable.heart,        Routes.INSIGHT),
    DEVICE  ("Device",   R.drawable.device,       Routes.DEVICE)
}

// ---------------------------------------------------------------------------
// Bottom nav bar
// ---------------------------------------------------------------------------

@Composable
fun PrimeraBottomNav(
    currentRoute: String,
    onDestinationSelected: (NavDestination) -> Unit,
    onFabClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp)
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val destinations = NavDestination.entries

            // Left two items
            destinations.take(2).forEach { dest ->
                NavItem(
                    destination = dest,
                    isSelected  = currentRoute == dest.route,
                    onClick     = { onDestinationSelected(dest) },
                    modifier    = Modifier.weight(1f)
                )
            }

            // FAB slot — raised circle in the center
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                FabButton(onClick = onFabClicked)
            }

            // Right two items
            destinations.drop(2).forEach { dest ->
                NavItem(
                    destination = dest,
                    isSelected  = currentRoute == dest.route,
                    onClick     = { onDestinationSelected(dest) },
                    modifier    = Modifier.weight(1f)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Single nav item
// ---------------------------------------------------------------------------

@Composable
private fun NavItem(
    destination: NavDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(destination.iconRes),
            contentDescription = destination.label,
            tint = if (isSelected) PrimeraViolet else Color(0xFF8B8FA8),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = destination.label,
            fontSize   = 11.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color      = if (isSelected) PrimeraViolet else Color(0xFF8B8FA8)
        )
    }
}

// ---------------------------------------------------------------------------
// Center FAB — raised circle with mic/record icon
// ---------------------------------------------------------------------------

@Composable
private fun FabButton(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(68.dp)
            .offset(y = (-24).dp)
            .shadow(elevation = 12.dp, shape = CircleShape)
            .border(width = 4.dp, color = Color.White, shape = CircleShape)
            .clip(CircleShape)
            .background(Color(0xFFB7A6D8))
            .clickable { onClick() }
    ) {
        Icon(
            painter            = painterResource(R.drawable.mic),
            contentDescription = "Microphone",
            tint               = Color.White,
            modifier           = Modifier.size(28.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@Preview(widthDp = 393)
@Composable
private fun PrimeraBottomNavPreview() {
    PrimeraTheme {
        PrimeraBottomNav(
            currentRoute          = Routes.DASHBOARD,
            onDestinationSelected = {},
            onFabClicked          = {}
        )
    }
}