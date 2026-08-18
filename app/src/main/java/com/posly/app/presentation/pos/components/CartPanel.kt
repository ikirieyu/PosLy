package com.posly.app.presentation.pos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.posly.app.domain.model.CartItem
import com.posly.app.presentation.ui.theme.*

/**
 * Permanent right-side cart panel for tablet, or bottom sheet for mobile.
 */
@Composable
fun CartPanel(
    cart: Cart,
    draftOrders: List<Cart> = emptyList(),
    modifier: Modifier = Modifier,
    onRemoveItem: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onUpdateNote: (String, String) -> Unit,
    onSetItemDiscount: (String, Double, Double) -> Unit,
    onSetGlobalDiscount: (Double, Double) -> Unit,
    onHoldOrder: (String) -> Unit,
    onResumeOrder: (Cart) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit
) {
    var showDrafts by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(SurfaceCard)
            .padding(16.dp)
    ) {
        // ── Cart Header ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Primary)
                Text(
                    "Keranjang",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                if (cart.totalItems > 0) {
                    Badge(containerColor = Primary) {
                        Text("${cart.totalItems}", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Row {
                // Hold order button
                if (!cart.isEmpty) {
                    IconButton(onClick = { onHoldOrder("Draft ${draftOrders.size + 1}") }) {
                        Icon(Icons.Default.Pause, contentDescription = "Tahan Order", tint = TextSecondary)
                    }
                }
                // Draft orders button
                if (draftOrders.isNotEmpty()) {
                    IconButton(onClick = { showDrafts = !showDrafts }) {
                        BadgedBox(badge = { Badge(containerColor = WarningAmber) { Text("${draftOrders.size}") } }) {
                            Icon(Icons.Default.Folder, contentDescription = "Draft Order", tint = TextSecondary)
                        }
                    }
                }
                // Clear cart
                if (!cart.isEmpty) {
                    IconButton(onClick = onClearCart) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus Keranjang", tint = ErrorRed.copy(alpha = 0.7f))
                    }
                }
            }
        }

        Divider(color = BorderDivider, modifier = Modifier.padding(vertical = 8.dp))

        // ── Draft Orders ──────────────────────────────────────────────────────
        if (showDrafts && draftOrders.isNotEmpty()) {
            Text("Order Tersimpan", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            LazyColumn(modifier = Modifier.heightIn(max = 160.dp)) {
                items(draftOrders) { draft ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(draft.draftLabel.ifBlank { "Draft" }, style = MaterialTheme.typography.bodyMedium)
                            Text("${draft.totalItems} item • Rp ${formatPrice(draft.totalAmount)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        TextButton(onClick = { onResumeOrder(draft); showDrafts = false }) {
                            Text("Lanjutkan", color = Primary)
                        }
                    }
                }
            }
            Divider(color = BorderDivider, modifier = Modifier.padding(vertical = 4.dp))
        }

        // ── Cart Items ────────────────────────────────────────────────────────
        if (cart.isEmpty) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null, tint = BorderDivider, modifier = Modifier.size(56.dp))
                    Text("Keranjang kosong", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("Ketuk produk untuk menambahkan", color = TextSecondary.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cart.items, key = { it.product.id }) { item ->
                    CartItemRow(
                        item = item,
                        onRemove = { onRemoveItem(item.product.id) },
                        onDecrement = { onUpdateQuantity(item.product.id, item.quantity - 1) },
                        onIncrement = { onUpdateQuantity(item.product.id, item.quantity + 1) }
                    )
                }
            }
        }

        // ── Summary & Checkout ────────────────────────────────────────────────
        if (!cart.isEmpty) {
            Divider(color = BorderDivider, modifier = Modifier.padding(vertical = 8.dp))

            // Subtotal
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Text("Rp ${formatPrice(cart.subtotalBeforeDiscount)}", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            }

            // Discount
            if (cart.globalDiscountAmount > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Diskon", color = SuccessGreen, style = MaterialTheme.typography.bodyMedium)
                    Text("-Rp ${formatPrice(cart.globalDiscountAmount)}", color = SuccessGreen, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("TOTAL", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(
                    "Rp ${formatPrice(cart.totalAmount)}",
                    style = MaterialTheme.typography.headlineSmall.copy(color = Primary, fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Checkout button
            Button(
                onClick = onCheckout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Proses Bayar", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold))
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onRemove: () -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderDivider)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Text("Rp ${formatPrice(item.product.sellingPrice)} / item", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                if (item.note.isNotBlank()) {
                    Text("📝 ${item.note}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            // Quantity stepper
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onDecrement, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
                }
                Text(
                    "${item.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.widthIn(min = 24.dp)
                )
                IconButton(onClick = onIncrement, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
                }
            }

            // Subtotal
            Column(horizontalAlignment = Alignment.End) {
                Text("Rp ${formatPrice(item.subtotal)}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary))
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Hapus", modifier = Modifier.size(14.dp), tint = ErrorRed.copy(alpha = 0.6f))
                }
            }
        }
    }
}
