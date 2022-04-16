package com.yuk.miuidialog

import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import kotlin.collections.HashSet

class XposedInit : IXposedHookLoadPackage, IXposedHookZygoteInit {

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
    }

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        val mPkgName = lpparam.packageName
        val isMiuiApp = mPkgName.startsWith("com.miui") || mPkgName.startsWith("com.xiaomi") || miuiApps.contains(mPkgName)
        if (isMiuiApp) {
            val dialogCls = XposedHelpers.findClassIfExists("miuix.appcompat.app.AlertController", lpparam.classLoader)
            XposedBridge.hookAllMethods(dialogCls, "setupDialogPanel", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (dialogCls != null) {
                        val mParentPanel = XposedHelpers.getObjectField(param.thisObject, "mParentPanel") as View
                        val layoutParams = mParentPanel.layoutParams as FrameLayout.LayoutParams
                        layoutParams.gravity = Gravity.BOTTOM
                        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                        mParentPanel.layoutParams = layoutParams
                    }
                }
            })
        }
    }
}


val miuiApps = HashSet<String>(
    listOf(
        "com.android.providers.downloads.ui",
        "com.android.fileexplorer"
    )
)