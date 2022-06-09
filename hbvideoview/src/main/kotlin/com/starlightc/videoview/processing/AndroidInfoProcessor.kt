package com.starlightc.videoview.processing

import com.starlightc.video.core.Constant
import com.starlightc.video.core.SimpleLogger
import com.starlightc.video.core.interfaces.InfoProcessor

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * TODO: description
 */
class AndroidInfoProcessor: InfoProcessor {
    override fun getName(): String {
        return Constant.ANDROID_MEDIA_PLAYER
    }

    override fun process(what: Int, extra: Int): Int {
        SimpleLogger.instance.debugD("Android MediaPlayer => What:$what Extra:$extra")
        return 0
    }
}