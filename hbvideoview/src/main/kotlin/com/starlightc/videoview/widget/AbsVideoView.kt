package com.starlightc.videoview.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.starlightc.video.core.Constant
import com.starlightc.video.core.SimpleLogger
import com.starlightc.videoview.R
import com.starlightc.videoview.audio.DefaultAudioManager
import com.starlightc.videoview.config.InfoCode
import com.starlightc.videoview.config.ScaleType
import com.starlightc.videoview.config.WindowMode
import com.starlightc.videoview.information.NetworkInfo
import com.starlightc.video.core.infomation.PlayInfo
import com.starlightc.video.core.infomation.PlayerState
import com.starlightc.video.core.infomation.VideoDataSource
import com.starlightc.video.core.infomation.VideoSize
import com.starlightc.video.core.interfaces.ErrorProcessor
import com.starlightc.video.core.interfaces.IMediaPlayer
import com.starlightc.video.core.interfaces.InfoProcessor
import com.starlightc.videoview.config.ErrorCode
import com.starlightc.videoview.interfaces.*
import com.starlightc.videoview.processing.DefaultInfoActor
import com.starlightc.videoview.tool.*
import java.net.HttpCookie
import java.util.*

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * 视频播放View
 */
abstract class AbsVideoView : FrameLayout, IVideoView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        preSetting(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        preSetting(attrs, defStyleAttr)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        preSetting(attrs, defStyleAttr)
    }

    /**
     * 播放器内核
     */
    override var mediaPlayer: IMediaPlayer<*>? = null
        set(value) {
            removeMediaPlayerObserver(field)
            field = value
            registerMediaPlayerObserver(field)
        }

    override var infoProcessor: InfoProcessor? = null
    override var errorProcessor: ErrorProcessor? = null

    override val infoActor: InfoActor = DefaultInfoActor()

    /**
     * 音频管理器
     */
    override var audioManager: IAudioManager = DefaultAudioManager(context, mediaPlayer)
    override var videoUI: VideoUI? = null
    set(value) {
        value?:return
        if (value is View) {
            uiLayer.removeAllViews()
            field?.release()
            val layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
            field = value
            value.videoView = this
            uiLayer.addView(value, layoutParams)
            uiLayer.visibility = VISIBLE
            value.visibility = VISIBLE
            uiLayer.requestLayout()
        }
    }

    override val volumeLD: MutableLiveData<Int> = MutableLiveData()


    private val infoHandler = Handler(Looper.getMainLooper())
    private var networkSpeedTimer: Timer? = null
    private var textureView: ResizableTextureView? = null
    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null
    private var isAutoPaused = false
    private var mAnimatorSet = AnimatorSet()
    private var isFastPlay = false


    var retryCount = 0

    var bitrate: Long = 0
        set(value) {
            if (mediaPlayer?.getPlayerName() != Constant.EXOPLAYER) {
                SimpleLogger.instance.debugW(text = "Warning: only exoplayer support bitrate switching")
            }
            if (value > 0) {
                mediaPlayer?.selectBitrate(value)
            }
            field = value
        }

    var origin: ViewGroup? = null
    var params: ViewGroup.LayoutParams? = null
        get() {
            return layoutParams ?: ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        set(value) {
            field = value ?: ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutParams = field
        }

    var speed: Int = 100
        set(value) {
            field = value
            mediaPlayer?.setSpeed(value.toFloat() / 100)
            danmakuProvider?.speed = value
        }

    var coverLayerAction: () -> Unit = { start() }

    var originalSpeed: Int = speed
    var playerType = "default"
    var pageInfo: Any? = null
    var userStateListener: UserStateListener? = null
    var playerStateListener: PlayerStateListener? = null
    var scaled = false
    var sharedPlayer = false
    var isNetworkPrompted = false
        set(value) {
            field = value
            VideoPlayerManager.instance.isNetworkTypePrompted = value
        }

    val networkSpeedLD: MutableLiveData<Long> = MutableLiveData()

    private fun preSetting(attrs: AttributeSet?, defStyleAttr: Int) {
        var playerType: String? = null
        if (attrs != null) {
            val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.AbsVideoView, defStyleAttr, 0)
            playerType = typedArray.getString(R.styleable.AbsVideoView_player)
            sharedPlayer = typedArray.getBoolean(R.styleable.AbsVideoView_sharedPlayer, false)
            //enableDanmaku = typedArray.getBoolean(R.styleable.AbsVideoView_danmaku, false)
            typedArray.recycle()
        }
        mediaPlayer = VideoPlayerManager.instance.getMediaPlayer(context, playerType?:Constant.ANDROID_MEDIA_PLAYER)
        infoProcessor = VideoPlayerManager.instance.getInfoProcessor(Constant.IJK_INFO_PROCESSOR)
        errorProcessor = VideoPlayerManager.instance.getErrorProcessor(Constant.IJK_ERROR_PROCESSOR)
        setup()
    }

    /**
     * 封面层
     */
    final override val coverLayer: RelativeLayout by lazy {
        RelativeLayout(context).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
            visibility = View.GONE
        }
    }

    /**
     * 封面
     */
    final override val cover: ImageView by lazy {
        ImageView(context).apply {
            visibility = View.GONE
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

    /**
     * UI层
     */
    final override val uiLayer: RelativeLayout by lazy {
        RelativeLayout(context).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
            visibility = View.GONE
        }
    }

    /**
     * 弹幕功能实现
     */
    override var danmakuProvider: DanmakuProvider<*>? = null

    /**
     * 弹幕层
     */
    final override val danmakuContainer: RelativeLayout by lazy {
        RelativeLayout(context).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
            visibility = View.GONE
        }
    }

    /**
     * 是否正在播放
     */
    override val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying == true

    /**
     * 当前位置
     */
    override val currentPosition: Long
        get() = mediaPlayer?.currentPosition ?: 0

    /**
     * 视频时长
     */
    override val duration: Long
        get() = when (playerState) {
            PlayerState.PREPARED, PlayerState.STARTED, PlayerState.PAUSED, PlayerState.STOPPED, PlayerState.COMPLETED -> {
                mediaPlayer?.duration ?: 0
            }
            else -> 0
        }

    /**
     * 视频高度
     */
    override val videoHeight: Int
        get() = mediaPlayer?.videoHeight ?: 0

    /**
     * 视频宽度
     */
    override val videoWidth: Int
        get() = mediaPlayer?.videoWidth ?: 0


    /**
     * 播放器状态
     */
    override val playerState: PlayerState
        get() = mediaPlayer?.playerStateLD?.value ?: PlayerState.IDLE

    /**
     * 目标状态
     */
    override val targetState: PlayerState
        get() = mediaPlayer?.targetState?: PlayerState.IDLE

    init {
        setTag(R.id.window_mode, WindowMode.NORMAL)
        this.setBackgroundColor(Color.BLACK)
        //添加播放器视图容器
        textureView = ResizableTextureView(context)
        textureView?.surfaceTextureListener = this
        val layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        )
        this.addView(
            textureView,
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
        )
        //添加弹幕层
        this.addView(this.danmakuContainer, layoutParams)
        //添加UI层
        initUI()
        this.addView(this.uiLayer, layoutParams)
        //添加封面遮罩容器
        cover.visibility = VISIBLE
        coverLayer.addView(cover, layoutParams)
        SimpleLogger.instance.debugI(Constant.TAG, "展示视频封面")
        coverLayer.visibility = VISIBLE
        initCover()
        coverLayer.setOnClickListener { coverLayerAction.invoke() }
        this.addView(this.coverLayer, layoutParams)
        //注册生命周期监听
        registerLifecycleCallback()
        //注册播放器状态监听
        registerMediaPlayerObserver(this.mediaPlayer)
    }

    /**
     * 初始化对视频信息、错误以及自定义内容的监听
     * hint: 拥有父类的情况下需保证父类中该函数体为空
     */
    abstract fun initObserverAndListener()

    /**
     * 初始化弹幕生成器
     * hint: 拥有父类的情况下需保证父类中该函数体为空
     */
    abstract fun initDanmaku()

    /**
     * 初始化交互界面
     * hint: 拥有父类的情况下需保证父类中该函数体为空
     */
    abstract fun initUI()

    /**
     * 初始化交互封面
     * hint: 拥有父类的情况下需保证父类中该函数体为空
     */
    abstract fun initCover()

    open fun setScreenMode(mode: WindowMode) {
        when(mode) {
            WindowMode.FULLSCREEN -> {
                setTag(R.id.window_mode, WindowMode.FULLSCREEN)
            }
            WindowMode.NORMAL -> {
                setTag(R.id.window_mode, WindowMode.NORMAL)
            }
            WindowMode.TINY -> {
                setTag(R.id.window_mode, WindowMode.TINY)
            }
        }
        videoUI?.switchScreenMode(mode)
    }

    fun getScreenMode(): WindowMode {
        return (getTag(R.id.window_mode) as WindowMode?) ?: WindowMode.NORMAL
    }

    fun fastPlay() {
        if (videoUI?.uiStateCode != 0) {
            return
        }
        originalSpeed = speed
        speed = 200
        videoUI?.showInfo(InfoCode.FAST_PLAY)
        isFastPlay = true
    }

    fun normalPlay() {
        if (videoUI?.uiStateCode != 0 || !isFastPlay) {
            return
        }
        speed = originalSpeed
        videoUI?.hideInfo()
        isFastPlay = false
    }

    fun setContentLeftMargin(left: Int) {
        val lp = (textureView?.layoutParams as LayoutParams)
        lp.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
        lp.leftMargin = left
    }


    fun animateResizeVideoContent(view: View) {
        if (scaled) {
            return
        }
        textureView ?: return
        val viewWidth = DisplayUtil.getViewWidth(view).toFloat()
        val contentWidth = DisplayUtil.getViewWidth(textureView!!)
        val containerWidth = DisplayUtil.getViewWidth(this).toFloat()
        val containerHeight = DisplayUtil.getViewHeight(this).toFloat()
        val ratio = (containerWidth - viewWidth) / containerWidth
        val originMargin = (containerWidth - contentWidth) / 2
        val leftMargin = originMargin * ratio
        val newH = containerHeight * ratio

        val marginAni: ObjectAnimator = ObjectAnimator.ofInt(
            this,
            "contentLeftMargin",
            originMargin.toInt(),
            leftMargin.toInt()
        )
        val heightAni: ObjectAnimator = ObjectAnimator.ofInt(
            textureView!!,
            "fixedContentHeight",
            containerHeight.toInt(),
            newH.toInt()
        )
        textureView?.setFixedSize(-1, newH.toInt())
        mAnimatorSet.duration = 300
        mAnimatorSet.removeAllListeners()
        mAnimatorSet.playTogether(
            heightAni,
            marginAni
        )
        mAnimatorSet.start()
        scaled = true
    }

    fun animateResetVideoContentSize() {
        if (!scaled) {
            return
        }
        textureView ?: return
        val contentHeight = DisplayUtil.getViewHeight(textureView!!).toFloat()
        val contentWidth = DisplayUtil.getViewWidth(textureView!!).toFloat()
        val rateWH = contentWidth / contentHeight
        val containerWidth = DisplayUtil.getViewWidth(this).toFloat()
        val containerHeight = DisplayUtil.getViewHeight(this).toFloat()
        val originWidth = containerHeight * rateWH
        val curMargin = (textureView?.layoutParams as LayoutParams).leftMargin
        val originMargin = (containerWidth - originWidth) / 2
        val marginAni: ObjectAnimator =
            ObjectAnimator.ofInt(this, "contentLeftMargin", curMargin, originMargin.toInt())
        val heightAni: ObjectAnimator = ObjectAnimator.ofInt(
            textureView!!,
            "fixedContentHeight",
            contentHeight.toInt(),
            containerHeight.toInt()
        )
        mAnimatorSet.duration = 300
        mAnimatorSet.removeAllListeners()
        mAnimatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                textureView?.let {
                    val lp = (it.layoutParams as LayoutParams)
                    lp.gravity = Gravity.CENTER
                    lp.leftMargin = 0
                    if (it.videoRotation % 180f != 0f) {
                        it.setScreenScale(ScaleType.SCALE_VERTICAL)
                    } else {
                        it.setScreenScale(ScaleType.DEFAULT)
                    }
                }
            }

            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}
        })
        mAnimatorSet.playTogether(
            heightAni,
            marginAni
        )
        mAnimatorSet.start()
        scaled = false
    }

    /**
     * 初始化
     */
    open fun setup() {}

    /**
     * 初始化播放
     */
    override fun prepare() {
        SimpleLogger.instance.debugI(Constant.TAG, "prepare() called")
        attach()
        when (playerState) {
            PlayerState.INITIALIZED, PlayerState.STOPPED -> {
                AndroidSystemUtil.getActivityViaContext(context)?.let {
                    VideoPlayerManager.instance.checkWIFIConnection(it)
                }
                mediaPlayer?.prepareAsync()
            }
            else -> {}
        }
    }

    /**
     * 播放
     */
    override fun start() {
        SimpleLogger.instance.debugI(Constant.TAG, "start() called")
        mediaPlayer?.targetState = PlayerState.STARTED
        mediaPlayer?.let {
            userStateListener?.onTargetState(PlayerState.STARTED)
        }
        when (playerState) {
            PlayerState.PREPARED, PlayerState.STARTED, PlayerState.PAUSED, PlayerState.COMPLETED -> {
                mediaPlayer?.start()
                videoUI?.start()
                keepScreenOn(true)
            }
            PlayerState.PREPARING -> {
                mediaPlayer?.playOnReady = true
                keepScreenOn(true)
                SimpleLogger.instance.debugI(Constant.TAG, "Player preparing, video will play after prepared")
            }
            else -> {}
        }
    }

    /**
     * 重试
     */
    override fun retry(): Boolean {
        if (retryCount++ > Constant.MAX_TIMES_RETRY) {
            (mediaPlayer?.playerStateLD as MutableLiveData<PlayerState>?)?.value = PlayerState.ERROR
            (mediaPlayer?.videoErrorLD as MutableLiveData<PlayInfo>?)?.value = PlayInfo(400, retryCount)
            return false
        }
        ++retryCount
        videoUI?.hideError()
        if (!isPlayable()) {
            reset()
            SimpleLogger.instance.debugI(Constant.TAG, "移除视频封面")
            coverLayer.visibility = GONE
            videoUI?.showLoading()
            userStateListener?.onRetry()
        } else {
            seekTo((currentPosition - 1000L).coerceAtLeast(1))
            start()
        }
        return true
    }

    /**
     * 重试
     */
    fun retry(errorCode: ErrorCode) {
        if (!retry()) {
            videoUI?.showError(errorCode)
        }
    }

    override fun handleInfo(what: Int, extra: Int) {
        infoProcessor?.process(what, extra)?.let { infoActor.doInfoAction(this, it, extra) }
    }

    override fun handleError(what: Int, extra: Int) {
        SimpleLogger.instance.debugE( "收到视频错误！！！！！！！！！！！！！！！！！！！！！！！")
        keepScreenOn(false)
        videoUI?.hideController(0)
        errorProcessor?.process(what, extra)?.let { infoActor.doErrorAction(this, it, extra) }
    }

    /**
     * 是否处于可播放状态
     */
    fun isPlayable(): Boolean {
        playerState.let {
            return when (it) {
                PlayerState.COMPLETED, PlayerState.STARTED, PlayerState.PREPARED, PlayerState.PAUSED -> true
                else -> false
            }
        }
    }

    /**
     * 是否全屏
     */
    fun isFullScreen(): Boolean {
        return getTag(R.id.window_mode) == WindowMode.FULLSCREEN
    }

    protected fun initNetworkSpeedTimer() {
        networkSpeedTimer?.cancel()
        networkSpeedTimer = Timer()
        if (Thread.currentThread() != context.mainLooper.thread) {
            Looper.prepare()
        }
        networkSpeedTimer?.schedule(object : TimerTask() {
            override fun run() {
                if (isPlayable()) {
                    infoHandler.post {
                        mediaPlayer?.let {
                            networkSpeedLD.value = it.getNetworkSpeedInfo()
                        }
                    }
                }
            }
        }, 0, 1000)
    }

    fun setVideoRotation(rotation: Float) {
        textureView?.let {
            it.videoRotation = rotation
            if (it.videoRotation % 180f != 0f) {
                it.setScreenScale(ScaleType.SCALE_VERTICAL)
            } else {
                it.setScreenScale(ScaleType.DEFAULT)
            }
        }
    }

    /**
     * 重播
     */
    override fun replay() {
        SimpleLogger.instance.debugI(Constant.TAG, "replay() called  current state: $playerState")
        when (playerState) {
            PlayerState.PREPARED, PlayerState.STARTED, PlayerState.PAUSED, PlayerState.COMPLETED -> {
                mediaPlayer?.seekTo(1)
                start()
                videoUI?.showController(Constant.CONTROLLER_HIDE_TIME)
            }
            else -> {}
        }
    }

    /**
     * 暂停
     */
    override fun pause() {
        SimpleLogger.instance.debugI(Constant.TAG, "pause() called")
        mediaPlayer?.targetState = PlayerState.PAUSED
        mediaPlayer?.let {
            userStateListener?.onTargetState(PlayerState.PAUSED)
        }
        when (playerState) {
            PlayerState.STARTED, PlayerState.PAUSED, PlayerState.COMPLETED -> {
                mediaPlayer?.pause()
            }
            else -> {}
        }
    }

    /**
     * 停止
     */
    override fun stop() {
        SimpleLogger.instance.debugI(Constant.TAG, "stop() called")
        mediaPlayer?.targetState = PlayerState.STOPPED
        mediaPlayer?.let {
            userStateListener?.onTargetState(PlayerState.STOPPED)
        }
        when (playerState) {
            PlayerState.PREPARED, PlayerState.STARTED, PlayerState.PAUSED, PlayerState.STOPPED, PlayerState.COMPLETED -> {
                mediaPlayer?.stop()
            }
            else -> {}
        }
    }

    /**
     * 释放资源
     */
    override fun release() {
        SimpleLogger.instance.debugI(Constant.TAG, "release() called")
        VideoPlayerManager.instance.release(mediaPlayer)
        userStateListener?.onTargetState(PlayerState.END)
        surfaceTexture?.release()
        surfaceTexture = null
        networkSpeedTimer?.cancel()
    }

    /**
     * 重置
     */
    override fun reset() {
        SimpleLogger.instance.debugI(Constant.TAG, "reset() called")
        SimpleLogger.instance.debugI(Constant.TAG, "展示视频封面")
        textureView?.videoRotation = 0f
        textureView?.setScreenScale(ScaleType.DEFAULT)
        videoUI?.reset()
        coverLayer.visibility = VISIBLE
        if (mediaPlayer == null) {
            setup()
        }
        mediaPlayer?.reset()
    }

    /**
     * 跳转
     */
    override fun seekTo(time: Long) {
        SimpleLogger.instance.debugI(Constant.TAG, "seekTo() called with: time = $time")
        when (playerState) {
            PlayerState.PREPARED, PlayerState.STARTED, PlayerState.PAUSED, PlayerState.COMPLETED -> {
                mediaPlayer?.seekTo(time)

            }
            PlayerState.PREPARING -> {
                mediaPlayer?.startPosition = time
            }
            else -> {}
        }
    }

    /**
     * 设置音量
     */
    override fun setVolume(volume: Int) {
        SimpleLogger.instance.debugI(Constant.TAG, "setVolume() called with: volume = $volume")
        audioManager.setVolume(volume)
        volumeLD.value = getVolumeInt()
    }

    override fun raiseError(what: Int, extra: Int) {
        mediaPlayer?.onAcceptError(what, extra)
    }

    override fun raiseInfo(what: Int, extra: Int) {
        mediaPlayer?.onAcceptInfo(what, extra)
    }

    /**
     * 绑定视图
     */
    override fun attach() {
        SimpleLogger.instance.debugI(Constant.TAG, "attach() called")
        surfaceTexture?.let {
            surface?.release()
            surface = Surface(it)
            mediaPlayer?.setSurface(surface)
        }
    }

    /**
     * 获取当前视图Bitmap
     */
    override fun getBitmap(): Bitmap? {
        return textureView?.bitmap
    }

    /**
     * Invoked when a [TextureView]'s SurfaceTexture is ready for use.
     *
     * @param surface The surface returned by
     * [android.view.TextureView.getSurfaceTexture]
     * @param width The width of the surface
     * @param height The height of the surface
     */
    final override fun onSurfaceTextureAvailable(
        surfaceTexture: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        SimpleLogger.instance.debugI(
            Constant.TAG,
            "onSurfaceTextureAvailable() called with: surfaceTexture = $surface, width = $width, height = $height"
        )
        val currentSurface = this.surfaceTexture
        if (currentSurface == null) {
            this.surfaceTexture = surfaceTexture
            attach()
        } else if (textureView?.surfaceTexture != currentSurface) {
            textureView?.setSurfaceTexture(currentSurface)
        }
    }

    /**
     * Invoked when the [SurfaceTexture]'s buffers size changed.
     *
     * @param surface The surface returned by
     * [android.view.TextureView.getSurfaceTexture]
     * @param width The new width of the surface
     * @param height The new height of the surface
     */
    final override fun onSurfaceTextureSizeChanged(
        surfaceTexture: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        SimpleLogger.instance.debugI(
            Constant.TAG,
            "onSurfaceTextureSizeChanged() called with: surfaceTexture = $surface, width = $width, height = $height"
        )
        val currentSurface = this.surfaceTexture
        if (textureView?.surfaceTexture != currentSurface) {
            if (currentSurface != null) {
                textureView?.setSurfaceTexture(currentSurface)
            }
        }
    }

    /**
     * Invoked when the specified [SurfaceTexture] is about to be destroyed.
     * If returns true, no rendering should happen inside the surface texture after this method
     * is invoked. If returns false, the client needs to call [SurfaceTexture.release].
     * Most applications should return true.
     *
     * @param surface The surface about to be destroyed
     */
    final override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
        SimpleLogger.instance.debugI(
            Constant.TAG,
            "onSurfaceTextureDestroyed() called with: surfaceTexture = $surfaceTexture"
        )
        //this.surfaceTexture = null
        return false
    }

    /**
     * Invoked when the specified [SurfaceTexture] is updated through
     * [SurfaceTexture.updateTexImage].
     *
     * @param surface The surface just updated
     */
    final override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
        if (textureView?.surfaceTexture != surfaceTexture) {
            textureView?.setSurfaceTexture(surfaceTexture)
        }
    }

    fun keepScreenOn(keepScreenOn: Boolean) {
        val window: Window = (context as Activity).window
        if (keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun getVolumeInt(): Int {
        val volume = audioManager.getVolume()
        val maxVolume = audioManager.getMaxVolume()
        return (volume * 1.0 / maxVolume * 100).toInt()
    }

    /**
     * 注册生命周期的监听
     */
    private fun registerLifecycleCallback() {
        val activity = AndroidSystemUtil.getActivityViaContext(context) ?: return
        if (activity is LifecycleOwner) {
            activity.lifecycle.addObserver(this)
        }
    }

    /**
     * 注册播放器内核的监听
     */
    private fun registerMediaPlayerObserver(mediaPlayer: IMediaPlayer<*>?) {
        mediaPlayer ?: return
        SimpleLogger.instance.debugI(Constant.TAG, "注册播放器内核监听")
        val activity = AndroidSystemUtil.getActivityViaContext(context)
        val lifecycleOwner = if (activity is LifecycleOwner) activity else return
        mediaPlayer.videoSizeLD.observe(lifecycleOwner, videoSizeObserver)
        mediaPlayer.playerStateLD.observe(lifecycleOwner, playerStateObserver)
        videoErrorObserver?.let {
            SimpleLogger.instance.debugI(Constant.TAG, "注册视频错误监听")
            mediaPlayer.videoErrorLD.observe(lifecycleOwner, it)
        }
        videoInfoObserver?.let {
            SimpleLogger.instance.debugI(Constant.TAG, "注册视频信息监听")
            mediaPlayer.videoInfoLD.observe(lifecycleOwner, it)
        }
        networkInfoObserver?.let {
            SimpleLogger.instance.debugI(Constant.TAG, "注册网络信息监听")
            VideoPlayerManager.instance.networkStateLD.observe(lifecycleOwner, it)
        }
    }

    /**
     * 移除播放器内核监听
     */
    private fun removeMediaPlayerObserver(mediaPlayer: IMediaPlayer<*>?) {
        mediaPlayer?.videoSizeLD?.removeObserver(videoSizeObserver)
        mediaPlayer?.playerStateLD?.removeObserver(playerStateObserver)
        videoErrorObserver?.let {
            mediaPlayer?.videoErrorLD?.removeObserver(it)
        }
        videoInfoObserver?.let {
            mediaPlayer?.videoInfoLD?.removeObserver(it)
        }
        networkInfoObserver?.let {
            VideoPlayerManager.instance.networkStateLD.removeObserver(it)
        }
        audioManager.abandonAudioFocus()
    }

    /**
     * 注册播放器信息监听
     */
    private fun registerVideoInfoObserver() {
        val activity = AndroidSystemUtil.getActivityViaContext(context)
        val lifecycleOwner = if (activity is LifecycleOwner) activity else return
        mediaPlayer?.videoInfoLD?.observe(lifecycleOwner, videoInfoObserver ?: return)
    }

    /**
     * 注册网络信息监听
     */
    private fun registerNetworkInfoObserver() {
        val activity = AndroidSystemUtil.getActivityViaContext(context)
        val lifecycleOwner = if (activity is LifecycleOwner) activity else return
        VideoPlayerManager.instance.networkStateLD.observe(lifecycleOwner, networkInfoObserver ?: return)
    }

    /**
     * 注册播放器错误监听
     */
    private fun registerVideoErrorObserver() {
        val activity = AndroidSystemUtil.getActivityViaContext(context)
        val lifecycleOwner = if (activity is LifecycleOwner) activity else return
        mediaPlayer?.videoErrorLD?.observe(lifecycleOwner, videoErrorObserver ?: return)
    }

    /**
     * 播放器错误监听
     */
    var videoErrorObserver: Observer<PlayInfo>? = null
        set(value) {
            field = value
            registerVideoErrorObserver()
        }

    /**
     * 播放器信息监听
     */
    var videoInfoObserver: Observer<PlayInfo>? = null
        set(value) {
            field = value
            registerVideoInfoObserver()
        }

    /**
     * 网络信息监听
     */
    var networkInfoObserver: Observer<NetworkInfo>? = null
        set(value) {
            field = value
            registerNetworkInfoObserver()
        }

    /**
     * 视频尺寸监听
     */
    private val videoSizeObserver = Observer<VideoSize> {
        SimpleLogger.instance.debugI(Constant.TAG, "VideoSize: width = ${it.width}, height = ${it.height}")
        textureView?.setVideoSize(it.width, it.height)
    }

    /**
     * 视频播放器状态监听
     */
    private val playerStateObserver = Observer<PlayerState> {
        SimpleLogger.instance.debugI(Constant.TAG, "PlayerState: ${it.javaClass.canonicalName}")
        playerStateListener?.onEvent()
        when (it) {
            PlayerState.PREPARED -> {
                playerStateListener?.onPrepared()
                if (mediaPlayer?.playOnReady == true) {
                    start()
                }
            }
            PlayerState.PAUSED -> {
                abandonAudioFocus()
                playerStateListener?.onPaused()
            }
            PlayerState.STARTED -> {
                requestAudioFocus()
                //移除封面
                postDelayed({
                    SimpleLogger.instance.debugI(Constant.TAG, "移除视频封面")
                    coverLayer.visibility = View.GONE
                }, 50)
                playerStateListener?.onStarted()
            }
            PlayerState.COMPLETED -> {
                VideoPlayerManager.instance.completionCheck(it, this)
                abandonAudioFocus()
            }
            PlayerState.STOPPED -> {
                abandonAudioFocus()
                playerStateListener?.onStopped()
            }
            PlayerState.END -> {
                abandonAudioFocus()
                playerStateListener?.onEnd()

            }
            PlayerState.ERROR -> {
                abandonAudioFocus()
                postDelayed({
                    SimpleLogger.instance.debugI(Constant.TAG, "移除视频封面")
                    coverLayer.visibility = View.GONE
                }, 50)
                playerStateListener?.onError()
            }
            else -> {}
        }
    }

    fun requestAudioFocus() {
        audioManager.requestAudioFocus()
    }

    fun abandonAudioFocus() {
        audioManager.abandonAudioFocus()
    }

    fun addVideoAndSelect(name: String?, uri: Uri, headers: Map<String?, String?>? = null, cookies: List<HttpCookie>? = null, isLive: Boolean = false) {
        addVideoDataSource(name, uri, headers, cookies, isLive)
        selectVideo((mediaPlayer?.videoList?.size?:1) - 1)
    }

    fun selectVideo(index: Int) {
        if (index >= 0 && mediaPlayer?.videoList?.size?:0 > index) {
            mediaPlayer?.selectVideo(index)
        } else {
            SimpleLogger.instance.debugE("VideoDS Index Out of Bounds")
        }
    }

    fun addVideoDataSource(name: String?, uri: Uri, headers: Map<String?, String?>? = null, cookies: List<HttpCookie>? = null, isLive: Boolean = false) {
        mediaPlayer?.addVideoDataSource(VideoDataSource(name, uri, headers, cookies, isLive))
    }

    fun clearVideoDataSourceList() {
        mediaPlayer?.clearVideoDataSourceList()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onActivityPause() {
        if (isPlaying) {
            pause()
            isAutoPaused = true
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onActivityResume() {
        if (isPlayable() && isAutoPaused) {
            isAutoPaused = false
            start()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onActivityDestroy() {
        abandonAudioFocus()
        AndroidSystemUtil.getActivityViaContext(context)?.let {
            VideoPlayerManager.instance.dismissFullscreen(it)
            VideoPlayerManager.instance.dismissTinyWindow(it)
        }
        stop()
        release()
    }
}