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
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class XposedInit : IXposedHookLoadPackage, IXposedHookZygoteInit {

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
    }

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        val mPkgName = lpparam.packageName
        val isMiuiApp = mPkgName.startsWith("com.miui") || mPkgName.startsWith("com.xiaomi") || miuiApps.contains(mPkgName)
        fun isDarkMode(context: Context): Boolean = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        if (isMiuiApp) {
            val dialogCls = XposedHelpers.findClassIfExists("miuix.appcompat.app.AlertController", lpparam.classLoader)
            XposedBridge.hookAllMethods(dialogCls, "setupDialogPanel", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (dialogCls != null) {
                        val context = AndroidAppHelper.currentApplication().applicationContext
                        val mParentPanel = XposedHelpers.getObjectField(param.thisObject, "mParentPanel") as View
                        val layoutParams = mParentPanel.layoutParams as FrameLayout.LayoutParams
                        layoutParams.gravity = Gravity.BOTTOM
                        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                        layoutParams.marginEnd = 0
                        layoutParams.marginStart = 0
                        mParentPanel.layoutParams = layoutParams
                        mParentPanel.background = if (isDarkMode(context)) moduleRes.getDrawable(R.drawable.dialog_background_night) else moduleRes.getDrawable(R.drawable.dialog_background)
                    }
                }
            })
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