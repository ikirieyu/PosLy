package com.posly.app.domain.model

/**
 * Financial summary for a given reporting period.
 */
data class FinancialReport(
    val period: ReportPeriod,
    val startDate: Long,
    val endDate: Long,
    /** Total revenue from all completed orders */
    val omzet: Double = 0.0,
    /** Total cost of goods sold */
    val totalHpp: Double = 0.0,
    /** Gross profit = omzet - totalHpp */
    val labaKotor: Double = 0.0,
    /** Total operational expenses */
    val totalBeban: Double = 0.0,
    /** Net profit = labaKotor - totalBeban */
    val labaBersih: Double = 0.0,
    /** Budget allocation results */
    val alokasi: BudgetAllocation = BudgetAllocation(),
    /** Number of transactions */
    val totalTransactions: Int = 0,
    /** Daily revenue data for chart */
    val dailyRevenue: List<DailyRevenue> = emptyList()
)

/**
 * Time period for financial reports.
 */
enum class ReportPeriod {
    DAILY, WEEKLY, MONTHLY, YEARLY
}

/**
 * Budget allocation breakdown from net profit.
 */
data class BudgetAllocation(
    val tabungan: Double = 0.0,
    val darurat: Double = 0.0,
    val bahanBaku: Double = 0.0,
    val transport: Double = 0.0
)

/**
 * Daily revenue data point for chart rendering.
 */
data class DailyRevenue(
    val date: Long,
    val revenue: Double,
    val profit: Double
)
