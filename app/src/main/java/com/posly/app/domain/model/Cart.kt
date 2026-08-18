package com.posly.app.domain.model

import java.util.UUID

/**
 * Represents a single item in the shopping cart (active POS session).
 */
data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val note: String = "",
    /** Discount per item — either percent or nominal amount */
    val discountPercent: Double = 0.0,
    val discountNominal: Double = 0.0
) {
    val subtotal: Double
        get() {
            val basePrice = product.sellingPrice * quantity
            val discountAmount = when {
                discountPercent > 0 -> basePrice * (discountPercent / 100)
                discountNominal > 0 -> discountNominal
                else -> 0.0
            }
            return (basePrice - discountAmount).coerceAtLeast(0.0)
        }

    val totalCost: Double get() = product.costPrice * quantity
}

/**
 * Active POS cart state.
 */
data class Cart(
    val id: String = UUID.randomUUID().toString(),
    val items: List<CartItem> = emptyList(),
    /** Global discount applied to cart total */
    val globalDiscountPercent: Double = 0.0,
    val globalDiscountNominal: Double = 0.0,
    val customerNote: String = "",
    val isDraft: Boolean = false,
    val draftLabel: String = ""
) {
    val subtotalBeforeDiscount: Double get() = items.sumOf { it.subtotal }

    val globalDiscountAmount: Double
        get() {
            return when {
                globalDiscountPercent > 0 -> subtotalBeforeDiscount * (globalDiscountPercent / 100)
                globalDiscountNominal > 0 -> globalDiscountNominal
                else -> 0.0
            }
        }

    val totalAmount: Double
        get() = (subtotalBeforeDiscount - globalDiscountAmount).coerceAtLeast(0.0)

    val totalCost: Double get() = items.sumOf { it.totalCost }

    val totalItems: Int get() = items.sumOf { it.quantity }

    val isEmpty: Boolean get() = items.isEmpty()
}

/**
 * Payment method enum.
 */
enum class PaymentMethod {
    CASH, QRIS
}
