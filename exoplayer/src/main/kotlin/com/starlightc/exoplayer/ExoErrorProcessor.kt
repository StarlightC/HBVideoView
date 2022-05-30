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
            Constant.EXOPLAYER_INFO_CODE_RENDERING_STARTED -> {
                // 开始视频渲染
                SimpleLogger.instance.debugI( "开始视频渲染 ExoPlayer")
                1
            }
            Constant.EXOPLAYER_INFO_CODE_LOADING_START -> {
                SimpleLogger.instance.debugI("开始loading ExoPlayer")
                2
            }
            Constant.EXOPLAYER_INFO_CODE_LOADING_COMPLETED -> {
                SimpleLogger.instance.debugI("loading结束 ExoPlayer")
                3
            }
            Constant.EXOPLAYER_INFO_CODE_LOADING_CANCELED -> {
                SimpleLogger.instance.debugI("loading取消 ExoPlayer")
                3
            }
            Constant.EXOPLAYER_INFO_CODE_IS_PLAYING -> {
                SimpleLogger.instance.debugI("状态：正在播放")
                3
            }
            else -> 0
        }
    }
}