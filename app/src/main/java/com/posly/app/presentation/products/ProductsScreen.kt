package com.posly.app.presentation.products

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

@Composable
fun ProductField(label: String, value: String, onValueChange: (String) -> Unit, suffix: String? = null) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, focusedLabelColor = Primary)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    viewModel: ProductsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isNew = productId == "new"
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("5") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var showScanner by remember { mutableStateOf(false) }

    // Launcher Galeri
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { imageUrl = it.toString() }
    }

    // Launcher Kamera Langsung
    var tempCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            imageUrl = tempCameraUri.toString()
        }
    }

    fun launchCamera() {
        val file = java.io.File(context.externalCacheDir, "product_${System.currentTimeMillis()}.jpg")
        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        tempCameraUri = uri
        cameraLauncher.launch(uri)
    }

    val marginPercent = remember(costPrice, sellingPrice) {
        val c = costPrice.toDoubleOrNull() ?: 0.0
        val s = sellingPrice.toDoubleOrNull() ?: 0.0
        if (s > 0) ((s - c) / s * 100) else 0.0
    }

    if (showScanner) {
        com.posly.app.presentation.components.BarcodeScannerDialog(
            onBarcodeScanned = { scannedCode ->
                sku = scannedCode
                showScanner = false
            },
            onDismiss = { showScanner = false }
        )
    }

    LaunchedEffect(productId) {
        if (!isNew) viewModel.loadProduct(productId) { p ->
            name = p.name; sku = p.sku; costPrice = p.costPrice.toString()
            sellingPrice = p.sellingPrice.toString(); stock = p.stock.toString(); minStock = p.minStockAlert.toString()
            imageUrl = p.imageUrl
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
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // ── Foto Produk Card ─────────────────────────────────────────
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, BorderDivider)) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Foto Produk", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = { galleryLauncher.launch("image/*") }, shape = RoundedCornerShape(10.dp)) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Galeri")
                                }
                                Button(onClick = { launchCamera() }, colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(10.dp)) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kamera", color = Color.White)
                                }
                            }
                            if (!imageUrl.isNullOrBlank()) {
                                Text("Foto terpilih!", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                            }
                        }
                    }

                    ProductField("Nama Produk *", name, { name = it })

                    // SKU + Scan Barcode Button
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProductField("SKU / Barcode", sku, { sku = it })
                        }
                        IconButton(
                            onClick = { showScanner = true },
                            modifier = Modifier.background(PrimaryContainer, RoundedCornerShape(10.dp)).size(52.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode Kamera", tint = Primary)
                        }
                    }

                    ProductField("Harga Modal (HPP)", costPrice, { costPrice = it }, "Rp")
                    ProductField("Harga Jual", sellingPrice, { sellingPrice = it }, "Rp")

                    // Margin preview
                    if (marginPercent > 0) {
                        Surface(shape = RoundedCornerShape(10.dp), color = LabaHijauBg) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Margin Keuntungan", color = LabaHijau)
                                Text("${String.format("%.1f", marginPercent)}%", color = LabaHijau, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    ProductField("Stok Awal", stock, { stock = it })
                    ProductField("Minimum Stok Alert", minStock, { minStock = it })

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
