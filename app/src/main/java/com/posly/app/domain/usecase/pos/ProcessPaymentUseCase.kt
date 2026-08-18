package com.posly.app.domain.usecase.pos

import com.posly.app.domain.model.Cart
import com.posly.app.domain.model.CartItem
import com.posly.app.domain.model.Order
import com.posly.app.domain.model.OrderItem
import com.posly.app.domain.model.OrderStatus
import com.posly.app.domain.model.PaymentMethod
import com.posly.app.domain.model.SyncStatus
import com.posly.app.domain.repository.AuthRepository
import com.posly.app.domain.repository.OrderRepository
import com.posly.app.domain.repository.ProductRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Use case: process payment and finalize a cart into a completed order.
 * Decrements stock for each item sold.
 */
class ProcessPaymentUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        cart: Cart,
        paymentMethod: PaymentMethod,
        paidAmount: Double
    ): Result<Order> = runCatching {
        if (cart.isEmpty) error("Cart is empty")
        if (paidAmount < cart.totalAmount) error("Paid amount insufficient")

        val cashierId = authRepository.currentProfile.let { flow ->
            // Collect current value synchronously
            var id = ""
            kotlinx.coroutines.flow.first(flow)?.let { id = it.id }
            id
        }

        val invoiceNumber = orderRepository.generateInvoiceNumber()
        val changeAmount = paidAmount - cart.totalAmount

        val orderItems = cart.items.map { cartItem ->
            OrderItem(
                id = UUID.randomUUID().toString(),
                orderId = "",  // will be set after order creation
                productId = cartItem.product.id,
                productName = cartItem.product.name,
                quantity = cartItem.quantity,
                unitCost = cartItem.product.costPrice,
                unitPrice = cartItem.product.sellingPrice,
                discountPerItem = cartItem.discountNominal + (cartItem.product.sellingPrice * cartItem.discountPercent / 100),
                subtotal = cartItem.subtotal,
                note = cartItem.note
            )
        }

        val order = Order(
            invoiceNumber = invoiceNumber,
            cashierId = cashierId,
            items = orderItems,
            totalAmount = cart.totalAmount,
            totalCost = cart.totalCost,
            discountAmount = cart.globalDiscountAmount,
            paidAmount = paidAmount,
            changeAmount = changeAmount,
            paymentMethod = paymentMethod,
            status = OrderStatus.COMPLETED,
            syncStatus = SyncStatus.PENDING
        )

        orderRepository.insertOrder(order).getOrThrow()

        // Decrement stock for each item
        cart.items.forEach { cartItem ->
            productRepository.decrementStock(
                productId = cartItem.product.id,
                quantity = cartItem.quantity
            )
        }

        order
    }
}
