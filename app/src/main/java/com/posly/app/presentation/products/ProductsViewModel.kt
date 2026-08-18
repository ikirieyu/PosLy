package com.posly.app.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posly.app.domain.model.Product
import com.posly.app.domain.model.SyncStatus
import com.posly.app.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = productRepository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadProduct(productId: String, onLoaded: (Product) -> Unit) {
        viewModelScope.launch {
            productRepository.getProductById(productId)?.let(onLoaded)
        }
    }

    fun saveProduct(
        id: String?,
        name: String,
        sku: String,
        costPrice: Double,
        sellingPrice: Double,
        stock: Int,
        minStock: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val product = Product(
                id = id ?: UUID.randomUUID().toString(),
                name = name, sku = sku,
                costPrice = costPrice, sellingPrice = sellingPrice,
                stock = stock, minStockAlert = minStock,
                syncStatus = SyncStatus.PENDING
            )
            if (id == null) productRepository.insertProduct(product)
            else productRepository.updateProduct(product)
            onSuccess()
        }
    }
}
