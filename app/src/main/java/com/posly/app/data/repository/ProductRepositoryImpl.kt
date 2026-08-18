package com.posly.app.data.repository

import com.posly.app.data.local.dao.ProductDao
import com.posly.app.data.local.entity.CategoryEntity
import com.posly.app.data.local.entity.ProductEntity
import com.posly.app.domain.model.Cart
import com.posly.app.domain.model.Category
import com.posly.app.domain.model.Product
import com.posly.app.domain.model.SyncStatus
import com.posly.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> =
        productDao.getAllProducts().map { list -> list.map { it.toDomain() } }

    override fun getProductsByCategory(categoryId: String): Flow<List<Product>> =
        productDao.getProductsByCategory(categoryId).map { list -> list.map { it.toDomain() } }

    override fun searchProducts(query: String): Flow<List<Product>> =
        productDao.searchProducts(query).map { list -> list.map { it.toDomain() } }

    override suspend fun getProductById(id: String): Product? =
        productDao.getProductById(id)?.toDomain()

    override suspend fun getProductByBarcode(barcode: String): Product? =
        productDao.getProductByBarcode(barcode)?.toDomain()

    override suspend fun insertProduct(product: Product): Result<Unit> = runCatching {
        productDao.insertProduct(product.toEntity())
    }

    override suspend fun updateProduct(product: Product): Result<Unit> = runCatching {
        productDao.updateProduct(product.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> = runCatching {
        productDao.softDeleteProduct(productId)
    }

    override suspend fun decrementStock(productId: String, quantity: Int): Result<Unit> = runCatching {
        productDao.decrementStock(productId, quantity)
    }

    override suspend fun adjustStock(productId: String, newStock: Int, reason: String): Result<Unit> = runCatching {
        productDao.updateStock(productId, newStock)
    }

    override fun getLowStockProducts(): Flow<List<Product>> =
        productDao.getLowStockProducts().map { list -> list.map { it.toDomain() } }

    override fun getAllCategories(): Flow<List<Category>> =
        productDao.getAllCategories().map { list -> list.map { it.toDomain() } }

    override suspend fun insertCategory(category: Category): Result<Unit> = runCatching {
        productDao.insertCategory(category.toEntity())
    }

    override suspend fun updateCategory(category: Category): Result<Unit> = runCatching {
        productDao.insertCategory(category.toEntity())
    }

    override suspend fun deleteCategory(categoryId: String): Result<Unit> = runCatching {
        productDao.deleteCategory(CategoryEntity(id = categoryId, name = ""))
    }

    // ─── Mappers ──────────────────────────────────────────────────────────────

    private fun ProductEntity.toDomain() = Product(
        id = id, categoryId = categoryId, name = name, sku = sku, barcode = barcode,
        costPrice = costPrice, sellingPrice = sellingPrice, stock = stock,
        minStockAlert = minStockAlert, imageUrl = imageUrl, isActive = isActive,
        createdAt = createdAt, updatedAt = updatedAt,
        syncStatus = SyncStatus.valueOf(syncStatus)
    )

    private fun Product.toEntity() = ProductEntity(
        id = id, categoryId = categoryId, name = name, sku = sku, barcode = barcode,
        costPrice = costPrice, sellingPrice = sellingPrice, stock = stock,
        minStockAlert = minStockAlert, imageUrl = imageUrl, isActive = isActive,
        createdAt = createdAt, updatedAt = updatedAt, syncStatus = syncStatus.name
    )

    private fun CategoryEntity.toDomain() = Category(id = id, name = name, createdAt = createdAt)
    private fun Category.toEntity() = CategoryEntity(id = id, name = name, createdAt = createdAt)
}
