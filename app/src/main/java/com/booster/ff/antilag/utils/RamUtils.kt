package com.booster.ff.antilag.utils

import android.app.ActivityManager
import android.content.Context

data class RamInfo(val totalMb: Int, val freeMb: Int, val usedPercent: Int)

object RamUtils {

    fun getRamInfo(context: Context): RamInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        val totalMb = (memInfo.totalMem / 1048576).toInt()
        val freeMb = (memInfo.availMem / 1048576).toInt()
        val usedPercent = ((totalMb - freeMb).toFloat() / totalMb * 100).toInt()
        return RamInfo(totalMb, freeMb, usedPercent)
    }

    fun cleanRam(context: Context): Int {
        val before = getRamInfo(context).freeMb
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = am.runningAppProcesses ?: return 0
        for (process in processes) {
            if (process.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                am.killBackgroundProcesses(process.processName)
            }
        }
        // Trigger GC
        System.runFinalization()
        Runtime.getRuntime().gc()
        System.gc()
        val after = getRamInfo(context).freeMb
        return maxOf(after - before, 10)
    }
}
