package com.example.primera.frontend.common.components

import com.example.primera.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.frontend.common.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// Gradient CTA Button
// ─────────────────────────────────────────────────────────────────────────────

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
            .height(54.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = PrimeraViolet.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(if (enabled) gradient else Brush.linearGradient(listOf(Color.Gray, Color.Gray)))
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
                fontFamily = Quicksand,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = SurfaceWhite
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Text Field
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PrimeraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(24.dp) // Increased corner radius for "pill" look

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    fontFamily = NunitoSans,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextHint
                )
            },
            singleLine = true,
            isError = isError,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility
                            else Icons.Outlined.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = IconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor  = SurfaceWhite,
                focusedContainerColor    = SurfaceWhite,
                unfocusedBorderColor     = InputBorder,
                focusedBorderColor       = PrimeraViolet,
                errorBorderColor         = ErrorRed,
                cursorColor              = PrimeraViolet,
                unfocusedTextColor       = TextPrimary,
                focusedTextColor         = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = ErrorRed,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 16.dp, top = 2.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Log In / Sign Up toggle tab
// ─────────────────────────────────────────────────────────────────────────────

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

// ─────────────────────────────────────────────────────────────────────────────
// "Or login with" divider row
// ─────────────────────────────────────────────────────────────────────────────

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

// ─────────────────────────────────────────────────────────────────────────────
// Social icon button
// ─────────────────────────────────────────────────────────────────────────────

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
            .size(width = 80.dp, height = 56.dp) // Rectangular shape as seen in design
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

// ─────────────────────────────────────────────────────────────────────────────
// Labelled input field with title
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall, // Smaller, lighter label
            color = TextSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        PrimeraTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            isPassword = isPassword,
            keyboardType = keyboardType,
            isError = isError,
            errorMessage = errorMessage
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Primera logo "N" bubble  (top of screen)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PrimeraLogoBubble(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.primera_logo),
        contentDescription = "Primera Logo",
        modifier = modifier.size(120.dp)
    )
}
