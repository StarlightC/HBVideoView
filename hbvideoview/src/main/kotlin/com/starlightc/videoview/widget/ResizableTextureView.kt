package com.starlightc.videoview.widget

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.TextureView
import android.view.View
import com.starlightc.videoview.config.ScaleType
import com.starlightc.videoview.tool.DisplayUtil

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * 可缩放TextureView，可设置缩放模式
 */
class ResizableTextureView : TextureView {
    private var mFixedHeight = -1
    private var mFixedWidth = -1
    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var screenType: ScaleType = ScaleType.DEFAULT
    private var mMatrix: Matrix? = null

    var videoRotation = 0f

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    fun setVideoSize(width: Int, height: Int) {
        mVideoWidth = width
        mVideoHeight = height
        matrixTransform()
        requestLayout()
    }

    private fun resetTransform() {
        mMatrix?.let {
            it.reset()
            setTransform(it)
            postInvalidate()
        }
    }

    fun requestMatrixTransform() {
        matrixTransform()
    }

    fun setFixedContentHeight(fixedH: Int){
        mFixedWidth = -1
        mFixedHeight = fixedH
        if (mFixedHeight != -1 || mFixedWidth != -1) {
            screenType = ScaleType.SCALE_CONSTRAINT_ORIGIN
            requestLayout()
        }
    }

    /**
     * 指定宽高，-1为不作约束
     */
    fun setFixedSize(fixedW: Int, fixedH: Int){
        mFixedWidth = fixedW
        mFixedHeight = fixedH
        if (mFixedHeight != -1 || mFixedWidth != -1) {
            screenType = ScaleType.SCALE_CONSTRAINT_ORIGIN
            requestLayout()
        }
    }

    fun setScreenScale(type: ScaleType) {
        screenType = type
        matrixTransform()
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSpec: Int = widthMeasureSpec
        var heightSpec = heightMeasureSpec
        var width = View.getDefaultSize(mVideoWidth, widthSpec)
        var height = View.getDefaultSize(mVideoHeight, heightSpec)
        when (screenType) {
            ScaleType.SCALE_ORIGINAL -> {
                width = mVideoWidth
                height = mVideoHeight
            }
            ScaleType.SCALE_16_9 -> if (height > width / 16 * 9) {
                height = width / 16 * 9
            } else {
                width = height / 9 * 16
            }
            ScaleType.SCALE_4_3 -> if (height > width / 4 * 3) {
                height = width / 4 * 3
            } else {
                width = height / 3 * 4
            }
            ScaleType.SCALE_MATCH_PARENT -> {
                width = widthSpec
                height = heightSpec
            }
            ScaleType.SCALE_CENTER -> if (mVideoWidth > 0 && mVideoHeight > 0) {
                if (mVideoWidth * height > width * mVideoHeight) {
                    width = height * mVideoWidth / mVideoHeight
                } else {
                    height = width * mVideoHeight / mVideoWidth
                }
            }
            ScaleType.DEFAULT -> if (mVideoWidth > 0 && mVideoHeight > 0) {
                val widthSpecMode = MeasureSpec.getMode(widthSpec)
                val widthSpecSize = MeasureSpec.getSize(widthSpec)
                val heightSpecMode = MeasureSpec.getMode(heightSpec)
                val heightSpecSize = MeasureSpec.getSize(heightSpec)
                if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) { // the size is fixed
                    width = widthSpecSize
                    height = heightSpecSize
                    // for compatibility, we adjust size based on aspect ratio
                    if (mVideoWidth * height < width * mVideoHeight) { //Log.i("@@@", "image too wide, correcting");
                        width = height * mVideoWidth / mVideoHeight
                    } else if (mVideoWidth * height > width * mVideoHeight) { //Log.i("@@@", "image too tall, correcting");
                        height = width * mVideoHeight / mVideoWidth
                    }
                } else if (widthSpecMode == MeasureSpec.EXACTLY) { // only the width is fixed, adjust the height to match aspect ratio if possible
                    width = widthSpecSize
                    height = width * mVideoHeight / mVideoWidth
                    if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) { // couldn't match aspect ratio within the constraints
                        height = heightSpecSize
                    }
                } else if (heightSpecMode == MeasureSpec.EXACTLY) { // only the height is fixed, adjust the width to match aspect ratio if possible
                    height = heightSpecSize
                    width = height * mVideoWidth / mVideoHeight
                    if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) { // couldn't match aspect ratio within the constraints
                        width = widthSpecSize
                    }
                } else { // neither the width nor the height are fixed, try to use actual video size
                    width = mVideoWidth
                    height = mVideoHeight
                    if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) { // too tall, decrease both width and height
                        height = heightSpecSize
                        width = height * mVideoWidth / mVideoHeight
                    }
                    if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) { // too wide, decrease both width and height
                        width = widthSpecSize
                        height = width * mVideoHeight / mVideoWidth
                    }
                }
            }
            ScaleType.SCALE_CONSTRAINT_ORIGIN -> {
                if (mFixedHeight != -1 && mVideoHeight != 0) {
                    height = mFixedHeight
                    width = (mFixedHeight.toFloat() * mVideoWidth.toFloat() / mVideoHeight.toFloat()).toInt()
                } else if (mFixedWidth != -1 && mVideoWidth != 0){
                    width = mFixedWidth
                    height = (mFixedWidth.toFloat() * mVideoHeight.toFloat() / mVideoWidth.toFloat()).toInt()
                }
            }
            ScaleType.SCALE_VERTICAL -> {
                width = DisplayUtil.getScreenWidth(context)
            }
        }
        setMeasuredDimension(width, height)
    }

    private fun verticalVideoTransform() {
        if (height == 0 || width == 0 || mVideoHeight == 0 || mVideoWidth == 0) {
            return
        }

        val scale = height.toFloat() / mVideoWidth.toFloat()

        mMatrix = matrix

        mMatrix!!.reset()

        //第2步:把视频区移动到View区,使两者中心点重合.
        mMatrix!!.preTranslate((width - mVideoWidth).toFloat() / 2, (height - mVideoHeight).toFloat() / 2)


        //第1步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
        mMatrix!!.preScale(mVideoWidth / width.toFloat(), mVideoHeight / height.toFloat())

        //第3步:应用旋转
        mMatrix!!.postRotate(
            videoRotation,
            width / 2f,
            height / 2f
        )

        //第4步,等比例放大或缩小,直到视频区的一边与View的另一边相等.
        mMatrix!!.postScale(
            scale,
            scale,
            (width / 2).toFloat(),
            height.toFloat() / 2
        )

        setTransform(mMatrix)
        postInvalidate()
    }

    private fun videoCropTransform() {
        if (height == 0 || width == 0 || mMatrix?.equals(matrix) == true) {
            return
        }
        val sx = width.toFloat() / mVideoWidth.toFloat()
        val sy = height.toFloat() / mVideoHeight.toFloat()

        val maxScale = Math.max(sx, sy)
        mMatrix = matrix

        //第2步:把视频区移动到View区,使两者中心点重合.
        mMatrix!!.preTranslate((width - mVideoWidth).toFloat() / 2, (height - mVideoHeight).toFloat() / 2)

        //第1步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
        mMatrix!!.preScale(mVideoWidth / width.toFloat(), mVideoHeight / height.toFloat())


        //第3步,等比例放大或缩小,直到视频区的一边超过View一边, 另一边与View的另一边相等. 因为超过的部分超出了View的范围,所以是不会显示的,相当于裁剪了.
        mMatrix!!.postScale(
            maxScale,
            maxScale,
            (width / 2).toFloat(),
            height.toFloat() / 2
        ) //后两个参数坐标是以整个View的坐标系以参考的

        setTransform(mMatrix)
        postInvalidate()
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        matrixTransform()

    }

    private fun matrixTransform() {
        if (videoRotation % 180f != 0f) {
            verticalVideoTransform()
        } else if (screenType == ScaleType.SCALE_CENTER_CROP) {
            videoCropTransform()
        } else if (screenType == ScaleType.DEFAULT) {
            resetTransform()
        }
    }
}