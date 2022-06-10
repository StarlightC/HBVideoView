package com.starlightc.videoview.interfaces

import android.graphics.Bitmap
import android.view.TextureView
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.starlightc.video.core.infomation.PlayerState
import com.starlightc.video.core.interfaces.ErrorProcessor
import com.starlightc.video.core.interfaces.IMediaPlayer
import com.starlightc.video.core.interfaces.InfoProcessor
import com.starlightc.videoview.information.NetworkInfo

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * 视频播放容器
 */
interface IVideoView : TextureView.SurfaceTextureListener, LifecycleObserver {

    /**
     * 封面
     */
    val cover: ImageView

    /**
     * 封面层
     */
    val coverLayer: RelativeLayout

    /**
     * UI层
     */
    val uiLayer: RelativeLayout

    /**
     * UI
     */
    var videoUI: VideoUI?

    /**
     * 弹幕容器层
     */
    val danmakuContainer: RelativeLayout

    /**
     * 播放器内核
     */
    var mediaPlayer: IMediaPlayer<*>?

    /**
     * 播放信息处理
     */
    var infoProcessor: InfoProcessor?

    /**
     * 播放错误处理
     */
    var errorProcessor: ErrorProcessor?

    /**
     * 是否正在播放
     */
    val isPlaying:Boolean

    /**
     * 当前位置
     */
    val currentPosition:Long

    /**
     * 视频时长
     */
    val duration:Long

    /**
     * 视频高度
     */
    val videoHeight:Int

    /**
     * 视频宽度
     */
    val videoWidth:Int

    /**
     * 当前音量相对值
     */
    val volumeLD: LiveData<Int>

    /**
     * 音频管理器
     */
    var audioManager:IAudioManager

    /**
     * 播放器状态
     */
    val playerState: PlayerState

    /**
     * 目标状态
     */
    val targetState: PlayerState

    val infoActor: InfoActor

    val networkInfoLD: LiveData<NetworkInfo>

    /**
     * 初始化播放
     */
    fun prepare()

    /**
     * 播放
     */
    fun start()

    /**
     * 重试
     */
    fun retry(): Boolean

    /**
     * 重播
     */
    fun replay()

    /**
     * 暂停
     */
    fun pause()

    /**
     * 停止
     */
    fun stop()

    /**
     * 释放资源
     */
    fun release()

    /**
     * 重置
     */
    fun reset()

    /**
     * 跳转
     */
    fun seekTo(time:Long)

    /**
     * 设置音量
     *
     */
    fun setVolume(volume:Int)

    /**
     * 绑定视图
     */
    fun attach()

    /**
     * 抛出错误
     */
    fun raiseError(what: Int, extra: Int)

    /**
     * 抛出信息
     */
    fun raiseInfo(what: Int, extra: Int)

    /**
     * Info处理结果对应操作
     */
    fun handleInfo(what: Int, extra: Int)

    /**
     * Error处理结果对应操作
     */
    fun handleError(what: Int, extra: Int)

    /**
     * 获取当前视图Bitmap
     */
    fun getBitmap(): Bitmap?
}