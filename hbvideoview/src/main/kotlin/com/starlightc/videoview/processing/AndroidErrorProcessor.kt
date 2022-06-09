package com.starlightc.videoview.processing

import com.starlightc.video.core.Constant
import com.starlightc.video.core.interfaces.ErrorProcessor

/**
 * @author StarlightC
 * @since 2022/6/1
 *
 */
class AndroidErrorProcessor: ErrorProcessor{
    override fun getName(): String {
        return Constant.ANDROID_MEDIA_PLAYER
    }

    override fun process(what: Int, extra: Int): Int {
        // do something
        return 0
    }
}