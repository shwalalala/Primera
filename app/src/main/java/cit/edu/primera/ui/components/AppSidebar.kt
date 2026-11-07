package cit.edu.primera.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.primera.core.navigation.Routes
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.MenuButtonBorder
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.SurfaceWhite
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary

sealed class SidebarEntry {
    data class Header(val title: String) : SidebarEntry()
    data class Item(val route: String, val label: String, val icon: ImageVector) : SidebarEntry()
}

@Composable
fun AppSidebar(
    userName: String,
    email: String?,
    photoUri: String?,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
    onEditProfilePicture: () -> Unit,
    onNotificationClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = listOf(
        SidebarEntry.Header("PREFERENCES"),
        SidebarEntry.Item(Routes.SETTINGS, "Settings", Icons.Default.Settings),
        SidebarEntry.Item(Routes.PROFILE, "My Profile", Icons.Default.Person),

        SidebarEntry.Header("HEALTH & TRACKING"),
        SidebarEntry.Item(Routes.DASHBOARD, "Home", Icons.Default.Home),

        SidebarEntry.Header("CONTENT"),
        @Suppress("DEPRECATION")
        SidebarEntry.Item(
            Routes.NEWS_EDUCATION,
            "News & Education",
            Icons.AutoMirrored.Filled.MenuBook
        )
    )

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .clip(RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)),
        color = SurfaceWhite,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(BackgroundCream)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            // Top Bar: Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                        .border(1.dp, MenuButtonBorder, CircleShape)
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Close Sidebar",
                        tint = PrimeraViolet,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(start = 3.dp)
                    )
                }

                Text(
                    text = "Menu",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.size(36.dp))
            }

            // Header: User Icon (editable), Name/Email, Notification Icon
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = SurfaceWhite,
                border = BorderStroke(1.dp, MenuButtonBorder),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        ProfileAvatar(
                            userName = userName,
                            photoUri = photoUri,
                            size = 48.dp,
                            showEditBadge = true,
                            onEditClick = onEditProfilePicture
                        )

                        Spacer(Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName.ifBlank { "User" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!email.isNullOrBlank()) {
                                Text(
                                    text = email,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimeraViolet.copy(alpha = 0.1f))
                            .clickable { onNotificationClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = PrimeraViolet,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            HorizontalDivider(
                color = MenuButtonBorder,
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(Modifier.height(12.dp))

            // Navigation Entries List with category headers
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                entries.forEach { entry ->
                    when (entry) {
                        is SidebarEntry.Header -> {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = entry.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimeraViolet.copy(alpha = 0.75f),
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 4.dp
                                ),
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(2.dp))
                        }

                        is SidebarEntry.Item -> {
                            val isSelected = currentRoute == entry.route

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) PrimeraViolet.copy(alpha = 0.15f)
                                        else Color.Transparent
                                    )
                                    .clickable { onNavigate(entry.route) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = entry.icon,
                                    contentDescription = entry.label,
                                    tint = if (isSelected) PrimeraViolet else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = entry.label,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PrimeraViolet else TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Logout button
            HorizontalDivider(
                color = MenuButtonBorder,
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Transparent)
                    .clickable { onLogoutClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Log Out",
                    tint = cit.edu.primera.core.theme.ErrorRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Log Out",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = cit.edu.primera.core.theme.ErrorRed
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppSidebarPreview() {
    PrimeraTheme {
        AppSidebar(
            userName = "Jane Doe",
            email = "janedoe@gmail.com",
            photoUri = null,
            currentRoute = Routes.DASHBOARD,
            onNavigate = {},
            onClose = {},
            onEditProfilePicture = {},
            onNotificationClick = {},
            onLogoutClick = {}
        )
    }
}
