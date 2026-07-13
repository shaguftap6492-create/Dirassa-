package com.dirassa.feerecord.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.dirassa.feerecord.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper for backing up and restoring the Room database file.
 */
object BackupHelper {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())

    /**
     * Copy the database file to the app's files directory and return the backup file.
     * The user can then share it via the share sheet.
     */
    suspend fun backupDatabase(context: Context): Result<File> = withContext(Dispatchers.IO) {
        try {
            // Close and checkpoint WAL
            AppDatabase.getInstance(context).close()
            AppDatabase.destroyInstance()

            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Database file not found"))
            }

            val backupName = "DIRASSA_Backup_${dateFormat.format(Date())}.db"
            val backupFile = File(context.filesDir, backupName)

            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }
            Result.success(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Restore database from a URI (selected by the user via file picker).
     */
    suspend fun restoreDatabase(context: Context, sourceUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Close current database connection
            AppDatabase.getInstance(context).close()
            AppDatabase.destroyInstance()

            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            dbFile.parentFile?.mkdirs()

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Could not open file"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Share a backup file */
    fun shareBackup(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "DIRASSA Fee Record Backup")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Backup"))
    }
}
