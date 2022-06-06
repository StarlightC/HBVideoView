package com.starlightc.videoview.interfaces

import android.view.WindowManager
import com.starlightc.videoview.config.ErrorCode
import com.starlightc.videoview.config.InfoCode
import com.starlightc.videoview.config.WindowMode
import com.starlightc.videoview.widget.AbsVideoView

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * 播放器UI
 */
interface VideoUI {
    var isMute: Boolean

    var uiStateCode: Int

    var videoView: AbsVideoView?

    fun start()

    fun resume()

    fun release()

    fun reset()

    fun setControllerEnabled(enable: Boolean)

    fun showLoading()

    fun hideLoading()

    fun showError(code: ErrorCode)

    fun showError(code: Int)

    fun hideError()

    fun showInfo(code: InfoCode)

    fun showInfo(code: Int)

    fun hideInfo()

    fun showVolumeChange(percent: Int)

    fun showBrightnessChange(attr: WindowManager.LayoutParams)

    fun showProgressChange(delta: Long, current: Long)

    fun showController(timeout: Long)

    fun hideController(delay: Long)

    fun switchScreenMode(mode: WindowMode)

    fun setTitle(title: CharSequence?)
}