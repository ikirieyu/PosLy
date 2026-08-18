package com.posly.app.presentation.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.posly.app.presentation.ui.theme.*

@Composable
fun PaymentSuccessScreen(
    orderId: String,
    onDone: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Success icon
            Box(
                modifier = Modifier.size(96.dp).background(SuccessGreenLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(56.dp))
            }

            Text("Pembayaran Berhasil!", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            Text("Transaksi telah tercatat", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Text(orderId, color = TextSecondary.copy(alpha = 0.6f), style = MaterialTheme.typography.labelMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Kembali ke Kasir", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
