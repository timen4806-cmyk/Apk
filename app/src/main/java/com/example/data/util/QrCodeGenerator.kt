package com.example.data.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrCodeGenerator {

    /**
     * Generates a high-resolution Android Bitmap QR Code dynamically using ZXing library.
     */
    fun generateQrBitmap(
        content: String,
        size: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = hashMapOf<EncodeHintType, Any>(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
            )
            val bitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) foregroundColor else backgroundColor
                }
            }
            Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Formats bank account details into a structured donation payload that QR scanners
     * and banking apps can read and parse for rapid redirection.
     */
    fun formatBankPayload(
        bankName: String,
        accountNumber: String,
        accountHolder: String,
        nominal: Long = 0,
        purpose: String = "Donasi & Infaq Masjid Jami' At-Taqwa"
    ): String {
        return buildString {
            append("DONASI MASJID JAMI' AT-TAQWA\n")
            append("Bank: $bankName\n")
            append("Rekening: $accountNumber\n")
            append("Atas Nama: $accountHolder\n")
            if (nominal > 0) {
                append("Nominal: Rp $nominal\n")
            }
            append("Keperluan: $purpose\n")
            append("Lokasi: Penancangan, Serang - Banten")
        }
    }

    /**
     * Formats official QRIS payload with optional nominal donation.
     */
    fun formatQrisPayload(
        merchantName: String = "MASJID JAMI AT TAQWA PENANCANGAN",
        nmid: String = "ID1020260012345",
        nominal: Long = 0
    ): String {
        return buildString {
            append("QRIS:ID.CO.QRIS.WWW\n")
            append("NMID: $nmid\n")
            append("Merchant: $merchantName\n")
            if (nominal > 0) {
                append("Nominal: Rp $nominal\n")
            }
            append("Peruntukan: Infaq & Sedekah Masjid At-Taqwa")
        }
    }
}
