package com.starlightc.videoview.interfaces

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * 提供弹幕功能
 */
interface DanmakuController {
    /**
     * 弹幕播放速度
     */
    var speed: Int

    var duration: Long
}