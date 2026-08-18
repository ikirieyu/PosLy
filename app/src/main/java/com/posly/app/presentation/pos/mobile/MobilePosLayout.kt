package com.posly.app.presentation.pos.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.posly.app.domain.model.Cart
import com.posly.app.domain.model.Product
import com.posly.app.presentation.pos.PosUiState
import com.posly.app.presentation.pos.components.CartPanel
import com.posly.app.presentation.pos.components.ProductCard
import com.posly.app.presentation.pos.components.SearchBar
import com.posly.app.presentation.pos.tablet.CategoryChip
import com.posly.app.presentation.ui.theme.*

/**
 * Mobile POS layout:
 * - Single pane: category chips + 2-column product grid
 * - Floating bottom bar showing cart summary
 * - Cart opens as bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobilePosLayout(
    uiState: PosUiState,
    onAddToCart: (Product) -> Unit,
    onRemoveFromCart: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onUpdateNote: (String, String) -> Unit,
    onSetItemDiscount: (String, Double, Double) -> Unit,
    onSetGlobalDiscount: (Double, Double) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onSearchChange: (String) -> Unit,
    onHoldOrder: (String) -> Unit,
    onResumeOrder: (Cart) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showCartSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchChange,
                onBarcodeClick = { /* launch camera */ },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
            )

            // Category chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                item { CategoryChip("Semua", uiState.selectedCategoryId == null) { onSelectCategory(null) } }
                items(uiState.categories) { category ->
                    CategoryChip(category.name, uiState.selectedCategoryId == category.id) { onSelectCategory(category.id) }
                }
            }

            // Product grid (2 columns on mobile)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.products.filter { it.isActive }, key = { it.id }) { product ->
                    ProductCard(product = product, onClick = { if (!product.isOutOfStock) onAddToCart(product) })
                }
            }
        }

        // Floating cart bar
        if (!uiState.cart.isEmpty) {
            FloatingCartBar(
                cart = uiState.cart,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                onClick = { showCartSheet = true }
            )
        }
    }

    // Cart bottom sheet
    if (showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = sheetState,
            containerColor = Background
        ) {
            CartPanel(
                cart = uiState.cart,
                draftOrders = uiState.draftOrders,
                modifier = Modifier.fillMaxWidth().heightIn(min = 400.dp, max = 600.dp),
                onRemoveItem = onRemoveFromCart,
                onUpdateQuantity = onUpdateQuantity,
                onUpdateNote = onUpdateNote,
                onSetItemDiscount = onSetItemDiscount,
                onSetGlobalDiscount = onSetGlobalDiscount,
                onHoldOrder = onHoldOrder,
                onResumeOrder = onResumeOrder,
                onClearCart = { onClearCart(); showCartSheet = false },
                onCheckout = { showCartSheet = false; onCheckout() }
            )
        }
    }
}

@Composable
fun FloatingCartBar(cart: Cart, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Primary,
        shadowElevation = 8.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Badge(containerColor = Color.White.copy(alpha = 0.25f)) {
                    Text("${cart.totalItems}", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
                Text("Lihat Keranjang", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                "Rp ${com.posly.app.presentation.pos.components.formatPrice(cart.totalAmount)}",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
