package com.starlightc.core.interfaces

import android.content.Context
import android.view.Surface
import android.view.SurfaceHolder
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LiveData
import com.starlightc.core.infomation.PlayInfo
import com.starlightc.core.infomation.PlayerState
import com.starlightc.core.infomation.VideoDataSource
import com.starlightc.core.infomation.VideoSize

/**
 * @author StarlightC
 * @since 2022/4/22
 *
 * TODO: description
 */
interface IMediaPlayer<T> : LifecycleOwner {
    var lifecycleRegistry: LifecycleRegistry

    /**
     * 播放器实例
     */
    var instance: T

    /**
     * 准备后是否播放
     */
    var playOnReady: Boolean

    /**
     * 是否正在播放
     */
    val isPlaying: Boolean

    /**
     * 最后位置
     */
    var lastPosition: Long

    /**
     * 开始播放位置
     */
    var startPosition: Long

    /**
     * 当前位置
     */
    val currentPosition: Long

    /**
     * 视频长度
     */
    val duration: Long

    /**
     * 视频高度
     */
    val videoHeight: Int

    /**
     * 视频宽度
     */
    val videoWidth: Int

    /**
     * 当前播放器状态
     */
    val playerState: PlayerState

    /**
     * 播放器目标状态
     */
    var targetState: PlayerState

    /**
     * 播放器状态监听
     */
    val playerStateLD: LiveData<PlayerState>

    /**
     * 播放器尺寸
     */
    val videoSizeLD: LiveData<VideoSize>

    /**
     * 加载进度
     */
    val bufferingProgressLD: LiveData<Int>

    /**
     * 是否跳转完成
     */
    val seekCompleteLD: LiveData<Boolean>

    /**
     * 视频播放器输出信息
     */
    val videoInfoLD: LiveData<PlayInfo>

    /**
     * 视频报错
     */
    val videoErrorLD: LiveData<PlayInfo>

    val videoList: ArrayList<VideoDataSource>

    var currentVideo: VideoDataSource?

    /**
     * 缓存的播放位置
     */
    var cacheSeekPosition: Long

    var context: Context

    fun create(context: Context)

    /**
     * 获取Player名称
     */
    fun getPlayerName(): String

    /**
     * 初始化设置
     */
    fun initSettings(settings: Settings)

    /**
     * 开始
     */
    fun start()

    /**
     * 准备
     */
    fun prepare()

    /**
     * 异步准备
     */
    fun prepareAsync()

    /**
     * 暂停
     */
    fun pause()

    /**
     * 停止
     */
    fun stop()

    /**
     * 跳转到指定到位置
     */
    fun seekTo(time: Long)

    /**
     * 重置
     */
    fun reset()

    /**
     * 释放
     */
    fun release()

    fun onAcceptError(what: Int, extra: Int)

    fun onAcceptInfo(what: Int, extra: Int)

    /**
     * 设置音量
     */
    fun setVolume(volume: Float)

    /**
     * 设置循环播放
     */
    fun setLooping(isLoop: Boolean)

    /**
     * 设置播放容器
     */
    fun setSurface(surface: Surface?)

    /**
     * 设置播放容器
     */
    fun setDisplay(surfaceHolder: SurfaceHolder)

    /**
     * 播放器静音
     */
    fun mutePlayer()

    /**
     * 取消播放器静音
     */
    fun cancelMutePlayer()

    /**
     * 添加视频源
     */
    fun addVideoDataSource(data: VideoDataSource)

    /**
     * 选择视频源
     */
    fun selectVideo(index: Int)

    /**
     * 清空视频列表
     */
    fun clearVideoDataSourceList()

    /**
     * 获取网速信息
     * @return -1表示该内核不支持获取
     */
    fun getNetworkSpeedInfo(): Long

    /**
     * 设置倍速
     */
    fun setSpeed(speed: Float)

    /**
     * 获取倍速信息
     */
    fun getSpeed(): Float

    /**
     * 获取当前码率
     */
    fun getBitrate(): Long

    /**
     * 选择码率
     */
    fun selectBitrate(bitrate: Long)
}