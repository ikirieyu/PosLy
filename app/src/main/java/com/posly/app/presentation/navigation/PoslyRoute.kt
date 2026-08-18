package com.posly.app.presentation.navigation

/**
 * All navigation routes in PosLy.
 */
sealed class PoslyRoute(val route: String) {
    // Auth
    object Splash : PoslyRoute("splash")
    object Login : PoslyRoute("login")
    object Register : PoslyRoute("register")

    // Main bottom-nav destinations
    object Pos : PoslyRoute("pos")
    object Orders : PoslyRoute("orders")
    object Finance : PoslyRoute("finance")
    object Settings : PoslyRoute("settings")

    // Owner-only
    object Products : PoslyRoute("products")
    object ProductDetail : PoslyRoute("product/{productId}") {
        fun create(productId: String = "new") = "product/$productId"
    }
    object Workers : PoslyRoute("workers")

    // POS sub-screens
    object Checkout : PoslyRoute("checkout")
    object PaymentSuccess : PoslyRoute("payment_success/{orderId}") {
        fun create(orderId: String) = "payment_success/$orderId"
    }
    object OrderDetail : PoslyRoute("order_detail/{orderId}") {
        fun create(orderId: String) = "order_detail/$orderId"
    }

    // Expense entry
    object AddExpense : PoslyRoute("add_expense")
}
