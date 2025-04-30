package com.example.fakio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fakio.presentation.navigation.MyApp
import com.example.fakio.presentation.ui.theme.FakioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FakioTheme {
                MyApp()
            }
        }
    }
}