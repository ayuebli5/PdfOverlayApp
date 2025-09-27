package com.example.pdfoverlay

import android.content.Context
import android.graphics.Color
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import java.io.File

class PdfOverlayProcessor(private val context: Context) {

    init { PDFBoxResourceLoader.init(context) }

    fun overlayPdf(
        inputPdf: File,
        linedTemplate: File,
        outputPdf: File,
        displayScale: Float = 0.6f,
        margin: Float = 10f,
        addBorder: Boolean = true
    ) {
        val srcDoc = PDDocument.load(inputPdf)
        val linedDoc = PDDocument.load(linedTemplate)
        val outputDoc = PDDocument()

        val pageSize = linedDoc.getPage(0).mediaBox
        val pageWidth = pageSize.width
        val pageHeight = pageSize.height

        // First lined page
        outputDoc.addPage(PDPage(pageSize))

        for (i in 0 until srcDoc.numberOfPages) {
            val newPage = PDPage(PDRectangle(pageWidth, pageHeight))
            outputDoc.addPage(newPage)

            val cs = PDPageContentStream(outputDoc, newPage)

            val templateImage = LosslessFactory.createFromImage(outputDoc, linedDoc.getPage(0).toBufferedImage())
            cs.drawImage(templateImage, 0f, 0f, pageWidth, pageHeight)

            val srcImage = LosslessFactory.createFromImage(outputDoc, srcDoc.getPage(i).toBufferedImage())
            val scaledW = page
