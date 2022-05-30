package com.starlightc.videoview

import android.app.Application
import com.starlightc.videoview.tool.VideoPlayerManager

/**
 * @author StarlightC
 * @since 2022/4/27
 *
 */
class SampleApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        VideoPlayerManager.instance.initManager(applicationContext, javaClass.classLoader)
    }

}