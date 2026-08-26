package com.example.primera.core.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.primera.feature.checkins.data.CheckinLogDto
import com.example.primera.feature.smartwatchconnection.domain.SmartwatchHealth
import java.io.File
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

        shareFile(context, csvBuilder.toString(), "Primera_Health_Data.csv")
    }

    private fun shareFile(context: Context, content: String, fileName: String) {
        val file = File(context.cacheDir, fileName)
        file.writeText(content)
        
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Export Data"))
    }
}
