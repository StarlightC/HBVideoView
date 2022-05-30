package com.starlightc.core.infomation

import java.io.Serializable

/**
 * @author StarlightC
 * @since 2022/4/22
 *
 * 视频宽高信息
 */
data class VideoSize(
    var width: Int = 0,
    var height: Int = 0
):Serializable
