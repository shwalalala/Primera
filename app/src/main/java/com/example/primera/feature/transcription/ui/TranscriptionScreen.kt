package com.example.primera.feature.transcription.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.primera.core.theme.BackgroundCream
import com.example.primera.core.theme.InputBorder
import com.example.primera.core.theme.PrimeraLilac
import com.example.primera.core.theme.PrimeraTheme
import com.example.primera.core.theme.PrimeraViolet
import com.example.primera.core.theme.TextPrimary
import com.example.primera.core.theme.TextSecondary
import com.example.primera.core.utils.clickableNoRipple
import com.example.primera.ui.components.FullScreenLoadingOverlay
import com.example.primera.ui.components.PrimeraGradientButton

@Composable
fun TranscriptionScreen(
    viewModel: TranscriptionViewModel,
    onBack: () -> Unit,
    onUseInCheckin: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val transcribedText by viewModel.transcribedText.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    TranscriptionContent(
        uiState = uiState,
        transcribedText = transcribedText,
        onStartListening = {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        },
        onStopListening = viewModel::stopListening,
        onTextEdited = viewModel::onTextEdited,
        onSave = viewModel::saveTranscription,
        onUseInCheckin = {
            if (transcribedText.isNotBlank()) {
                onUseInCheckin(transcribedText)
            }
        },
        onDismissError = viewModel::dismissError,
        onBack = onBack
    )
}

@Composable
private fun TranscriptionContent(
    uiState: TranscriptionUiState,
    transcribedText: String,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onTextEdited: (String) -> Unit,
    onSave: () -> Unit,
    onUseInCheckin: () -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundCream,
                        PrimeraLilac.copy(alpha = 0.45f)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    text = "Voice Transcription",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(Modifier.width(48.dp))
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Tap the microphone to start recording your thoughts",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(1f))

            MicrophoneButton(
                uiState = uiState,
                onClick = {
                    when (uiState) {
                        is TranscriptionUiState.Listening -> onStopListening()
                        else -> onStartListening()
                    }
                }
            )

            Spacer(Modifier.weight(1f))

            if (
                uiState is TranscriptionUiState.Success ||
                uiState is TranscriptionUiState.Uploading ||
                uiState is TranscriptionUiState.Uploaded
            ) {
                OutlinedTextField(
                    value = transcribedText,
                    onValueChange = onTextEdited,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    label = { Text("Transcribed Text") },
                    trailingIcon = {
                        if (transcribedText.isNotEmpty()) {
                            IconButton(onClick = { onTextEdited("") }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear text"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White.copy(alpha = 0.6f),
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = InputBorder,
                        focusedBorderColor = PrimeraViolet
                    )
                )

                if (uiState is TranscriptionUiState.Success) {
                    Text(
                        text = "Confidence: ${(uiState.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                PrimeraGradientButton(
                    text = if (uiState is TranscriptionUiState.Uploaded) {
                        "Saved!"
                    } else {
                        "Save to Firestore"
                    },
                    onClick = onSave,
                    enabled = uiState is TranscriptionUiState.Success,
                    isLoading = uiState is TranscriptionUiState.Uploading
                )

                Spacer(Modifier.height(12.dp))

                PrimeraGradientButton(
                    text = "Use in Check-In",
                    onClick = onUseInCheckin,
                    enabled = transcribedText.isNotBlank() &&
                            (uiState is TranscriptionUiState.Success || uiState is TranscriptionUiState.Uploaded),
                    isLoading = false
                )
            }
        }

        if (uiState is TranscriptionUiState.Processing) {
            FullScreenLoadingOverlay()
        }

        if (uiState is TranscriptionUiState.Error) {
            AlertDialog(
                onDismissRequest = onDismissError,
                title = { Text("Error") },
                text = { Text(uiState.message) },
                confirmButton = {
                    TextButton(onClick = onDismissError) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun MicrophoneButton(
    uiState: TranscriptionUiState,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val buttonScale = if (uiState is TranscriptionUiState.Listening) scale else 1f

    val buttonColor = when (uiState) {
        is TranscriptionUiState.Listening -> Color.Red
        is TranscriptionUiState.Uploaded -> Color.Green
        else -> PrimeraViolet
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(120.dp)
            .scale(buttonScale)
            .clip(CircleShape)
            .background(buttonColor.copy(alpha = 0.2f))
            .padding(16.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        buttonColor,
                        buttonColor.copy(alpha = 0.8f)
                    )
                )
            )
            .clickableNoRipple(onClick = onClick)
    ) {
        Icon(
            imageVector = when (uiState) {
                is TranscriptionUiState.Listening -> Icons.Default.Stop
                is TranscriptionUiState.Uploaded -> Icons.Default.CheckCircle
                else -> Icons.Default.Mic
            },
            contentDescription = "Microphone",
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Preview(name = "Compact Phone", device = Devices.PHONE)
@Composable
private fun TranscriptionScreenPreview() {
    PrimeraTheme {
        TranscriptionContent(
            uiState = TranscriptionUiState.Idle,
            transcribedText = "",
            onStartListening = {},
            onStopListening = {},
            onTextEdited = {},
            onSave = {},
            onUseInCheckin = {},
            onDismissError = {},
            onBack = {}
        )
    }
}

@Preview(name = "Success State", device = Devices.PHONE)
@Composable
private fun TranscriptionScreenSuccessPreview() {
    PrimeraTheme {
        TranscriptionContent(
            uiState = TranscriptionUiState.Success(
                "I feel dizzy and I have headache.",
                0.95f,
                5000L
            ),
            transcribedText = "I feel dizzy and I have headache.",
            onStartListening = {},
            onStopListening = {},
            onTextEdited = {},
            onSave = {},
            onUseInCheckin = {},
            onDismissError = {},
            onBack = {}
        )
    }
}