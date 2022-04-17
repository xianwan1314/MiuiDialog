package com.yuk.miuidialog.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import com.yuk.miuidialog.BuildConfig;

import java.lang.reflect.Array;

public class PrefsUtils {

    public static SharedPreferences mSharedPreferences = null;

    public static String mPrefsName = BuildConfig.APPLICATION_ID + "_preferences";
    public static String mPrefsPath = "/data/user_de/0/" + BuildConfig.APPLICATION_ID + "/shared_prefs";
    public static String mPrefsFile = mPrefsPath + "/" + mPrefsName + ".xml";

    public static SharedPreferences getSharedPrefs(Context context, boolean protectedStorage) {
        return getSharedPrefs(context, protectedStorage, false);
    }
    public static SharedPreferences getSharedPrefs(Context context, boolean protectedStorage, boolean multiProcess) {
        if (protectedStorage) context = getProtectedContext(context);
        try {
            return context.getSharedPreferences(mPrefsName, multiProcess ? Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE : Context.MODE_WORLD_READABLE);
        } catch (Throwable t) {
            return context.getSharedPreferences(mPrefsName, multiProcess ? Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE : Context.MODE_PRIVATE);
        }
    }


    public static synchronized Context getProtectedContext(Context context) {
        return getProtectedContext(context, null);
    }

    public static synchronized Context getProtectedContext(Context context, Configuration config) {
        try {
            Context mContext = context.isDeviceProtectedStorage() ? context : context.createDeviceProtectedStorageContext();
            return config == null ? mContext : mContext.createConfigurationContext(config);
        } catch (Throwable t) {
            return context;
        }
    }
}
