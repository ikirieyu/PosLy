package com.posly.app.presentation.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.posly.app.domain.model.Product
import com.posly.app.presentation.ui.theme.*

/**
 * Product card for the POS catalog grid.
 */
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnavailable = product.isOutOfStock

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isUnavailable, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnavailable) Neutral100 else Color.White
        ),
        border = BorderStroke(1.dp, BorderDivider),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box {
            Column {
                // Product image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(SurfaceCard),
                    contentAlignment = Alignment.Center
                ) {
                    if (product.imageUrl != null) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        )
                    } else {
                        Text(
                            text = product.name.take(1),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = Primary.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Out of stock overlay
                    if (isUnavailable) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Habis", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // Product info
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isUnavailable) TextSecondary else TextPrimary
                    )
                    Text(
                        text = "Rp ${formatPrice(product.sellingPrice)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (isUnavailable) TextSecondary else Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    if (product.isLowStock && !isUnavailable) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(10.dp))
                            Text("Stok: ${product.stock}", style = MaterialTheme.typography.labelSmall, color = WarningAmber)
                        }
                    }
                }
            }

            // Add button
            if (!isUnavailable) {
                SmallFloatingActionButton(
                    onClick = onClick,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp).size(28.dp),
                    containerColor = Primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

fun formatPrice(price: Double): String {
    return String.format("%,.0f", price)
}
