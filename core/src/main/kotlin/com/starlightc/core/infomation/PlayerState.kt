package com.starlightc.core.infomation

/**
 * @author StarlightC
 * @since 2022/4/22
 *
 * 播放状态
 * 参考: https://developer.android.google.cn/reference/android/media/MediaPlayer#state-diagram
 */
sealed class PlayerState(val code: Int) {

    object IDLE :
        PlayerState(1 shl 1)

    object INITIALIZED :
        PlayerState(1 shl 2)

    object PREPARING :
        PlayerState(1 shl 3)

    object PREPARED :
        PlayerState(1 shl 4)

    object STARTED :
        PlayerState(1 shl 5)

    object CACHING :
        PlayerState(1 shl 6)

    object PAUSED :
        PlayerState(1 shl 7)

    object STOPPED :
        PlayerState(1 shl 8)

    object COMPLETED :
        PlayerState(1 shl 9)

    object ERROR :
        PlayerState(1 shl 10)

    object END :
        PlayerState(1 shl 11)

}
