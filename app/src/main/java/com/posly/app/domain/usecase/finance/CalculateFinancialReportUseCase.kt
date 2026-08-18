package com.posly.app.domain.usecase.finance

import com.posly.app.domain.model.BudgetAllocation
import com.posly.app.domain.model.FinancialReport
import com.posly.app.domain.model.ReportPeriod
import com.posly.app.domain.repository.FinanceRepository
import com.posly.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case: calculate the full financial report for a given period,
 * including budget allocation distribution.
 */
class CalculateFinancialReportUseCase @Inject constructor(
    private val financeRepository: FinanceRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(period: ReportPeriod): Result<FinancialReport> = runCatching {
        val settings = settingsRepository.getStoreSettings().first()
        val report = financeRepository.getFinancialReport(period)

        val allocation = financeRepository.calculateBudgetAllocation(
            netProfit = report.labaBersih,
            savingsPercent = settings.savingsPercent,
            emergencyPercent = settings.emergencyPercent,
            restockPercent = settings.restockPercent,
            transportPercent = settings.transportPercent
        )

        report.copy(alokasi = allocation)
    }
}
