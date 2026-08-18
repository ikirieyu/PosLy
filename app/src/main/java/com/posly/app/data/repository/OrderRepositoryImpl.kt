package com.posly.app.data.repository

import com.posly.app.data.local.dao.OrderDao
import com.posly.app.data.local.entity.DraftOrderEntity
import com.posly.app.data.local.entity.OrderEntity
import com.posly.app.data.local.entity.OrderItemEntity
import com.posly.app.domain.model.*
import com.posly.app.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao
) : OrderRepository {

    override fun getAllOrders(): Flow<List<Order>> =
        orderDao.getAllOrders().map { list -> list.map { it.toDomain() } }

    override fun getOrdersByDateRange(startDate: Long, endDate: Long): Flow<List<Order>> =
        orderDao.getOrdersByDateRange(startDate, endDate).map { list -> list.map { it.toDomain() } }

    override suspend fun getOrderById(id: String): Order? =
        orderDao.getOrderById(id)?.toDomain()

    override suspend fun getOrderByInvoice(invoiceNumber: String): Order? =
        orderDao.getOrderByInvoice(invoiceNumber)?.toDomain()

    override suspend fun insertOrder(order: Order): Result<Unit> = runCatching {
        orderDao.insertOrder(order.toEntity())
        val items = order.items.map { it.toEntity(order.id) }
        orderDao.insertOrderItems(items)
    }

    override suspend fun voidOrder(orderId: String, reason: String, approvedBy: String): Result<Unit> = runCatching {
        orderDao.voidOrder(orderId, reason, approvedBy)
    }

    override suspend fun generateInvoiceNumber(): String {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        val count = orderDao.countOrdersSince(startOfDay) + 1
        return "INV-$dateStr-${count.toString().padStart(4, '0')}"
    }

    override fun getPendingSyncOrders(): Flow<List<Order>> =
        orderDao.getPendingSyncOrders().map { list -> list.map { it.toDomain() } }

    override suspend fun markOrderSynced(orderId: String) =
        orderDao.updateSyncStatus(orderId, "SYNCED")

    override suspend fun markOrderSyncFailed(orderId: String) =
        orderDao.updateSyncStatus(orderId, "FAILED")

    override suspend fun saveDraftOrder(cart: Cart, label: String): Result<Unit> = runCatching {
        val cartJson = Json.encodeToString(cart)
        orderDao.insertDraftOrder(
            DraftOrderEntity(id = cart.id, label = label, cartJson = cartJson)
        )
    }

    override fun getAllDraftOrders(): Flow<List<Cart>> =
        orderDao.getAllDraftOrders().map { list ->
            list.mapNotNull { draft ->
                runCatching { Json.decodeFromString<Cart>(draft.cartJson) }.getOrNull()
            }
        }

    override suspend fun deleteDraftOrder(draftId: String) =
        orderDao.deleteDraftOrder(draftId)

    // ─── Mappers ──────────────────────────────────────────────────────────────

    private fun OrderEntity.toDomain() = Order(
        id = id, invoiceNumber = invoiceNumber, cashierId = cashierId,
        cashierName = cashierName, totalAmount = totalAmount, totalCost = totalCost,
        discountAmount = discountAmount, paidAmount = paidAmount, changeAmount = changeAmount,
        paymentMethod = PaymentMethod.valueOf(paymentMethod),
        status = OrderStatus.valueOf(status),
        voidReason = voidReason, voidApprovedBy = voidApprovedBy,
        createdAt = createdAt, syncStatus = SyncStatus.valueOf(syncStatus)
    )

    private fun Order.toEntity() = OrderEntity(
        id = id, invoiceNumber = invoiceNumber, cashierId = cashierId,
        cashierName = cashierName, totalAmount = totalAmount, totalCost = totalCost,
        discountAmount = discountAmount, paidAmount = paidAmount, changeAmount = changeAmount,
        paymentMethod = paymentMethod.name, status = status.name,
        voidReason = voidReason, voidApprovedBy = voidApprovedBy,
        createdAt = createdAt, syncStatus = syncStatus.name
    )

    private fun OrderItem.toEntity(orderId: String) = OrderItemEntity(
        id = id, orderId = orderId, productId = productId, productName = productName,
        quantity = quantity, unitCost = unitCost, unitPrice = unitPrice,
        discountPerItem = discountPerItem, subtotal = subtotal, note = note
    )
}
