package com.posly.app.data.printer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.posly.app.domain.model.Order
import com.posly.app.domain.model.StoreSettings
import java.io.OutputStream
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*

/**
 * ESC/POS thermal printer engine.
 * Supports: Bluetooth SPP and Wi-Fi TCP/IP.
 */
class EscPosPrinterEngine {

    // ─── ESC/POS Constants ────────────────────────────────────────────────────
    private val ESC = 0x1B.toByte()
    private val GS = 0x1D.toByte()
    private val INIT = byteArrayOf(ESC, 0x40)
    private val ALIGN_LEFT = byteArrayOf(ESC, 0x61, 0x00)
    private val ALIGN_CENTER = byteArrayOf(ESC, 0x61, 0x01)
    private val ALIGN_RIGHT = byteArrayOf(ESC, 0x61, 0x02)
    private val BOLD_ON = byteArrayOf(ESC, 0x45, 0x01)
    private val BOLD_OFF = byteArrayOf(ESC, 0x45, 0x00)
    private val FONT_DOUBLE = byteArrayOf(GS, 0x21, 0x11)
    private val FONT_NORMAL = byteArrayOf(GS, 0x21, 0x00)
    private val CUT = byteArrayOf(GS, 0x56, 0x41, 0x00)
    private val LF = byteArrayOf(0x0A)

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    /**
     * Build ESC/POS byte array for a receipt.
     */
    fun buildReceipt(order: Order, settings: StoreSettings): ByteArray {
        val buffer = mutableListOf<Byte>()

        fun write(bytes: ByteArray) = buffer.addAll(bytes.toList())
        fun writeLine(text: String) { write(text.toByteArray(Charsets.UTF_8)); write(LF) }
        fun writeSeparator() = writeLine("-".repeat(32))

        write(INIT)

        // ── Header ───────────────────────────────────────────────────────────
        write(ALIGN_CENTER)
        write(BOLD_ON)
        write(FONT_DOUBLE)
        writeLine(settings.storeName.take(14))
        write(FONT_NORMAL)
        write(BOLD_OFF)
        if (settings.slogan.isNotBlank()) writeLine(settings.slogan)
        if (settings.address.isNotBlank()) writeLine(settings.address)
        if (settings.phone.isNotBlank()) writeLine("Telp: ${settings.phone}")
        writeLine("")

        // ── Metadata ──────────────────────────────────────────────────────────
        write(ALIGN_LEFT)
        writeLine("No. Invoice  : ${order.invoiceNumber}")
        writeLine("Tanggal      : ${dateFormatter.format(Date(order.createdAt))}")
        writeLine("Kasir        : ${order.cashierName}")
        writeSeparator()

        // ── Items ─────────────────────────────────────────────────────────────
        order.items.forEach { item ->
            writeLine(item.productName.take(24))
            val qtyPrice = "  ${item.quantity} x ${formatPrice(item.unitPrice)}"
            val subtotal = formatPrice(item.subtotal)
            val spacer = " ".repeat((32 - qtyPrice.length - subtotal.length).coerceAtLeast(1))
            writeLine("$qtyPrice$spacer$subtotal")
            if (item.discountPerItem > 0) writeLine("  Diskon: -${formatPrice(item.discountPerItem)}")
        }

        writeSeparator()

        // ── Summary ───────────────────────────────────────────────────────────
        fun writeRow(label: String, value: String) {
            val spacer = " ".repeat((32 - label.length - value.length).coerceAtLeast(1))
            writeLine("$label$spacer$value")
        }

        writeRow("Subtotal", formatPrice(order.totalAmount + order.discountAmount))
        if (order.discountAmount > 0) writeRow("Diskon", "-${formatPrice(order.discountAmount)}")
        write(BOLD_ON)
        writeRow("TOTAL", "Rp ${formatPrice(order.totalAmount)}")
        write(BOLD_OFF)
        writeRow("Dibayar", "Rp ${formatPrice(order.paidAmount)}")
        writeRow("Kembalian", "Rp ${formatPrice(order.changeAmount)}")
        writeRow("Metode", order.paymentMethod.name)

        writeSeparator()

        // ── Footer ────────────────────────────────────────────────────────────
        write(ALIGN_CENTER)
        writeLine(settings.receiptFooter)
        if (settings.socialMedia.isNotBlank()) writeLine(settings.socialMedia)
        writeLine("")
        writeLine("")
        writeLine("")

        write(CUT)

        return buffer.toByteArray()
    }

    /**
     * Print via Bluetooth SPP.
     * SPP UUID: 00001101-0000-1000-8000-00805F9B34FB
     */
    suspend fun printViaBluetooth(
        device: BluetoothDevice,
        data: ByteArray
    ): Result<Unit> = runCatching {
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
        socket.use {
            socket.connect()
            socket.outputStream.use { stream ->
                stream.write(data)
                stream.flush()
            }
        }
    }

    /**
     * Print via Wi-Fi / LAN TCP socket (default port 9100).
     */
    suspend fun printViaWifi(
        ipAddress: String,
        port: Int = 9100,
        data: ByteArray
    ): Result<Unit> = runCatching {
        Socket(ipAddress, port).use { socket ->
            socket.getOutputStream().use { stream ->
                stream.write(data)
                stream.flush()
            }
        }
    }

    private fun formatPrice(price: Double): String = String.format("%,.0f", price)
}
