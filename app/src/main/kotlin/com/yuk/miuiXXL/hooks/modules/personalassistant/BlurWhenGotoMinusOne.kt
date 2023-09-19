package com.yuk.miuiXXL.hooks.modules.personalassistant

import android.content.res.Configuration
import com.github.kyuubiran.ezxhelper.ObjectHelper.Companion.objectHelper
import com.yuk.miuiXXL.hooks.modules.BaseHook
import com.yuk.miuiXXL.utils.KotlinXposedHelper.callMethod
import com.yuk.miuiXXL.utils.KotlinXposedHelper.findClass
import com.yuk.miuiXXL.utils.KotlinXposedHelper.getIntField
import com.yuk.miuiXXL.utils.KotlinXposedHelper.hookBeforeAllMethods
import com.yuk.miuiXXL.utils.KotlinXposedHelper.hookBeforeMethod
import com.yuk.miuiXXL.utils.KotlinXposedHelper.new
import com.yuk.miuiXXL.utils.KotlinXposedHelper.replaceMethod
import com.yuk.miuiXXL.utils.XSharedPreferences.getBoolean

object BlurWhenGotoMinusOne : BaseHook() {
    override fun init() {

        if (!getBoolean("personalassistant_minus_one_blur", false)) return
        val deviceAdapter = "com.miui.personalassistant.device.DeviceAdapter".findClass()
        val foldableDeviceAdapter = "com.miui.personalassistant.device.FoldableDeviceAdapter".findClass()
        deviceAdapter.hookBeforeAllMethods("create") {
            it.result = foldableDeviceAdapter.new(it.args[0])
        }
        try {
            foldableDeviceAdapter.hookBeforeMethod("onEnter", Boolean::class.java) {
                it.thisObject.objectHelper().setObject("mScreenSize", 3)
            }
        } catch (e: ClassNotFoundException) {
            foldableDeviceAdapter.hookBeforeMethod("onOpened") {
                it.thisObject.objectHelper().setObject("mScreenSize", 3)
            }
        }
        foldableDeviceAdapter.hookBeforeMethod("onConfigurationChanged", Configuration::class.java) {
            it.thisObject.objectHelper().setObject("mScreenSize", 3)
        }
        foldableDeviceAdapter.replaceMethod("onScroll", Float::class.java) {
            val f = it.args[0] as Float
            val i = (f * 100.0f).toInt()
            val mCurrentBlurRadius: Int = it.thisObject.getIntField("mCurrentBlurRadius")
            if (mCurrentBlurRadius != i) {
                if (mCurrentBlurRadius <= 0 || i >= 0) {
                    it.thisObject.objectHelper().setObject("mCurrentBlurRadius", i)
                } else {
                    it.thisObject.objectHelper().setObject("mCurrentBlurRadius", 0)
                }
                it.thisObject.callMethod("blurOverlayWindow", mCurrentBlurRadius)
            }
        }
    }

}
