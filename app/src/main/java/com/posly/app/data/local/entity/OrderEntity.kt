package com.posly.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["cashierId"]),
        Index(value = ["createdAt"]),
        Index(value = ["syncStatus"])
    ]
)
data class OrderEntity(
    @PrimaryKey val id: String,
    val invoiceNumber: String,
    val cashierId: String = "",
    val cashierName: String = "",
    val totalAmount: Double,
    val totalCost: Double,
    val discountAmount: Double = 0.0,
    val paidAmount: Double,
    val changeAmount: Double = 0.0,
    val paymentMethod: String,   // "CASH" | "QRIS"
    val status: String = "COMPLETED",  // COMPLETED | VOID | REFUNDED
    val voidReason: String? = null,
    val voidApprovedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class OrderItemEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitCost: Double,
    val unitPrice: Double,
    val discountPerItem: Double = 0.0,
    val subtotal: Double,
    val note: String = ""
)

@Entity(tableName = "draft_orders")
data class DraftOrderEntity(
    @PrimaryKey val id: String,
    val label: String = "",
    val cartJson: String,  // Serialized Cart JSON
    val createdAt: Long = System.currentTimeMillis()
)
