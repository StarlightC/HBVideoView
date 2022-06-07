package com.starlightc.videoview

import android.app.Application
import com.starlightc.exoplayer.ExoPlayer
import com.starlightc.video.core.SimpleLogger
import com.starlightc.videoview.tool.VideoPlayerManager

/**
 * @author StarlightC
 * @since 2022/4/27
 *
 */
class SampleApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        VideoPlayerManager.instance.initManager(applicationContext, { ExoPlayer() })
        SimpleLogger.instance.isDebug = BuildConfig.DEBUG
    }

}