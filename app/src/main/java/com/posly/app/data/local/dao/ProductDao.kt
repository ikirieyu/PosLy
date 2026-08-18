package com.posly.app.data.local.dao

import androidx.room.*
import com.posly.app.data.local.entity.CategoryEntity
import com.posly.app.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isActive = 1 ORDER BY name ASC")
    fun getProductsByCategory(categoryId: String): Flow<List<ProductEntity>>

    @Query("""
        SELECT * FROM products 
        WHERE isActive = 1 
        AND (name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode AND isActive = 1 LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE stock <= minStockAlert AND isActive = 1 ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET isActive = 0, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun softDeleteProduct(productId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stock = stock - :quantity, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun decrementStock(productId: String, quantity: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stock = :newStock, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun updateStock(productId: String, newStock: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE products SET syncStatus = :syncStatus WHERE id = :productId")
    suspend fun updateSyncStatus(productId: String, syncStatus: String)

    // Categories
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}
