package com.posly.app.data.export

import android.content.Context
import android.os.Environment
import com.posly.app.domain.model.FinancialReport
import com.posly.app.domain.model.Order
import com.posly.app.domain.model.StoreSettings
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Excel (.xlsx) exporter with Apache POI.
 * Generates professional financial report with active formulas.
 */
class ExcelExporter(private val context: Context) {

    fun exportFinancialReport(
        report: FinancialReport,
        orders: List<Order>,
        settings: StoreSettings
    ): Result<String> = runCatching {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Laporan Keuangan")

        // ── Styles ────────────────────────────────────────────────────────────
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.CORNFLOWER_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            val font = workbook.createFont().also {
                it.bold = true
                it.color = IndexedColors.WHITE.index
                it.fontName = "Calibri"
                it.fontHeightInPoints = 12
            }
            setFont(font)
        }

        val titleStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont().also {
                it.bold = true
                it.fontName = "Calibri"
                it.fontHeightInPoints = 14
            }
            setFont(font)
        }

        val currencyStyle = workbook.createCellStyle().apply {
            dataFormat = workbook.creationHelper.createDataFormat().getFormat("\"Rp \"#,##0")
            alignment = HorizontalAlignment.RIGHT
        }

        val currencyBoldStyle = workbook.createCellStyle().apply {
            dataFormat = workbook.creationHelper.createDataFormat().getFormat("\"Rp \"#,##0")
            alignment = HorizontalAlignment.RIGHT
            val font = workbook.createFont().also { it.bold = true; it.fontName = "Calibri" }
            setFont(font)
        }

        val zebraStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_TURQUOISE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }

        // ── Title ─────────────────────────────────────────────────────────────
        var rowNum = 0
        val titleRow = sheet.createRow(rowNum++)
        titleRow.createCell(0).apply { setCellValue("LAPORAN KEUANGAN — ${settings.storeName}"); cellStyle = titleStyle }
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 6))

        sheet.createRow(rowNum++).createCell(0).setCellValue(
            "Periode: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(report.startDate))} - ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(report.endDate))}"
        )
        rowNum++ // blank row

        // ── Summary metrics ───────────────────────────────────────────────────
        fun summaryRow(label: String, formula: String? = null, value: Double? = null) {
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(label)
            val cell = row.createCell(1)
            if (formula != null) cell.cellFormula = formula
            else if (value != null) cell.setCellValue(value)
            cell.cellStyle = currencyBoldStyle
        }

        // Mark data start at row 5 (0-indexed row 4) for formulas
        val dataStartRow = rowNum + 2
        val headerRow2 = sheet.createRow(rowNum++)
        listOf("No. Invoice", "Tanggal", "Kasir", "Omzet", "HPP", "Laba Kotor", "Beban").forEachIndexed { i, label ->
            headerRow2.createCell(i).apply { setCellValue(label); cellStyle = headerStyle }
        }

        val orderDataStartRow = rowNum + 1 // 1-indexed for Excel formulas
        orders.forEachIndexed { idx, order ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(order.invoiceNumber)
            row.createCell(1).setCellValue(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(order.createdAt)))
            row.createCell(2).setCellValue(order.cashierName)
            row.createCell(3).apply { setCellValue(order.totalAmount); cellStyle = currencyStyle }
            row.createCell(4).apply { setCellValue(order.totalCost); cellStyle = currencyStyle }
            row.createCell(5).apply { cellFormula = "D${rowNum}-E${rowNum}"; cellStyle = currencyStyle }
            // Apply zebra striping
            if (idx % 2 == 1) (0..6).forEach { col -> row.getCell(col)?.cellStyle = zebraStyle }
        }

        val orderDataEndRow = rowNum // 1-indexed last data row

        // ── Summary formulas ──────────────────────────────────────────────────
        rowNum++ // blank
        val summaryStartRow = rowNum
        summaryRow("Omzet Total", "SUM(D${orderDataStartRow}:D${orderDataEndRow})")
        val omzetRow = summaryStartRow + 1
        summaryRow("Total HPP", "SUM(E${orderDataStartRow}:E${orderDataEndRow})")
        val hppRow = omzetRow + 1
        summaryRow("Laba Kotor", "B${omzetRow}-B${hppRow}")
        val labaKotorRow = hppRow + 1
        summaryRow("Total Beban", value = report.totalBeban)
        val bebanRow = labaKotorRow + 1
        summaryRow("Laba Bersih", "B${labaKotorRow}-B${bebanRow}")
        val labaBersihRow = bebanRow + 1

        // ── Budget allocation formulas ─────────────────────────────────────────
        rowNum++
        sheet.createRow(rowNum++).createCell(0).apply { setCellValue("ALOKASI ANGGARAN"); cellStyle = titleStyle }
        summaryRow("Tabungan Bisnis (${settings.savingsPercent.toInt()}%)", "B${labaBersihRow}*${settings.savingsPercent / 100}")
        summaryRow("Dana Darurat (${settings.emergencyPercent.toInt()}%)", "B${labaBersihRow}*${settings.emergencyPercent / 100}")
        summaryRow("Bahan Baku (${settings.restockPercent.toInt()}%)", "B${labaBersihRow}*${settings.restockPercent / 100}")
        summaryRow("Transport (${settings.transportPercent.toInt()}%)", "B${labaBersihRow}*${settings.transportPercent / 100}")

        // ── Column widths ─────────────────────────────────────────────────────
        sheet.setColumnWidth(0, 5000)
        sheet.setColumnWidth(1, 4000)
        sheet.setColumnWidth(2, 4000)
        (3..6).forEach { sheet.setColumnWidth(it, 4000) }

        // ── Save ──────────────────────────────────────────────────────────────
        val fileName = "PosLy_Laporan_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.xlsx"
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        val file = File(dir, fileName)
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()

        file.absolutePath
    }
}
