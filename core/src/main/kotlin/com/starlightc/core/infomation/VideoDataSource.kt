package com.starlightc.core.infomation

import android.net.Uri
import java.io.Serializable
import java.net.HttpCookie

/**
 * @author StarlightC
 * @since 2022/4/24
 *
 * TODO: description
 */
data class VideoDataSource(
    var name: String?,
    var uri: Uri?,
    var headers: Map<String?, String?>? = null,
    var cookies: List<HttpCookie>? = null,
    var isLive: Boolean = false
) : Serializable