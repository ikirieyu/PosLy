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

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    fun register(name: String, email: String, password: String, pin: String) {
        if (pin.length != 6) {
            _uiState.value = RegisterState.Error("PIN harus 6 digit")
            return
        }
        viewModelScope.launch {
            _uiState.value = RegisterState.Loading
            authRepository.registerOwner(name, email, password, pin)
                .onSuccess { _uiState.value = RegisterState.Success }
                .onFailure { e -> _uiState.value = RegisterState.Error(e.message ?: "Registrasi gagal") }
        }
    }
}
