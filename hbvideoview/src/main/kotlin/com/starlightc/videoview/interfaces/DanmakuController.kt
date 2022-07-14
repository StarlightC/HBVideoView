package com.starlightc.videoview.interfaces

import androidx.lifecycle.MutableLiveData

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * 提供弹幕功能
 */
interface DanmakuController {
    /**
     * 弹幕播放速度
     */
    var speed: Int

    var duration: Long

    var isShown: Boolean

    var alpha: Float

    val danmakuDisplayAreaLD : MutableLiveData<Int>

    fun hide()

    fun show()

    fun start()

    fun start(position: Long)

    fun stop()

    fun resume()

    fun refreshPosition()

    fun pause()

    fun seekTo(position: Long)
}