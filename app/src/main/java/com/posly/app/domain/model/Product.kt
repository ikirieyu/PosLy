package com.posly.app.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Domain model for a product category.
 */
@Serializable
data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Domain model for a product with HPP and selling price.
 */
@Serializable
data class Product(
    val id: String = UUID.randomUUID().toString(),
    val categoryId: String? = null,
    val categoryName: String? = null,
    val name: String,
    val sku: String = "",
    val barcode: String = "",
    /** Harga Modal (Cost of Goods) per unit */
    val costPrice: Double = 0.0,
    /** Harga Jual (Selling Price) per unit */
    val sellingPrice: Double = 0.0,
    val stock: Int = 0,
    val minStockAlert: Int = 5,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
) {
    /** Margin per unit: sellingPrice - costPrice */
    val marginAmount: Double get() = sellingPrice - costPrice

    /** Margin percentage: (margin / sellingPrice) * 100 */
    val marginPercent: Double
        get() = if (sellingPrice > 0) (marginAmount / sellingPrice) * 100 else 0.0

    /** True if stock is at or below minimum alert threshold */
    val isLowStock: Boolean get() = stock <= minStockAlert && stock > 0

    /** True if stock is completely depleted */
    val isOutOfStock: Boolean get() = stock <= 0
}
