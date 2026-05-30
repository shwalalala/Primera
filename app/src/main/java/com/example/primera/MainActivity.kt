package com.example.primera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.primera.core.theme.PrimeraTheme
import com.example.primera.core.navigation.AppNavGraph
import com.example.primera.feature.smartwatchconnection.ui.SmartwatchRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrimeraTheme {
                AppNavGraph()
//                SmartwatchRoute()
            }
        }
    }
}
