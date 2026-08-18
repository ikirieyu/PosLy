package com.posly.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.posly.app.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToWorkers: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onLogout: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Background).padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // ── Store info ────────────────────────────────────────────────────
            item { SettingsSectionHeader("Informasi Usaha") }
            item {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, BorderDivider)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingsTextField("Nama Toko", settings.storeName) { viewModel.updateStoreName(it) }
                        SettingsTextField("Slogan", settings.slogan) { viewModel.updateSlogan(it) }
                        SettingsTextField("Alamat", settings.address) { viewModel.updateAddress(it) }
                        SettingsTextField("Nomor WhatsApp / Telepon", settings.phone) { viewModel.updatePhone(it) }
                        SettingsTextField("Footer Struk", settings.receiptFooter) { viewModel.updateReceiptFooter(it) }
                        Button(onClick = { viewModel.saveSettings() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(10.dp)) {
                            Text("Simpan Pengaturan Toko", color = Color.White)
                        }
                    }
                }
            }

            // ── Supabase connection ───────────────────────────────────────────
            item { SettingsSectionHeader("Koneksi Database Supabase") }
            item {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, BorderDivider)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        var supabaseUrl by remember { mutableStateOf(settings.supabaseUrl) }
                        var supabaseKey by remember { mutableStateOf(settings.supabaseAnonKey) }
                        SettingsTextField("Supabase URL", supabaseUrl) { supabaseUrl = it }
                        SettingsTextField("Anon Key", supabaseKey) { supabaseKey = it }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { viewModel.testConnection(supabaseUrl, supabaseKey) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                                Text("Test Koneksi")
                            }
                            Button(onClick = { viewModel.saveSupabaseConfig(supabaseUrl, supabaseKey) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(10.dp)) {
                                Text("Simpan", color = Color.White)
                            }
                        }
                    }
                }
            }

            // ── Owner-only menu ───────────────────────────────────────────────
            if (currentRole == com.posly.app.domain.model.UserRole.OWNER) {
                item { SettingsSectionHeader("Manajemen") }
                item {
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, BorderDivider)) {
                        Column {
                            SettingsMenuItem("Manajemen Produk", Icons.Default.Inventory2, onNavigateToProducts)
                            Divider(color = BorderDivider)
                            SettingsMenuItem("Manajemen Kasir", Icons.Default.People, onNavigateToWorkers)
                        }
                    }
                }

                // Budget allocation
                item { SettingsSectionHeader("Alokasi Dana (%)") }
                item {
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, BorderDivider)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Total harus = 100%", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            AllocationSlider("Bahan Baku", settings.restockPercent.toFloat(), PosBahanBaku) { viewModel.updateRestockPercent(it.toDouble()) }
                            AllocationSlider("Tabungan Bisnis", settings.savingsPercent.toFloat(), PosTabungan) { viewModel.updateSavingsPercent(it.toDouble()) }
                            AllocationSlider("Dana Darurat", settings.emergencyPercent.toFloat(), PosDarurat) { viewModel.updateEmergencyPercent(it.toDouble()) }
                            AllocationSlider("Transport", settings.transportPercent.toFloat(), PosTransport) { viewModel.updateTransportPercent(it.toDouble()) }
                        }
                    }
                }
            }

            // ── Logout ────────────────────────────────────────────────────────
            item {
                OutlinedButton(
                    onClick = { viewModel.logout(onLogout) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                    border = BorderStroke(1.dp, ErrorRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Keluar")
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary, fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun SettingsTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, focusedLabelColor = Primary))
}

@Composable
fun SettingsMenuItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = Primary)
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
fun AllocationSlider(label: String, value: Float, color: Color, onValueChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${value.toInt()}%", style = MaterialTheme.typography.bodyMedium.copy(color = color, fontWeight = FontWeight.SemiBold))
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = 0f..100f, steps = 19, colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color))
    }
}

// ── WorkersScreen ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkersScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val workers by viewModel.workers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Manajemen Kasir") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background))
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* show add worker dialog */ }, containerColor = Primary, contentColor = Color.White) {
                Icon(Icons.Default.PersonAdd, null)
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().background(Background).padding(paddingValues).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(workers, key = { it.id }) { worker ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, BorderDivider)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(40.dp).background(PrimaryContainer, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                            Text(worker.name.take(1), style = MaterialTheme.typography.titleMedium.copy(color = Primary, fontWeight = FontWeight.Bold))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(worker.name, style = MaterialTheme.typography.titleSmall)
                            Text(worker.email, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        IconButton(onClick = { viewModel.deactivateWorker(worker.id) }) {
                            Icon(Icons.Default.Block, contentDescription = "Nonaktifkan", tint = ErrorRed)
                        }
                    }
                }
            }
        }
    }
}
