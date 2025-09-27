package com.example.pdfoverlay

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var txtStatus: TextView
    private lateinit var edtFileName: EditText
    private var inputPdfFile: File? = null
    private var linedPdfFile: File? = null
    private var outputPdfFile: File? = null

    companion object {
        private const val PICK_INPUT_PDF = 1001
        private const val PICK_LINED_PDF = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnPickInput = findViewById<Button>(R.id.btnPickInput)
        val btnPickLined = findViewById<Button>(R.id.btnPickLined)
        val btnRun = findViewById<Button>(R.id.btnRunOverlay)
        val btnOpen = findViewById<Button>(R.id.btnOpenPdf)
        val btnShare = findViewById<Button>(R.id.btnSharePdf)
        edtFileName = findViewById(R.id.edtFileName)
        txtStatus = findViewById(R.id.txtStatus)

        btnPickInput.setOnClickListener { pickPdf(PICK_INPUT_PDF) }
        btnPickLined.setOnClickListener { pickPdf(PICK_LINED_PDF) }

        btnRun.setOnClickListener {
            if (inputPdfFile != null && linedPdfFile != null) {
                try {
                    val processor = PdfOverlayProcessor(this)

                    val name = edtFileName.text.toString().trim()
                    val finalName = if (name.isEmpty()) "output.pdf" else "$name.pdf"
                    outputPdfFile = File(filesDir, finalName)

                    processor.overlayPdf(
                        inputPdfFile!!,
                        linedPdfFile!!,
                        outputPdfFile!!,
                        displayScale = 0.6f,
                        margin = 10f,
                        addBorder = true
                    )

                    txtStatus.text = "✅ Done! Saved: ${outputPdfFile!!.absolutePath}"

                } catch (e: Exception) {
                    txtStatus.text = "❌ Error: ${e.message}"
                }
            } else {
                txtStatus.text = "⚠️ Please pick both PDFs first"
            }
        }

        btnOpen.setOnClickListener {
            outputPdfFile?.let { openPdf(it) } ?: run {
                txtStatus.text = "⚠️ No output PDF yet"
            }
        }

        btnShare.setOnClickListener {
            outputPdfFile?.let { sharePdf(it) } ?: run {
                txtStatus.text = "⚠️ No output PDF yet"
            }
        }
    }

    private fun pickPdf(requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            val uri: Uri? = data.data
            uri?.let {
                val file = copyUriToFile(it, if (requestCode == PICK_INPUT_PDF) "input.pdf" else "lined.pdf")
                if (requestCode == PICK_INPUT_PDF) {
                    inputPdfFile = file
                    txtStatus.text = "📄 Input PDF selected"
                } else {
                    linedPdfFile = file
                    txtStatus.text = "📄 Lined PDF selected"
                }
            }
        }
    }

    private fun copyUriToFile(uri: Uri, filename: String): File {
        val file = File(filesDir, filename)
        contentResolver.openInputStream(uri).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                copyStream(inputStream!!, outputStream)
            }
        }
        return file
    }

    private fun copyStream(input: InputStream, output: FileOutputStream) {
        val buffer = ByteArray(1024)
        var length: Int
        while (true) {
            length = input.read(buffer)
            if (length <= 0) break
            output.write(buffer, 0, length)
        }
    }

    private fun openPdf(file: File) {
        val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Open PDF with"))
    }

    private fun sharePdf(file: File) {
        val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share PDF via"))
    }
}
