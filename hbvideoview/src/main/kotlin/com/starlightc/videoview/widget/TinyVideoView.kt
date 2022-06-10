package com.starlightc.videoview.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * TODO: description
 */
class TinyVideoView: AbsVideoView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun initObserverAndListener() {
        TODO("Not yet implemented")
    }

    override fun initDanmaku(danmakuView: View) {
        TODO("Not yet implemented")
    }

    override fun initUI() {
        TODO("Not yet implemented")
    }

    override fun initCover() {
        TODO("Not yet implemented")
    }
}