package com.dirassa.feerecord.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.dirassa.feerecord.R
import com.dirassa.feerecord.databinding.ActivityHomeBinding
import com.dirassa.feerecord.ui.fee.FeeRecordActivity
import com.dirassa.feerecord.ui.report.MonthlyReportActivity
import com.dirassa.feerecord.ui.settings.SettingsActivity
import com.dirassa.feerecord.ui.student.AddEditStudentActivity

/**
 * Main home screen with four navigation cards:
 * Add Student | Fee Record | Monthly Report | Settings
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Play staggered card entrance animations
        animateCards()

        // Navigation buttons
        binding.cardAddStudent.setOnClickListener {
            startActivity(Intent(this, AddEditStudentActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.cardFeeRecord.setOnClickListener {
            startActivity(Intent(this, FeeRecordActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.cardMonthlyReport.setOnClickListener {
            startActivity(Intent(this, MonthlyReportActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.cardSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun animateCards() {
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
        val cards = listOf(
            binding.cardAddStudent,
            binding.cardFeeRecord,
            binding.cardMonthlyReport,
            binding.cardSettings
        )
        cards.forEachIndexed { index, card ->
            card.postDelayed({
                card.startAnimation(slideUp)
            }, index * 100L)
        }
    }

    override fun onBackPressed() {
        // Double-back-press to exit
        super.onBackPressed()
        finishAffinity()
    }
}
