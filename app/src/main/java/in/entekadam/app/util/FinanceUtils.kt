package in.entekadam.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import in.entekadam.app.data.*
import com.google.gson.Gson
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun money(paise: Long): String = "₹" + NumberFormat.getNumberInstance(Locale("en", "IN")).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }.format(paise / 100.0)
fun parsePaise(value: String): Long? = value.trim().replace(",", "").toBigDecimalOrNull()?.let { runCatching { it.movePointRight(2).longValueExact() }.getOrNull() }
fun dateText(time: Long?): String = time?.let { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(it)) } ?: "—"

 data class BackupData(val formatVersion: Int = 1, val createdAt: Long = System.currentTimeMillis(), val people: List<Person>, val debts: List<Debt>, val payments: List<Payment>, val audit: List<AuditEntry>)

object BackupManager {
    private val gson = Gson()
    fun encode(data: BackupData): String = gson.toJson(data)
    fun decode(json: String): BackupData {
        val result = gson.fromJson(json, BackupData::class.java) ?: error("Invalid backup")
        require(result.formatVersion == 1) { "Unsupported backup version" }
        require(result.people != null && result.debts != null && result.payments != null && result.audit != null) { "Backup is incomplete" }
        require(result.people.all { it.id > 0 && it.name.isNotBlank() && it.side in setOf("RECEIVE", "PAY") } && result.people.map { it.id }.distinct().size == result.people.size) { "Invalid person data" }
        val personIds = result.people.map { it.id }.toSet()
        require(result.debts.all { it.id > 0 && it.personId in personIds && it.amountPaise > 0 } && result.debts.map { it.id }.distinct().size == result.debts.size) { "Invalid debt data" }
        val debtIds = result.debts.map { it.id }.toSet()
        require(result.payments.all { it.id > 0 && it.debtId in debtIds && it.amountPaise > 0 } && result.payments.map { it.id }.distinct().size == result.payments.size) { "Invalid payment data" }
        return result
    }
}

fun createPdf(context: Context, title: String, lines: List<String>): File {
    val pdf = PdfDocument()
    val paint = Paint().apply { textSize = 12f; isAntiAlias = true }
    var pageNo = 1
    var page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNo).create())
    var y = 54f
    page.canvas.drawText(title, 40f, y, Paint(paint).apply { textSize = 20f; isFakeBoldText = true })
    y += 34f
    lines.forEach { line ->
        if (y > 800f) { pdf.finishPage(page); pageNo++; page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNo).create()); y = 50f }
        page.canvas.drawText(line.take(90), 40f, y, paint); y += 22f
    }
    pdf.finishPage(page)
    val dir = File(context.cacheDir, "reports").apply { mkdirs() }
    val file = File(dir, "ente-kadam-report-${System.currentTimeMillis()}.pdf")
    file.outputStream().use { pdf.writeTo(it) }; pdf.close()
    return file
}

fun shareText(context: Context, text: String, whatsappOnly: Boolean = false) {
    val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
    if (whatsappOnly) send.setPackage("com.whatsapp")
    try { context.startActivity(Intent.createChooser(send, "Share account statement")) } catch (_: Exception) {
        if (whatsappOnly) context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "Share statement"))
    }
}
