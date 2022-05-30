package com.starlightc.ijkplayer

import com.google.auto.service.AutoService
import com.starlightc.core.Constant
import com.starlightc.core.SimpleLogger
import com.starlightc.core.interfaces.InfoProcessor
import tv.danmaku.ijk.media.player.IMediaPlayer

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * TODO: description
 */

@AutoService(InfoProcessor::class)
class IjkInfoProcessor: InfoProcessor {
    /**
     * 返回InfoProcessor名称
     */
    override fun getName(): String {
        return Constant.IJK_INFO_PROCESSOR
    }

    /**
     * @return 处理结果的代号
     */
    override fun process(what: Int, extra: Int): Int {
        SimpleLogger.instance.debugI(Constant.TAG, "收到视频信息************************")
        return when (what) {
            IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING -> {// 视频日志跟踪
                SimpleLogger.instance.debugI(Constant.TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING")
                0
            }
            IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> { // 开始视频渲染
                SimpleLogger.instance.debugI(
                    Constant.TAG,
                    "开始视频渲染 MEDIA_INFO_VIDEO_RENDERING_START"
                )
                1
            }
            IMediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                // 开始缓冲
                SimpleLogger.instance.debugI(Constant.TAG, "开始缓冲 MEDIA_INFO_BUFFERING_START")
                2
            }
            IMediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                // 缓冲结束
                SimpleLogger.instance.debugI(Constant.TAG, "缓冲结束 MEDIA_INFO_BUFFERING_END")
                3
            }
            IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH -> { // 网络带宽
                SimpleLogger.instance.debugI(
                    Constant.TAG,
                    "网络带宽 MEDIA_INFO_NETWORK_BANDWIDTH${extra}"
                )
                0
            }
            IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING -> {  // 交叉存取异常
                SimpleLogger.instance.debugI(Constant.TAG, "交叉存取异常 MEDIA_INFO_BAD_INTERLEAVING")
                0
            }
            IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE -> { // 不可拖动
                SimpleLogger.instance.debugI(Constant.TAG, "不可拖动 MEDIA_INFO_NOT_SEEKABLE")
                0
            }
            IMediaPlayer.MEDIA_INFO_METADATA_UPDATE -> { // meta数据更新
                SimpleLogger.instance.debugI(Constant.TAG, "meta数据更新 MEDIA_INFO_METADATA_UPDATE")
                0
            }
            IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE -> { // 不支持字幕
                SimpleLogger.instance.debugI(Constant.TAG, "不支持字幕 MEDIA_INFO_UNSUPPORTED_SUBTITLE")
                0
            }
            IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT -> {// 字幕请求超时
                SimpleLogger.instance.debugI(Constant.TAG, "字幕请求超时 MEDIA_INFO_SUBTITLE_TIMED_OUT")
                0
            }
            IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED -> { // 视频方向更改
                SimpleLogger.instance.debugI(
                    Constant.TAG,
                    "视频方向更改 MEDIA_INFO_VIDEO_ROTATION_CHANGED:${extra}"
                )
                4
            }
            IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START -> { // 开始音频渲染
                SimpleLogger.instance.debugI(
                    Constant.TAG,
                    "开始音频渲染 MEDIA_INFO_AUDIO_RENDERING_START:"
                )
                1
            }
            IMediaPlayer.MEDIA_INFO_AUDIO_DECODED_START -> {
                SimpleLogger.instance.debugI(Constant.TAG, "开始音频解码 MEDIA_AUDIO_DECODED_START:")
                0
            }
            IMediaPlayer.MEDIA_INFO_VIDEO_DECODED_START -> {
                SimpleLogger.instance.debugI(Constant.TAG, "开始视频解码 MEDIA_INFO_VIDEO_DECODED_START:")
                0
            }
            IMediaPlayer.MEDIA_INFO_OPEN_INPUT -> {
                SimpleLogger.instance.debugI(Constant.TAG, "开启输入 MEDIA_INFO_OPEN_INPUT:")
                0
            }
            IMediaPlayer.MEDIA_INFO_FIND_STREAM_INFO -> {
                SimpleLogger.instance.debugI(Constant.TAG, "寻找流信息 MEDIA_INFO_FIND_STREAM_INFO:")
                0
            }
            IMediaPlayer.MEDIA_INFO_COMPONENT_OPEN -> {
                SimpleLogger.instance.debugI(Constant.TAG, "打开内容 MEDIA_INFO_COMPONENT_OPEN:")
                0
            }
            IMediaPlayer.MEDIA_INFO_VIDEO_SEEK_RENDERING_START -> {
                SimpleLogger.instance.debugI(
                    Constant.TAG,
                    "开始视频跳转渲染 MEDIA_INFO_VIDEO_SEEK_RENDERING_START:"
                )
                0
            }
            IMediaPlayer.MEDIA_INFO_AUDIO_SEEK_RENDERING_START -> {
                SimpleLogger.instance.debugI(
                    Constant.TAG,
                    "开始音频跳转渲染 MEDIA_INFO_AUDIO_SEEK_RENDERING_START:"
                )
                0
            }
            IMediaPlayer.MEDIA_INFO_MEDIA_ACCURATE_SEEK_COMPLETE -> {
                SimpleLogger.instance.debugI(
                    Constant.TAG,
                    "精确跳转完成 MEDIA_INFO_MEDIA_ACCURATE_SEEK_COMPLETE:"
                )
                0
            }
            else -> {
                SimpleLogger.instance.debugI(Constant.TAG, "未知类型： $what : $extra")
                0
            }
        }
    }

}