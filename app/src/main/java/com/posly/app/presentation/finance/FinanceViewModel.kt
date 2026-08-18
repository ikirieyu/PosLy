package com.posly.app.presentation.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posly.app.domain.model.FinancialReport
import com.posly.app.domain.model.ReportPeriod
import com.posly.app.domain.usecase.finance.CalculateFinancialReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FinanceUiState(
    val selectedPeriod: ReportPeriod = ReportPeriod.DAILY,
    val report: FinancialReport? = null,
    val isLoading: Boolean = false,
    val exportPath: String? = null,
    val error: String? = null
)

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val calculateFinancialReportUseCase: CalculateFinancialReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    init {
        loadReport(ReportPeriod.DAILY)
    }

    fun selectPeriod(period: ReportPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
        loadReport(period)
    }

    private fun loadReport(period: ReportPeriod) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            calculateFinancialReportUseCase(period)
                .onSuccess { report -> _uiState.update { it.copy(report = report, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    fun exportExcel() {
        // TODO: Invoke ExcelExporter via use case
        viewModelScope.launch {
            // Implementation pending ExcelExporter dependency injection
        }
    }
}
