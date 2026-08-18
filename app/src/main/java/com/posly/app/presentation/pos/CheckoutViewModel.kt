package com.posly.app.presentation.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posly.app.domain.model.Cart
import com.posly.app.domain.model.PaymentMethod
import com.posly.app.domain.usecase.pos.ProcessPaymentUseCase
import com.posly.app.domain.repository.OrderRepository
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
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val posViewModel: PosViewModel  // shared via Hilt activity scope
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val uiState: StateFlow<CheckoutState> = _uiState.asStateFlow()

    val cart = posViewModel.uiState.let { flow ->
        MutableStateFlow(Cart()).also { cartFlow ->
            viewModelScope.launch {
                flow.collect { cartFlow.value = it.cart }
            }
        }
    }

    fun processPayment(paymentMethod: PaymentMethod, paidAmount: Double) {
        viewModelScope.launch {
            _uiState.value = CheckoutState.Loading
            processPaymentUseCase(
                cart = cart.value,
                paymentMethod = paymentMethod,
                paidAmount = paidAmount
            ).onSuccess { order ->
                posViewModel.clearCart()
                _uiState.value = CheckoutState.Success(order.id)
            }.onFailure { e ->
                _uiState.value = CheckoutState.Error(e.message ?: "Pembayaran gagal")
            }
        }
    }
}
