package com.starlightc.ijkplayer

import android.content.Context
import android.preference.PreferenceManager
import androidx.annotation.StringRes
import com.starlightc.core.interfaces.Settings

/**
 * @author StarlightC
 * @since 2022/4/26
 *
 * TODO: description
 */
class IjkSettings(context: Context): Settings {
    private var mAppContext = context.applicationContext
    private var mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext)

    /**
     * 后台播放
     */
    fun getEnableBackgroundPlay(): Boolean {
        val key = getStringFromRes(R.string.setting_video_background_play)
        return mSharedPreferences.getBoolean(key, false)
    }

    fun setEnableBackgroundPlay(flag: Boolean) {
        val key = getStringFromRes(R.string.setting_video_background_play)
        putBoolean(key, flag)
    }

    /**
     * 播放器内核
     */
    fun getPlayer(): MEDIA_PLAYER_TYPE {
        val key = getStringFromRes(R.string.setting_video_player)
        val value = mSharedPreferences.getString(key, "")
        return convertCode2Enum(value?:"")
    }

    fun setPlayer(type: MEDIA_PLAYER_TYPE) {
        val key = getStringFromRes(R.string.setting_video_player)
        putString(key, convertEnum2Code(type))
    }

    /**
     * 硬件加速解码
     */
    fun getUsingMediaCodec(): Boolean {
        val key = mAppContext.getString(R.string.setting_video_using_media_codec)
        return mSharedPreferences.getBoolean(key, false)
    }

    fun setUsingMediaCodec(flag: Boolean) {
        val key = mAppContext.getString(R.string.setting_video_using_media_codec)
        putBoolean(key, flag)
    }

    /**
     * 硬件加速(自动旋转)
     */
    fun getUsingMediaCodecAutoRotate(): Boolean {
        val key = mAppContext.getString(R.string.setting_video_using_media_codec_auto_rotate)
        return mSharedPreferences.getBoolean(key, false)
    }

    fun setUsingMediaCodecAutoRotate(flag: Boolean) {
        val key = mAppContext.getString(R.string.setting_video_using_media_codec_auto_rotate)
        putBoolean(key, flag)
    }

    /**
     * 硬件加速(处理环境改变)
     */
    fun getMediaCodecHandleResolutionChange(): Boolean {
        val key = mAppContext.getString(R.string.setting_video_media_codec_handle_resolution_change)
        return mSharedPreferences.getBoolean(key, true)
    }

    fun setMediaCodecHandleResolutionChange(flag: Boolean) {
        val key = mAppContext.getString(R.string.setting_video_media_codec_handle_resolution_change)
        putBoolean(key, flag)
    }

    /**
     * 像素制式
     */
    fun getPixelFormat(): PIXEL_FORMAT {
        val key = mAppContext.getString(R.string.setting_video_pixel_format)
        return convertPFCode2Enum(mSharedPreferences.getString(key, "")?:"")
    }

    fun getPixelFormatValue(): String {
        val key = mAppContext.getString(R.string.setting_video_pixel_format)
        return mSharedPreferences.getString(key, "")?:""
    }

    fun setPixelFormat(format: PIXEL_FORMAT) {
        val key = mAppContext.getString(R.string.setting_video_pixel_format)
        putString(key, convertEnum2PFCode(format))
    }

    /**
     * 无视图
     */
    fun getEnableNoView(): Boolean {
        val key = mAppContext.getString(R.string.setting_video_enable_no_view)
        return mSharedPreferences.getBoolean(key, false)
    }

    fun setEnableNoView(flag: Boolean) {
        val key = mAppContext.getString(R.string.setting_video_enable_no_view)
        putBoolean(key, flag)
    }

    /**
     * SurfaceView
     */
    fun getEnableSurfaceView(): Boolean {
        val key = mAppContext.getString(R.string.setting_video_enable_surface_view)
        return mSharedPreferences.getBoolean(key, false)
    }

    fun setEnableSurfaceView(flag: Boolean) {
        val key = mAppContext.getString(R.string.setting_video_enable_surface_view)
        putBoolean(key, flag)
    }

    /**
     * TextureView
     */
    fun getEnableTextureView(): Boolean {
        val key = mAppContext.getString(R.string.setting_video_enable_texture_view)
        return mSharedPreferences.getBoolean(key, false)
    }

    fun setEnableTextureView(flag: Boolean) {
        val key = mAppContext.getString(R.string.setting_video_enable_texture_view)
        putBoolean(key, flag)
    }

    /**
     * DetachedSurfaceTextureView
     */
    fun getEnableDetachedSurfaceTextureView(): Boolean {
        val key = mAppContext.getString(R.string.setting_video_enable_detached_surface_texture)
        return mSharedPreferences.getBoolean(key, false)
    }

    fun setEnableDetachedSurfaceTextureView(flag: Boolean) {
        val key = mAppContext.getString(R.string.setting_video_enable_detached_surface_texture)
        putBoolean(key, flag)
    }

    /**
     * 使用多媒体资源
     */
    fun getUsingMediaDataSource(): Boolean {
        val key = mAppContext.getString(R.string.setting_video_using_mediadatasource)
        return mSharedPreferences.getBoolean(key, false)
    }

    fun setUsingMediaDataSource(flag: Boolean) {
        val key = mAppContext.getString(R.string.setting_video_using_mediadatasource)
        putBoolean(key, flag)
    }





    //Utils

    private fun getStringFromRes(@StringRes strRes: Int) : String {
        return mAppContext.getString(strRes)
    }

    private fun putString(key:String, value: String?){
        val editor = mSharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun putBoolean(key: String, value: Boolean) {
        val editor = mSharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    private fun convertEnum2Code(type: MEDIA_PLAYER_TYPE) : String {
        return when(type) {
            MEDIA_PLAYER_TYPE.IJK_MEDIA_PLAYER -> "0"
            MEDIA_PLAYER_TYPE.ANDROID_MEDIA_PLAYER -> "1"
            MEDIA_PLAYER_TYPE.EXO_MEDIA_PLAYER -> "2"
        }
    }

    private fun convertCode2Enum(code: String) : MEDIA_PLAYER_TYPE {
        return when(code) {
            "0" -> MEDIA_PLAYER_TYPE.IJK_MEDIA_PLAYER
            "1" -> MEDIA_PLAYER_TYPE.ANDROID_MEDIA_PLAYER
            "2" -> MEDIA_PLAYER_TYPE.EXO_MEDIA_PLAYER
            else -> MEDIA_PLAYER_TYPE.IJK_MEDIA_PLAYER
        }
    }

    enum class MEDIA_PLAYER_TYPE {
        IJK_MEDIA_PLAYER,
        ANDROID_MEDIA_PLAYER,
        EXO_MEDIA_PLAYER
    }

    private fun convertEnum2PFCode(type: PIXEL_FORMAT) : String {
        return when(type) {
            PIXEL_FORMAT.RGBX_8888 -> "fcc-rv32"
            PIXEL_FORMAT.RGB_565-> "fcc-rv16"
            PIXEL_FORMAT.RGB_888 -> "fcc-rv24"
            PIXEL_FORMAT.YV12 -> "fcc-yv12"
            PIXEL_FORMAT.penGL_ES2 -> "fcc-_es2"
        }
    }

    private fun convertPFCode2Enum(code: String) : PIXEL_FORMAT {
        return when(code) {
            "fcc-rv32" -> PIXEL_FORMAT.RGBX_8888
            "fcc-rv16" -> PIXEL_FORMAT.RGB_565
            "fcc-rv24" -> PIXEL_FORMAT.RGB_888
            "fcc-yv12" -> PIXEL_FORMAT.YV12
            "fcc-_es2" -> PIXEL_FORMAT.penGL_ES2
            else -> PIXEL_FORMAT.RGBX_8888
        }
    }

    enum class PIXEL_FORMAT {
        RGBX_8888,
        RGB_565,
        RGB_888,
        YV12,
        penGL_ES2
    }

}