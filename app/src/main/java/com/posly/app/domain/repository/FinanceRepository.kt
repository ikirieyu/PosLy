package com.posly.app.domain.repository

import com.posly.app.domain.model.BudgetAllocation
import com.posly.app.domain.model.Expense
import com.posly.app.domain.model.FinancialReport
import com.posly.app.domain.model.ReportPeriod
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    suspend fun getFinancialReport(period: ReportPeriod): FinancialReport
    suspend fun getFinancialReportByDateRange(startDate: Long, endDate: Long): FinancialReport

    // Expenses
    fun getAllExpenses(): Flow<List<Expense>>
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>
    suspend fun insertExpense(expense: Expense): Result<Unit>
    suspend fun deleteExpense(expenseId: String): Result<Unit>

    // Budget allocation calculator
    suspend fun calculateBudgetAllocation(
        netProfit: Double,
        savingsPercent: Double,
        emergencyPercent: Double,
        restockPercent: Double,
        transportPercent: Double
    ): BudgetAllocation
}
