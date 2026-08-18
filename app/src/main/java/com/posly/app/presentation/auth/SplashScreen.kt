package com.posly.app.presentation.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.posly.app.presentation.ui.theme.*

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToPos: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            SplashState.NavigateToLogin -> onNavigateToLogin()
            SplashState.NavigateToPos -> onNavigateToPos()
            SplashState.NavigateToRegister -> onNavigateToRegister()
            SplashState.Loading -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "PosLy",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = OnPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                text = "Kasir & Finansial UMKM",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = OnPrimary.copy(alpha = 0.8f)
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(color = OnPrimary)
        }
    }
}
