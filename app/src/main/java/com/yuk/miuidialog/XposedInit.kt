package com.yuk.miuidialog

import android.app.AndroidAppHelper
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.findField
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.isPrivate
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class XposedInit : IXposedHookLoadPackage, IXposedHookZygoteInit {

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
    }

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        EzXHelperInit.initHandleLoadPackage(lpparam)
        val mPkgName = lpparam.packageName
        val isMiuiApps = mPkgName.startsWith("com.miui") || mPkgName.startsWith("com.xiaomi") || miuiApps.contains(mPkgName)
        if (isMiuiApps) {
            findAllMethods("miuix.appcompat.app.AlertController", lpparam.classLoader) {
                isPrivate && parameterCount == 1 && parameterTypes[0] == Configuration::class.java && returnType == Void.TYPE
            }.hookAfter {
                val context = AndroidAppHelper.currentApplication().applicationContext
                val dialogCls = XposedHelpers.findClass("miuix.internal.widget.DialogParentPanel", lpparam.classLoader)
                val field = findField("miuix.appcompat.app.AlertController", lpparam.classLoader) { type == dialogCls }
                val mParentPanel = field.get(it.thisObject) as View
                val radius = floatArrayOf(dp2px(25f, context).toFloat(), dp2px(25f, context).toFloat(), 0f, 0f)
                val background = createDrawable(Color.parseColor(if (isDarkMode(context)) "#242424" else "#FFFFFF"), 0, 0, radius)
                val layoutParams = mParentPanel.layoutParams as FrameLayout.LayoutParams
                layoutParams.gravity = Gravity.BOTTOM
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                layoutParams.marginEnd = 0
                layoutParams.marginStart = 0
                mParentPanel.layoutParams = layoutParams
                mParentPanel.background = background
            }
        }
    }

    private val miuiApps = HashSet<String>(
        listOf(
            "android",
            "com.android.fileexplorer",
            "com.android.nfc",
            "com.android.providers.downloads.ui",
            "com.android.settings",
            "com.miui.cleanmaster",
            "com.miui.cloudservice",
            "com.miui.gallery",
            "com.miui.mishare.connectivity",
            "com.miui.notes",
            "com.xiaomi.misettings",
        )
    )
}


/**
 * @param color       填充色
 * @param strokeColor 线条颜色
 * @param strokeWidth 线条宽度  单位px
 * @param radius      角度  px,长度为4,分别表示左上,右上,右下,左下的角度
 */
fun createDrawable(color: Int, strokeColor: Int, strokeWidth: Int, radius: FloatArray): GradientDrawable {
    return try {
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            if (strokeColor != 0) setStroke(strokeWidth, strokeColor)
            if (radius.size == 4) {
                cornerRadii = floatArrayOf(radius[0], radius[0], radius[1], radius[1], radius[2], radius[2], radius[3], radius[3])
            }
        }
    } catch (e: Exception) {
        GradientDrawable()
    }
}

fun isDarkMode(context: Context): Boolean = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun dp2px(dpValue: Float,context: Context): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.resources.displayMetrics).toInt()