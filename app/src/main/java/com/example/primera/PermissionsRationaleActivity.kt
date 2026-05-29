package com.example.primera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class PermissionsRationaleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Text(
                    text = "This app reads your heart rate, oxygen saturation, step count, and sleep data from Health Connect only after you give permission. The data is saved for maternal health monitoring purposes.",
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}