package com.starlightc.videoview.tool

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.MutableLiveData
import com.starlightc.core.Constant
import com.starlightc.core.SimpleLogger
import com.starlightc.core.infomation.PlayerState
import com.starlightc.core.interfaces.ErrorProcessor
import com.starlightc.core.interfaces.IMediaPlayer
import com.starlightc.core.interfaces.InfoProcessor
import com.starlightc.videoview.R
import com.starlightc.videoview.config.WindowMode
import com.starlightc.videoview.information.NetworkInfo
import com.starlightc.videoview.widget.AbsVideoView
import com.starlightc.videoview.widget.TinyVideoView
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * 视频播放器管理类
 */
class VideoPlayerManager {

    companion object {
        val instance:VideoPlayerManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED){
            VideoPlayerManager()
        }
    }

    private var videoViewRef: WeakReference<AbsVideoView>? = null

    var surface: Surface? = null

    private val playerList: ConcurrentHashMap<String, IMediaPlayer<*>?> = ConcurrentHashMap()
    private val infoProcessorList: ConcurrentHashMap<String, InfoProcessor> = ConcurrentHashMap()
    private val errorProcessorList: ConcurrentHashMap<String, ErrorProcessor> = ConcurrentHashMap()

    /**
     * 是否已经提示网络类型
     */
    var isNetworkTypePrompted: Boolean = false

    var networkStateLD: MutableLiveData<NetworkInfo> = MutableLiveData(NetworkInfo.WIFI)

    fun initManager(context: Context, loader: ClassLoader?) {
        loadAllPlayers(context, loader)
        loadInfoProcessor(context, loader)
        loadErrorProcessor(context, loader)
    }

    private fun loadInfoProcessor(context: Context, loader: ClassLoader?) {
        val processorSet = ServiceLoader.load(InfoProcessor::class.java,loader).toList()
        processorSet.forEach{
            infoProcessorList[it.getName()] = it
            SimpleLogger.instance.debugI("InfoProcessor load: ${it.getName()}")
        }
    }

    private fun loadErrorProcessor(context: Context, loader: ClassLoader?) {
        val processorSet = ServiceLoader.load(ErrorProcessor::class.java,loader).toList()
        processorSet.forEach{
            errorProcessorList[it.getName()] = it
            SimpleLogger.instance.debugI("ErrorProcessor load: ${it.getName()}")
        }
    }

    private fun loadAllPlayers(context: Context, loader: ClassLoader?) {
        val playerSet = ServiceLoader.load(IMediaPlayer::class.java,loader).toList()
        playerSet.forEach{
            it.create(context)
            playerList[it.getPlayerName()] = it
            SimpleLogger.instance.debugI("Player load: ${it.getPlayerName()}")
        }
    }

    fun release() {
        for (player in playerList.elements()) {
            player?.release()
        }
        playerList.clear()
    }

    /**
     * 获取播放器实例
     */
    fun getMediaPlayer(name: String = Constant.ANDROID_MEDIA_PLAYER): IMediaPlayer<*>? {
        return playerList[name]
    }

    /**
     * 获取信息处理类
     */
    fun getInfoProcessor(name: String = Constant.ANDROID_INFO_PROCESSOR): InfoProcessor? {
        return infoProcessorList[name]
    }

    /**
     * 获取错误处理类
     */
    fun getErrorProcessor(name: String = Constant.ANDROID_ERROR_PROCESSOR): ErrorProcessor? {
        return errorProcessorList[name]
    }

    /**
     * 拦截返回事件
     * @return  是否已拦截,若拦截则返回该View的origin
     * @see AbsVideoView.origin
     */
    fun blockBackPress(activity: Activity): AbsVideoView? {
        val decorView = activity.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        val videoView = findFullscreenView(decorView)
        videoView?.let {
            return dismissFullscreen(activity) as AbsVideoView
        }
        return null
    }

    /**
     * 开启全屏
     */
    fun startFullscreen(activity: Activity, videoView: View, container: ViewGroup?, orientation: Int = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
        Log.d("HVideoPlayer", "开启全屏")
        videoView.setTag(R.id.system_ui_visibility, activity.window.decorView.systemUiVisibility)
        if(videoView is AbsVideoView) {
            videoView.origin = container
            videoView.setScreenMode(WindowMode.FULLSCREEN)
        } else {
            videoView.setTag(R.id.window_mode, WindowMode.FULLSCREEN)
        }
        DisplayUtil.hideSupportActionBar(activity)
        val decorView = activity.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        findFullscreenView(decorView)?.let {
            decorView.removeView(it)
        }
        container?.removeView(videoView)
        decorView.addView(videoView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        AndroidSystemUtil.setRequestedOrientation(activity, orientation)
        DisplayUtil.hideSystemUI(activity)
    }

    /**
     * 开启反向全屏
     */
    fun startFullscreenReverse(activity: Activity, videoView: AbsVideoView, container: ViewGroup?) {
        startFullscreen(activity, videoView, container, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
    }


    /**
     * 取消全屏
     */
    fun dismissFullscreen(activity: Activity): View? {
        val decorView = activity.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        val fullScreenView = findFullscreenView(decorView)
        fullScreenView?.let {
            decorView.removeView(it)
            if(it is AbsVideoView){
                it.origin?.addView(it)
                it.setScreenMode(WindowMode.NORMAL)
            } else {
                it.setTag(R.id.window_mode, WindowMode.NORMAL)
            }
            AndroidSystemUtil.setRequestedOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            DisplayUtil.showSupportActionBar(activity)
            DisplayUtil.showSystemUI(activity, fullScreenView.getTag(R.id.system_ui_visibility) as Int)
            return it
        }
        return null
    }

    private fun findFullscreenView(viewGroup: ViewGroup?): View? {
        val videoViewList = ArrayList<View>()
        getFullscreenView(viewGroup, videoViewList)
        if (videoViewList.size > 0) {
            return videoViewList[0]
        }
        return null
    }

    private fun getFullscreenView(viewGroup: ViewGroup?, videoViewList: ArrayList<View>) {
        viewGroup?:return
        val mode = viewGroup.getTag(R.id.window_mode)
        if (mode == WindowMode.FULLSCREEN) {
            videoViewList.add(viewGroup)
            return
        }
        for (childView in viewGroup.children) {
            if (childView is ViewGroup) {
                getFullscreenView(childView as ViewGroup, videoViewList)
            }
        }
        return
    }

    /**
     * 开启小窗
     */
    fun startTinyWindow(activity: Activity, tinyVideoView: TinyVideoView) {
        TODO()
    }

    /**
     * 取消小窗
     */
    fun dismissTinyWindow(activity: Activity) {
        TODO()
    }

    /**
     * 检查网络连接状态
     */
    fun checkWIFIConnection(activity: Activity): Boolean {
        var isConnected = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            && (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_NETWORK_STATE
            ) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            networkStateLD.value = NetworkInfo.WIFI
            return isConnected
        }
        val cm = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = cm.activeNetworkInfo
        if (activeNetworkInfo == null ||
            activeNetworkInfo.type != ConnectivityManager.TYPE_WIFI ||
            !activeNetworkInfo.isConnected
        ) {
            isConnected = false
            if (activeNetworkInfo == null) {
                networkStateLD.value = NetworkInfo.NONE
            } else {
                //获取 TelephonyManager 对象
                val telephonyManager = activity.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                //获得网络类型
                val networkType = telephonyManager.networkType;

                when (networkType) {
                    TelephonyManager.NETWORK_TYPE_GPRS,
                    TelephonyManager.NETWORK_TYPE_CDMA,
                    TelephonyManager.NETWORK_TYPE_EDGE,
                    TelephonyManager.NETWORK_TYPE_1xRTT,
                    TelephonyManager.NETWORK_TYPE_IDEN -> {
                        networkStateLD.value = NetworkInfo.GEN2
                    }
                    TelephonyManager.NETWORK_TYPE_EVDO_A,
                    TelephonyManager.NETWORK_TYPE_UMTS,
                    TelephonyManager.NETWORK_TYPE_EVDO_0,
                    TelephonyManager.NETWORK_TYPE_HSDPA,
                    TelephonyManager.NETWORK_TYPE_HSUPA,
                    TelephonyManager.NETWORK_TYPE_HSPA,
                    TelephonyManager.NETWORK_TYPE_EVDO_B,
                    TelephonyManager.NETWORK_TYPE_EHRPD,
                    TelephonyManager.NETWORK_TYPE_HSPAP -> {
                        networkStateLD.value = NetworkInfo.GEN3
                    }
                    TelephonyManager.NETWORK_TYPE_LTE,
                    TelephonyManager.NETWORK_TYPE_IWLAN-> {
                        networkStateLD.value = NetworkInfo.GEN4
                    }
                    TelephonyManager.NETWORK_TYPE_NR -> {
                        /**
                         * 该方法对5G的判断不一定准确，部分机型会返回UNKNOWN
                         */
                        networkStateLD.value = NetworkInfo.GEN5
                    }

                    else -> {
                        networkStateLD.value = NetworkInfo.MOBILE
                    }
                }
            }
        } else {
            networkStateLD.value = NetworkInfo.WIFI
        }
        return isConnected
    }

    /**
     * 检测是否播放完毕（Ijkplayer中存在状态码与实际播放进度不符合的问题）
     */
    fun completionCheck(state:PlayerState, videoView: AbsVideoView) {
        val dur = videoView.duration
        val pos = videoView.currentPosition
        videoView.keepScreenOn(false)
        if (dur >= 0 && pos - dur > -10 * 1000) {
            SimpleLogger.instance.debugI(Constant.TAG, "onCompletion MEDIA_PLAYBACK_COMPLETE")
            if (state == PlayerState.ERROR) {
                return
            }
            videoView.userStateListener?.onTargetState(videoView.targetState)
            videoView.userStateListener?.onCompleted()
            videoView.playerStateListener?.onCompleted()
        } else {
            videoView.raiseError(0, 0)
            SimpleLogger.instance.debugE(Constant.TAG, "Error CompletionCheck Failed:(" + 1 + "," + "0" + ")")
        }
    }

    fun setCurrentVideoView(videoView: AbsVideoView){
        videoViewRef = WeakReference(videoView)
    }

    fun getCurrentVideoView(): AbsVideoView? {
        return videoViewRef?.get()
    }
}