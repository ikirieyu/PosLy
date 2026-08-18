package com.posly.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.posly.app.presentation.navigation.PoslyNavGraph
import com.posly.app.presentation.ui.theme.PoslyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PoslyTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                val navController = rememberNavController()
                val viewModel: MainViewModel = hiltViewModel()
                val currentRole by viewModel.currentRole.collectAsState()

                PoslyScaffold(
                    navController = navController,
                    windowSizeClass = windowSizeClass,
                    currentRole = currentRole
                ) {
                    PoslyNavGraph(
                        navController = navController,
                        currentRole = currentRole
                    )
                }
            }
        }
    }
}
