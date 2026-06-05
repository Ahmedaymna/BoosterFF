package com.booster.ff.antilag.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.booster.ff.antilag.R
import com.booster.ff.antilag.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        binding.logoImage.startAnimation(fadeIn)
        binding.appName.startAnimation(slideUp)
        binding.tagline.startAnimation(slideUp)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }, 2500)
    }
}
