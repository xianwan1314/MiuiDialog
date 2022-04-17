package com.yuk.miuidialog;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.yuk.miuidialog.utils.PrefsUtils;

public class MainActivity extends Activity {

    private MainFragment mainFrag = new MainFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mainFrag)
                .commit();
    }


    public static class MainFragment extends PreferenceFragment {

        private SharedPreferences mSharedPreferences;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_main);
            mSharedPreferences = PrefsUtils.mSharedPreferences;
            findPreference("prefs_key_various_dialog_gravity").setOnPreferenceChangeListener((preference, o) -> {
                findPreference("prefs_key_various_dialog_bottom_margin").setEnabled("2".equals(o));
                return true;
            });
        }
    }
}
