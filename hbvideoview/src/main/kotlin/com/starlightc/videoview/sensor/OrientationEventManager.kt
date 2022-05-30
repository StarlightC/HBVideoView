package com.starlightc.videoview.sensor

import android.content.Context
import android.content.pm.ActivityInfo
import android.provider.Settings
import android.util.Log
import android.view.OrientationEventListener
import com.starlightc.videoview.tool.AndroidSystemUtil
import com.starlightc.videoview.widget.AbsVideoView

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * 重力感应方向管理器
 */
class OrientationEventManager {
    var isLockByUser = false
    var isFullscreen = false
    private var currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    private var orientationListenerDelayTime: Long = 0
    private var isRotateLocked = 0 //0 代表方向锁定，1 代表没方向锁定
    private var mOrientationChangeListener: OnOrientationChangeListener? = null
    private var orientationEventListener: OrientationEventListener? = null //加速度传感器监听

    fun orientationDisable() {
        orientationEventListener?.disable()
    }

    fun orientationEnable(){
        orientationEventListener?.enable()
    }

    fun orientationEnable(context: Context, videoView: AbsVideoView, orientationChangeListener: OnOrientationChangeListener?) {
        mOrientationChangeListener = orientationChangeListener
        orientationEventListener = object : OrientationEventListener(context, 5) {
            // 加速度传感器监听，用于自动旋转屏幕
            override fun onOrientationChanged(orientation: Int) {
                if (isFullscreen) {
                    Log.d(
                        javaClass.name,
                        "onOrientationChanged() called with orientation: $orientation"
                    );
                    try {
                        //系统是否开启方向锁定
                        isRotateLocked = Settings.System.getInt(
                            context.contentResolver,
                            Settings.System.ACCELEROMETER_ROTATION
                        )
                    } catch (e: Settings.SettingNotFoundException) {
                        e.printStackTrace()
                    }
                    if (isRotateLocked == 0) return  //方向被锁定，直接返回
                    val operationDelay =
                        System.currentTimeMillis() - orientationListenerDelayTime > 500
                    if ((orientation >= 300 || orientation <= 30) && operationDelay) {
                        //屏幕顶部朝上
                        onOrientationPortrait(videoView)
                        orientationListenerDelayTime = System.currentTimeMillis()
                    } else if (orientation in 260..280 && operationDelay) {
                        //屏幕左边朝上
                        onOrientationLandscape(videoView)
                        orientationListenerDelayTime = System.currentTimeMillis()
                    } else if (orientation in 70..90 && operationDelay) {
                        //屏幕右边朝上
                        onOrientationReverseLandscape(videoView)
                        orientationListenerDelayTime = System.currentTimeMillis()
                    }
                }
            }
        }
        currentOrientation = AndroidSystemUtil.getRequestedOrientation(context)
        orientationEventListener?.enable()
    }

    /**
     * 横屏(屏幕左边朝上)
     */
    private fun onOrientationLandscape(videoView: AbsVideoView) {
        if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) return
        if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) return
        currentOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        if (isLockByUser)
            return
        mOrientationChangeListener?.onOrientationLandscape()
    }

    /**
     * 反向横屏(屏幕右边朝上)
     */
    private fun onOrientationReverseLandscape(videoView: AbsVideoView) {
        if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) return
        if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) return
        currentOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        if (isLockByUser)
            return
        mOrientationChangeListener?.onOrientationReverseLandscape()
    }

    /**
     * 竖屏
     */
    private fun onOrientationPortrait(videoView: AbsVideoView) {
        /*
        if ((currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || currentOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)) {
            return
        }
        if (isLockByUser) {d
            mOrientationChangeListener?.onLockedByUser(videoView)
        }
        currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        mOrientationChangeListener?.onOrientationPortrait(videoView)
        */
    }

    /**
     * 设置重力感应横竖屏管理
     */
    fun setOnOrientationChangeListener(orientationChangeListener: OnOrientationChangeListener?) {
        mOrientationChangeListener = orientationChangeListener
    }

    interface OnOrientationChangeListener {
        fun onOrientationLandscape()
        fun onOrientationReverseLandscape()
        fun onOrientationPortrait()
    }
}