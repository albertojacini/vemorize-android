package com.example.vemorize

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.vemorize.ui.navigation.VemorizeApp
import com.example.vemorize.ui.theme.VemorizeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VemorizeTheme {
                VemorizeApp()
            }
        }
    }
}