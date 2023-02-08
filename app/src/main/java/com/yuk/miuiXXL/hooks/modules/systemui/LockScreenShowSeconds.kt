package com.yuk.miuiXXL.hooks.modules.systemui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.provider.Settings
import android.widget.LinearLayout
import android.widget.TextView
import com.github.kyuubiran.ezxhelper.utils.findConstructor
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.getObjectAs
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.yuk.miuiXXL.hooks.modules.BaseHook
import com.yuk.miuiXXL.utils.getBoolean
import de.robv.android.xposed.XC_MethodHook
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Timer
import java.util.TimerTask

object LockScreenShowSeconds : BaseHook() {
    private var nowTime: Date = Calendar.getInstance().time

    override fun init() {
        if (!getBoolean("systemui_lockscreen_show_seconds", false)) return
        findConstructor("com.miui.clock.MiuiBaseClock") {
            parameterCount == 2
        }.hookAfter {
            try {
                val viewGroup = it.thisObject as LinearLayout
                val d: Method = viewGroup.javaClass.getDeclaredMethod("updateTime")
                val r = Runnable {
                    d.isAccessible = true
                    d.invoke(viewGroup)
                }

                class T : TimerTask() {
                    override fun run() {
                        Handler(viewGroup.context.mainLooper).post(r)
                    }
                }
                Timer().scheduleAtFixedRate(T(), 1000 - System.currentTimeMillis() % 1000, 1000)
            } catch (_: Exception) {
            }
        }

        findMethod("com.miui.clock.MiuiLeftTopClock") {
            name == "updateTime"
        }.hookAfter { updateTime(it, false) }

        findMethod("com.miui.clock.MiuiCenterHorizontalClock") {
            name == "updateTime"
        }.hookAfter { updateTime(it, false) }

        findMethod("com.miui.clock.MiuiLeftTopLargeClock") {
            name == "updateTime"
        }.hookAfter { updateTime(it, false) }

        findMethod("com.miui.clock.MiuiVerticalClock") {
            name == "updateTime"
        }.hookAfter { updateTime(it, true) }
    }

    private fun updateTime(it: XC_MethodHook.MethodHookParam, isVertical: Boolean) {
        val textV = it.thisObject.getObjectAs<TextView>("mTimeText")
        val c: Context = textV.context
        val is24 = Settings.System.getString(c.contentResolver, Settings.System.TIME_12_24) == "24"
        nowTime = Calendar.getInstance().time
        textV.text = getTime(is24, isVertical)

    }


    @SuppressLint("SimpleDateFormat")
    private fun getTime(is24: Boolean, isVertical: Boolean): String {
        var timePattern = ""
        timePattern += if (isVertical) { //垂直
            if (is24) "HH\nmm\nss" else "hh\nmm\nss"
        } else { //水平
            if (is24) "HH:mm:ss" else "h:mm:ss"
        }
        timePattern = SimpleDateFormat(timePattern).format(nowTime)
        return timePattern
    }

}
