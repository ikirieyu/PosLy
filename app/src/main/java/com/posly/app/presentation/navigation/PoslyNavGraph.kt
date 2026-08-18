package com.posly.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.posly.app.domain.model.UserRole
import com.posly.app.presentation.auth.LoginScreen
import com.posly.app.presentation.auth.RegisterScreen
import com.posly.app.presentation.auth.SplashScreen
import com.posly.app.presentation.finance.FinanceScreen
import com.posly.app.presentation.orders.OrderDetailScreen
import com.posly.app.presentation.orders.OrdersScreen
import com.posly.app.presentation.pos.CheckoutScreen
import com.posly.app.presentation.pos.PaymentSuccessScreen
import com.posly.app.presentation.pos.PosScreen
import com.posly.app.presentation.products.ProductDetailScreen
import com.posly.app.presentation.products.ProductsScreen
import com.posly.app.presentation.settings.SettingsScreen
import com.posly.app.presentation.settings.WorkersScreen

@Composable
fun PoslyNavGraph(
    navController: NavHostController,
    currentRole: UserRole?
) {
    NavHost(
        navController = navController,
        startDestination = PoslyRoute.Splash.route
    ) {
        // ── Auth ──────────────────────────────────────────────────────────────
        composable(PoslyRoute.Splash.route) {
            SplashScreen(
                onNavigateToLogin = { navController.navigate(PoslyRoute.Login.route) {
                    popUpTo(PoslyRoute.Splash.route) { inclusive = true }
                }},
                onNavigateToPos = { navController.navigate(PoslyRoute.Pos.route) {
                    popUpTo(PoslyRoute.Splash.route) { inclusive = true }
                }},
                onNavigateToRegister = { navController.navigate(PoslyRoute.Register.route) {
                    popUpTo(PoslyRoute.Splash.route) { inclusive = true }
                }}
            )
        }

        composable(PoslyRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val dest = if (role == UserRole.OWNER) PoslyRoute.Pos.route else PoslyRoute.Pos.route
                    navController.navigate(dest) {
                        popUpTo(PoslyRoute.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(PoslyRoute.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(PoslyRoute.Pos.route) {
                        popUpTo(PoslyRoute.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // ── POS ───────────────────────────────────────────────────────────────
        composable(PoslyRoute.Pos.route) {
            PosScreen(
                onNavigateToCheckout = { navController.navigate(PoslyRoute.Checkout.route) },
                onNavigateToProducts = { navController.navigate(PoslyRoute.Products.route) }
            )
        }

        composable(PoslyRoute.Checkout.route) {
            CheckoutScreen(
                onPaymentSuccess = { orderId ->
                    navController.navigate(PoslyRoute.PaymentSuccess.create(orderId)) {
                        popUpTo(PoslyRoute.Checkout.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(PoslyRoute.PaymentSuccess.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            PaymentSuccessScreen(
                orderId = orderId,
                onDone = {
                    navController.navigate(PoslyRoute.Pos.route) {
                        popUpTo(PoslyRoute.Pos.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Orders ────────────────────────────────────────────────────────────
        composable(PoslyRoute.Orders.route) {
            OrdersScreen(
                onOrderClick = { orderId ->
                    navController.navigate(PoslyRoute.OrderDetail.create(orderId))
                }
            )
        }

        composable(PoslyRoute.OrderDetail.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailScreen(
                orderId = orderId,
                onBack = { navController.popBackStack() }
            )
        }

        // ── Finance ───────────────────────────────────────────────────────────
        composable(PoslyRoute.Finance.route) {
            FinanceScreen(
                onAddExpense = { navController.navigate(PoslyRoute.AddExpense.route) }
            )
        }

        // ── Products ──────────────────────────────────────────────────────────
        composable(PoslyRoute.Products.route) {
            ProductsScreen(
                onProductClick = { productId ->
                    navController.navigate(PoslyRoute.ProductDetail.create(productId))
                },
                onAddProduct = {
                    navController.navigate(PoslyRoute.ProductDetail.create())
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(PoslyRoute.ProductDetail.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: "new"
            ProductDetailScreen(
                productId = productId,
                onBack = { navController.popBackStack() }
            )
        }

        // ── Settings ──────────────────────────────────────────────────────────
        composable(PoslyRoute.Settings.route) {
            SettingsScreen(
                onNavigateToWorkers = { navController.navigate(PoslyRoute.Workers.route) },
                onNavigateToProducts = { navController.navigate(PoslyRoute.Products.route) },
                onLogout = {
                    navController.navigate(PoslyRoute.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(PoslyRoute.Workers.route) {
            WorkersScreen(onBack = { navController.popBackStack() })
        }
    }
}
