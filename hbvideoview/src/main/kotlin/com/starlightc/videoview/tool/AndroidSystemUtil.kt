package com.starlightc.videoview.tool

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import com.starlightc.core.Constant

/**
 * @author StarlightC
 * @since 2022/4/24
 *
 * 系统方法调用工具
 */
object AndroidSystemUtil {
    fun setRequestedOrientation(context: Context?, orientation: Int) {
        if (getAppCompActivity(context) != null) {
            getAppCompActivity(context)?.requestedOrientation = orientation
        } else {
            getActivityViaContext(context)?.requestedOrientation = orientation
        }
    }

    fun getRequestedOrientation(context: Context?): Int {
        return if (getAppCompActivity(context) != null) {
            getAppCompActivity(context)?.requestedOrientation ?: 0
        } else {
            getActivityViaContext(context)?.requestedOrientation ?: 0
        }
    }

    fun getAppCompActivity(context: Context?): AppCompatActivity? {
        if (context == null) return null
        if (context is AppCompatActivity) {
            return context
        } else if (context is ContextThemeWrapper) {
            return getAppCompActivity(context.baseContext)
        }
        return null
    }

    fun getActivityViaContext(context: Context?): Activity? {
        if (context == null) return null
        if (context is Activity) {
            return context
        } else if (context is ContextWrapper) {
            return getActivityViaContext(context.baseContext)
        }
        return null
    }

    fun saveSPData(
        context: Context,
        tag: String = Constant.TAG,
        operation: (editor: SharedPreferences.Editor) -> SharedPreferences.Editor
    ) {
        operation.invoke(getSharedPreference(context, tag).edit()).apply()
    }

    fun getSharedPreference(context: Context, name: String = Constant.TAG): SharedPreferences {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }
}