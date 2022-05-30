package com.starlightc.exoplayer

import android.content.Context
import android.view.Surface
import android.view.SurfaceHolder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.LoadEventInfo
import com.google.android.exoplayer2.source.MediaLoadData
import com.google.auto.service.AutoService
import com.starlightc.core.Constant
import com.starlightc.core.SimpleLogger
import com.starlightc.core.infomation.PlayInfo
import com.starlightc.core.infomation.PlayerState
import com.starlightc.core.infomation.VideoDataSource
import com.starlightc.core.infomation.VideoSize
import com.starlightc.core.interfaces.IMediaPlayer
import com.starlightc.core.interfaces.Settings

/**
 * @author StarlightC
 * @since 2022/5/26
 *
 * ExoPlayer封装
 */

@AutoService(IMediaPlayer::class)
class ExoPlayer: IMediaPlayer<ExoPlayer>, AnalyticsListener {
    override lateinit var lifecycleRegistry: LifecycleRegistry

    /**
     * 播放器实例
     */
    override lateinit var instance: ExoPlayer

    /**
     * 准备后是否播放
     */
    override var playOnReady: Boolean = false

    /**
     * 是否正在播放
     */
    override val isPlaying: Boolean
        get() {
            try {
                return instance.isPlaying
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

    /**
     * 最后位置
     */
    override var lastPosition: Long = 0L

    /**
     * 开始播放位置
     */
    override var startPosition: Long = 0L

    /**
     * 当前位置
     */
    override val currentPosition: Long
        get() = try {
            lastPosition = instance.currentPosition
            lastPosition
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }

    /**
     * 视频长度
     */
    override val duration: Long
        get() = try {
            instance.duration
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }

    /**
     * 视频高度
     */
    override val videoHeight: Int
        get() = try {
            instance.videoSize.height
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }

    /**
     * 视频宽度
     */
    override val videoWidth: Int
        get() = try {
            instance.videoSize.width
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }

    /**
     * 当前播放器状态
     */
    override val playerState: PlayerState
        get() = playerStateLD.value ?: PlayerState.IDLE

    /**
     * 播放器目标状态
     */
    override var targetState: PlayerState = PlayerState.IDLE

    /**
     * 播放器状态监听
     */
    override val playerStateLD: MutableLiveData<PlayerState> = MutableLiveData()

    /**
     * 播放器尺寸
     */
    override val videoSizeLD: MutableLiveData<VideoSize> = MutableLiveData()

    /**
     * 加载进度
     */
    override val bufferingProgressLD: MutableLiveData<Int> = MutableLiveData()

    /**
     * 是否跳转完成
     */
    override val seekCompleteLD: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * 视频播放器输出信息
     */
    override val videoInfoLD: MutableLiveData<PlayInfo> = MutableLiveData()

    /**
     * 视频报错
     */
    override val videoErrorLD: MutableLiveData<PlayInfo> = MutableLiveData()
    override val videoList: ArrayList<VideoDataSource> = ArrayList()
    override var currentVideo: VideoDataSource? = null

    /**
     * 缓存的播放位置
     */
    override var cacheSeekPosition: Long = 0L

    override lateinit var context: Context

    override fun create(context: Context) {
        this.context = context
        instance = ExoPlayer.Builder(context).build()
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    /**
     * 获取Player名称
     */
    override fun getPlayerName(): String {
        return Constant.EXOPLAYER
    }

    /**
     * 初始化设置
     */
    override fun initSettings(settings: Settings) {
        // do something
    }

    /**
     * 开始
     */
    override fun start() {
        try {
            playerStateLD.value = PlayerState.STARTED
            SimpleLogger.instance.debugI("ExoPlayer start")
            instance.playWhenReady = true
            if (playerState == PlayerState.PREPARED && startPosition in 0L until duration) {
                seekTo(startPosition)
                startPosition = 0L
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    /**
     * 准备
     */
    override fun prepare() {
        try {
            playerStateLD.value = PlayerState.PREPARING
            instance.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 异步准备
     */
    override fun prepareAsync() {
        try {
            playerStateLD.value = PlayerState.PREPARING
            instance.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 暂停
     */
    override fun pause() {
        try {
            playerStateLD.value = PlayerState.PAUSED
            instance.pause()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    /**
     * 停止
     */
    override fun stop() {
        try {
            playerStateLD.value = PlayerState.STOPPED
            instance.stop()
            lastPosition = currentPosition
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 跳转到指定到位置
     */
    override fun seekTo(time: Long) {
        try {
            instance.seekTo(time)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    /**
     * 重置
     */
    override fun reset() {
        try {
            instance.seekTo(0)
            instance.stop()
            startPosition = 0
            lastPosition = 0
            videoList.clear()
            currentVideo = null
            playerStateLD.value = PlayerState.IDLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 释放
     */
    override fun release() {
        SimpleLogger.instance.debugI("ExoPlayer Release")
        try {
            playerStateLD.value = PlayerState.END
            instance.release()
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onAcceptError(what: Int, extra: Int) {
        videoErrorLD.value = PlayInfo(what, extra)
        playerStateLD.value = PlayerState.ERROR
    }

    override fun onAcceptInfo(what: Int, extra: Int) {
        videoInfoLD.value = PlayInfo(what, extra)
    }

    /**
     * 设置音量
     */
    override fun setVolume(volume: Float) {
        try {
            instance.volume = volume
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置循环播放
     */
    override fun setLooping(isLoop: Boolean) {
        try {
            instance.repeatMode = if (isLoop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置播放容器
     */
    override fun setSurface(surface: Surface?) {
        try {
            instance.setVideoSurface(surface)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置播放容器
     */
    override fun setDisplay(surfaceHolder: SurfaceHolder) {
        try {
            instance.setVideoSurfaceHolder(surfaceHolder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 播放器静音
     */
    override fun mutePlayer() {
       setVolume(0f)
    }

    /**
     * 取消播放器静音
     */
    override fun cancelMutePlayer() {
        setVolume(1f)
    }

    /**
     * 添加视频源
     */
    override fun addVideoDataSource(data: VideoDataSource) {
        videoList.add(data)
    }

    /**
     * 选择视频源
     */
    override fun selectVideo(index: Int) {
        currentVideo = videoList[index]
        val uri = currentVideo!!.uri
        val mediaItem = MediaItem.fromUri(uri?:return)
        instance.setMediaItem(mediaItem)
        playerStateLD.value = PlayerState.INITIALIZED
    }

    /**
     * 清空视频列表
     */
    override fun clearVideoDataSourceList() {
        videoList.clear()
        currentVideo = null
    }

    /**
     * 获取网速信息
     * @return -1表示该内核不支持获取
     */
    override fun getNetworkSpeedInfo(): Long {
        return -1L
        // TODO: 实时网速信息
    }

    /**
     * 设置倍速
     */
    override fun setSpeed(speed: Float) {
        val playbackParam = PlaybackParameters(speed, 1.0f)
        instance.playbackParameters = playbackParam
    }

    /**
     * 获取倍速信息
     */
    override fun getSpeed(): Float {
        return  instance.playbackParameters.speed
    }

    /**
     * 获取当前码率
     */
    override fun getBitrate(): Long {
        return -1L
        // TODO Bitrate
    }

    /**
     * 选择码率
     */
    override fun selectBitrate(bitrate: Long) {
        //TODO: Select bitrate
    }

    /**
     * Returns the Lifecycle of the provider.
     *
     * @return The lifecycle of the provider.
     */
    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }


    /**
     * Called when a media source started loading data.
     *
     * @param eventTime The event time.
     * @param loadEventInfo The [LoadEventInfo] defining the load event.
     * @param mediaLoadData The [MediaLoadData] defining the data being loaded.
     */
    override fun onLoadStarted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        videoInfoLD.value = PlayInfo(Constant.EXOPLAYER_INFO_CODE_LOADING_START, Constant.EXOPLAYER_INFO_CODE_LOADING_START)
        SimpleLogger.instance.debugI("ExoPlayer 开始加载")
    }

    /**
     * Called when a media source completed loading data.
     *
     * @param eventTime The event time.
     * @param loadEventInfo The [LoadEventInfo] defining the load event.
     * @param mediaLoadData The [MediaLoadData] defining the data being loaded.
     */
    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        videoInfoLD.value = PlayInfo(Constant.EXOPLAYER_INFO_CODE_LOADING_COMPLETED, Constant.EXOPLAYER_INFO_CODE_LOADING_COMPLETED)
        SimpleLogger.instance.debugI("ExoPlayer 加载完毕")
    }

    /**
     * Called when a media source canceled loading data.
     *
     * @param eventTime The event time.
     * @param loadEventInfo The [LoadEventInfo] defining the load event.
     * @param mediaLoadData The [MediaLoadData] defining the data being loaded.
     */
    override fun onLoadCanceled(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        videoInfoLD.value = PlayInfo(Constant.EXOPLAYER_INFO_CODE_LOADING_CANCELED, Constant.EXOPLAYER_INFO_CODE_LOADING_CANCELED)
        SimpleLogger.instance.debugI("ExoPlayer 加载取消")
    }

    /**
     * Called when a frame is rendered for the first time since setting the surface, or since the
     * renderer was reset, or since the stream being rendered was changed.
     *
     * @param eventTime The event time.
     * @param output The output to which a frame has been rendered. Normally a [Surface],
     * however may also be other output types (e.g., a [VideoDecoderOutputBufferRenderer]).
     * @param renderTimeMs [SystemClock.elapsedRealtime] when the first frame was rendered.
     */
    override fun onRenderedFirstFrame(
        eventTime: AnalyticsListener.EventTime,
        output: Any,
        renderTimeMs: Long
    ) {
        super.onRenderedFirstFrame(eventTime, output, renderTimeMs)
    }

    /**
     * Called when the playback state changed.
     *
     * @param eventTime The event time.
     * @param state The new [playback state][Player.State].
     */
    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        super.onPlaybackStateChanged(eventTime, state)
        when(state){
            Player.STATE_READY -> {
                if (instance.isPlaying){
                    playerStateLD.value = PlayerState.STARTED
                } else {
                    playerStateLD.value = PlayerState.PREPARED
                }
            }
            Player.STATE_BUFFERING -> {
                bufferingProgressLD.value = instance.bufferedPercentage
            }
            Player.STATE_ENDED -> {
                playerStateLD.value = PlayerState.COMPLETED
            }
            Player.STATE_IDLE -> {
                if (playerStateLD.value != PlayerState.STOPPED) {
                    playerStateLD.value = PlayerState.IDLE
                }
            }
        }
    }

    /**
     * Called when the player starts or stops playing.
     *
     * @param eventTime The event time.
     * @param isPlaying Whether the player is playing.
     */
    override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlaying: Boolean) {
        if (isPlaying) {
            videoInfoLD.value = PlayInfo(Constant.EXOPLAYER_INFO_CODE_IS_PLAYING, Constant.EXOPLAYER_INFO_CODE_IS_PLAYING)
            SimpleLogger.instance.debugI("正在播放")
        }
    }

    override fun onSeekProcessed(eventTime: AnalyticsListener.EventTime) {
        seekCompleteLD.value = true
    }
}