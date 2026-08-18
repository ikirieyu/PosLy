package com.posly.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posly.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashState {
    object Loading : SplashState()
    object NavigateToLogin : SplashState()
    object NavigateToPos : SplashState()
    object NavigateToRegister : SplashState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashState>(SplashState.Loading)
    val uiState: StateFlow<SplashState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Check if there's any account set up
            val hasOwner = authRepository.hasOwnerAccount()
            if (!hasOwner) {
                _uiState.value = SplashState.NavigateToRegister
                return@launch
            }
            // Check if already logged in
            authRepository.currentProfile.collect { profile ->
                _uiState.value = if (profile != null) {
                    SplashState.NavigateToPos
                } else {
                    SplashState.NavigateToLogin
                }
            }
        }
    }
}
