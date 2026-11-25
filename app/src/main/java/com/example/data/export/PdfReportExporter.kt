package com.example.data.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.local.ScreeningSessionEntity
import com.example.data.local.UserProfileEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class ExportedPdfInfo(
    val fileName: String,
    val shareUri: Uri,
    val savedToDownloads: Boolean
)

object PdfReportExporter {

    fun generateAndSavePdf(
        context: Context,
        profile: UserProfileEntity,
        session: ScreeningSessionEntity
    ): Result<ExportedPdfInfo> {
        return runCatching {
            val document = PdfDocument()
            val pageWidth = 595 // A4 points
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val margin = 36f
            val contentWidth = pageWidth - margin * 2
            var y = 36f

            // Header Banner
            val headerPaint = Paint().apply {
                color = Color.rgb(11, 107, 203)
                isAntiAlias = true
            }
            canvas.drawRoundRect(
                RectF(margin, y, pageWidth - margin, y + 76f),
                12f,
                12f,
                headerPaint
            )

            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 20f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            val subHeaderPaint = Paint().apply {
                color = Color.rgb(214, 234, 255)
                textSize = 11f
                isAntiAlias = true
            }
            canvas.drawText("VisionScan AI — Preliminary Eye Screening Report", margin + 18f, y + 32f, titlePaint)
            canvas.drawText(
                "Your Eyes, Your Health, Our Priority.  |  UN SDG 3: Good Health & Well-Being",
                margin + 18f,
                y + 54f,
                subHeaderPaint
            )
            y += 96f

            val sectionPaint = Paint().apply {
                color = Color.rgb(11, 107, 203)
                textSize = 13f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            val bodyPaint = Paint().apply {
                color = Color.rgb(15, 33, 51)
                textSize = 10.5f
                isAntiAlias = true
            }
            val mutedPaint = Paint().apply {
                color = Color.rgb(85, 105, 125)
                textSize = 9.5f
                isAntiAlias = true
            }
            val dividerPaint = Paint().apply {
                color = Color.rgb(210, 224, 238)
                strokeWidth = 1f
            }

            fun drawWrappedText(text: String, paint: Paint, startX: Float, maxWidth: Float, lineHeight: Float = 15f) {
                for (paragraph in text.split("\n")) {
                    val words = paragraph.split(" ")
                    var line = StringBuilder()
                    for (word in words) {
                        val candidate = if (line.isEmpty()) word else "$line $word"
                        if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                            if (y < pageHeight - 60f) {
                                canvas.drawText(line.toString(), startX, y, paint)
                            }
                            y += lineHeight
                            line = StringBuilder(word)
                        } else {
                            line = StringBuilder(candidate)
                        }
                    }
                    if (line.isNotEmpty() && y < pageHeight - 60f) {
                        canvas.drawText(line.toString(), startX, y, paint)
                        y += lineHeight
                    }
                }
            }

            // Participant details
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val dateStr = dateFormat.format(Date(session.updatedAt))
            canvas.drawText("Participant & Session Information", margin, y, sectionPaint)
            y += 18f
            drawWrappedText(
                "Name: ${profile.name}   |   Age: ${profile.age} years   |   Eyewear: ${if (profile.wearsGlasses) "Yes" else "No"}",
                bodyPaint,
                margin,
                contentWidth
            )
            drawWrappedText(
                "Screening Date: $dateStr   |   Modules Completed: ${session.completedModuleCount} / 4",
                mutedPaint,
                margin,
                contentWidth
            )
            y += 6f
            canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
            y += 20f

            // Module Results Table
            canvas.drawText("Recorded Screening Module Responses", margin, y, sectionPaint)
            y += 18f

            val acuityLine = if (session.acuityCompleted) {
                "• Visual Acuity (Preliminary): Right Eye Level ${session.rightEyeLevel}/6 (${session.rightEyeCorrect}/${session.rightEyeTotal} correct), Left Eye Level ${session.leftEyeLevel}/6 (${session.leftEyeCorrect}/${session.leftEyeTotal} correct). Scale: ${String.format(Locale.US, "%.2f", session.calibrationScale)}x."
            } else {
                "• Visual Acuity Screening: Not completed in this session."
            }
            drawWrappedText(acuityLine, bodyPaint, margin, contentWidth)
            y += 4f

            val colorLine = if (session.colorCompleted) {
                "• Color Vision (Preliminary): ${session.colorCorrect}/${session.colorTotal} screening plates correctly identified (${session.colorOutcomeLabel})."
            } else {
                "• Color Vision Screening: Not completed in this session."
            }
            drawWrappedText(colorLine, bodyPaint, margin, contentWidth)
            y += 4f

            val blinkLine = if (session.blinkCompleted) {
                "• Blink Monitoring: ${session.blinkCount} blinks in ${session.blinkDurationSec}s (~${session.blinksPerMinute.roundToInt()} blinks/min estimated; avg openness ${(session.avgEyeOpenProb * 100).roundToInt()}%)."
            } else {
                "• Eye Blink Monitoring: Not completed in this session."
            }
            drawWrappedText(blinkLine, bodyPaint, margin, contentWidth)
            y += 4f

            val movementLine = if (session.movementCompleted) {
                "• Eye Movement Tracking: ${session.movementTargetsTracked}/${session.movementTargetsTotal} target checkpoints tracked (${(session.movementTrackingRatio * 100).roundToInt()}%)."
            } else {
                "• Eye Movement Tracking: Not completed in this session."
            }
            drawWrappedText(movementLine, bodyPaint, margin, contentWidth)
            y += 10f
            canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
            y += 20f

            // AI Summary Section
            canvas.drawText("Consolidated Screening Explanation", margin, y, sectionPaint)
            y += 18f
            if (session.aiReportGenerated) {
                if (session.aiIsPlaceholder) {
                    drawWrappedText(
                        "Note: Sample local explanation (Live Gemini AI requires enabling Firebase AI Logic).",
                        mutedPaint,
                        margin,
                        contentWidth
                    )
                    y += 4f
                }
                drawWrappedText(session.aiSummary, bodyPaint, margin, contentWidth)
                y += 8f
                canvas.drawText("Recommendations", margin, y, sectionPaint)
                y += 16f
                drawWrappedText(session.aiRecommendations, bodyPaint, margin, contentWidth)
            } else {
                drawWrappedText(
                    "AI explanation was not generated before exporting this PDF. Open the AI Eye Screening Report screen in VisionScan AI to synthesize a summary.",
                    mutedPaint,
                    margin,
                    contentWidth
                )
            }

            // Medical Disclaimer Footer Box
            val footerTop = pageHeight - 96f
            val disclaimerBg = Paint().apply {
                color = Color.rgb(255, 244, 229)
                isAntiAlias = true
            }
            canvas.drawRoundRect(
                RectF(margin, footerTop, pageWidth - margin, pageHeight - 28f),
                8f,
                8f,
                disclaimerBg
            )
            val disclaimerTitlePaint = Paint().apply {
                color = Color.rgb(160, 82, 0)
                textSize = 10f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            val disclaimerBodyPaint = Paint().apply {
                color = Color.rgb(95, 58, 12)
                textSize = 8.8f
                isAntiAlias = true
            }
            canvas.drawText("MEDICAL DISCLAIMER — PRELIMINARY SCREENING ONLY", margin + 12f, footerTop + 18f, disclaimerTitlePaint)
            y = footerTop + 32f
            drawWrappedText(
                "This VisionScan AI report is for preliminary awareness only. It does NOT diagnose eye diseases, measure clinical Snellen visual acuity, or replace a comprehensive eye examination by a licensed optometrist or ophthalmologist.",
                disclaimerBodyPaint,
                margin + 12f,
                contentWidth - 24f,
                12f
            )

            document.finishPage(page)

            val timestampTag = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "VisionScan_Report_$timestampTag.pdf"

            // Always write to app-private files/reports for FileProvider sharing
            val reportsDir = File(context.filesDir, "reports").apply { mkdirs() }
            val internalFile = File(reportsDir, fileName)
            FileOutputStream(internalFile).use { out ->
                document.writeTo(out)
            }

            // Also save to public Downloads via zero-permission MediaStore on Android 10+
            var savedToDownloads = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/VisionScanAI")
                }
                val downloadUri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (downloadUri != null) {
                    context.contentResolver.openOutputStream(downloadUri)?.use { out ->
                        internalFile.inputStream().use { input -> input.copyTo(out) }
                        savedToDownloads = true
                    }
                }
            }

            document.close()

            val shareUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                internalFile
            )
            ExportedPdfInfo(
                fileName = fileName,
                shareUri = shareUri,
                savedToDownloads = savedToDownloads
            )
        }
    }
}
