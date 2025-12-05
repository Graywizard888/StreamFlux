package com.streamflux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.streamflux.ui.navigation.NavGraph
import com.streamflux.ui.theme.StreamFluxTheme
import com.streamflux.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            StreamFluxApp()
        }
    }
}

@Composable
fun StreamFluxApp() {
    val settingsViewModel: SettingsViewModel = viewModel()
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    
    val systemUiController = rememberSystemUiController()
    val darkTheme = isDarkMode ?: isSystemInDarkTheme()
    
    StreamFluxTheme(darkTheme = darkTheme) {
        systemUiController.setSystemBarsColor(
            color = androidx.compose.ui.graphics.Color.Transparent,
            darkIcons = !darkTheme
        )
        
        NavGraph()
    }
}
