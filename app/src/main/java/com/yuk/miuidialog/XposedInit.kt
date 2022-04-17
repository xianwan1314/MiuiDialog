package com.yuk.miuidialog

import android.app.AndroidAppHelper
import android.content.Context
import android.content.res.Configuration
import android.os.Process
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.init.InitFields.moduleRes
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.findField
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.isPrivate
import com.yuk.miuidialog.utils.PrefsMap
import com.yuk.miuidialog.utils.PrefsUtils
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File

class XposedInit : IXposedHookLoadPackage, IXposedHookZygoteInit {

    var mPrefsMap: PrefsMap<String, Any> = PrefsMap<String, Any>()

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)

        if (mPrefsMap.size === 0) {
            var pref: XSharedPreferences? = null
            try {
                if (XposedBridge.getXposedVersion() >= 93) pref = XSharedPreferences(BuildConfig.APPLICATION_ID, PrefsUtils.mPrefsName) else pref = XSharedPreferences(File(PrefsUtils.mPrefsFile))
                pref!!.makeWorldReadable()
            } catch (t: Throwable) {
                XposedBridge.log(t)
            }
            val allPrefs = pref?.all
            if (allPrefs == null || allPrefs.size == 0) XposedBridge.log("[UID " + Process.myUid() + "] Cannot read module's SharedPreferences, some mods might not work!") else mPrefsMap.putAll(allPrefs)
        }
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

                val mDialogGravity: String? = mPrefsMap.getString("various_dialog_gravity", "0")
                val mDialogHorizontalMargin: Int = mPrefsMap.getInt("various_dialog_horizontal_margin", 0)
                val mDialogBottomMargin: Int = mPrefsMap.getInt("various_dialog_bottom_margin", 0)

                val context = AndroidAppHelper.currentApplication().applicationContext
                val dialogCls = XposedHelpers.findClass("miuix.internal.widget.DialogParentPanel", lpparam.classLoader)
                val f = findField("miuix.appcompat.app.AlertController", lpparam.classLoader) { type == dialogCls }
                val mParentPanel = f.get(it.thisObject) as View
                val layoutParams = mParentPanel.layoutParams as FrameLayout.LayoutParams

                if (mDialogGravity.equals("1")) {
                    layoutParams.gravity = Gravity.CENTER
                    if (mDialogHorizontalMargin != 0) {
                        layoutParams.marginStart = mDialogHorizontalMargin * 2
                        layoutParams.marginEnd = mDialogHorizontalMargin * 2
                        layoutParams.setMargins(mDialogHorizontalMargin * 2, 0, mDialogHorizontalMargin * 2, 0)
                        XposedBridge.log("mDialogGravity is " + mDialogGravity)
                    }
                } else if (mDialogGravity.equals("2")) {
                    layoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER
                    if (mDialogBottomMargin != 0) {
                        if (mDialogHorizontalMargin != 0) {
                            layoutParams.marginStart = mDialogHorizontalMargin * 2
                            layoutParams.marginEnd = mDialogHorizontalMargin * 2
                            layoutParams.setMargins(mDialogHorizontalMargin * 2, 0, mDialogHorizontalMargin * 2, mDialogBottomMargin * 2)
                            XposedBridge.log("mDialogHorizontalMargin is " + mDialogHorizontalMargin)
                        } else {
                            layoutParams.marginStart = layoutParams.marginStart
                            layoutParams.marginEnd = layoutParams.marginEnd
                            layoutParams.setMargins(layoutParams.marginStart, 0, layoutParams.marginEnd, mDialogBottomMargin * 2)
                            XposedBridge.log("mDialogHorizontalMargin is " + mDialogHorizontalMargin)
                        }
                    }
                    XposedBridge.log("mDialogGravity is " + mDialogGravity)
                }
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