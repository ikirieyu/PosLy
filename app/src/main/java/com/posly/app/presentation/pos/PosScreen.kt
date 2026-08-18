package com.posly.app.presentation.pos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.posly.app.presentation.pos.tablet.TabletPosLayout
import com.posly.app.presentation.pos.mobile.MobilePosLayout

/**
 * Adaptive POS screen root.
 * - Tablet (≥ 840dp): dual-pane split layout
 * - Mobile (<840dp): single-pane with floating cart bar
 */
@Composable
fun PosScreen(
    viewModel: PosViewModel = hiltViewModel(),
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    onNavigateToCheckout: () -> Unit,
    onNavigateToProducts: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (windowWidthSizeClass >= WindowWidthSizeClass.Expanded) {
        TabletPosLayout(
            uiState = uiState,
            onAddToCart = viewModel::addToCart,
            onRemoveFromCart = viewModel::removeFromCart,
            onUpdateQuantity = viewModel::updateQuantity,
            onUpdateNote = viewModel::updateItemNote,
            onSetItemDiscount = viewModel::setItemDiscount,
            onSetGlobalDiscount = viewModel::setGlobalDiscount,
            onSelectCategory = viewModel::selectCategory,
            onSearchChange = viewModel::updateSearchQuery,
            onHoldOrder = viewModel::holdCurrentOrder,
            onResumeOrder = viewModel::resumeDraftOrder,
            onClearCart = viewModel::clearCart,
            onCheckout = onNavigateToCheckout,
            onBarcodeScanned = viewModel::onBarcodeScanned,
            onManageProducts = onNavigateToProducts
        )
    } else {
        MobilePosLayout(
            uiState = uiState,
            onAddToCart = viewModel::addToCart,
            onRemoveFromCart = viewModel::removeFromCart,
            onUpdateQuantity = viewModel::updateQuantity,
            onUpdateNote = viewModel::updateItemNote,
            onSetItemDiscount = viewModel::setItemDiscount,
            onSetGlobalDiscount = viewModel::setGlobalDiscount,
            onSelectCategory = viewModel::selectCategory,
            onSearchChange = viewModel::updateSearchQuery,
            onHoldOrder = viewModel::holdCurrentOrder,
            onResumeOrder = viewModel::resumeDraftOrder,
            onClearCart = viewModel::clearCart,
            onCheckout = onNavigateToCheckout,
            onBarcodeScanned = viewModel::onBarcodeScanned
        )
    }
}
