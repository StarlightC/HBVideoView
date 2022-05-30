package com.starlightc.videoview.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.view.Surface
import android.view.SurfaceHolder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import com.google.auto.service.AutoService
import com.starlightc.core.infomation.PlayInfo
import com.starlightc.core.infomation.PlayerState
import com.starlightc.core.infomation.VideoDataSource
import com.starlightc.core.infomation.VideoSize
import com.starlightc.core.interfaces.IMediaPlayer
import com.starlightc.core.interfaces.Settings
import com.starlightc.core.Constant
import com.starlightc.core.SimpleLogger
import java.lang.Exception

/**
 * @author StarlightC
 * @since 2022/4/22
 *
 * Android MediaPlayer的封装
 */
@AutoService(IMediaPlayer::class)
class AndroidMediaPlayer: IMediaPlayer<MediaPlayer>,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnBufferingUpdateListener,
    MediaPlayer.OnSeekCompleteListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnInfoListener,
    MediaPlayer.OnVideoSizeChangedListener {

    //region IMediaPlayer
    override lateinit var context: Context

    override lateinit var lifecycleRegistry: LifecycleRegistry

    override var instance: MediaPlayer = MediaPlayer()

    override var playOnReady: Boolean = false

    override val isPlaying: Boolean
        get() {
            try {
                return instance.isPlaying
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

    override var lastPosition: Long = 0L
    override var startPosition: Long = 0L

    override val currentPosition: Long
        get() = try {
            lastPosition = instance.currentPosition.toLong()
            lastPosition
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }

    override val duration: Long
        get() = try {
            instance.duration.toLong()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }

    override val videoHeight: Int
        get() = try {
            instance.videoHeight
        } catch (e: Exception) {
            SimpleLogger.instance.debugE(Constant.TAG, e.toString())
            0
        }

    override val videoWidth: Int
        get() = try {
            instance.videoWidth
        } catch (e: Exception) {
            SimpleLogger.instance.debugE(Constant.TAG ,e.toString())
            0
        }

    override val playerState: PlayerState
        get() {
            return playerStateLD.value ?: PlayerState.IDLE
        }

    override var targetState: PlayerState = PlayerState.IDLE
    override val playerStateLD: MutableLiveData<PlayerState> = MutableLiveData()
    override val videoSizeLD: MutableLiveData<VideoSize> = MutableLiveData()
    override val bufferingProgressLD: MutableLiveData<Int> = MutableLiveData()
    override val seekCompleteLD: MutableLiveData<Boolean> = MutableLiveData()
    override val videoInfoLD: MutableLiveData<PlayInfo> =  MutableLiveData()
    override val videoErrorLD: MutableLiveData<PlayInfo> = MutableLiveData()
    override val videoList: ArrayList<VideoDataSource> = ArrayList()
    override var currentVideo: VideoDataSource? = null
    override var cacheSeekPosition: Long = 0L

    override fun create(context: Context) {
        this.context = context
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun getPlayerName(): String {
        return Constant.ANDROID_MEDIA_PLAYER
    }

    override fun initSettings(settings: Settings) {
        //do nothing
    }

    override fun start() {
        try {
            SimpleLogger.instance.debugI(Constant.TAG, "AndroidMediaPlayer Start")
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

    override fun prepare() {
        try {
            instance.prepareAsync()
            playerStateLD.value = PlayerState.PREPARING
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun prepareAsync() {
        try {
            instance.prepareAsync()
            playerStateLD.value = PlayerState.PREPARING
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun pause() {
        try {
            instance.pause()
            playerStateLD.value = PlayerState.PAUSED
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun stop() {
        try {
            instance.stop()
            lastPosition = currentPosition
            playerStateLD.value = PlayerState.STOPPED
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun seekTo(time: Long) {
        try {
            instance.seekTo(time.toInt())
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

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

    override fun release() {
        SimpleLogger.instance.debugI(Constant.TAG, "AndroidMediaPlayer Release")
        try {
            instance.release()
            playerStateLD.value = PlayerState.END
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setVolume(volume: Float) {
        try {
            instance.setVolume(volume, volume)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setLooping(isLoop: Boolean) {
        try {
            instance.isLooping = isLoop
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setSurface(surface: Surface?) {
        try {
            instance.setSurface(surface)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setDisplay(surfaceHolder: SurfaceHolder) {
        try {
            instance.setDisplay(surfaceHolder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun mutePlayer() {
        setVolume(0f)
    }

    override fun cancelMutePlayer() {
        setVolume(1f)
    }

    override fun addVideoDataSource(data: VideoDataSource) {
        videoList.add(data)
    }

    override fun selectVideo(index: Int) {
        if (videoList.size <= 0) {
            SimpleLogger.instance.debugE(text = "Error VideoList is empty!")
            return
        }
        if (index >= videoList.size || index < 0) {
            SimpleLogger.instance.debugE(text = "Error index out of videoList bounds: Index:$index, VideoListSize: ${videoList.size}")
            return
        }
        currentVideo = videoList[index]
        if (currentVideo?.uri ==  null) {
            SimpleLogger.instance.debugE("VideoResource at that index is null")
            return
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                instance.setDataSource(context, currentVideo!!.uri!!, currentVideo!!.headers, currentVideo!!.cookies)
            } else {
                instance.setDataSource(context, currentVideo!!.uri!!, currentVideo!!.headers)
            }
            playerStateLD.value = PlayerState.INITIALIZED
        }
    }

    override fun clearVideoDataSourceList() {
        videoList.clear()
        currentVideo = null
    }

    override fun getNetworkSpeedInfo(): Long {
        return -1
    }

    override fun setSpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            instance.playbackParams.speed = speed
        }
    }

    override fun getSpeed(): Float {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            instance.playbackParams.speed
        } else {
            1f
        }
    }

    override fun getBitrate(): Long {
        return -1L
    }

    override fun selectBitrate(bitrate: Long) {
        //do nothing
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
    //endregion

    //region MediaPlayer

    override fun onPrepared(p0: MediaPlayer?) {
        playerStateLD.value = PlayerState.PREPARED
    }

    override fun onCompletion(p0: MediaPlayer?) {
        playerStateLD.value = PlayerState.COMPLETED
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        bufferingProgressLD.value = percent
    }

    override fun onSeekComplete(p0: MediaPlayer?) {
        seekCompleteLD.value = true
    }

    override fun onAcceptError(what: Int, extra: Int) {
        videoErrorLD.value = PlayInfo(what, extra)
        playerStateLD.value = PlayerState.ERROR
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        onAcceptError(what, extra)
        return false
    }

    override fun onAcceptInfo(what: Int, extra: Int) {
        videoInfoLD.value = PlayInfo(what, extra)
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        onAcceptInfo(what, extra)
        return false
    }

    override fun onVideoSizeChanged(mp: MediaPlayer?, width: Int, height: Int) {
        videoSizeLD.value = VideoSize(width, height)
    }
//endregion


    init {
        playerStateLD.value = PlayerState.IDLE
        instance.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
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