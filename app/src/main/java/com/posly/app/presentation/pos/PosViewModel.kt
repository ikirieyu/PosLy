package com.posly.app.presentation.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posly.app.domain.model.*
import com.posly.app.domain.repository.OrderRepository
import com.posly.app.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PosUiState(
    val categories: List<Category> = emptyList(),
    val products: List<Product> = emptyList(),
    val selectedCategoryId: String? = null,  // null = All
    val searchQuery: String = "",
    val cart: Cart = Cart(),
    val draftOrders: List<Cart> = emptyList(),
    val isLoading: Boolean = false,
    val lowStockProducts: List<Product> = emptyList()
)

@HiltViewModel
class PosViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _cart = MutableStateFlow(Cart())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _products = combine(_selectedCategoryId, _searchQuery) { categoryId, query ->
        Pair(categoryId, query)
    }.flatMapLatest { (categoryId, query) ->
        when {
            query.isNotBlank() -> productRepository.searchProducts(query)
            categoryId != null -> productRepository.getProductsByCategory(categoryId)
            else -> productRepository.getAllProducts()
        }
    }

    val uiState: StateFlow<PosUiState> = combine(
        productRepository.getAllCategories(),
        _products,
        _cart,
        orderRepository.getAllDraftOrders(),
        productRepository.getLowStockProducts()
    ) { categories, products, cart, drafts, lowStock ->
        PosUiState(
            categories = categories,
            products = products.filter { it.isActive },
            selectedCategoryId = _selectedCategoryId.value,
            searchQuery = _searchQuery.value,
            cart = cart,
            draftOrders = drafts,
            lowStockProducts = lowStock
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PosUiState())

    // ─── Cart operations ──────────────────────────────────────────────────────

    fun addToCart(product: Product) {
        _cart.update { cart ->
            val existingIndex = cart.items.indexOfFirst { it.product.id == product.id }
            if (existingIndex >= 0) {
                val updatedItems = cart.items.toMutableList()
                val existing = updatedItems[existingIndex]
                updatedItems[existingIndex] = existing.copy(quantity = existing.quantity + 1)
                cart.copy(items = updatedItems)
            } else {
                cart.copy(items = cart.items + CartItem(product = product))
            }
        }
    }

    fun removeFromCart(productId: String) {
        _cart.update { cart ->
            cart.copy(items = cart.items.filter { it.product.id != productId })
        }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) { removeFromCart(productId); return }
        _cart.update { cart ->
            val updatedItems = cart.items.map { item ->
                if (item.product.id == productId) item.copy(quantity = quantity) else item
            }
            cart.copy(items = updatedItems)
        }
    }

    fun updateItemNote(productId: String, note: String) {
        _cart.update { cart ->
            val updatedItems = cart.items.map { item ->
                if (item.product.id == productId) item.copy(note = note) else item
            }
            cart.copy(items = updatedItems)
        }
    }

    fun setItemDiscount(productId: String, percent: Double = 0.0, nominal: Double = 0.0) {
        _cart.update { cart ->
            val updatedItems = cart.items.map { item ->
                if (item.product.id == productId) item.copy(discountPercent = percent, discountNominal = nominal) else item
            }
            cart.copy(items = updatedItems)
        }
    }

    fun setGlobalDiscount(percent: Double = 0.0, nominal: Double = 0.0) {
        _cart.update { it.copy(globalDiscountPercent = percent, globalDiscountNominal = nominal) }
    }

    fun clearCart() {
        _cart.value = Cart()
    }

    // ─── Category & Search ────────────────────────────────────────────────────

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // ─── Draft orders ─────────────────────────────────────────────────────────

    fun holdCurrentOrder(label: String) {
        val cart = _cart.value
        if (cart.isEmpty) return
        viewModelScope.launch {
            orderRepository.saveDraftOrder(cart, label)
            clearCart()
        }
    }

    fun resumeDraftOrder(draft: Cart) {
        _cart.value = draft
        viewModelScope.launch {
            orderRepository.deleteDraftOrder(draft.id)
        }
    }

    fun deleteDraftOrder(draftId: String) {
        viewModelScope.launch { orderRepository.deleteDraftOrder(draftId) }
    }

    // ─── Barcode scan ─────────────────────────────────────────────────────────

    fun onBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            val product = productRepository.getProductByBarcode(barcode)
            if (product != null && product.isActive) {
                addToCart(product)
            }
        }
    }
}
