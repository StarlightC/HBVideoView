package com.starlightc.videoview.config

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * TextureView缩放模式
 */
sealed class ScaleType {

    object DEFAULT : ScaleType()

    object SCALE_ORIGINAL : ScaleType()

    object SCALE_16_9 : ScaleType()

    object SCALE_4_3 : ScaleType()

    object SCALE_MATCH_PARENT : ScaleType()

    object SCALE_CENTER : ScaleType()

    object SCALE_CENTER_CROP : ScaleType()

    object SCALE_CONSTRAINT_ORIGIN: ScaleType()

    object SCALE_VERTICAL: ScaleType()
}