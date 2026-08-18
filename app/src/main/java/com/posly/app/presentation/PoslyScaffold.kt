package com.posly.app.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.posly.app.domain.model.UserRole
import com.posly.app.presentation.navigation.PoslyRoute
import com.posly.app.presentation.ui.theme.Primary

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val ownerOnly: Boolean = false
)

private val navItems = listOf(
    NavItem("Kasir", Icons.Default.PointOfSale, PoslyRoute.Pos.route),
    NavItem("Riwayat", Icons.Default.Receipt, PoslyRoute.Orders.route),
    NavItem("Laporan", Icons.Default.BarChart, PoslyRoute.Finance.route, ownerOnly = true),
    NavItem("Pengaturan", Icons.Default.Settings, PoslyRoute.Settings.route),
)

@Composable
fun PoslyScaffold(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass,
    currentRole: UserRole?,
    content: @Composable () -> Unit
) {
    val isTablet = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine which screens should show nav
    val showNav = currentRoute !in listOf(
        PoslyRoute.Splash.route,
        PoslyRoute.Login.route,
        PoslyRoute.Register.route,
        PoslyRoute.Checkout.route,
        PoslyRoute.PaymentSuccess.route,
    )

    val visibleItems = navItems.filter { item ->
        !item.ownerOnly || currentRole == UserRole.OWNER
    }

    if (isTablet && showNav) {
        // Tablet: Navigation Rail
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                visibleItems.forEach { item ->
                    NavigationRailItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
            content()
        }
    } else if (showNav) {
        // Mobile: Bottom Navigation Bar
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    visibleItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Surface(modifier = Modifier.padding(paddingValues)) {
                content()
            }
        }
    } else {
        content()
    }
}
