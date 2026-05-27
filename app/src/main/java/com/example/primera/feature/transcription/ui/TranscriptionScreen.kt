package com.example.primera.feature.transcription.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.primera.core.theme.*
import com.example.primera.core.utils.clickableNoRipple
import com.example.primera.ui.components.FullScreenLoadingOverlay
import com.example.primera.ui.components.PrimeraGradientButton

@Composable
fun TranscriptionScreen(
    viewModel: TranscriptionViewModel,
    onBack: () -> Unit
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
    onDismissError: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundCream, PrimeraLilac.copy(alpha = 0.45f))
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
            
            Text(
                text = "Voice Transcription",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            
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

            if (uiState is TranscriptionUiState.Success || uiState is TranscriptionUiState.Uploading || uiState is TranscriptionUiState.Uploaded) {
                OutlinedTextField(
                    value = transcribedText,
                    onValueChange = onTextEdited,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    label = { Text("Transcribed Text") },
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
                    text = if (uiState is TranscriptionUiState.Uploaded) "Saved!" else "Save to Firestore",
                    onClick = onSave,
                    enabled = uiState is TranscriptionUiState.Success,
                    isLoading = uiState is TranscriptionUiState.Uploading
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
            animation = tween(1000, easing = FastOutSlowInEasing),
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
                    colors = listOf(buttonColor, buttonColor.copy(alpha = 0.8f))
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
            uiState = TranscriptionUiState.Success("Hello, this is a test transcription.", 0.95f, 5000L),
            transcribedText = "Hello, this is a test transcription.",
            onStartListening = {},
            onStopListening = {},
            onTextEdited = {},
            onSave = {},
            onDismissError = {},
            onBack = {}
        )
    }
}
