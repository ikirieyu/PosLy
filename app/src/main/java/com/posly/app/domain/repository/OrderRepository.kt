package com.posly.app.domain.repository

import com.posly.app.domain.model.Order
import com.posly.app.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getAllOrders(): Flow<List<Order>>
    fun getOrdersByDateRange(startDate: Long, endDate: Long): Flow<List<Order>>
    suspend fun getOrderById(id: String): Order?
    suspend fun getOrderByInvoice(invoiceNumber: String): Order?
    suspend fun insertOrder(order: Order): Result<Unit>
    suspend fun voidOrder(
        orderId: String,
        reason: String,
        approvedBy: String
    ): Result<Unit>
    suspend fun generateInvoiceNumber(): String
    fun getPendingSyncOrders(): Flow<List<Order>>
    suspend fun markOrderSynced(orderId: String)
    suspend fun markOrderSyncFailed(orderId: String)

    // Draft/Hold orders
    suspend fun saveDraftOrder(cart: com.posly.app.domain.model.Cart, label: String): Result<Unit>
    fun getAllDraftOrders(): Flow<List<com.posly.app.domain.model.Cart>>
    suspend fun deleteDraftOrder(draftId: String)
}
