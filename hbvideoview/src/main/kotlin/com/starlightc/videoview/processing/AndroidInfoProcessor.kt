package com.starlightc.videoview.processing

import com.google.auto.service.AutoService
import com.starlightc.core.Constant
import com.starlightc.core.SimpleLogger
import com.starlightc.core.interfaces.InfoProcessor

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * TODO: description
 */
@AutoService(InfoProcessor::class)
class AndroidInfoProcessor: InfoProcessor {
    override fun getName(): String {
        return Constant.ANDROID_INFO_PROCESSOR
    }

    override fun process(what: Int, extra: Int): Int {
        SimpleLogger.instance.debugD("Android MediaPlayer => What:$what Extra:$extra")
        return 0
    }
}