package com.starlightc.core.interfaces

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * 处理PlayInfo
 */
interface InfoProcessor {
    /**
     * 返回InfoProcessor名称
     */
    fun getName():String

    /**
     * @return 处理结果的代号
     */
    fun process(what: Int, extra: Int): Int
}