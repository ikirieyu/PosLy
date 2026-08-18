package com.posly.app.presentation.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posly.app.domain.model.Cart
import com.posly.app.domain.model.PaymentMethod
import com.posly.app.domain.usecase.pos.ProcessPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CheckoutState {
    object Idle : CheckoutState()
    object Loading : CheckoutState()
    data class Success(val orderId: String) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val processPaymentUseCase: ProcessPaymentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val uiState: StateFlow<CheckoutState> = _uiState.asStateFlow()

    fun processPayment(cart: Cart, paymentMethod: PaymentMethod, paidAmount: Double, onPaymentDone: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = CheckoutState.Loading
            processPaymentUseCase(
                cart = cart,
                paymentMethod = paymentMethod,
                paidAmount = paidAmount
            ).onSuccess { order ->
                _uiState.value = CheckoutState.Success(order.id)
                onPaymentDone(order.id)
            }.onFailure { e ->
                _uiState.value = CheckoutState.Error(e.message ?: "Pembayaran gagal")
            }
        }
    }
}
