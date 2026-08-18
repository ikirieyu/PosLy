package com.posly.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["createdAt"])]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val category: String,  // BAHAN_BAKU | TRANSPORT | OPERASIONAL | DARURAT | LAINNYA
    val amount: Double,
    val notes: String = "",
    val receiptImageUrl: String? = null,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)

@Entity(tableName = "store_settings")
data class StoreSettingsEntity(
    @PrimaryKey val id: String = "singleton",
    val storeName: String = "",
    val slogan: String = "",
    val address: String = "",
    val phone: String = "",
    val socialMedia: String = "",
    val logoUrl: String? = null,
    val qrisImageUrl: String? = null,
    val receiptFooter: String = "Terima kasih atas kunjungan Anda!",
    val printReceiptHeader: Boolean = true,
    val autoPrintReceipt: Boolean = false,
    val savingsPercent: Double = 30.0,
    val emergencyPercent: Double = 20.0,
    val restockPercent: Double = 35.0,
    val transportPercent: Double = 15.0,
    val supabaseUrl: String = "",
    val supabaseAnonKey: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
