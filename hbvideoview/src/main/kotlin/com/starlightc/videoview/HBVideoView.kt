package com.starlightc.videoview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.starlightc.videoview.config.ErrorCode
import com.starlightc.videoview.config.InfoCode
import com.starlightc.videoview.gesture.FullScreenGestureListener
import com.starlightc.videoview.information.NetworkInfo
import com.starlightc.video.core.infomation.PlayInfo
import com.starlightc.video.core.infomation.PlayerState
import com.starlightc.video.core.Constant
import com.starlightc.video.core.SimpleLogger
import com.starlightc.videoview.tool.DisplayUtil
import com.starlightc.videoview.widget.AbsVideoView
import com.starlightc.videoview.widget.BaseUI

/**
 * @author StarlightC
 * @since 2022/4/22
 *
 * TODO: description
 */
@SuppressLint("ClickableViewAccessibility")
class HBVideoView : AbsVideoView {
    constructor(context: Context) : super(context)

    constructor(context: Context, type: String, share: Boolean, danmaku: Boolean) : super(context, type, share, danmaku)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val gestureListener: FullScreenGestureListener?

    var isVolumeGestureEnable: Boolean = true //是否开启音量手势
        set(value) {
            field = value
            gestureListener?.isVolumeGestureEnable = value
        }
    var isBrightnessGestureEnable: Boolean = true //是否开启亮度手势
        set(value) {
            field = value
            gestureListener?.isBrightnessGestureEnable = value
        }
    var isProgressGestureEnable: Boolean = true //是否开启进度拖动手势
        set(value) {
            field = value
            gestureListener?.isProgressGestureEnable = value
        }

    var isDoubleTapGestureEnable: Boolean = true //是否开启双击播放/暂停手势
        set(value) {
            field = value
            gestureListener?.isEnableDoubleTap = value
        }

    override fun setup() {
        super.setup()
        SimpleLogger.instance.debugI("Setup")
        setScreenMode(getScreenMode())
        initNetworkSpeedTimer()
    }

    override fun initObserverAndListener() {
        SimpleLogger.instance.debugI(Constant.TAG, "Init Observers")
        initNetworkObserver()
        initVideoInfoObserver()
        initVideoErrorObserver()
    }

    override fun initDanmaku(danmakuView: View) {
        if (!danmakuInitialized && danmakuController != null) {
            val layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutParams.topMargin = DisplayUtil.dp2px(context, 4f)
            layoutParams.bottomMargin = DisplayUtil.dp2px(context, 4f)
            danmakuContainer.addView(
                danmakuView,
                layoutParams
            )
            danmakuContainer.visibility = VISIBLE
            danmakuInitialized = true
        }
    }

    override fun initUI() {
        val layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER)
        videoUI = BaseUI(context)
        videoUI?.videoView = this
        uiLayer.addView(videoUI as FrameLayout, layoutParams)
        uiLayer.visibility = visibility
        (videoUI as FrameLayout).visibility = visibility
    }

    override fun initCover() {

    }

    private fun initNetworkObserver() {
        networkInfoObserver = Observer<NetworkInfo> {
            if (mediaPlayer == null) {
                return@Observer
            }
            when(it) {
                NetworkInfo.WIFI -> {
                    SimpleLogger.instance.debugI(Constant.TAG,"WIFI连接 NETWORK_INFO_WIFI_CONNECTED")
                    if((isPlayable() || playerState == PlayerState.STOPPED )&& playerState != PlayerState.STARTED
                        || (playerState == PlayerState.INITIALIZED && mediaPlayer?.targetState == PlayerState.STARTED)) {
                        videoUI?.resume()
                    }
                }
                NetworkInfo.MOBILE,
                NetworkInfo.GEN5,
                NetworkInfo.GEN4,
                NetworkInfo.GEN3,
                NetworkInfo.GEN2 -> {
                    SimpleLogger.instance.debugI(Constant.TAG, "WIFI连接断开 NETWORK_INFO_WIFI_DISCONNECTED")
                    if (!isNetworkPrompted) {
                        if (isPlayable() && isPlaying) {
                            SimpleLogger.instance.debugI(Constant.TAG, "移除视频封面")
                            coverLayer.visibility = GONE
                            //stop()
                            mediaPlayer?.startPosition = mediaPlayer?.lastPosition ?: 0L
                        }
                        if (playerState != PlayerState.ERROR) {
                            SimpleLogger.instance.debugI(Constant.TAG, "移除视频封面")
                            coverLayer.visibility = GONE
                            videoUI?.showInfo(InfoCode.WIFI_DISCONNECTED)
                        }
                    }
                }
                NetworkInfo.NONE -> {
                    SimpleLogger.instance.debugI(Constant.TAG,"无网络 NETWORK_INFO_NONE")
                    stop()
                    videoUI?.showError(ErrorCode.INTERNET_DISCONNECTED)
                    (mediaPlayer?.playerStateLD as MutableLiveData<PlayerState>).value = PlayerState.ERROR
                }
                else -> {
                    SimpleLogger.instance.debugI(Constant.TAG,"未知状态 NETWORK_INFO_UNKNOWN")
                }
            }
        }
    }

    private fun initVideoInfoObserver() {
        videoInfoObserver = Observer<PlayInfo> {
            handleInfo(it.what, it.extra)
        }
    }

    private fun initVideoErrorObserver() {
        videoErrorObserver = Observer<PlayInfo> {
            handleError(it.what, it.extra)
        }
    }

    init {
        gestureListener = FullScreenGestureListener(this, isVolumeGestureEnable, isBrightnessGestureEnable, isProgressGestureEnable)
        val gestureDetector = GestureDetector(context, gestureListener)
        setOnTouchListener { v, event ->
            if (gestureDetector.onTouchEvent(event)) {
                true
            } else {
                gestureListener.onTouch(v, event)
            }
        }
        initObserverAndListener()
    }
}