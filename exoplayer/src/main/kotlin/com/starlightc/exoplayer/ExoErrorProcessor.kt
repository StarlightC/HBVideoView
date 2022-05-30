package com.starlightc.exoplayer


import com.google.auto.service.AutoService
import com.starlightc.core.Constant
import com.starlightc.core.SimpleLogger
import com.starlightc.core.interfaces.ErrorProcessor

/**
 * @author StarlightC
 * @since 2022/5/26
 *
 */
@AutoService(ErrorProcessor::class)
class ExoErrorProcessor: ErrorProcessor {
    override fun getName(): String {
        return Constant.EXO_ERROR_PROCESSOR
    }

    override fun process(what: Int, extra: Int): Int {
        return when (what) {
            Constant.EXOPLAYER_ERROR_CODE_LOADING -> {
                SimpleLogger.instance.debugE("视频错误: LOADING_ERROR")
                1
            }
            Constant.EXOPLAYER_ERROR_CODE_IO -> {
                SimpleLogger.instance.debugE("视频错误: IO_ERROR")
                1
            }
            else -> {
                SimpleLogger.instance.debugE("视频错误: $what")
                1
            }
        }
    }
}