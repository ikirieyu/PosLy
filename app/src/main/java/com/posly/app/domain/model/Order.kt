package com.posly.app.domain.model

import java.util.UUID

/**
 * Sync status for offline-first architecture.
 */
enum class SyncStatus {
    PENDING,   // Not yet synced to Supabase
    SYNCED,    // Successfully synced
    FAILED     // Sync attempted but failed
}

/**
 * Domain model for a completed order/transaction.
 */
data class Order(
    val id: String = UUID.randomUUID().toString(),
    val invoiceNumber: String,
    val cashierId: String,
    val cashierName: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double,
    val totalCost: Double,
    val discountAmount: Double = 0.0,
    val paidAmount: Double,
    val changeAmount: Double = 0.0,
    val paymentMethod: PaymentMethod,
    val status: OrderStatus = OrderStatus.COMPLETED,
    val voidReason: String? = null,
    val voidApprovedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
) {
    val grossProfit: Double get() = totalAmount - totalCost
}

/**
 * Status of a transaction.
 */
enum class OrderStatus {
    COMPLETED, VOID, REFUNDED
}

/**
 * Individual line item inside an order.
 */
data class OrderItem(
    val id: String = UUID.randomUUID().toString(),
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
