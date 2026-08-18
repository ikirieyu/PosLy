package com.posly.app.presentation.products

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
import com.posly.app.domain.model.Product
import com.posly.app.presentation.pos.components.formatPrice
import com.posly.app.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ProductsViewModel = hiltViewModel(),
    onProductClick: (String) -> Unit,
    onAddProduct: () -> Unit,
    onBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Produk", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                containerColor = Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Produk")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Background).padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(products, key = { it.id }) { product ->
                ProductListItem(product = product, onClick = { onProductClick(product.id) })
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun ProductListItem(product: Product, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderDivider),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Product icon
            Box(modifier = Modifier.size(48.dp).background(PrimaryContainer, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Text(product.name.take(1), style = MaterialTheme.typography.titleLarge.copy(color = Primary, fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text("SKU: ${product.sku.ifBlank { "-" }}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Stock indicator
                    val stockColor = when {
                        product.isOutOfStock -> ErrorRed
                        product.isLowStock -> WarningAmber
                        else -> SuccessGreen
                    }
                    Text("Stok: ${product.stock}", style = MaterialTheme.typography.bodySmall, color = stockColor, fontWeight = FontWeight.Medium)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Rp ${formatPrice(product.sellingPrice)}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary))
                Text("Modal: Rp ${formatPrice(product.costPrice)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("+${String.format("%.1f", product.marginPercent)}%", style = MaterialTheme.typography.labelSmall, color = SuccessGreen)
            }
        }
    }
}

// ── ProductDetailScreen ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    viewModel: ProductsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val isNew = productId == "new"
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("5") }

    val marginPercent = remember(costPrice, sellingPrice) {
        val c = costPrice.toDoubleOrNull() ?: 0.0
        val s = sellingPrice.toDoubleOrNull() ?: 0.0
        if (s > 0) ((s - c) / s * 100) else 0.0
    }

    LaunchedEffect(productId) {
        if (!isNew) viewModel.loadProduct(productId) { p ->
            name = p.name; sku = p.sku; costPrice = p.costPrice.toString()
            sellingPrice = p.sellingPrice.toString(); stock = p.stock.toString(); minStock = p.minStockAlert.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Tambah Produk" else "Edit Produk") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Background).padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                fun field(label: String, value: String, onValueChange: (String) -> Unit, suffix: String? = null) {
                    OutlinedTextField(
                        value = value, onValueChange = onValueChange,
                        label = { Text(label) },
                        suffix = suffix?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, focusedLabelColor = Primary)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    field("Nama Produk *", name, { name = it })
                    field("SKU", sku, { sku = it })
                    field("Harga Modal (HPP)", costPrice, { costPrice = it }, "Rp")
                    field("Harga Jual", sellingPrice, { sellingPrice = it }, "Rp")

                    // Margin preview
                    if (marginPercent > 0) {
                        Surface(shape = RoundedCornerShape(10.dp), color = LabaHijauBg) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Margin Keuntungan", color = LabaHijau)
                                Text("${String.format("%.1f", marginPercent)}%", color = LabaHijau, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    field("Stok Awal", stock, { stock = it })
                    field("Minimum Stok Alert", minStock, { minStock = it })

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.saveProduct(
                                id = if (isNew) null else productId,
                                name = name, sku = sku,
                                costPrice = costPrice.toDoubleOrNull() ?: 0.0,
                                sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                                stock = stock.toIntOrNull() ?: 0,
                                minStock = minStock.toIntOrNull() ?: 5,
                                onSuccess = onBack
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = name.isNotBlank()
                    ) {
                        Text(if (isNew) "Simpan Produk" else "Update Produk", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
