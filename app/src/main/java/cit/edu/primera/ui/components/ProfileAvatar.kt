package cit.edu.primera.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.primera.core.theme.PrimeraLogoEnd
import cit.edu.primera.core.theme.PrimeraLogoStart
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.SurfaceWhite
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ProfileAvatar(
    userName: String,
    modifier: Modifier = Modifier,
    photoUri: String? = null,
    size: Dp = 60.dp,
    showEditBadge: Boolean = false,
    onEditClick: (() -> Unit)? = null
) {
    val initial = userName.trim().take(1).uppercase().ifEmpty { "P" }

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (onEditClick != null) Modifier.clickable { onEditClick() } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(photoUri))
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(2.dp, SurfaceWhite, CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(PrimeraLogoStart, PrimeraLogoEnd)
                        )
                    )
                    .border(2.dp, SurfaceWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = SurfaceWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.45f).sp
                )
            }
        }

        if (showEditBadge || onEditClick != null) {
            Box(
                modifier = Modifier
                    .size(size * 0.35f)
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(PrimeraViolet)
                    .border(1.5.dp, SurfaceWhite, CircleShape)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Edit Profile Picture",
                    tint = SurfaceWhite,
                    modifier = Modifier.size(size * 0.2f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileAvatarPreview() {
    PrimeraTheme {
        ProfileAvatar(
            userName = "Rlexandra",
            showEditBadge = true,
            onEditClick = {}
        )
    }
}
