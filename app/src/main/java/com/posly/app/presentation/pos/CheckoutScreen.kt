package com.posly.app.presentation.pos

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
import com.posly.app.domain.model.PaymentMethod
import com.posly.app.presentation.pos.components.formatPrice
import com.posly.app.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel = hiltViewModel(),
    onPaymentSuccess: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var paidAmount by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is CheckoutState.Success) {
            onPaymentSuccess((uiState as CheckoutState.Success).orderId)
        }
    }

    val cart = viewModel.cart.collectAsState().value
    val totalAmount = cart.totalAmount

    val quickAmounts = listOf(10_000.0, 20_000.0, 50_000.0, 100_000.0, 200_000.0)
    val paidAmountDouble = paidAmount.toDoubleOrNull() ?: 0.0
    val change = (paidAmountDouble - totalAmount).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proses Pembayaran", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Background).padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Order Summary ─────────────────────────────────────────────────
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, BorderDivider)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ringkasan Pesanan", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        cart.items.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${item.product.name} x${item.quantity}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text("Rp ${formatPrice(item.subtotal)}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = BorderDivider)
                        if (cart.globalDiscountAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Diskon", color = SuccessGreen)
                                Text("-Rp ${formatPrice(cart.globalDiscountAmount)}", color = SuccessGreen)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Rp ${formatPrice(totalAmount)}", style = MaterialTheme.typography.titleLarge.copy(color = Primary, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // ── Payment Method ────────────────────────────────────────────────
            item {
                Text("Metode Pembayaran", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PaymentMethodButton("Tunai", Icons.Default.Money, selectedMethod == PaymentMethod.CASH) { selectedMethod = PaymentMethod.CASH }
                    PaymentMethodButton("QRIS", Icons.Default.QrCode, selectedMethod == PaymentMethod.QRIS) { selectedMethod = PaymentMethod.QRIS }
                }
            }

            // ── Cash Payment ──────────────────────────────────────────────────
            if (selectedMethod == PaymentMethod.CASH) {
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, BorderDivider)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = paidAmount,
                                onValueChange = { paidAmount = it.filter { c -> c.isDigit() } },
                                label = { Text("Jumlah Dibayar") },
                                prefix = { Text("Rp ") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, focusedLabelColor = Primary)
                            )
                            // Quick amount buttons
                            Text("Uang pas:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                quickAmounts.take(4).forEach { amount ->
                                    val amountNeeded = Math.ceil(totalAmount / amount) * amount
                                    OutlinedButton(
                                        onClick = { paidAmount = amountNeeded.toLong().toString() },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, BorderDivider),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("${(amount / 1000).toInt()}rb", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                            // Change
                            if (paidAmountDouble >= totalAmount) {
                                Surface(shape = RoundedCornerShape(10.dp), color = SuccessGreenLight) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Kembalian", style = MaterialTheme.typography.titleMedium, color = Color(0xFF064E3B))
                                        Text("Rp ${formatPrice(change)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SuccessGreen))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Error ─────────────────────────────────────────────────────────
            if (uiState is CheckoutState.Error) {
                item {
                    Text((uiState as CheckoutState.Error).message, color = ErrorRed, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // ── Pay button ────────────────────────────────────────────────────
            item {
                val canPay = when (selectedMethod) {
                    PaymentMethod.CASH -> paidAmountDouble >= totalAmount
                    PaymentMethod.QRIS -> true
                }
                Button(
                    onClick = { viewModel.processPayment(selectedMethod, paidAmountDouble.takeIf { it > 0 } ?: totalAmount) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = canPay && uiState !is CheckoutState.Loading
                ) {
                    if (uiState is CheckoutState.Loading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Konfirmasi Pembayaran", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PaymentMethodButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick, shape = RoundedCornerShape(12.dp),
        color = if (selected) PrimaryContainer else Color.White,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Primary else BorderDivider),
        modifier = Modifier.height(60.dp).width(120.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (selected) Primary else TextSecondary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.titleSmall.copy(color = if (selected) Primary else TextSecondary, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal))
        }
    }
}
