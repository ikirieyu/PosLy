package com.posly.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posly.app.domain.model.Profile
import com.posly.app.domain.model.StoreSettings
import com.posly.app.domain.model.UserRole
import com.posly.app.domain.repository.AuthRepository
import com.posly.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(StoreSettings())
    val settings: StateFlow<StoreSettings> = _settings.asStateFlow()

    val currentRole: StateFlow<UserRole?> = authRepository.currentProfile
        .map { it?.role }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val workers: StateFlow<List<Profile>> = authRepository.getAllWorkers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            settingsRepository.getStoreSettings().collect { _settings.value = it }
        }
    }

    fun updateStoreName(name: String) = _settings.update { it.copy(storeName = name) }
    fun updateSlogan(slogan: String) = _settings.update { it.copy(slogan = slogan) }
    fun updateAddress(address: String) = _settings.update { it.copy(address = address) }
    fun updatePhone(phone: String) = _settings.update { it.copy(phone = phone) }
    fun updateReceiptFooter(footer: String) = _settings.update { it.copy(receiptFooter = footer) }
    fun updateQrisImageUrl(url: String) = _settings.update { it.copy(qrisImageUrl = url) }
    fun updatePrintReceiptHeader(enabled: Boolean) = _settings.update { it.copy(printReceiptHeader = enabled) }
    fun updateAutoPrintReceipt(enabled: Boolean) = _settings.update { it.copy(autoPrintReceipt = enabled) }
    fun updateSavingsPercent(p: Double) = _settings.update { it.copy(savingsPercent = p) }
    fun updateEmergencyPercent(p: Double) = _settings.update { it.copy(emergencyPercent = p) }
    fun updateRestockPercent(p: Double) = _settings.update { it.copy(restockPercent = p) }
    fun updateTransportPercent(p: Double) = _settings.update { it.copy(transportPercent = p) }

    fun saveSettings() {
        viewModelScope.launch { settingsRepository.updateStoreSettings(_settings.value) }
    }

    fun saveSupabaseConfig(url: String, key: String) {
        viewModelScope.launch { settingsRepository.updateSupabaseConfig(url, key) }
    }

    fun testConnection(url: String, key: String) {
        viewModelScope.launch { settingsRepository.testSupabaseConnection(url, key) }
    }

    fun deactivateWorker(workerId: String) {
        viewModelScope.launch { authRepository.deactivateWorker(workerId) }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch { authRepository.logout(); onLogout() }
    }
}
