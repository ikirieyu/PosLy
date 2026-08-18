package com.posly.app.presentation.pos.tablet

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.posly.app.domain.model.Cart
import com.posly.app.domain.model.Category
import com.posly.app.domain.model.Product
import com.posly.app.presentation.pos.PosUiState
import com.posly.app.presentation.pos.components.CartPanel
import com.posly.app.presentation.pos.components.ProductCard
import com.posly.app.presentation.pos.components.SearchBar
import com.posly.app.presentation.ui.theme.*

/**
 * Tablet split-pane POS layout:
 * Left 60%: Category tabs + 4-5 column product grid
 * Right 40%: Permanent cart panel + payment calculator
 */
@Composable
fun TabletPosLayout(
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
    onBarcodeScanned: (String) -> Unit,
    onManageProducts: () -> Unit
) {
    var showScanner by remember { mutableStateOf(false) }

    if (showScanner) {
        com.posly.app.presentation.components.BarcodeScannerDialog(
            onBarcodeScanned = { barcode ->
                onBarcodeScanned(barcode)
                showScanner = false
            },
            onDismiss = { showScanner = false }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // ── Left pane: Catalog (60%) ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .background(Background)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchChange,
                    onBarcodeClick = { showScanner = true },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onManageProducts) {
                    Icon(Icons.Default.Inventory2, contentDescription = "Manajemen Produk", tint = TextSecondary)
                }
            }

            // Category chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                item {
                    CategoryChip(
                        label = "Semua",
                        selected = uiState.selectedCategoryId == null,
                        onClick = { onSelectCategory(null) }
                    )
                }
                items(uiState.categories) { category ->
                    CategoryChip(
                        label = category.name,
                        selected = uiState.selectedCategoryId == category.id,
                        onClick = { onSelectCategory(category.id) }
                    )
                }
            }

            Divider(color = BorderDivider, modifier = Modifier.padding(vertical = 8.dp))

            // Product grid (4 columns on tablet)
            if (uiState.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                        Text("Produk tidak ditemukan", color = TextSecondary)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onClick = { if (!product.isOutOfStock) onAddToCart(product) }
                        )
                    }
                }
            }
        }

        // Vertical divider
        Divider(
            modifier = Modifier.fillMaxHeight().width(1.dp),
            color = BorderDivider
        )

        // ── Right pane: Cart (40%) ────────────────────────────────────────────
        CartPanel(
            cart = uiState.cart,
            draftOrders = uiState.draftOrders,
            modifier = Modifier.weight(0.4f).fillMaxHeight(),
            onRemoveItem = onRemoveFromCart,
            onUpdateQuantity = onUpdateQuantity,
            onUpdateNote = onUpdateNote,
            onSetItemDiscount = onSetItemDiscount,
            onSetGlobalDiscount = onSetGlobalDiscount,
            onHoldOrder = onHoldOrder,
            onResumeOrder = onResumeOrder,
            onClearCart = onClearCart,
            onCheckout = onCheckout
        )
    }
}

@Composable
fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PrimaryContainer,
            selectedLabelColor = Primary,
            containerColor = SurfaceCard,
            labelColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = Primary.copy(alpha = 0.3f),
            borderColor = BorderDivider
        )
    )
}
