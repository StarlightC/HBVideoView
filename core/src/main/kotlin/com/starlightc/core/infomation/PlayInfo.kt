package com.starlightc.core.infomation

import java.io.Serializable

/**
 * @author StarlightC
 * @since 2022/4/22
 *
 * 播放时信息
 */
data class PlayInfo(
    var what: Int = 0,
    var extra: Int = 0
): Serializable
