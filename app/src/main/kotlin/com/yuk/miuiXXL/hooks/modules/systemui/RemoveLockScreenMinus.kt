package com.yuk.miuiXXL.hooks.modules.systemui

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yuk.miuiXXL.hooks.modules.BaseHook
import com.yuk.miuiXXL.utils.XSharedPreferences.getBoolean

object RemoveLockScreenMinus : BaseHook() {
    override fun init() {

        if (!getBoolean("systemui_lockscreen_remove_minus", false)) return
        loadClass("com.android.keyguard.negative.MiuiKeyguardMoveLeftViewContainer").methodFinder().filterByName("inflateLeftView").first().createHook {
            before {
                it.result = null
            }
        }
    }

}
