package com.dirassa.feerecord.ui.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dirassa.feerecord.databinding.ActivitySplashBinding
import com.dirassa.feerecord.ui.home.HomeActivity

/**
 * Splash screen shown for 2 seconds on app launch.
 * Plays a logo animation then navigates to HomeActivity.
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate logo and title
        playEntryAnimation()

        // Navigate after 2.5 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 2500)
    }

    private fun playEntryAnimation() {
        // Logo: scale from 0 + fade in
        binding.ivLogo.apply {
            scaleX = 0f; scaleY = 0f; alpha = 0f
        }
        binding.tvAppName.apply { alpha = 0f; translationY = 40f }
        binding.tvTagline.apply { alpha = 0f; translationY = 40f }

        val logoScaleX = ObjectAnimator.ofFloat(binding.ivLogo, View.SCALE_X, 0f, 1f).apply { duration = 600 }
        val logoScaleY = ObjectAnimator.ofFloat(binding.ivLogo, View.SCALE_Y, 0f, 1f).apply { duration = 600 }
        val logoAlpha  = ObjectAnimator.ofFloat(binding.ivLogo, View.ALPHA, 0f, 1f).apply { duration = 600 }

        val titleAlpha = ObjectAnimator.ofFloat(binding.tvAppName, View.ALPHA, 0f, 1f).apply {
            duration = 500; startDelay = 500
        }
        val titleY = ObjectAnimator.ofFloat(binding.tvAppName, View.TRANSLATION_Y, 40f, 0f).apply {
            duration = 500; startDelay = 500
        }
        val tagAlpha = ObjectAnimator.ofFloat(binding.tvTagline, View.ALPHA, 0f, 1f).apply {
            duration = 500; startDelay = 700
        }
        val tagY = ObjectAnimator.ofFloat(binding.tvTagline, View.TRANSLATION_Y, 40f, 0f).apply {
            duration = 500; startDelay = 700
        }

        AnimatorSet().apply {
            playTogether(logoScaleX, logoScaleY, logoAlpha, titleAlpha, titleY, tagAlpha, tagY)
            start()
        }
    }
}
