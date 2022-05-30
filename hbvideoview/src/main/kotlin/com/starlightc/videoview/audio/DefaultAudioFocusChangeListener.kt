package com.starlightc.videoview.audio

import android.app.Service
import android.content.Context
import android.media.AudioManager
import com.starlightc.core.infomation.PlayerState
import com.starlightc.videoview.interfaces.IAudioManager
import com.starlightc.core.interfaces.IMediaPlayer
import java.lang.ref.WeakReference

/**
 * @author StarlightC
 * @since 2022/4/24
 *
 * 提供默认音频焦点处理
 */
open class DefaultAudioFocusChangeListener(
    private val contextReference: WeakReference<Context>,
    private val audioManager: IAudioManager,
    private val mediaPlayer: IMediaPlayer<*>?
) : AudioManager.OnAudioFocusChangeListener {
    private var playOnAudioFocus = true;
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                val context = contextReference.get() ?: return
                // 重新获得焦点，恢复正常音量，恢复播放
                if (playOnAudioFocus && isPlayable() && mediaPlayer?.isPlaying != true) {
                    mediaPlayer?.start();
                } else if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer.setVolume(getCurrentVolume(context));
                }
                playOnAudioFocus = false;
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                //短暂失去焦点，无须停止播放，只适当降低播放器音量
                val context = contextReference.get() ?: return
                val duckVolume = getCurrentVolume(context) * 0.8f
                mediaPlayer?.setVolume(duckVolume);
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                //暂时失去焦点，暂停
                if (mediaPlayer?.isPlaying == true) {
                    playOnAudioFocus = true;
                    mediaPlayer.pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                //失去焦点，停止播放，如播放其他多媒体
                audioManager.abandonAudioFocus()
                playOnAudioFocus = false;
                mediaPlayer?.stop()
            }
        }
    }

    private fun isPlayable(): Boolean {
        return when (mediaPlayer?.playerStateLD?.value ?: PlayerState.IDLE) {
            PlayerState.COMPLETED, PlayerState.STARTED, PlayerState.PREPARED, PlayerState.PAUSED -> true
            else -> false
        }
    }

    /**
     * 获取当前系统音量
     */
    private fun getCurrentVolume(context: Context): Float {
        val audioManager = context.getSystemService(Service.AUDIO_SERVICE) as AudioManager
        val streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        return streamVolume * 1.000f / maxVolume
    }
}