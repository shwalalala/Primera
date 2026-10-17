package cit.edu.primera.core.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import cit.edu.primera.feature.checkins.data.CheckinLogDto
import cit.edu.primera.feature.dashboard.domain.DashboardData
import cit.edu.primera.feature.smartwatchconnection.domain.SmartwatchHealth
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object DataExportHelper {

    fun exportToCsv(
        context: Context,
        logs: List<CheckinLogDto>,
        healthRecords: List<SmartwatchHealth>
    ) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val csvBuilder = StringBuilder()
        
        // Header
        csvBuilder.append("Date,Type,Category,Value/Description\n")
        
        // Health Records
        healthRecords.forEach { record ->
            csvBuilder.append("${record.date},Health,Steps,${record.steps}\n")
            csvBuilder.append("${record.date},Health,Heart Rate,${record.averageHeartRate ?: "--"}\n")
            csvBuilder.append("${record.date},Health,Sleep Minutes,${record.sleepMinutes}\n")
            csvBuilder.append("${record.date},Health,SpO2,${record.spO2 ?: "--"}\n")
        }
        
        // Logs
        logs.forEach { log ->
            val dateStr = log.timestamp?.let { sdf.format(it) } ?: "--"
            csvBuilder.append("$dateStr,Log,${log.category ?: "Other"},\"${log.description ?: ""}\"\n")
        }

        shareFile(context, csvBuilder.toString(), "Primera_Health_Data.csv", "text/csv")
    }

    fun exportSummaryToPdf(
        context: Context,
        healthRecords: List<SmartwatchHealth>,
        dashboardData: DashboardData? = null
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var pageNumber = 1
        
        // Brand Colors
        val primaryColor = Color.parseColor("#6A4C93") // Deep violet/purple
        val textColor = Color.parseColor("#333333")
        val lightGray = Color.parseColor("#F5F5F5")
        val dividerColor = Color.parseColor("#E0E0E0")
        
        val headerBgPaint = Paint().apply { color = primaryColor }
        val titleTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val dateTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 12f
            isAntiAlias = true
        }
        val sectionTitlePaint = Paint().apply {
            color = primaryColor
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val tableHeaderPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val tableBgPaint = Paint().apply { color = lightGray }
        val textPaint = Paint().apply {
            color = textColor
            textSize = 11f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = dividerColor
            strokeWidth = 1f
        }
        val pageNumPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val margin = 50f
        val pageWidth = 595f
        val pageHeight = 842f
        var yPosition = 0f
        
        fun drawHeader(canvas: Canvas) {
            canvas.drawRect(0f, 0f, pageWidth, 90f, headerBgPaint)
            canvas.drawText("Primera Health Summary", margin, 45f, titleTextPaint)
            val sdf = SimpleDateFormat("MMMM dd, yyyy - HH:mm", Locale.getDefault())
            canvas.drawText("Generated on: ${sdf.format(Date())}", margin, 70f, dateTextPaint)
        }

        fun drawFooter(canvas: Canvas, pageNum: Int) {
            canvas.drawText("- Page $pageNum -", pageWidth / 2, pageHeight - 30f, pageNumPaint)
        }

        drawHeader(canvas)
        drawFooter(canvas, pageNumber)
        yPosition = 130f
        
        fun checkPageBreak(requiredSpace: Float): Boolean {
            if (yPosition + requiredSpace > pageHeight - 60f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                pageNumber++
                drawHeader(canvas)
                drawFooter(canvas, pageNumber)
                yPosition = 130f
                return true
            }
            return false
        }
        
        dashboardData?.let { data ->
            // --- PATIENT PROFILE & PHYSICAL EXAM ---
            checkPageBreak(120f)
            canvas.drawText("Physical Examination & Profile", margin, yPosition, sectionTitlePaint)
            yPosition += 25f
            
            val fullName = listOfNotNull(data.firstName, data.lastName).joinToString(" ")
            if (fullName.isNotBlank()) {
                canvas.drawText("Name: $fullName", margin, yPosition, textPaint)
                yPosition += 20f
            }
            
            if (data.weightKg != null && data.heightCm != null && data.heightCm > 0) {
                val heightM = data.heightCm / 100f
                val bmi = data.weightKg / (heightM * heightM)
                val bmiRounded = String.format(Locale.US, "%.1f", bmi)
                
                var bmiStatus = ""
                var weightRec = ""
                when {
                    bmi < 18.5f -> {
                        bmiStatus = "Underweight"
                        weightRec = "28-40 lbs (12.5-18 kg)"
                    }
                    bmi < 25f -> {
                        bmiStatus = "Normal weight"
                        weightRec = "25-35 lbs (11.5-16 kg)"
                    }
                    bmi < 30f -> {
                        bmiStatus = "Overweight"
                        weightRec = "15-25 lbs (7-11.5 kg)"
                    }
                    else -> {
                        bmiStatus = "Obese"
                        weightRec = "11-20 lbs (5-9 kg)"
                    }
                }
                
                canvas.drawText("Pre-pregnancy Weight: ${data.weightKg} kg", margin, yPosition, textPaint)
                canvas.drawText("Height: ${data.heightCm} cm", margin + 200f, yPosition, textPaint)
                yPosition += 20f
                
                canvas.drawText("BMI: $bmiRounded ($bmiStatus)", margin, yPosition, textPaint)
                yPosition += 20f
                
                canvas.drawText("Recommended Total Weight Gain: $weightRec", margin, yPosition, textPaint)
                yPosition += 30f
            }
            
            // --- OBSTETRICAL HISTORY ---
            checkPageBreak(100f)
            canvas.drawText("Obstetrical History", margin, yPosition, sectionTitlePaint)
            yPosition += 25f
            
            val isFirst = data.isFirstPregnancy == true
            val pastPregnancies = data.pregnancyHistories
            val gravida = if (isFirst) 1 else pastPregnancies.size + 1
            val para = pastPregnancies.size
            
            canvas.drawText("Total Pregnancies (Gravida): $gravida", margin, yPosition, textPaint)
            yPosition += 20f
            canvas.drawText("Past Deliveries (Para): $para", margin, yPosition, textPaint)
            yPosition += 25f
            
            if (pastPregnancies.isNotEmpty()) {
                val pCol1 = margin
                val pCol2 = margin + 50f
                val pCol3 = margin + 150f
                val pCol4 = margin + 280f
                val pCol5 = margin + 380f
                
                canvas.drawRect(margin, yPosition - 15f, pageWidth - margin, yPosition + 10f, tableBgPaint)
                canvas.drawText("No.", pCol1, yPosition, tableHeaderPaint)
                canvas.drawText("Date", pCol2, yPosition, tableHeaderPaint)
                canvas.drawText("Delivery", pCol3, yPosition, tableHeaderPaint)
                canvas.drawText("Outcome", pCol4, yPosition, tableHeaderPaint)
                canvas.drawText("Complications", pCol5, yPosition, tableHeaderPaint)
                yPosition += 25f
                
                val sdfHist = SimpleDateFormat("MM/yyyy", Locale.getDefault())
                
                pastPregnancies.forEachIndexed { index, history ->
                    if (checkPageBreak(30f)) {
                        canvas.drawRect(margin, yPosition - 15f, pageWidth - margin, yPosition + 10f, tableBgPaint)
                        canvas.drawText("No.", pCol1, yPosition, tableHeaderPaint)
                        canvas.drawText("Date", pCol2, yPosition, tableHeaderPaint)
                        canvas.drawText("Delivery", pCol3, yPosition, tableHeaderPaint)
                        canvas.drawText("Outcome", pCol4, yPosition, tableHeaderPaint)
                        canvas.drawText("Complications", pCol5, yPosition, tableHeaderPaint)
                        yPosition += 25f
                    }
                    
                    canvas.drawText("${index + 1}", pCol1, yPosition, textPaint)
                    canvas.drawText(history.deliveryDate?.let { sdfHist.format(it) } ?: "--", pCol2, yPosition, textPaint)
                    canvas.drawText(history.deliveryType.ifBlank { "--" }, pCol3, yPosition, textPaint)
                    canvas.drawText(history.birthOutcome.ifBlank { "--" }, pCol4, yPosition, textPaint)
                    
                    val comps = if (history.complications.isNotEmpty()) history.complications.joinToString(", ") else "--"
                    // simple truncation if too long
                    val truncComps = if (comps.length > 20) comps.substring(0, 17) + "..." else comps
                    canvas.drawText(truncComps, pCol5, yPosition, textPaint)
                    
                    yPosition += 10f
                    canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint)
                    yPosition += 20f
                }
            }
            yPosition += 10f
            
            // --- PREGNANCY PROGRESS ---
            val stageAndSize = getPregnancyStageAndSize(data.lmpDate, data.dueDate)
            if (stageAndSize != null) {
                checkPageBreak(70f)
                canvas.drawText("Pregnancy Progress", margin, yPosition, sectionTitlePaint)
                yPosition += 25f
                
                canvas.drawText("Stage: ${stageAndSize.first}", margin, yPosition, textPaint)
                yPosition += 20f
                canvas.drawText("Baby Size: ${stageAndSize.second}", margin, yPosition, textPaint)
                yPosition += 30f
            }
        }
        
        if (healthRecords.isNotEmpty()) {
            checkPageBreak(50f)
            canvas.drawText("Health Metrics (Recent)", margin, yPosition, sectionTitlePaint)
            yPosition += 25f
            
            val col1 = margin
            val col2 = margin + 110f
            val col3 = margin + 210f
            val col4 = margin + 310f
            val col5 = margin + 410f
            
            canvas.drawRect(margin, yPosition - 15f, pageWidth - margin, yPosition + 10f, tableBgPaint)
            canvas.drawText("Date", col1, yPosition, tableHeaderPaint)
            canvas.drawText("Steps", col2, yPosition, tableHeaderPaint)
            canvas.drawText("Heart Rate", col3, yPosition, tableHeaderPaint)
            canvas.drawText("Sleep", col4, yPosition, tableHeaderPaint)
            canvas.drawText("SpO2", col5, yPosition, tableHeaderPaint)
            yPosition += 25f
            
            healthRecords.take(30).forEach { record ->
                if (checkPageBreak(30f)) {
                    canvas.drawRect(margin, yPosition - 15f, pageWidth - margin, yPosition + 10f, tableBgPaint)
                    canvas.drawText("Date", col1, yPosition, tableHeaderPaint)
                    canvas.drawText("Steps", col2, yPosition, tableHeaderPaint)
                    canvas.drawText("Heart Rate", col3, yPosition, tableHeaderPaint)
                    canvas.drawText("Sleep", col4, yPosition, tableHeaderPaint)
                    canvas.drawText("SpO2", col5, yPosition, tableHeaderPaint)
                    yPosition += 25f
                }
                
                canvas.drawText(record.date ?: "--", col1, yPosition, textPaint)
                canvas.drawText(record.steps.toString(), col2, yPosition, textPaint)
                canvas.drawText("${record.averageHeartRate ?: "--"} bpm", col3, yPosition, textPaint)
                val hours = record.sleepMinutes / 60
                val mins = record.sleepMinutes % 60
                canvas.drawText("${hours}h ${mins}m", col4, yPosition, textPaint)
                canvas.drawText("${record.spO2 ?: "--"}%", col5, yPosition, textPaint)
                
                yPosition += 10f
                canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint)
                yPosition += 20f
            }
            yPosition += 20f
        }
        
        pdfDocument.finishPage(page)
        
        val fileName = "Primera_Summary_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfDocument.close()
        
        shareFile(context, "", fileName, "application/pdf", file)
    }

    private fun shareFile(context: Context, content: String, fileName: String, mimeType: String, existingFile: File? = null) {
        val file = existingFile ?: File(context.cacheDir, fileName).apply {
            writeText(content)
        }
        
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Export Data"))
    }

    private fun getPregnancyStageAndSize(lmpDate: Date?, dueDate: Date?): Pair<String, String>? {
        val lmp = lmpDate ?: dueDate?.let { 
            val calendar = Calendar.getInstance()
            calendar.time = it
            calendar.add(Calendar.DAY_OF_YEAR, -280)
            calendar.time
        } ?: return null

        val diff = Date().time - lmp.time
        if (diff < 0) return Pair("Pre-pregnancy / Week 0", "N/A")
        val days = (diff / (1000 * 60 * 60 * 24)).toInt()
        val weeks = days / 7
        val trimester = when {
            weeks < 14 -> "1st Trimester"
            weeks < 28 -> "2nd Trimester"
            else -> "3rd Trimester"
        }

        val babySize = when (weeks) {
            in 0..3 -> "Poppy seed"
            4 -> "Poppy seed"
            5 -> "Apple seed"
            6 -> "Sweet pea"
            7 -> "Blueberry"
            8 -> "Raspberry"
            9 -> "Green olive"
            10 -> "Prune"
            11 -> "Lime"
            12 -> "Plum"
            13 -> "Peach"
            14 -> "Lemon"
            15 -> "Apple"
            16 -> "Avocado"
            17 -> "Turnip"
            18 -> "Bell pepper"
            19 -> "Heirloom tomato"
            20 -> "Banana"
            21 -> "Carrot"
            22 -> "Spaghetti squash"
            23 -> "Large mango"
            24 -> "Ear of corn"
            25 -> "Rutabaga"
            26 -> "Scallion"
            27 -> "Cauliflower"
            28 -> "Large eggplant"
            29 -> "Butternut squash"
            30 -> "Large cabbage"
            31 -> "Coconut"
            32 -> "Jicama"
            33 -> "Pineapple"
            34 -> "Cantaloupe"
            35 -> "Honeydew melon"
            36 -> "Romaine lettuce"
            37 -> "Swiss chard"
            38 -> "Leek"
            39 -> "Mini watermelon"
            40 -> "Small pumpkin"
            else -> "Newborn"
        }
        
        return Pair("Week $weeks ($trimester)", babySize)
    }
}
