package com.starlightc.core.interfaces

/**
 * @author StarlightC
 * @since 2022/4/26
 *
 * 播放错误处理
 */
interface ErrorProcessor {
    fun getName():String

    fun process(what: Int, extra: Int): Int
}