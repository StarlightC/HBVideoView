package com.starlightc.videoview.tool

import java.text.NumberFormat
import java.util.*

/**
 * @author StarlightC
 * @since 2022/4/24
 *
 * 格式化工具
 */
object FormatUtil {
    const val KILO_BIT = 1024
    const val MEGA_BIT = 1024 * KILO_BIT
    const val GIGA_BIT = 1024 * MEGA_BIT

    fun convertNetworkSpeed2String(speed: Long): String? {
        var result: String? = null
        if (speed >= GIGA_BIT) {
            result = String.format("%.2f%s",speed.toFloat() / GIGA_BIT, "GB/s")
        } else if (speed >=  MEGA_BIT) {
            result = String.format("%.2f%s",speed.toFloat() / MEGA_BIT, "MB/s")
        } else if (speed >= KILO_BIT) {
            result = "${speed / KILO_BIT}KB/s"
        } else if (speed >= 0) {
            result = "${speed}B/s"
        }
        return result
    }

    fun getPercent(value: Double): String? {
        val numberFormat = NumberFormat.getPercentInstance(Locale.US)
        numberFormat.minimumFractionDigits = 0
        numberFormat.maximumFractionDigits = 0
        numberFormat.isGroupingUsed = false
        return numberFormat.format(value)
    }



    fun getTimeString(duration: Long): String {
        val totalSeconds = duration / 1000
        val hours = totalSeconds / 3600
        val minutes = totalSeconds % 3600 / 60
        val seconds = totalSeconds % 60
        if (duration <= 0) {
            return "--:--"
        }
        return when {
            hours >= 100 -> {
                String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            }
            hours > 0 -> {
                String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
            }
            else -> {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }
    }
}