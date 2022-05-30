package com.starlightc.videoview.interfaces

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * 播放器状态监听
 */
interface PlayerStateListener {
    fun onPrepared()
    fun onStarted()
    fun onCompleted()
    fun onStopped()
    fun onEnd()
    fun onError()
    fun onPaused()
    fun onEvent()
}