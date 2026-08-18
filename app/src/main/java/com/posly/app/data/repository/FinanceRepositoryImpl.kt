package com.posly.app.data.repository

import com.posly.app.data.local.dao.ExpenseDao
import com.posly.app.data.local.dao.OrderDao
import com.posly.app.data.local.entity.ExpenseEntity
import com.posly.app.domain.model.*
import com.posly.app.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject

class FinanceRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
    private val expenseDao: ExpenseDao
) : FinanceRepository {

    override suspend fun getFinancialReport(period: ReportPeriod): FinancialReport {
        val (startDate, endDate) = period.toDateRange()
        return getFinancialReportByDateRange(startDate, endDate)
    }

    override suspend fun getFinancialReportByDateRange(startDate: Long, endDate: Long): FinancialReport {
        val omzet = orderDao.getTotalRevenue(startDate, endDate)
        val totalHpp = orderDao.getTotalCost(startDate, endDate)
        val labaKotor = omzet - totalHpp
        val totalBeban = expenseDao.getTotalExpenses(startDate, endDate)
        val labaBersih = labaKotor - totalBeban
        val transactionCount = orderDao.getTransactionCount(startDate, endDate)

        return FinancialReport(
            period = ReportPeriod.DAILY, // default, caller overrides
            startDate = startDate,
            endDate = endDate,
            omzet = omzet,
            totalHpp = totalHpp,
            labaKotor = labaKotor,
            totalBeban = totalBeban,
            labaBersih = labaBersih,
            totalTransactions = transactionCount
        )
    }

    override fun getAllExpenses(): Flow<List<Expense>> =
        expenseDao.getAllExpenses().map { list -> list.map { it.toDomain() } }

    override fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> =
        expenseDao.getExpensesByDateRange(startDate, endDate).map { list -> list.map { it.toDomain() } }

    override suspend fun insertExpense(expense: Expense): Result<Unit> = runCatching {
        expenseDao.insertExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(expenseId: String): Result<Unit> = runCatching {
        expenseDao.deleteExpense(expenseId)
    }

    override suspend fun calculateBudgetAllocation(
        netProfit: Double,
        savingsPercent: Double,
        emergencyPercent: Double,
        restockPercent: Double,
        transportPercent: Double
    ): BudgetAllocation {
        if (netProfit <= 0) return BudgetAllocation()
        return BudgetAllocation(
            tabungan = netProfit * (savingsPercent / 100),
            darurat = netProfit * (emergencyPercent / 100),
            bahanBaku = netProfit * (restockPercent / 100),
            transport = netProfit * (transportPercent / 100)
        )
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun ReportPeriod.toDateRange(): Pair<Long, Long> {
        val now = Calendar.getInstance()
        val end = now.timeInMillis
        return when (this) {
            ReportPeriod.DAILY -> {
                now.set(Calendar.HOUR_OF_DAY, 0)
                now.set(Calendar.MINUTE, 0)
                now.set(Calendar.SECOND, 0)
                Pair(now.timeInMillis, end)
            }
            ReportPeriod.WEEKLY -> {
                now.add(Calendar.DAY_OF_YEAR, -7)
                Pair(now.timeInMillis, end)
            }
            ReportPeriod.MONTHLY -> {
                now.set(Calendar.DAY_OF_MONTH, 1)
                now.set(Calendar.HOUR_OF_DAY, 0)
                Pair(now.timeInMillis, end)
            }
            ReportPeriod.YEARLY -> {
                now.set(Calendar.DAY_OF_YEAR, 1)
                now.set(Calendar.HOUR_OF_DAY, 0)
                Pair(now.timeInMillis, end)
            }
        }
    }

    private fun ExpenseEntity.toDomain() = Expense(
        id = id, category = ExpenseCategory.valueOf(category),
        amount = amount, notes = notes, receiptImageUrl = receiptImageUrl,
        createdBy = createdBy, createdAt = createdAt, syncStatus = SyncStatus.valueOf(syncStatus)
    )

    private fun Expense.toEntity() = ExpenseEntity(
        id = id, category = category.name, amount = amount,
        notes = notes, receiptImageUrl = receiptImageUrl,
        createdBy = createdBy, createdAt = createdAt, syncStatus = syncStatus.name
    )
}
