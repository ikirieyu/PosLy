package com.posly.app.presentation.finance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.posly.app.domain.model.FinancialReport
import com.posly.app.domain.model.ReportPeriod
import com.posly.app.presentation.pos.components.formatPrice
import com.posly.app.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: FinanceViewModel = hiltViewModel(),
    onAddExpense: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Finansial", style = MaterialTheme.typography.headlineSmall) },
                actions = {
                    IconButton(onClick = { viewModel.exportExcel() }) {
                        Icon(Icons.Default.Download, contentDescription = "Ekspor Excel", tint = Primary)
                    }
                    IconButton(onClick = onAddExpense) {
                        Icon(Icons.Default.Add, contentDescription = "Catat Beban")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Background).padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Period filter ─────────────────────────────────────────────────
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    items(ReportPeriod.values().toList()) { period ->
                        FilterChip(
                            selected = uiState.selectedPeriod == period,
                            onClick = { viewModel.selectPeriod(period) },
                            label = { Text(period.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryContainer,
                                selectedLabelColor = Primary
                            )
                        )
                    }
                }
            }

            // ── Main metric cards ─────────────────────────────────────────────
            uiState.report?.let { report ->
                item {
                    // Omzet card
                    FinanceMetricCard(
                        title = "Omzet Penjualan",
                        amount = report.omzet,
                        subtitle = "${report.totalTransactions} transaksi",
                        bgColor = OmzetBlueBg,
                        valueColor = OmzetBlue,
                        icon = Icons.Default.TrendingUp
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FinanceMetricCard(
                            title = "Total HPP",
                            amount = report.totalHpp,
                            bgColor = HppOrangeBg,
                            valueColor = HppOrange,
                            icon = Icons.Default.Inventory,
                            modifier = Modifier.weight(1f)
                        )
                        FinanceMetricCard(
                            title = "Laba Kotor",
                            amount = report.labaKotor,
                            bgColor = LabaHijauBg,
                            valueColor = LabaHijau,
                            icon = Icons.Default.ShowChart,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FinanceMetricCard(
                            title = "Total Beban",
                            amount = report.totalBeban,
                            bgColor = ErrorRedLight,
                            valueColor = ErrorRed,
                            icon = Icons.Default.Receipt,
                            modifier = Modifier.weight(1f)
                        )
                        FinanceMetricCard(
                            title = "Laba Bersih",
                            amount = report.labaBersih,
                            bgColor = LabaBersihGoldBg,
                            valueColor = LabaBersihGold,
                            icon = Icons.Default.Stars,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ── Budget allocation ─────────────────────────────────────────
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, BorderDivider)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Alokasi Dana Bersih", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                            AllocationRow("Bahan Baku", report.alokasi.bahanBaku, PosBahanBaku, PosBahanBakuBg)
                            AllocationRow("Tabungan Bisnis", report.alokasi.tabungan, PosTabungan, PosTabunganBg)
                            AllocationRow("Dana Darurat", report.alokasi.darurat, PosDarurat, PosDaruratBg)
                            AllocationRow("Transport", report.alokasi.transport, PosTransport, PosTransportBg)
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) } }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun FinanceMetricCard(
    title: String,
    amount: Double,
    subtitle: String? = null,
    bgColor: Color,
    valueColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = bgColor), modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, contentDescription = null, tint = valueColor, modifier = Modifier.size(18.dp))
                Text(title, style = MaterialTheme.typography.labelLarge, color = valueColor)
            }
            Text("Rp ${formatPrice(amount)}", style = MaterialTheme.typography.titleLarge.copy(color = valueColor, fontWeight = FontWeight.Bold))
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = valueColor.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun AllocationRow(label: String, amount: Double, color: Color, bgColor: Color) {
    Surface(shape = RoundedCornerShape(10.dp), color = bgColor) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(50)))
                Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            }
            Text("Rp ${formatPrice(amount)}", style = MaterialTheme.typography.titleSmall.copy(color = color, fontWeight = FontWeight.SemiBold))
        }
    }
}

private val ReportPeriod.label get() = when (this) {
    ReportPeriod.DAILY -> "Hari Ini"
    ReportPeriod.WEEKLY -> "Minggu Ini"
    ReportPeriod.MONTHLY -> "Bulan Ini"
    ReportPeriod.YEARLY -> "Tahun Ini"
}
