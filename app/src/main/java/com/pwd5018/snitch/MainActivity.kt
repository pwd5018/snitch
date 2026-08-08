package com.pwd5018.snitch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pwd5018.snitch.navigation.SnitchNavHost
import com.pwd5018.snitch.ui.theme.SnitchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnitchTheme {
                SnitchNavHost()
            }
        }
    }
}
