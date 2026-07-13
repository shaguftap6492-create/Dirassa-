package com.dirassa.feerecord.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.dirassa.feerecord.FeeRecordApplication
import com.dirassa.feerecord.R
import com.dirassa.feerecord.data.database.AppDatabase
import com.dirassa.feerecord.databinding.ActivitySettingsBinding
import com.dirassa.feerecord.util.BackupHelper
import com.dirassa.feerecord.util.ExcelHelper
import com.dirassa.feerecord.util.PdfHelper
import kotlinx.coroutines.launch

/**
 * Settings screen:
 * - Backup / Restore DB
 * - Export Excel / PDF
 * - Reset All Data
 * - Dark Mode toggle
 * - About
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val app get() = application as FeeRecordApplication

    // File picker for restore
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { restoreDatabase(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings_title)

        setupDarkModeSwitch()

        binding.layoutBackup.setOnClickListener { backupDatabase() }
        binding.layoutRestore.setOnClickListener { filePickerLauncher.launch("application/octet-stream") }
        binding.layoutExportExcel.setOnClickListener { exportExcel() }
        binding.layoutExportPdf.setOnClickListener { exportPdf() }
        binding.layoutReset.setOnClickListener { confirmReset() }
        binding.layoutAbout.setOnClickListener { showAbout() }
    }

    private fun setupDarkModeSwitch() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", false)
        binding.switchDarkMode.isChecked = isDark

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun backupDatabase() {
        lifecycleScope.launch {
            binding.pbLoading.visibility = android.view.View.VISIBLE
            val result = BackupHelper.backupDatabase(this@SettingsActivity)
            binding.pbLoading.visibility = android.view.View.GONE

            result.fold(
                onSuccess = { file ->
                    Toast.makeText(this@SettingsActivity, getString(R.string.backup_success), Toast.LENGTH_SHORT).show()
                    BackupHelper.shareBackup(this@SettingsActivity, file)
                },
                onFailure = {
                    Toast.makeText(this@SettingsActivity, "Backup failed: ${it.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun restoreDatabase(uri: Uri) {
        AlertDialog.Builder(this)
            .setTitle("Restore Database")
            .setMessage("This will overwrite all current data. Continue?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    binding.pbLoading.visibility = android.view.View.VISIBLE
                    val result = BackupHelper.restoreDatabase(this@SettingsActivity, uri)
                    binding.pbLoading.visibility = android.view.View.GONE

                    result.fold(
                        onSuccess = {
                            Toast.makeText(this@SettingsActivity, getString(R.string.restore_success), Toast.LENGTH_SHORT).show()
                        },
                        onFailure = {
                            Toast.makeText(this@SettingsActivity, "Restore failed: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportExcel() {
        lifecycleScope.launch {
            binding.pbLoading.visibility = android.view.View.VISIBLE
            try {
                val students = app.studentRepository.getAllStudentsOnce()
                val feeRecords = app.feeRecordRepository.getAllStudentsWithFeeOnce()
                val file = ExcelHelper.generateExcelReport(this@SettingsActivity, students, feeRecords)
                binding.pbLoading.visibility = android.view.View.GONE
                ExcelHelper.shareExcel(this@SettingsActivity, file)
            } catch (e: Exception) {
                binding.pbLoading.visibility = android.view.View.GONE
                Toast.makeText(this@SettingsActivity, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun exportPdf() {
        lifecycleScope.launch {
            binding.pbLoading.visibility = android.view.View.VISIBLE
            try {
                val feeRecords = app.feeRecordRepository.getAllStudentsWithFeeOnce()
                val file = PdfHelper.generateMonthlyReport(
                    this@SettingsActivity,
                    "All Months",
                    0,
                    feeRecords
                )
                binding.pbLoading.visibility = android.view.View.GONE
                Toast.makeText(this@SettingsActivity, getString(R.string.pdf_generated), Toast.LENGTH_SHORT).show()
                PdfHelper.sharePdf(this@SettingsActivity, file)
            } catch (e: Exception) {
                binding.pbLoading.visibility = android.view.View.GONE
                Toast.makeText(this@SettingsActivity, "PDF failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmReset() {
        AlertDialog.Builder(this)
            .setTitle("⚠ Reset All Data")
            .setMessage(getString(R.string.reset_confirm))
            .setPositiveButton("DELETE ALL") { _, _ -> resetAllData() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetAllData() {
        lifecycleScope.launch {
            try {
                app.studentRepository.deleteAllStudents()
                Toast.makeText(this@SettingsActivity, getString(R.string.reset_success), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Reset failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle("About DIRASSA CLASSES")
            .setMessage(getString(R.string.about_text))
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
