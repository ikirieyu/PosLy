package com.posly.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["sku"], unique = true),
        Index(value = ["barcode"])
    ]
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val categoryId: String? = null,
    val name: String,
    val sku: String = "",
    val barcode: String = "",
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val stock: Int = 0,
    val minStockAlert: Int = 5,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"  // PENDING | SYNCED | FAILED
)
