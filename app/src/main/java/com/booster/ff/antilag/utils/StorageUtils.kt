package com.booster.ff.antilag.utils

import android.content.Context
import android.os.Environment
import android.os.StatFs
import java.io.File

data class StorageInfo(val totalGb: String, val usedGb: String, val usedPercent: Int)

object StorageUtils {

    fun getStorageInfo(context: Context): StorageInfo {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availBlocks = stat.availableBlocksLong
        val totalBytes = totalBlocks * blockSize
        val freeBytes = availBlocks * blockSize
        val usedBytes = totalBytes - freeBytes
        val totalGb = String.format("%.1f", totalBytes / 1073741824.0)
        val usedGb = String.format("%.1f", usedBytes / 1073741824.0)
        val usedPercent = ((usedBytes.toFloat() / totalBytes) * 100).toInt()
        return StorageInfo(totalGb, usedGb, usedPercent)
    }

    fun clearCache(context: Context): Int {
        var cleared = 0L
        try {
            val cacheDir = context.cacheDir
            cleared += deleteDir(cacheDir)
            val externalCache = context.externalCacheDir
            if (externalCache != null) cleared += deleteDir(externalCache)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (cleared / 1048576).toInt().coerceAtLeast(1)
    }

    private fun deleteDir(dir: File?): Long {
        if (dir == null || !dir.isDirectory) return 0
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) deleteDir(file)
            else { val s = file.length(); file.delete(); s }
        }
        return size
    }
}
