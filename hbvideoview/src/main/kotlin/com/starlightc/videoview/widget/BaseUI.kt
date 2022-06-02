package com.starlightc.videoview.widget

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.starlightc.video.core.Constant
import com.starlightc.video.core.SimpleLogger
import com.starlightc.video.core.infomation.PlayerState
import com.starlightc.videoview.R
import com.starlightc.videoview.config.ErrorCode
import com.starlightc.videoview.config.InfoCode
import com.starlightc.videoview.config.WindowMode
import com.starlightc.videoview.databinding.LayoutBaseUiBinding
import com.starlightc.videoview.interfaces.VideoUI
import com.starlightc.videoview.sensor.OrientationEventManager
import com.starlightc.videoview.tool.AndroidSystemUtil
import com.starlightc.videoview.tool.DisplayUtil
import com.starlightc.videoview.tool.FormatUtil
import java.util.*

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * 基础的UI
 */
class BaseUI(context: Context): FrameLayout(context), VideoUI {
    protected var mBinding: LayoutBaseUiBinding = LayoutBaseUiBinding.inflate(LayoutInflater.from(context))
    private val eventHandler = VideoHandler(Looper.getMainLooper())

    private var isSeekBarDragging = false
    private var isControllerVisible = false
    private lateinit var mClickListener: OnClickListener
    private var enableController = true

    var currentPowerPercent = 100
    var seekBarSlidePos = 0L
    var horizontalMargin = 0
        set(value){
            field = value
            (mBinding.vgTopPanelContent.layoutParams as LinearLayout.LayoutParams).leftMargin = value
            (mBinding.vgTopPanelContent.layoutParams as LinearLayout.LayoutParams).rightMargin = value
            (mBinding.vgBottomPanelContent.layoutParams as LinearLayout.LayoutParams).leftMargin = value
            (mBinding.vgBottomPanelContent.layoutParams as LinearLayout.LayoutParams).rightMargin = value
            (mBinding.ivLockScreen.layoutParams as RelativeLayout.LayoutParams).rightMargin = value + DisplayUtil.dp2px(context, 12f)
        }

    var isCharging = false
    var isControllerShow = true

    private var volumeAnimationDrawable: AnimationDrawable? = null

    private val seekTimeLD: MutableLiveData<Long> = MutableLiveData()

    /**
     * 音量变化监听
     */
    private val volumeObserver = Observer<Int> {
        SimpleLogger.instance.debugI("Volume: $it")
        isMute = it == 0
    }

    private val orientationEventManager = OrientationEventManager()
    var orientationChangeListener: OrientationEventManager.OnOrientationChangeListener? = null
        set(value) {
            field = value
            if (videoView != null && orientationChangeListener != null) {
                orientationEventManager.orientationEnable(
                    context,
                    videoView!!,
                    orientationChangeListener
                )
            }
        }
    val alphaAnimatorLeft = AlphaAnimation(0.4f, 1.0f)
    val alphaAnimatorRight = AlphaAnimation(1.0f, 0.4f)

    override var isLock = false
    override var isMute = false

    override var uiStateCode: Int = 0
    override var videoView: AbsVideoView? = null
        set(value) {
            field = value
            registerLD()
        }

    override fun start() {
        eventHandler.sendEmptyMessage(MSG_SHOW_PROGRESS)
    }

    override fun resume() {
        Log.d(Constant.TAG,"Resume")
        if (uiStateCode == -1)
            return
        uiStateCode = 0
        hideInfo()
        showController(Constant.CONTROLLER_HIDE_TIME)
        if (videoView?.isPlayable() == true) {
            videoView?.start()
        } else {
            videoView?.mediaPlayer?.playOnReady = true
            videoView?.prepare()
            showLoading()
        }
    }

    override fun release() {
        eventHandler.removeCallbacksAndMessages(null)
    }

    override fun reset() {
        eventHandler.removeCallbacksAndMessages(null)
        hideController(0L)
        hideInfo()
    }

    override fun setControllerEnabled(enable: Boolean) {
        enableController = enable
    }

    override fun showLoading() {
        //TODO("Not yet implemented")
    }

    override fun hideLoading() {
        //TODO("Not yet implemented")
    }

    override fun showError(code: ErrorCode) {
        //TODO("Not yet implemented")
    }

    override fun showError(code: Int) {
        //TODO("Not yet implemented")
    }

    override fun hideError() {
        //TODO("Not yet implemented")
    }

    override fun showInfo(code: InfoCode) {
        //TODO("Not yet implemented")
    }

    override fun showInfo(code: Int) {
        //TODO("Not yet implemented")
    }

    override fun hideInfo() {
        //TODO("Not yet implemented")
    }

    override fun showVolumeChange(percent: Int) {
        //TODO("Not yet implemented")
    }

    override fun showBrightnessChange(attr: WindowManager.LayoutParams) {
        //TODO("Not yet implemented")
    }

    override fun showProgressChange(delta: Long, current: Long) {
        if ( delta != 0L ) {
            val seekTime = SpannableStringBuilder(FormatUtil.getTimeString(current))
            seekTime.setSpan(
                ForegroundColorSpan(-0x7800),
                0,
                seekTime.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            seekTime.append(" / ").append(FormatUtil.getTimeString(getDuration()))
            updateSeekBarProgress()
            refreshPlayButton()
        }
    }

    override fun showController(timeout: Long) {
        if (!enableController || videoView?.mediaPlayer == null) {
            return
        }
        if (isControllerVisible && !isSeekBarDragging) {
            hideController(0L)
            return
        }
        refreshPlayButton()
        updateSeekBarProgress()
        showBottomPanel()
        showTopPanel()
        isControllerVisible = true

        mBinding.seekBar.isEnabled = (videoView?.playerState != PlayerState.COMPLETED)

        eventHandler.sendEmptyMessage(MSG_SHOW_PROGRESS)
        eventHandler.removeMessages(MSG_HIDE_PROGRESS)
        if (timeout != 0L) {
            val msg: Message = eventHandler.obtainMessage(MSG_HIDE_PROGRESS)
            eventHandler.sendMessageDelayed(msg, timeout)
        }
    }

    override fun hideController(delay: Long) {
        if (enableController && isControllerVisible) {
            hideBottomPanel()
            hideTopPanel()
            isControllerVisible = false
        }
    }

    override fun switchScreenMode(mode: WindowMode) {
        //TODO("Not yet implemented")
    }

    override fun setTitle(title: CharSequence?) {
        mBinding.tvTitle.text = title
        mBinding.tvTitle.requestLayout()
    }

    init {
        val layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addView(mBinding.root, layoutParams)
        initialViews()
        //initBatteryInfo()fegisterSeekTimeObserver()
    }

    protected open fun initialViews() {
        initSeekBar()
        initClickListener()
        initClickEvent()
        initSpeedUPAnimation()
    }

    protected fun initSeekBar() {
        mBinding.seekBar.max = 1000
        mBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(bar: SeekBar) {
                seekBarSlidePos = seekTimeLD.value?:0L
                showController(3600000)
                isSeekBarDragging = true
                eventHandler.removeMessages(MSG_SHOW_PROGRESS)
            }

            override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    onProgressSlide()
                }
                seekTimeLD.value = getDuration() * progress / 1000L
            }

            override fun onStopTrackingTouch(bar: SeekBar) {
                val seekTime = seekTimeLD.value?:return
                if (isSeekBarDragging && seekTime >= 0) {
                    isSeekBarDragging = false
                    videoView?.seekTo(seekTime)
                    updateSeekBarProgress()
                    showController(Constant.CONTROLLER_HIDE_TIME)
                    eventHandler.sendEmptyMessage(MSG_SHOW_PROGRESS)
                }
            }
        })
        mBinding.seekBar.isEnabled = false
    }

    fun onProgressSlide() {
        seekTimeLD.value?.let {
            val delta: Long = seekBarSlidePos - it
            showProgressChange(delta, it)
            showController(Constant.CONTROLLER_HIDE_TIME)
            eventHandler.sendEmptyMessage(MSG_SHOW_PROGRESS)
        }
    }



    protected open fun initClickListener() {
        SimpleLogger.instance.debugI("初始化点击")
        mClickListener = OnClickListener { v ->
            SimpleLogger.instance.debugI("*** 点击 ***")
            videoView?.let {
                SimpleLogger.instance.debugI("View ID:${v.id}")
                when (v.id) {
                    R.id.iv_back, R.id.tv_title -> {
                        //mInteractionListener?.onClickBack(v)
                    }
                    R.id.vg_expand-> {
                        //mInteractionListener?.onClickFullScreen(true)
                    }
                    R.id.vg_play_button -> {
                        if (it.isPlaying) {
                            it.pause()
                        } else {
                            if (it.playerState == PlayerState.COMPLETED) {
                                it.replay()
                            } else if (uiStateCode == 1){
                                videoView?.isNetworkPrompted = true
                                //mInteractionListener?.onNetworkTypePrompted()
                                //Log.d(Utils.TAG, "已提示网络状态")
                                //continuePlay()
                            } else {
                                it.start()
                            }
                        }
                        //onPlayButtonClicked()
                    }
                    R.id.vg_top_panel, R.id.vg_bottom_panel -> {
                        continueShowController()
                    }
                }
            }
        }
    }

    private fun initSpeedUPAnimation() {
        alphaAnimatorLeft.duration = 300
        alphaAnimatorRight.duration = 300
        alphaAnimatorLeft.repeatCount = Animation.INFINITE
        alphaAnimatorRight.repeatCount = Animation.INFINITE
        alphaAnimatorLeft.fillAfter = false
        alphaAnimatorRight.fillAfter = false
        alphaAnimatorLeft.repeatMode = Animation.REVERSE
        alphaAnimatorRight.repeatMode = Animation.REVERSE
    }

    protected open fun initClickEvent() {
        SimpleLogger.instance.debugI("设置点击事件")
        mBinding.vgExpand.setOnClickListener(mClickListener)
        mBinding.vgPlayButton.setOnClickListener(mClickListener)
        mBinding.ivBack.setOnClickListener(mClickListener)
        mBinding.vgTopPanel.setOnClickListener(mClickListener)
        mBinding.vgBottomPanel.setOnClickListener(mClickListener)
    }

   fun  continueShowController() {
       if (!enableController || videoView?.mediaPlayer == null) {
           return
       }
       if (!isControllerShow) {
           showController(Constant.CONTROLLER_HIDE_TIME)
           return
       }
       refreshPlayButton()
       updateSeekBarProgress()

       mBinding.seekBar.isEnabled = videoView?.playerState != PlayerState.COMPLETED
       eventHandler.sendEmptyMessage(MSG_SHOW_PROGRESS)
       eventHandler.removeMessages(Constant.CONTROLLER_HIDE_TIME.toInt())
       val msg: Message = eventHandler.obtainMessage(Constant.CONTROLLER_HIDE_TIME.toInt())
       eventHandler.sendMessageDelayed(msg, Constant.CONTROLLER_HIDE_TIME)
   }

    /**
     * 注册监听
     */
    private fun registerLD() {

    }

    companion object {
        const val MSG_RETRY = -1
        const val MSG_SHOW_PROGRESS = 0
        const val MSG_HIDE_PROGRESS = 1
    }

    private fun getCurrentPosition(): Long {
        return if (videoView?.isPlayable() == true) {
            videoView?.mediaPlayer?.currentPosition ?: 0L
        } else 0L
    }

    private fun getDuration(): Long {
        return if (videoView?.isPlayable() == true) {
            videoView?.mediaPlayer?.duration ?: 1L
        } else 1L
    }

    private fun updateSeekBarProgress(): Long {
        if (enableController && isSeekBarDragging) {
            return 0
        }
        val position: Long = getCurrentPosition()
        val duration: Long = getDuration()
        if (duration > 0) {
            // use long to avoid overflow
            val location = 1000L * position / duration
            mBinding.seekBar.progress = location.toInt()
        }
        registerBufferPercentage()
        mBinding.tvWholeDuration.text = FormatUtil.getTimeString(duration)
        mBinding.tvCurrentPosition.text = FormatUtil.getTimeString(position)

        return position
    }

    /**
     * 注册缓存进度监听
     */
    private fun registerBufferPercentage(){
        videoView?.mediaPlayer?.bufferingProgressLD?.let {
            if (!it.hasObservers()) {
                val observer = Observer<Int> {  secondProgress ->
                    mBinding.seekBar.secondaryProgress = secondProgress * 10
                }
                val activity = AndroidSystemUtil.getActivityViaContext(context)
                val lifecycleOwner = if (activity is LifecycleOwner) activity else return
                it.observe(lifecycleOwner, observer)
            }
        }
    }

    protected fun showTopPanel() {
        if(videoView?.isFullScreen() == true) {
            refreshTimeHint()
            if (mBinding.vgTopPanel.visibility != VISIBLE) {
                val topInAnimation = AnimationUtils.loadAnimation(context, R.anim.view_top_in)
                mBinding.vgTopPanel.clearAnimation()
                topInAnimation.interpolator = DecelerateInterpolator()
                topInAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                        mBinding.vgTopPanel.visibility = VISIBLE
                    }
                    override fun onAnimationEnd(animation: Animation?) {}
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
                mBinding.vgTopPanel.startAnimation(topInAnimation)
            }
        }
    }

    protected fun hideTopPanel() {
        if (videoView?.isFullScreen() == true && mBinding.vgTopPanel.visibility != GONE) {
            val topOutAnimation = AnimationUtils.loadAnimation(context, R.anim.view_top_out)
            mBinding.vgTopPanel.clearAnimation()
            topOutAnimation.interpolator = DecelerateInterpolator()
            topOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    mBinding.vgTopPanel.visibility = GONE
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            mBinding.vgTopPanel.startAnimation(topOutAnimation)
        }
    }

    protected fun showBottomPanel() {
        if ( mBinding.vgBottomPanel.visibility != VISIBLE) {
            val bottomInAnimation = AnimationUtils.loadAnimation(context, R.anim.window_bottom_in)
            mBinding.vgBottomPanel.clearAnimation()
            bottomInAnimation.interpolator = DecelerateInterpolator()
            bottomInAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    mBinding.vgBottomPanel.visibility = VISIBLE
                }

                override fun onAnimationEnd(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            mBinding.vgBottomPanel.startAnimation(bottomInAnimation)
        }
    }

    protected fun hideBottomPanel() {
        if (mBinding.vgBottomPanel.visibility != GONE) {
            mBinding.vgBottomPanel.clearAnimation()
            val bottomOutAnimation = AnimationUtils.loadAnimation(context, R.anim.window_bottom_out)
            bottomOutAnimation.interpolator = DecelerateInterpolator()
            bottomOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    mBinding.vgBottomPanel.visibility = GONE
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
            mBinding.vgBottomPanel.startAnimation(bottomOutAnimation)
        }
    }

    /**
     * 刷新时间提示语
     */
    private fun refreshTimeHint() {
        val calendar: Calendar = Calendar.getInstance()
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        if (hours in 1..4) {
            mBinding.tvClock.text = "夜深了"
        } else {
            mBinding.tvClock.text = ""
        }
    }

    /**
     * 刷新播放按钮图标
     */
    private fun refreshPlayButton() {
        videoView?.playerState?.let {
            refreshPlayButton(it)
        }
    }


    /**
     * 刷新播放按钮图标
     */
    private fun refreshPlayButton(state: PlayerState) {
        when (state) {
            PlayerState.STARTED -> {
                val drawable =  ResourcesCompat.getDrawable(resources, R.drawable.icon_pause, null)
                drawable?.setTint(ResourcesCompat.getColor(resources, R.color.white, null))
                mBinding.vgPlayButton.setImageDrawable(drawable)
            }
            else -> {
                val drawable =  ResourcesCompat.getDrawable(resources, R.drawable.icon_play, null)
                drawable?.setTint(ResourcesCompat.getColor(resources, R.color.white, null))
                mBinding.vgPlayButton.setImageDrawable(drawable)
            }
        }
    }

    private inner class VideoHandler(looper: Looper): Handler(looper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_HIDE_PROGRESS -> { hideController(0L) }
                MSG_SHOW_PROGRESS -> {
                    if (isSeekBarDragging
                        && isControllerVisible
                        && videoView?.isPlaying == true) {
                        val message = obtainMessage(MSG_SHOW_PROGRESS)
                        sendMessageDelayed(message, 1000L - ((getCurrentPosition() % getDuration()) / getDuration() * 1000L))
                    }
                    null
                }
                MSG_RETRY -> { videoView?.retry() }
            }
            super.handleMessage(msg)
        }
    }
}