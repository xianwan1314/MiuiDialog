package com.yuk.miuidialog

import android.app.AndroidAppHelper
import android.content.Context
import android.content.res.Configuration
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.init.InitFields.moduleRes
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
        fun isDarkMode(context: Context): Boolean = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        if (isMiuiApps) {
            findAllMethods("miuix.appcompat.app.AlertController", lpparam.classLoader) {
                isPrivate && parameterCount == 1 && parameterTypes[0] == Configuration::class.java && returnType == Void.TYPE
            }.hookAfter {
                val context = AndroidAppHelper.currentApplication().applicationContext
                val dialogCls = XposedHelpers.findClass("miuix.internal.widget.DialogParentPanel", lpparam.classLoader)
                val f = findField("miuix.appcompat.app.AlertController", lpparam.classLoader) { type == dialogCls }
                val mParentPanel = f.get(it.thisObject) as View
                val layoutParams = mParentPanel.layoutParams as FrameLayout.LayoutParams
                layoutParams.gravity = Gravity.BOTTOM
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                layoutParams.marginEnd = 0
                layoutParams.marginStart = 0
                mParentPanel.layoutParams = layoutParams
                mParentPanel.background = if (isDarkMode(context)) moduleRes.getDrawable(R.drawable.dialog_background_night) else moduleRes.getDrawable(R.drawable.dialog_background)
            }
        }
    }
}

val miuiApps = HashSet<String>(
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