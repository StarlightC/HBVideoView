package com.starlightc.videoview.audio

import android.app.Service
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.starlightc.videoview.interfaces.IAudioManager
import com.starlightc.video.core.interfaces.IMediaPlayer
import com.starlightc.video.core.Constant
import com.starlightc.video.core.SimpleLogger
import java.lang.ref.WeakReference

/**
 * @author StarlightC
 * @since 2022/4/24
 *
 * 音频管理
 *
 */
open class DefaultAudioManager(context: Context, mediaPlayer: IMediaPlayer<*>?, private var audioRequestMode: AudioRequestMode = AudioRequestMode.TRANSIENT) :
    IAudioManager {
    private val audioManager: AudioManager = context.getSystemService(Service.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    override var onAudioFocusChangeListener: AudioManager.OnAudioFocusChangeListener = DefaultAudioFocusChangeListener(
        WeakReference(context), this, mediaPlayer)

    override fun mute() {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND)
    }

    override fun setVolume(volume: Int) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        val level = volume % maxVolume
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, level.toInt(), AudioManager.FLAG_PLAY_SOUND)
    }

    override fun getVolume(): Int {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    override fun getMaxVolume(): Int {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) - 1
    }

    override fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestAudioFocusAfterEight()
        } else {
            requestAudioFocusBeforeEight()
        }
    }

    override fun abandonAudioFocus() {
        SimpleLogger.instance.debugD(Constant.TAG,"放弃音频焦点----------------")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
                audioFocusRequest = null
                Log.d(Constant.TAG,"执行放弃音频焦点AfterO")
            }
        } else {
            audioManager.abandonAudioFocus(onAudioFocusChangeListener)
            Log.d(Constant.TAG,"执行放弃音频焦点BeforeO")
        }
    }


    private fun requestAudioFocusBeforeEight(){
        if (audioRequestMode == AudioRequestMode.NONE)
            return
        val mode = when(audioRequestMode){
            AudioRequestMode.GAIN -> AudioManager.AUDIOFOCUS_GAIN
            AudioRequestMode.DUCK -> AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            AudioRequestMode.TRANSIENT -> AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            AudioRequestMode.EXCLUSIVE -> AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            else -> AudioManager.AUDIOFOCUS_GAIN
        }
        audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC,mode)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestAudioFocusAfterEight(){
        var builder: AudioFocusRequest.Builder?
        when(audioRequestMode){

            AudioRequestMode.GAIN ->{
                builder = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setFocusGain(
                    AudioManager.AUDIOFOCUS_GAIN)
            }
            AudioRequestMode.DUCK ->{
                builder = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).setFocusGain(
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            }
            AudioRequestMode.TRANSIENT ->{
                builder = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setFocusGain(
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            }
            AudioRequestMode.EXCLUSIVE ->{
                builder = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE).setFocusGain(
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            }
            else ->{
                return
            }
        }
        audioFocusRequest = builder
            .setAudioAttributes(createAudioAttributes())
            .setOnAudioFocusChangeListener(onAudioFocusChangeListener)
            .setAcceptsDelayedFocusGain(false)
            .build().also {
                audioManager.requestAudioFocus(it)
            }
    }

    private fun createAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
    }


    enum class AudioRequestMode{
        NONE,           //不请求焦点
        GAIN,           //打断正在播放
        TRANSIENT,      //短暂获得焦点，暂停之前的音频，释放后恢复
        DUCK,           //获得焦点，降低此前音频音量，混合输出
        EXCLUSIVE       //短暂获得焦点
    }
}