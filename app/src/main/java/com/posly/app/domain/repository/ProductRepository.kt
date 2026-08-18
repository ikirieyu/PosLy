package com.posly.app.domain.repository

import com.posly.app.domain.model.Category
import com.posly.app.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>
    fun searchProducts(query: String): Flow<List<Product>>
    suspend fun getProductById(id: String): Product?
    suspend fun getProductByBarcode(barcode: String): Product?
    suspend fun insertProduct(product: Product): Result<Unit>
    suspend fun updateProduct(product: Product): Result<Unit>
    suspend fun deleteProduct(productId: String): Result<Unit>
    suspend fun decrementStock(productId: String, quantity: Int): Result<Unit>
    suspend fun adjustStock(productId: String, newStock: Int, reason: String): Result<Unit>
    fun getLowStockProducts(): Flow<List<Product>>

    // Categories
    fun getAllCategories(): Flow<List<Category>>
    suspend fun insertCategory(category: Category): Result<Unit>
    suspend fun updateCategory(category: Category): Result<Unit>
    suspend fun deleteCategory(categoryId: String): Result<Unit>
}
