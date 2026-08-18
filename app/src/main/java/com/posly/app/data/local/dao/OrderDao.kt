package com.posly.app.data.local.dao

import androidx.room.*
import com.posly.app.data.local.entity.DraftOrderEntity
import com.posly.app.data.local.entity.OrderEntity
import com.posly.app.data.local.entity.OrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getOrdersByDateRange(startDate: Long, endDate: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    suspend fun getOrderByInvoice(invoiceNumber: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE syncStatus = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingSyncOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("""
        UPDATE orders 
        SET status = 'VOID', voidReason = :reason, voidApprovedBy = :approvedBy
        WHERE id = :orderId
    """)
    suspend fun voidOrder(orderId: String, reason: String, approvedBy: String)

    @Query("UPDATE orders SET syncStatus = :status WHERE id = :orderId")
    suspend fun updateSyncStatus(orderId: String, status: String)

    // Order Items
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: String): List<OrderItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    // Count today's orders for invoice number generation
    @Query("SELECT COUNT(*) FROM orders WHERE createdAt >= :startOfDay")
    suspend fun countOrdersSince(startOfDay: Long): Int

    // Financial aggregations
    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0.0) FROM orders 
        WHERE status = 'COMPLETED' 
        AND createdAt BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalRevenue(startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(totalCost), 0.0) FROM orders 
        WHERE status = 'COMPLETED' 
        AND createdAt BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalCost(startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COUNT(*) FROM orders 
        WHERE status = 'COMPLETED' 
        AND createdAt BETWEEN :startDate AND :endDate
    """)
    suspend fun getTransactionCount(startDate: Long, endDate: Long): Int

    // Draft orders
    @Query("SELECT * FROM draft_orders ORDER BY createdAt DESC")
    fun getAllDraftOrders(): Flow<List<DraftOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraftOrder(draft: DraftOrderEntity)

    @Query("DELETE FROM draft_orders WHERE id = :draftId")
    suspend fun deleteDraftOrder(draftId: String)
}
