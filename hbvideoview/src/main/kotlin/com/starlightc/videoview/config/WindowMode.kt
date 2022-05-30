package com.starlightc.videoview.config

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * 窗口显示模式
 */
sealed class WindowMode {
    object NORMAL : WindowMode()

    object LIST : WindowMode()

    object FULLSCREEN : WindowMode()

    object TINY : WindowMode()
}