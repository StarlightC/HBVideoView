package com.starlightc.core

import android.util.Log

/**
 * @author StarlightC
 * @since 2022/4/24
 *
 * 对Log方法的简易封装与管理
 */
class SimpleLogger private constructor(){
    companion object {
        @JvmStatic
        val instance: SimpleLogger by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SimpleLogger()
        }
    }
    private var tagAvailabilityMap: HashMap<String, Boolean> = HashMap()
    private var isSilence = false
    private val isDebug: Boolean
    get() {
        return BuildConfig.DEBUG
    }

    init {
        tagAvailabilityMap[Constant.TAG] = true
    }

    fun debugD(text: String) {
        if (isDebug) {
            logD(Constant.TAG, text)
        }
    }

    fun debugD(tag: String = Constant.TAG, text: String) {
        if (isDebug) {
            logD(tag, text)
        }
    }

    fun debugE(text: String) {
        if (isDebug) {
            logE(Constant.TAG, text)
        }
    }
    fun debugE(tag: String = Constant.TAG, text: String) {
        if (isDebug) {
            logE(tag, text)
        }
    }

    fun debugI(text: String) {
        if (isDebug) {
            logI(Constant.TAG, text)
        }
    }

    fun debugI(tag: String = Constant.TAG, text: String) {
        if (isDebug) {
            logI(tag, text)
        }
    }

    fun debugW(text: String) {
        if (isDebug) {
            logW(Constant.TAG, text)
        }
    }

    fun debugW(tag: String = Constant.TAG, text: String) {
        if (isDebug) {
            logW(tag, text)
        }
    }

    fun logD(tag: String = Constant.TAG, text: String) {
        if (isTagEnabled(tag)) {
            Log.d(tag, text)
        }
    }

    fun logE(tag: String = Constant.TAG, text: String) {
        if (isTagEnabled(tag)) {
            Log.e(tag, text)
        }
    }

    fun logI(tag: String = Constant.TAG, text: String) {
        if (isTagEnabled(tag)) {
            Log.i(tag, text)
        }
    }

    fun logW(tag: String = Constant.TAG, text: String) {
        if (isTagEnabled(tag)) {
            Log.w(tag, text)
        }
    }

    fun logWTF(tag: String = Constant.TAG, text: String) {
        if (isTagEnabled(tag)) {
            Log.wtf(tag, text)
        }
    }

    fun logV(tag: String = Constant.TAG, text: String) {
        if (isTagEnabled(tag)) {
            Log.v(tag, text)
        }
    }


    fun silence() {
        isSilence = true
    }

    fun active() {
        isSilence = false
    }

    private fun isTagEnabled(tag: String): Boolean {
       return if (isSilence) {
           false
       } else {
           tagAvailabilityMap[tag] != false
       }
    }


}