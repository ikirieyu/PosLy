package com.posly.app.presentation.orders

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.posly.app.domain.model.Order
import com.posly.app.domain.model.OrderStatus
import com.posly.app.presentation.pos.components.formatPrice
import com.posly.app.presentation.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel = hiltViewModel(),
    onOrderClick: (String) -> Unit
) {
    val orders by viewModel.orders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Transaksi", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { paddingValues ->
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = BorderDivider, modifier = Modifier.size(64.dp))
                    Text("Belum ada transaksi", color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Background).padding(paddingValues).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(orders, key = { it.id }) { order ->
                    OrderCard(order = order, onClick = { onOrderClick(order.id) })
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, onClick: () -> Unit) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val isVoid = order.status == OrderStatus.VOID

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isVoid) SurfaceCard else Color.White),
        border = BorderStroke(1.dp, if (isVoid) ErrorRed.copy(alpha = 0.3f) else BorderDivider),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Left icon
            Box(modifier = Modifier.size(44.dp).background(if (isVoid) ErrorRedLight else PrimaryContainer, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(if (isVoid) Icons.Default.Cancel else Icons.Default.Receipt, contentDescription = null, tint = if (isVoid) ErrorRed else Primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(order.invoiceNumber, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(dateFormatter.format(Date(order.createdAt)), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(order.cashierName, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            // Amount + status
            Column(horizontalAlignment = Alignment.End) {
                Text("Rp ${formatPrice(order.totalAmount)}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = if (isVoid) TextSecondary else TextPrimary))
                Surface(shape = RoundedCornerShape(6.dp), color = if (isVoid) ErrorRedLight else SuccessGreenLight) {
                    Text(
                        if (isVoid) "VOID" else order.paymentMethod.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(color = if (isVoid) ErrorRed else SuccessGreen)
                    )
                }
            }
        }
    }
}

// ── OrderDetailScreen stub ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    viewModel: OrdersViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val order by viewModel.getOrderById(orderId).collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Transaksi") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { paddingValues ->
        order?.let { o ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Background).padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, BorderDivider)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Invoice: ${o.invoiceNumber}", style = MaterialTheme.typography.titleLarge)
                            Text("Kasir: ${o.cashierName}", color = TextSecondary)
                            Text("Metode: ${o.paymentMethod}", color = TextSecondary)
                            Divider(color = BorderDivider)
                            o.items.forEach { item ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${item.productName} x${item.quantity}")
                                    Text("Rp ${formatPrice(item.subtotal)}")
                                }
                            }
                            Divider(color = BorderDivider)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("TOTAL", fontWeight = FontWeight.Bold)
                                Text("Rp ${formatPrice(o.totalAmount)}", fontWeight = FontWeight.Bold, color = Primary)
                            }
                        }
                    }
                }

                // Void button (if completed)
                if (o.status == OrderStatus.COMPLETED) {
                    item {
                        OutlinedButton(
                            onClick = { /* show void dialog */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            border = BorderStroke(1.dp, ErrorRed)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Batalkan Transaksi (Void)")
                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
    }
}
