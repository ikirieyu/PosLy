package com.posly.app.domain.model

import java.util.UUID

/**
 * Expense category enum.
 */
enum class ExpenseCategory(val displayName: String) {
    BAHAN_BAKU("Bahan Baku"),
    TRANSPORT("Transport"),
    OPERASIONAL("Operasional"),
    DARURAT("Dana Darurat"),
    LAINNYA("Lainnya")
}

/**
 * Domain model for an operational expense entry.
 */
data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val category: ExpenseCategory,
    val amount: Double,
    val notes: String = "",
    val receiptImageUrl: String? = null,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
