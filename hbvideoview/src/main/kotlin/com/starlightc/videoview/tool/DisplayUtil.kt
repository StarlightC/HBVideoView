package com.starlightc.videoview.tool

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.starlightc.video.core.Constant

/**
 * @author StarlightC
 * @since 2022/4/24
 *
 * 显示相关工具
 */
object DisplayUtil {
    var mHandler: Handler? = null

    fun getViewWidth(view: View): Int {
        return if (view.width > 0) {
            view.width
        } else if (view.layoutParams != null && view.layoutParams.width > 0) {
            view.layoutParams.width
        } else {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            view.measuredWidth
        }
    }

    fun getViewHeight(view: View): Int {
        return if (view.height > 0) {
            view.height
        } else if (view.layoutParams != null && view.layoutParams.height > 0) {
            view.layoutParams.height
        } else {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            view.measuredHeight
        }
    }

    fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dp(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun getScreenWidth(context: Context): Int {
        val wm = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        val wm = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.heightPixels
    }

    fun lightStatusBar(activity: Activity, isLight : Boolean) {
        // 修改状态栏字体颜色，用AndroidX官方兼容API
        val wic: WindowInsetsControllerCompat? = ViewCompat.getWindowInsetsController(activity.window.decorView)
        wic?.isAppearanceLightStatusBars = isLight
    }

    @SuppressLint("RestrictedApi")
    fun hideSupportActionBar(context: Context?) {
        if (AndroidSystemUtil.getAppCompActivity(context) != null) {
            val ab = AndroidSystemUtil.getAppCompActivity(context)?.supportActionBar
            if (ab != null) {
                ab.setShowHideAnimationEnabled(false)
                ab.hide()
            }
        }
        getWindow(context)?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    @SuppressLint("RestrictedApi")
    fun showSupportActionBar(context: Context?) {
        if (AndroidSystemUtil.getAppCompActivity(context) != null) {
            val actionBar = AndroidSystemUtil.getAppCompActivity(context)?.supportActionBar
            if (actionBar != null) {
                actionBar.setShowHideAnimationEnabled(false)
                actionBar.show()
            }
        }
        getWindow(context)?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun getWindow(context: Context?): Window? {
        return if (AndroidSystemUtil.getAppCompActivity(context) != null) {
            AndroidSystemUtil.getAppCompActivity(context)?.window
        } else {
            AndroidSystemUtil.getActivityViaContext(context)?.window
        }
    }

    fun hideSystemUINow(activity: Activity) {
        activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    fun hideSystemUI(activity: Activity) {
        activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        activity.window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->

            Log.d(Constant.TAG, "SystemUIVisibilityChanged!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!              $visibility")
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                val runnable = Runnable {
                    activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                            // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN)
                }
                prepareHandler()
                mHandler?.postDelayed(runnable,2000)
            }
        }
    }

    fun showSystemUI(activity: Activity, system_ui_visibility: Int) {
        activity.window.decorView.setOnSystemUiVisibilityChangeListener { }
        mHandler?.removeCallbacksAndMessages(null)
        activity.window.decorView.systemUiVisibility = system_ui_visibility
    }

    private fun prepareHandler() {
        if (mHandler == null) {
            if (Looper.getMainLooper().thread != Thread.currentThread()) {
                Looper.prepare()
            }
            mHandler = Handler()
        }
    }

    fun setTransparentForWindow(window: Window?) {
        window!!.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    fun immersive(activity: Activity) :Int {
        val decorView = activity.window.decorView
        val ret = decorView.systemUiVisibility
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        return ret
    }
}