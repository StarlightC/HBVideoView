package com.starlightc.videoview.interfaces

import com.starlightc.videoview.widget.AbsVideoView

/**
 * @author StarlightC
 * @since 2022/4/26
 */
interface InfoActor {
    fun doInfoAction(view: AbsVideoView, code: Int, extra: Int)

    fun doErrorAction(view: AbsVideoView, code: Int, extra: Int)
}