package com.starlightc.videoview.tool

import androidx.lifecycle.MutableLiveData

/**
 * @author StarlightC
 * @since 2022/4/25
 *
 * 电池信息获取帮助类
 */
class BatteryInfoHelper {

    val batteryPercentLD: MutableLiveData<Int> = MutableLiveData()
    val batteryChargeLD: MutableLiveData<Boolean> = MutableLiveData()
}