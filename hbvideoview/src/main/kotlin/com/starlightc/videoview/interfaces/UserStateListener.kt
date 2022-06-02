package com.starlightc.videoview.interfaces

import com.starlightc.video.core.infomation.PlayerState

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * TODO: description
 */
interface UserStateListener {
    fun onRetry()
    fun onClickCover()
    fun onTargetState(s: PlayerState)
    fun onCompleted()
}