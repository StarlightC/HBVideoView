package com.starlightc.ijkplayer

import android.content.Context
import android.media.AudioManager
import android.text.TextUtils
import android.view.Surface
import android.view.SurfaceHolder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import com.google.auto.service.AutoService
import com.starlightc.core.Constant
import com.starlightc.core.SimpleLogger
import com.starlightc.core.infomation.PlayInfo
import com.starlightc.core.infomation.PlayerState
import com.starlightc.core.infomation.VideoDataSource
import com.starlightc.core.infomation.VideoSize
import com.starlightc.core.interfaces.IMediaPlayer
import com.starlightc.core.interfaces.Settings
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import tv.danmaku.ijk.media.player.misc.ITrackInfo

/**
 * @author StarlightC
 * @since 2022/4/26
 *
 * IjkMediaPlayer封装
 */
@AutoService(IMediaPlayer::class)
class IJkPlayer: IMediaPlayer<IjkMediaPlayer>,
    tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener,
    tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener,
    tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener,
    tv.danmaku.ijk.media.player.IMediaPlayer.OnSeekCompleteListener,
    tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener,
    tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener,
    tv.danmaku.ijk.media.player.IMediaPlayer.OnVideoSizeChangedListener{
    //region IMediaPlayer

    override lateinit var context: Context
    
    override  lateinit var lifecycleRegistry: LifecycleRegistry
    /**
     * 播放器实例
     */
    override var instance: IjkMediaPlayer = IjkMediaPlayer()

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
            instance.videoHeight
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }

    /**
     * 视频宽度
     */
    override val videoWidth: Int
        get() = try {
            instance.videoHeight
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

    override fun create(context: Context) {
        this.context = context
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    /**
     * 获取Player名称
     */
    override fun getPlayerName(): String {
        return Constant.IJKPLAYER
    }

    /**
     * 初始化设置
     */
    override fun initSettings(settings: Settings) {
        val ijkSettings : IjkSettings
        if (settings is IjkSettings) {
            ijkSettings = settings
        } else {
            return
        }
        instance.setOption(
            IjkMediaPlayer.OPT_CATEGORY_PLAYER,
            "start-on-prepared",
            0
        ) //禁用player的自动播放

        instance.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,
            "auto_convert", 0)

        if (ijkSettings.getUsingMediaCodec()) {
            instance.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
            if (ijkSettings.getUsingMediaCodecAutoRotate()) {
                instance.setOption(
                    IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                    "mediacodec-auto-rotate",
                    1
                )
            } else {
                instance.setOption(
                    IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                    "mediacodec-auto-rotate",
                    0
                )
            }
            if (ijkSettings.getMediaCodecHandleResolutionChange()) {
                instance.setOption(
                    IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                    "mediacodec-handle-resolution-change",
                    1
                )
            } else {
                instance.setOption(
                    IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                    "mediacodec-handle-resolution-change",
                    0
                )
            }
        } else {
            instance.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0)
        }
        instance.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0)
        val pixelFormat: String = ijkSettings.getPixelFormatValue()
        if (TextUtils.isEmpty(pixelFormat)) {
            instance.setOption(
                IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                "overlay-format",
                IjkMediaPlayer.SDL_FCC_RV32.toLong()
            )
        } else {
            instance.setOption(
                IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                "overlay-format",
                pixelFormat
            )
        }
        instance.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1)
        instance.setOption(
            IjkMediaPlayer.OPT_CATEGORY_FORMAT,
            "http-detect-range-support",
            0
        )
        instance.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48)
        instance.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "safe", 0)
        instance.setOption(
            IjkMediaPlayer.OPT_CATEGORY_PLAYER,
            "protocol_whitelist",
            "ffconcat,file,http,https"
        )
        instance.setOption(
            IjkMediaPlayer.OPT_CATEGORY_FORMAT,
            "protocol_whitelist",
            "concat,http,tcp,https,tls,file"
        )
        instance.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1)
        IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG)
    }

    /**
     * 开始
     */
    override fun start() {
        try {
            SimpleLogger.instance.debugI("IjkPlayer start")
            instance.start()
            if (playerState == PlayerState.PREPARED && startPosition in 0L until duration) {
                seekTo(startPosition)
                startPosition = 0L
            }
            playerStateLD.value = PlayerState.STARTED
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    /**
     * 准备
     */
    override fun prepare() {
        try {
            instance.prepareAsync()
            playerStateLD.value = PlayerState.PREPARING
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 异步准备
     */
    override fun prepareAsync() {
        try {
            instance.prepareAsync()
            playerStateLD.value = PlayerState.PREPARING
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 暂停
     */
    override fun pause() {
        try {
            instance.pause()
            playerStateLD.value = PlayerState.PAUSED
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    /**
     * 停止
     */
    override fun stop() {
        try {
            instance.stop()
            lastPosition = currentPosition
            playerStateLD.value = PlayerState.STOPPED
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
            instance.reset()
            lastPosition = 0
            startPosition = 0
            playerStateLD.value = PlayerState.IDLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 释放
     */
    override fun release() {
        SimpleLogger.instance.debugI(Constant.TAG, "IjkPlayer Release")
        try {
            instance.release()
            playerStateLD.value = PlayerState.END
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
            instance.setVolume(volume, volume)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置循环播放
     */
    override fun setLooping(isLoop: Boolean) {
        try {
            instance.isLooping = isLoop
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置播放容器
     */
    override fun setSurface(surface: Surface?) {
        try {
            instance.setSurface(surface)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置播放容器
     */
    override fun setDisplay(surfaceHolder: SurfaceHolder) {
        try {
            instance.setDisplay(surfaceHolder)
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
        if (uri?.scheme.equals("file", true)) {
            instance.setDataSource(uri.toString(), currentVideo!!.headers)
        } else {
            instance.setDataSource(context, currentVideo!!.uri, currentVideo!!.headers?:HashMap())
        }
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
        return instance.tcpSpeed
    }

    /**
     * 设置倍速
     */
    override fun setSpeed(speed: Float) {
        instance.setSpeed(speed)
    }

    /**
     * 获取倍速信息
     */
    override fun getSpeed(): Float {
        return instance.getSpeed(1f)
    }

    /**
     * 获取当前码率
     */
    override fun getBitrate(): Long {
        return instance.trackInfo[instance.getSelectedTrack(ITrackInfo.MEDIA_TRACK_TYPE_VIDEO)].format.getInteger("bitrate").toLong()
    }

    /**
     * 选择码率
     */
    override fun selectBitrate(bitrate: Long) {
        for ((index, track)in instance.trackInfo.withIndex()) {
            if (track.format.getInteger("bitrate") == bitrate.toInt()) {
                instance.selectTrack(index)
            }
        }
    }

    /**
     * Returns the Lifecycle of the provider.
     *
     * @return The lifecycle of the provider.
     */
    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
    //endregion

    override fun onPrepared(mp: tv.danmaku.ijk.media.player.IMediaPlayer?) {
        playerStateLD.value = PlayerState.PREPARED
    }

    override fun onCompletion(mp: tv.danmaku.ijk.media.player.IMediaPlayer?) {
        playerStateLD.value = PlayerState.COMPLETED
    }

    override fun onBufferingUpdate(mp: tv.danmaku.ijk.media.player.IMediaPlayer?, percent: Int) {
        bufferingProgressLD.value = percent
    }

    override fun onSeekComplete(mp: tv.danmaku.ijk.media.player.IMediaPlayer?) {
        seekCompleteLD.value = true
    }

    override fun onVideoSizeChanged(
        mp: tv.danmaku.ijk.media.player.IMediaPlayer?,
        width: Int,
        height: Int,
        sar_num: Int,
        sar_den: Int
    ) {
        videoSizeLD.value = VideoSize(width, height)
    }

    override fun onError(
        mp: tv.danmaku.ijk.media.player.IMediaPlayer?,
        what: Int,
        extra: Int
    ): Boolean {
        onAcceptError(what, extra)
        return false
    }

    override fun onInfo(
        mp: tv.danmaku.ijk.media.player.IMediaPlayer?,
        what: Int,
        extra: Int
    ): Boolean {
        onAcceptInfo(what, extra)
        return false
    }

    init {
        playerStateLD.value = PlayerState.IDLE
        targetState = PlayerState.IDLE
        instance.setAudioStreamType(AudioManager.STREAM_MUSIC)
        instance.setScreenOnWhilePlaying(true)
        instance.setOnPreparedListener(this)
        instance.setOnCompletionListener(this)
        instance.setOnBufferingUpdateListener(this)
        instance.setOnSeekCompleteListener(this)
        instance.setOnErrorListener(this)
        instance.setOnInfoListener(this)
        instance.setOnVideoSizeChangedListener(this)
    }
}