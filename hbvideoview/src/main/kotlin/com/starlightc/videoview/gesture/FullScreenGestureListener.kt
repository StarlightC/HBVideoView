package com.starlightc.videoview.gesture

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.starlightc.core.infomation.PlayerState
import com.starlightc.videoview.tool.AndroidSystemUtil
import com.starlightc.core.Constant
import com.starlightc.videoview.widget.AbsVideoView
import kotlin.math.abs

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * 全屏手势监听
 */
class FullScreenGestureListener(
    private val target: AbsVideoView,
    var isVolumeGestureEnable:Boolean, //是否开启音量手势
    var isBrightnessGestureEnable: Boolean, //是否开启亮度手势
    var isProgressGestureEnable: Boolean //是否开启进度拖动手势
) : GestureDetector.SimpleOnGestureListener(), View.OnTouchListener {
    private var currentX = 0f
    private var currentY = 0f
    private var currentWidth = 0f
    private var currentHeight = 0f

    private var currentVolume: Int = 0
    private var currentBrightness: Float = 0f
    private var currentProgress: Long = 0
    private var duration: Long = 0
    private var progressOffset: Long = 0
    private var originalSpeed: Int = 100

    private var isFirstTouch = false //按住屏幕不放的第一次点击，则为true
    private var isChangeProgress = false//判断是改变进度条则为true，否则为false
    private var isChangeBrightness = false //判断是不是改变亮度的操作
    private var isChangeVolume = false //判断是不是改变音量的操作
    var isEnableDoubleTap = true //是否使用双击暂停

    override fun onDown(e: MotionEvent): Boolean {
        currentX = target.x
        currentY = target.y
        currentWidth = target.width.toFloat()
        currentHeight = target.height.toFloat()

        isFirstTouch = true
        isChangeProgress = false
        isChangeVolume = false
        isChangeBrightness = false
        currentVolume = target.audioManager.getVolume()
        currentBrightness =
            AndroidSystemUtil.getActivityViaContext(target.context)?.window?.attributes?.screenBrightness
                ?: 0f
        currentProgress = target.currentPosition
        duration = target.duration
        originalSpeed = target.speed
        return true
    }

    fun enableGesture(enable: Boolean) {
        isBrightnessGestureEnable = enable
        isProgressGestureEnable = enable
        isVolumeGestureEnable = enable
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (e1 == null || e2 == null) {
            return false
        }
        //全屏
        if (e2.pointerCount == 1) { //单指移动
            val mOldX = e1.x
            val mOldY = e1.y
            val x = e2.rawX.toInt()
            val y = e2.rawY.toInt()
            Log.d(
                "GestureTest",
                "mOldX: $mOldX mOldY: $mOldY  currentWidth: $currentWidth  currentHeight: $currentHeight isFirst: $isFirstTouch"
            )
            if (isFirstTouch) {
                isChangeProgress =
                    abs(distanceX) >= abs(distanceY) && (mOldY > currentHeight * 1.0 / 4) && (mOldY < currentHeight * 3.0 / 4)
                if (!isChangeProgress) {
                    if (mOldX > currentWidth * 2.0 / 3) { //右边三分之一区域滑动
                        isChangeVolume = true && isVolumeGestureEnable
                    } else if (mOldX < currentWidth / 3.0) { //左边三分之一区域滑动
                        isChangeBrightness = true && isBrightnessGestureEnable
                    }
                }
                isChangeProgress = isChangeProgress && isProgressGestureEnable
                isFirstTouch = false
            }
            if (isChangeProgress) {
                onSeekProgressControl((x - mOldX) / currentWidth)
            } else if (isChangeBrightness) {
                Log.d("GestureTest", "Brightness")
                onBrightnessSlide((mOldY - y) / currentHeight)
            } else if (isChangeVolume) {
                Log.d("GestureTest", "Volume")
                onVolumeSlide((mOldY - y) / currentHeight)
            }
            return true
        }
        return false
    }

    /**
     * 滑动改变播放的快进快退
     *
     * @param seekDistance
     */
    private fun onSeekProgressControl(percent: Float) {
        val offset = (currentProgress + percent * duration).toLong()
        progressOffset = if (offset <= 10) 10 else if (offset > duration) duration else offset
        target.videoUI?.showProgressChange((-1 * percent * 100).toLong(), progressOffset)
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private fun onVolumeSlide(percent: Float) {
        val maxVolume = target.audioManager.getMaxVolume()
        var index: Float = currentVolume + percent * maxVolume
        index = if (index > maxVolume) maxVolume.toFloat() else if (index < 0) 0f else index
        // 变更声音
        target.setVolume(index.toInt())
        val unit = 100f / maxVolume.toFloat()
        var pbProgress =
            ((currentVolume.toFloat() / maxVolume.toFloat() + percent) * 100 - unit + 1).toInt()
        if (pbProgress > 100) {
            pbProgress = 100
        } else if (pbProgress < 0) {
            pbProgress = 0
        }
        target.videoUI?.showVolumeChange(pbProgress)
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private fun onBrightnessSlide(percent: Float) {
        val activity = AndroidSystemUtil.getActivityViaContext(target.context) ?: return
        val attributes = activity.window.attributes
        var brightness = currentBrightness + percent * 1.5f
        brightness = if (brightness > 1.0f) 1.0f else if (brightness < 0.1f) 0.1f else brightness
        attributes.screenBrightness = brightness
        activity.window.attributes = attributes
        target.videoUI?.showBrightnessChange(attributes)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_UP) {
            if (isChangeProgress) {
                target.seekTo(progressOffset)
            }
            isChangeProgress = false
            isChangeVolume = false
            isChangeBrightness = false
            target.normalPlay()
        }
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        target.videoUI?.showController(Constant.CONTROLLER_HIDE_TIME)
        return target.performClick()
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        if (isEnableDoubleTap) {
            //双击播放或暂停
            if (target.isPlaying) {
                target.pause()
            } else if (target.playerState.code > PlayerState.PREPARED.code) {
                target.start()
            }
        }
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
        target.fastPlay()
    }
}