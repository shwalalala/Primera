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
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.EnumMap
import java.util.Locale

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

    fun createSummaryPdfFile(
        context: Context,
        healthRecords: List<SmartwatchHealth>,
        dashboardData: DashboardData? = null
    ): File {
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
                
                canvas.drawText(record.date, col1, yPosition, textPaint)
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

        return file
    }

    suspend fun uploadSummaryPdfAndGetUrl(
        context: Context,
        healthRecords: List<SmartwatchHealth>,
        dashboardData: DashboardData? = null
    ): String {
        val pdfFile = createSummaryPdfFile(context, healthRecords, dashboardData)

        val publicUrl = uploadPdfToPublicHost(pdfFile)
        if (!publicUrl.isNullOrBlank()) {
            android.util.Log.d("DataExportHelper", "Public PDF URL generated: $publicUrl")
            return publicUrl
        }

        try {
            val bytes = pdfFile.readBytes()
            val userId =
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                    .ifBlank { "guest" }
            val fileName = "summary_${userId}_${System.currentTimeMillis()}.pdf"

            val storage = try {
                com.google.firebase.storage.FirebaseStorage.getInstance("gs://primera-eafe6.firebasestorage.app")
            } catch (_: Exception) {
                com.google.firebase.storage.FirebaseStorage.getInstance()
            }

            val storageRef = storage.reference.child("profile_pictures/$fileName")
            storageRef.putBytes(bytes).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            if (downloadUrl.isNotBlank()) {
                android.util.Log.d("DataExportHelper", "PDF Uploaded successfully: $downloadUrl")
                return downloadUrl
            }
        } catch (e: Exception) {
            android.util.Log.e("DataExportHelper", "Firebase Storage upload failed", e)
        }

        val localServerUrl = LocalPdfServer.startServer(pdfFile)
        if (!localServerUrl.isNullOrBlank()) {
            android.util.Log.d("DataExportHelper", "Using Local PDF Server URL: $localServerUrl")
            return localServerUrl
        }

        return "file://${pdfFile.absolutePath}"
    }

    private suspend fun uploadPdfToPublicHost(pdfFile: File): String? =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            // Attempt 1: tmpfiles.org
            try {
                val boundary = "---PrimeraBoundary" + System.currentTimeMillis()
                val url = java.net.URL("https://tmpfiles.org/api/v1/upload")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                )
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                conn.connectTimeout = 12000
                conn.readTimeout = 12000

                conn.outputStream.use { os ->
                    val writer = java.io.PrintWriter(java.io.OutputStreamWriter(os, "UTF-8"), true)
                    writer.append("--$boundary\r\n")
                    writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"${pdfFile.name}\"\r\n")
                    writer.append("Content-Type: application/pdf\r\n\r\n")
                    writer.flush()

                    pdfFile.inputStream().use { input ->
                        input.copyTo(os)
                    }
                    os.flush()

                    writer.append("\r\n--$boundary--\r\n")
                    writer.flush()
                }

                val code = conn.responseCode
                android.util.Log.d("DataExportHelper", "tmpfiles.org response code: $code")
                if (code == 200) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    android.util.Log.d("DataExportHelper", "tmpfiles.org response: $response")
                    val urlIndex = response.indexOf("\"url\":\"")
                    if (urlIndex != -1) {
                        val start = urlIndex + 7
                        val end = response.indexOf("\"", start)
                        if (end != -1) {
                            val rawUrl = response.substring(start, end).replace("\\/", "/")
                            return@withContext rawUrl.replace("tmpfiles.org/", "tmpfiles.org/dl/")
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DataExportHelper", "tmpfiles.org upload failed", e)
            }

            // Attempt 2: file.io
            try {
                val boundary = "---PrimeraBoundary" + System.currentTimeMillis()
                val url = java.net.URL("https://file.io")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                )
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                conn.connectTimeout = 12000
                conn.readTimeout = 12000

                conn.outputStream.use { os ->
                    val writer = java.io.PrintWriter(java.io.OutputStreamWriter(os, "UTF-8"), true)
                    writer.append("--$boundary\r\n")
                    writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"${pdfFile.name}\"\r\n")
                    writer.append("Content-Type: application/pdf\r\n\r\n")
                    writer.flush()

                    pdfFile.inputStream().use { input ->
                        input.copyTo(os)
                    }
                    os.flush()

                    writer.append("\r\n--$boundary--\r\n")
                    writer.flush()
                }

                if (conn.responseCode == 200) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val linkIndex = response.indexOf("\"link\":\"")
                    if (linkIndex != -1) {
                        val start = linkIndex + 8
                        val end = response.indexOf("\"", start)
                        if (end != -1) {
                            return@withContext response.substring(start, end).replace("\\/", "/")
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DataExportHelper", "file.io upload failed", e)
            }

            null
        }

    object LocalPdfServer {
        private var serverSocket: java.net.ServerSocket? = null
        private var currentPdfFile: File? = null

        fun startServer(pdfFile: File): String? {
            currentPdfFile = pdfFile
            val ip = getLocalIpAddress() ?: return null
            if (serverSocket != null && !serverSocket!!.isClosed) {
                return "http://$ip:${serverSocket!!.localPort}/Primera_Summary.pdf"
            }
            return try {
                val socket = java.net.ServerSocket(0)
                serverSocket = socket
                val port = socket.localPort

                val thread = Thread {
                    while (!socket.isClosed) {
                        try {
                            val client = socket.accept()
                            handleClient(client)
                        } catch (_: Exception) {
                        }
                    }
                }
                thread.isDaemon = true
                thread.start()

                "http://$ip:$port/Primera_Summary.pdf"
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        private fun handleClient(client: java.net.Socket) {
            try {
                val file = currentPdfFile ?: return
                val bytes = file.readBytes()
                val output = client.getOutputStream()
                val header = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: application/pdf\r\n" +
                        "Content-Disposition: inline; filename=\"Primera_Summary.pdf\"\r\n" +
                        "Content-Length: ${bytes.size}\r\n" +
                        "Connection: close\r\n\r\n"
                output.write(header.toByteArray())
                output.write(bytes)
                output.flush()
                client.close()
            } catch (_: Exception) {
            }
        }

        private fun getLocalIpAddress(): String? {
            try {
                val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val intf = interfaces.nextElement()
                    val addrs = intf.inetAddresses
                    while (addrs.hasMoreElements()) {
                        val addr = addrs.nextElement()
                        if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                            val host = addr.hostAddress
                            if (host != null && host != "127.0.0.1" && host != "10.0.2.15") {
                                return host
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }
            return null
        }
    }

    fun exportSummaryToPdf(
        context: Context,
        healthRecords: List<SmartwatchHealth>,
        dashboardData: DashboardData? = null
    ) {
        val file = createSummaryPdfFile(context, healthRecords, dashboardData)
        sharePdfFile(context, file)
    }

    fun generateSummaryText(
        healthRecords: List<SmartwatchHealth>,
        dashboardData: DashboardData? = null
    ): String {
        val sb = StringBuilder()
        sb.append("PRIMERA HEALTH SUMMARY\n")
        sb.append("======================\n")

        dashboardData?.let { data ->
            val fullName = listOfNotNull(data.firstName, data.lastName).joinToString(" ").trim()
            if (fullName.isNotBlank()) {
                sb.append("Patient: $fullName\n")
            }
            val stageAndSize = getPregnancyStageAndSize(data.lmpDate, data.dueDate)
            if (stageAndSize != null) {
                sb.append("Stage: ${stageAndSize.first}\n")
                sb.append("Baby Size: ${stageAndSize.second}\n")
            }
            if (data.weightKg != null && data.heightCm != null && data.heightCm > 0) {
                val heightM = data.heightCm / 100f
                val bmi = data.weightKg / (heightM * heightM)
                sb.append(
                    "Pre-pregnancy Weight: ${data.weightKg} kg | Height: ${data.heightCm} cm | BMI: ${
                        String.format(
                            Locale.US,
                            "%.1f",
                            bmi
                        )
                    }\n"
                )
            }
        }

        if (healthRecords.isNotEmpty()) {
            val latest = healthRecords.first()
            sb.append("\nRecent Health Vitals:\n")
            sb.append("• Steps: ${latest.steps}\n")
            sb.append("• Heart Rate: ${latest.averageHeartRate ?: "--"} bpm\n")
            val hours = latest.sleepMinutes / 60
            val mins = latest.sleepMinutes % 60
            sb.append("• Sleep: ${hours}h ${mins}m\n")
            sb.append("• SpO2: ${latest.spO2 ?: "--"}%\n")
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sb.append("\nGenerated on: ${sdf.format(Date())}")
        sb.append("\nPrimera App")
        return sb.toString()
    }

    fun generateQrCodeBitmap(
        content: String,
        width: Int = 512,
        height: Int = 512,
        foregroundColor: Int = Color.parseColor("#6A4C93"),
        backgroundColor: Int = Color.WHITE
    ): android.graphics.Bitmap {
        val hints =
            EnumMap<com.google.zxing.EncodeHintType, Any>(com.google.zxing.EncodeHintType::class.java).apply {
                put(com.google.zxing.EncodeHintType.MARGIN, 1)
                put(com.google.zxing.EncodeHintType.CHARACTER_SET, "UTF-8")
            }
        val bitMatrix = com.google.zxing.qrcode.QRCodeWriter()
            .encode(content, com.google.zxing.BarcodeFormat.QR_CODE, width, height, hints)
        val bmp = android.graphics.Bitmap.createBitmap(
            width,
            height,
            android.graphics.Bitmap.Config.ARGB_8888
        )
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) foregroundColor else backgroundColor)
            }
        }
        return bmp
    }

    fun savePdfToDownloads(context: Context, pdfFile: File): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "Primera_Summary_${sdf.format(Date())}.pdf"
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(
                        android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                        android.os.Environment.DIRECTORY_DOWNLOADS
                    )
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(
                    android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { output ->
                        pdfFile.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    android.widget.Toast.makeText(
                        context,
                        "Summary saved to Downloads",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    true
                } else {
                    android.widget.Toast.makeText(
                        context,
                        "Failed to save PDF",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    false
                }
            } else {
                val downloadsDir =
                    android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                val destFile = File(downloadsDir, fileName)
                pdfFile.copyTo(destFile, overwrite = true)
                android.widget.Toast.makeText(
                    context,
                    "Summary saved to Downloads",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(
                context,
                "Error saving PDF: ${e.localizedMessage}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            false
        }
    }

    fun printPdfFile(context: Context, pdfFile: File) {
        val printManager =
            context.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager
        if (printManager == null) {
            android.widget.Toast.makeText(
                context,
                "Printing unavailable on this device",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        val jobName = "Primera Health Summary ${
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }"
        val printAdapter = object : android.print.PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: android.print.PrintAttributes?,
                newAttributes: android.print.PrintAttributes,
                cancellationSignal: android.os.CancellationSignal?,
                callback: LayoutResultCallback,
                extras: android.os.Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }
                val builder = android.print.PrintDocumentInfo.Builder("Primera_Health_Summary.pdf")
                    .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(android.print.PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build()
                callback.onLayoutFinished(builder, true)
            }

            override fun onWrite(
                pages: Array<out android.print.PageRange>?,
                destination: android.os.ParcelFileDescriptor,
                cancellationSignal: android.os.CancellationSignal?,
                callback: WriteResultCallback
            ) {
                try {
                    java.io.FileInputStream(pdfFile).use { input ->
                        FileOutputStream(destination.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.message)
                }
            }
        }
        printManager.print(jobName, printAdapter, null)
    }

    fun sharePdfFile(context: Context, pdfFile: File) {
        shareFile(context, "", pdfFile.name, "application/pdf", pdfFile)
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
