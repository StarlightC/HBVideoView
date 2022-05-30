package com.starlightc.videoview.widget

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import com.starlightc.core.Constant
import com.starlightc.core.SimpleLogger
import com.starlightc.videoview.config.ErrorCode
import com.starlightc.videoview.config.InfoCode
import com.starlightc.videoview.config.WindowMode
import com.starlightc.videoview.interfaces.VideoUI

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * 基础的UI
 */
class BaseUI(context: Context): FrameLayout(context), VideoUI {
    private val eventHandler = VideoHandler(Looper.getMainLooper())

    private var isSeekBarDragging = false
    private var isControllerVisible = false

    private var enableController = true

    /**
     * 音量变化监听
     */
    private val volumeObserver = Observer<Int> {
        SimpleLogger.instance.debugI("Volume: $it")
        isMute = it == 0
    }
    override var isLock = false
    override var isMute = false

    override var uiStateCode: Int = 0
    override var videoView: AbsVideoView? = null
        set(value) {
            field = value
            registerLD()
        }

    override fun start() {
        eventHandler.sendEmptyMessage(MSG_SHOW_PROGRESS)
    }

    override fun resume() {
        Log.d(Constant.TAG,"Resume")
        if (uiStateCode == -1)
            return
        uiStateCode = 0
        hideInfo()
        showController(Constant.CONTROLLER_HIDE_TIME)
        if (videoView?.isPlayable() == true) {
            videoView?.start()
        } else {
            videoView?.mediaPlayer?.playOnReady = true
            videoView?.prepare()
            showLoading()
        }
    }

    override fun release() {
        eventHandler.removeCallbacksAndMessages(null)
    }

    override fun reset() {
        eventHandler.removeCallbacksAndMessages(null)
        hideController(0L)
        hideInfo()
    }

    override fun setControllerEnabled(enable: Boolean) {

    }

    override fun showLoading() {
        //TODO("Not yet implemented")
    }

    override fun hideLoading() {
        //TODO("Not yet implemented")
    }

    override fun showError(code: ErrorCode) {
        //TODO("Not yet implemented")
    }

    override fun showError(code: Int) {
        //TODO("Not yet implemented")
    }

    override fun hideError() {
        //TODO("Not yet implemented")
    }

    override fun showInfo(code: InfoCode) {
        //TODO("Not yet implemented")
    }

    override fun showInfo(code: Int) {
        //TODO("Not yet implemented")
    }

    override fun hideInfo() {
        //TODO("Not yet implemented")
    }

    override fun showVolumeChange(percent: Int) {
        //TODO("Not yet implemented")
    }

    override fun showBrightnessChange(attr: WindowManager.LayoutParams) {
        //TODO("Not yet implemented")
    }

    override fun showProgressChange(delta: Long, current: Long) {
        //TODO("Not yet implemented")
    }

    override fun showController(timeout: Long) {
        //TODO("Not yet implemented")
    }

    override fun hideController(delay: Long) {
        //TODO("Not yet implemented")
    }

    override fun switchScreenMode(mode: WindowMode) {
        //TODO("Not yet implemented")
    }

    override fun setTitle(title: CharSequence?) {
        //TODO("Not yet implemented")
    }

    /**
     * 注册监听
     */
    private fun registerLD() {

    }

    companion object {
        const val MSG_RETRY = -1
        const val MSG_SHOW_PROGRESS = 0
        const val MSG_HIDE_PROGRESS = 1
    }

    private fun getCurrentPosition(): Long {
        return if (videoView?.isPlayable() == true) {
            videoView?.mediaPlayer?.currentPosition ?: 0L
        } else 0L
    }

    private fun getDuration(): Long {
        return if (videoView?.isPlayable() == true) {
            videoView?.mediaPlayer?.duration ?: 1L
        } else 1L
    }

    private inner class VideoHandler(looper: Looper): Handler(looper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_HIDE_PROGRESS -> { hideController(0L) }
                MSG_SHOW_PROGRESS -> {
                    if (isSeekBarDragging
                        && isControllerVisible
                        && videoView?.isPlaying == true) {
                        val message = obtainMessage(MSG_SHOW_PROGRESS)
                        sendMessageDelayed(message, 1000L - getCurrentPosition() % getDuration())
                    }
                    null
                }
                MSG_RETRY -> { videoView?.retry() }
            }
            super.handleMessage(msg)
        }
    }
}