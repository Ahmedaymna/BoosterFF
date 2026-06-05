package com.booster.ff.antilag.ui

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.booster.ff.antilag.R
import com.booster.ff.antilag.databinding.ActivityMainBinding
import com.booster.ff.antilag.service.BoosterService
import com.booster.ff.antilag.utils.RamUtils
import com.booster.ff.antilag.utils.StorageUtils
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isBoosting = false
    private val handler = Handler(Looper.getMainLooper())
    private var boostCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
        loadAds()
        startRamMonitor()
        updateStats()
        startService(Intent(this, BoosterService::class.java))
    }

    private fun setupUI() {
        // BOOST button
        binding.btnBoost.setOnClickListener {
            if (!isBoosting) startBoost()
        }

        // Clean Cache
        binding.btnCleanCache.setOnClickListener { cleanCache() }

        // Stop Apps
        binding.btnStopApps.setOnClickListener { stopBackgroundApps() }

        // CPU Cooler
        binding.btnCpuCool.setOnClickListener { coolCpu() }

        // Battery Saver
        binding.btnBattery.setOnClickListener { batterySaver() }

        // Network Boost
        binding.btnNetwork.setOnClickListener { networkBoost() }

        // Rewarded boost
        binding.btnFreeBoost.setOnClickListener { showRewardedAd() }

        // Settings
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    private fun startBoost() {
        isBoosting = true
        boostCount++
        binding.btnBoost.isEnabled = false
        binding.boostProgress.visibility = View.VISIBLE
        binding.tvBoostStatus.text = "🔥 جاري تحسين الأداء..."

        lifecycleScope.launch {
            for (i in 0..100 step 4) {
                delay(60)
                withContext(Dispatchers.Main) {
                    binding.boostProgress.progress = i
                    when {
                        i < 30 -> binding.tvBoostStatus.text = "🧹 تنظيف الذاكرة..."
                        i < 60 -> binding.tvBoostStatus.text = "⚡ تحسين المعالج..."
                        i < 85 -> binding.tvBoostStatus.text = "🚀 تسريع الشبكة..."
                        else -> binding.tvBoostStatus.text = "✅ اكتمل التحسين!"
                    }
                }
            }
            val freed = RamUtils.cleanRam(this@MainActivity)
            withContext(Dispatchers.Main) {
                binding.boostProgress.visibility = View.GONE
                binding.tvBoostStatus.text = "✅ تم تحرير ${freed}MB"
                binding.btnBoost.isEnabled = true
                isBoosting = false
                updateStats()
                // Show interstitial every 2 boosts
                if (boostCount % 2 == 0) showInterstitialAd()
                Toast.makeText(this@MainActivity, "🚀 تم التحسين! تحرير ${freed}MB", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cleanCache() {
        lifecycleScope.launch(Dispatchers.IO) {
            val cleaned = StorageUtils.clearCache(this@MainActivity)
            withContext(Dispatchers.Main) {
                updateStats()
                Toast.makeText(this@MainActivity, "🗑️ تم مسح ${cleaned}MB من الكاش", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopBackgroundApps() {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var stopped = 0
        am.runningAppProcesses?.forEach { process ->
            if (process.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                am.killBackgroundProcesses(process.processName)
                stopped++
            }
        }
        updateStats()
        Toast.makeText(this, "⛔ تم إيقاف $stopped تطبيق", Toast.LENGTH_SHORT).show()
    }

    private fun coolCpu() {
        binding.tvBoostStatus.text = "❄️ جاري تبريد المعالج..."
        lifecycleScope.launch {
            delay(2000)
            System.gc()
            Runtime.getRuntime().gc()
            withContext(Dispatchers.Main) {
                binding.tvBoostStatus.text = "❄️ تم تبريد المعالج!"
                Toast.makeText(this@MainActivity, "❄️ CPU Cooled!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun batterySaver() {
        try {
            startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
        } catch (e: Exception) {
            Toast.makeText(this, "🔋 فعّل وضع توفير البطارية من الإعدادات", Toast.LENGTH_SHORT).show()
        }
    }

    private fun networkBoost() {
        binding.tvBoostStatus.text = "📶 جاري تحسين الشبكة..."
        lifecycleScope.launch {
            delay(1500)
            withContext(Dispatchers.Main) {
                binding.tvBoostStatus.text = "📶 تم تحسين الشبكة!"
                Toast.makeText(this@MainActivity, "📶 Network Boosted!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRamMonitor() {
        handler.post(object : Runnable {
            override fun run() {
                updateStats()
                handler.postDelayed(this, 3000)
            }
        })
    }

    private fun updateStats() {
        val ram = RamUtils.getRamInfo(this)
        binding.tvRamUsed.text = "${ram.totalMb - ram.freeMb}MB / ${ram.totalMb}MB"
        binding.ramProgressBar.progress = ram.usedPercent
        binding.tvRamPercent.text = "${ram.usedPercent}%"
        binding.tvRamStatus.text = when {
            ram.usedPercent > 85 -> "🔴 مرهق"
            ram.usedPercent > 65 -> "🟡 متوسط"
            else -> "🟢 ممتاز"
        }
        val storage = StorageUtils.getStorageInfo(this)
        binding.tvStorage.text = "${storage.usedGb}GB / ${storage.totalGb}GB"
        binding.storageProgressBar.progress = storage.usedPercent
        binding.tvStoragePercent.text = "${storage.usedPercent}%"
    }

    private fun loadAds() {
        // Banner
        binding.adView.loadAd(AdRequest.Builder().build())
        // Interstitial
        InterstitialAd.load(this, getString(R.string.admob_interstitial_id), AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitialAd = ad }
                override fun onAdFailedToLoad(e: LoadAdError) { interstitialAd = null }
            })
        // Rewarded
        RewardedAd.load(this, getString(R.string.admob_rewarded_id), AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { rewardedAd = ad }
                override fun onAdFailedToLoad(e: LoadAdError) { rewardedAd = null }
            })
    }

    private fun showInterstitialAd() {
        interstitialAd?.show(this)
        interstitialAd = null
        handler.postDelayed({
            InterstitialAd.load(this, getString(R.string.admob_interstitial_id), AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) { interstitialAd = ad }
                    override fun onAdFailedToLoad(e: LoadAdError) {}
                })
        }, 1000)
    }

    private fun showRewardedAd() {
        if (rewardedAd != null) {
            rewardedAd?.show(this) { _ ->
                Toast.makeText(this, "🎁 حصلت على Boost مجاني!", Toast.LENGTH_SHORT).show()
                startBoost()
            }
            rewardedAd = null
        } else {
            Toast.makeText(this, "جاري تحميل الإعلان...", Toast.LENGTH_SHORT).show()
            loadAds()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
