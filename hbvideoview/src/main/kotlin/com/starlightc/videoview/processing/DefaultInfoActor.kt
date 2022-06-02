package com.starlightc.videoview.processing

import androidx.lifecycle.MutableLiveData
import com.starlightc.video.core.SimpleLogger
import com.starlightc.video.core.infomation.PlayerState
import com.starlightc.videoview.config.ErrorCode
import com.starlightc.videoview.interfaces.InfoActor
import com.starlightc.videoview.widget.AbsVideoView

/**
 * @author StarlightC
 * @since 2022/4/26
 *
 */
class DefaultInfoActor:  InfoActor{
    override fun doInfoAction(view: AbsVideoView, code: Int, extra: Int) {
        when(code) {
            1 -> { //开始视频or音频渲染
                view.videoUI?.hideLoading()
                view.videoUI?.hideError()
                //videoUI?.hideShare()
                view.retryCount = 0
            }
            2 -> { //开始缓冲
                view.videoUI?.showLoading()
                //danmakuView?.pause()
            }
            3 -> { //从缓冲结束
                view.videoUI?.hideLoading()
                view.retryCount = 0
            }
            4 -> { //视频方向更改
                view.setVideoRotation(extra.toFloat())
            }
        }
    }

    override fun doErrorAction(view: AbsVideoView, code: Int, extra: Int) {
        when (code) {
            -1 -> { // 未知
                view.videoUI?.showError(ErrorCode.UNKNOWN)
                (view.mediaPlayer?.playerStateLD as MutableLiveData<PlayerState>).value = PlayerState.ERROR
            }
            1 -> { // 默认错误重试
                view.retry()
            }
            2 -> { // 超时重试
                view.retry(ErrorCode.ERROR_TIME_OUT)
            }
        }
    }
}