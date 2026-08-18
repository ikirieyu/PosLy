package com.posly.app.domain.model

import java.util.UUID

/**
 * Domain model for store configuration & budget allocation percentages.
 */
data class StoreSettings(
    val id: String = UUID.randomUUID().toString(),
    val storeName: String = "",
    val slogan: String = "",
    val address: String = "",
    val phone: String = "",
    val socialMedia: String = "",
    val logoUrl: String? = null,
    val receiptFooter: String = "Terima kasih atas kunjungan Anda!",
    // Budget allocation percentages (must sum to 100)
    val savingsPercent: Double = 30.0,      // Tabungan Bisnis
    val emergencyPercent: Double = 20.0,    // Dana Darurat
    val restockPercent: Double = 35.0,      // Belanja Bahan Baku
    val transportPercent: Double = 15.0,    // Transport & Operasional
    // Supabase connection (stored encrypted locally)
    val supabaseUrl: String = "",
    val supabaseAnonKey: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
