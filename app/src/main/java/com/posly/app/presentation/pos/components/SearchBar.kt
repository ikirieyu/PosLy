package com.posly.app.presentation.pos.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.posly.app.presentation.ui.theme.BorderDivider
import com.posly.app.presentation.ui.theme.Primary
import com.posly.app.presentation.ui.theme.SurfaceCard
import com.posly.app.presentation.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBarcodeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Cari produk atau scan barcode...", color = TextSecondary) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Hapus pencarian", tint = TextSecondary)
                }
            } else {
                IconButton(onClick = onBarcodeClick) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan barcode", tint = Primary)
                }
            }
        },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = BorderDivider,
            unfocusedContainerColor = SurfaceCard,
            focusedContainerColor = SurfaceCard
        ),
        singleLine = true
    )
}
