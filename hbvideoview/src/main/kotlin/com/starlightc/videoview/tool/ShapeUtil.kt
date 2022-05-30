package com.starlightc.videoview.tool

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.core.content.res.ResourcesCompat

/**
 * @author StarlightC
 * @since 2022/4/24
 *
 * Shape生成工具
 */
object ShapeUtil {
    /**
     *
     * @param context
     * @param colorId 内容填充颜色(id)
     * @param roundRadiusDp 4个角的圆角半径(dp)
     * @return
     */
    fun getRectShape(context: Context, colorId: Int, roundRadiusDp: Float): GradientDrawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        val roundRadiusPx: Float = DisplayUtil.dp2px(context, roundRadiusDp).toFloat()
        drawable.cornerRadius = roundRadiusPx
        if (colorId != -1) {
            drawable.setColor(ResourcesCompat.getColor(context.resources, colorId, null))
        }
        return drawable
    }
}