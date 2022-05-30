package com.starlightc.exoplayer

import android.util.Log
import com.google.auto.service.AutoService
import com.starlightc.core.Constant
import com.starlightc.core.SimpleLogger
import com.starlightc.core.interfaces.InfoProcessor

/**
 * @author StarlightC
 * @since 2022/5/26
 *
 */
@AutoService(InfoProcessor::class)
class ExoInfoProcessor: InfoProcessor{
    /**
     * 返回InfoProcessor名称
     */
    override fun getName(): String {
        return Constant.EXO_INFO_PROCESSOR
    }

    /**
     * @return 处理结果的代号
     */
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