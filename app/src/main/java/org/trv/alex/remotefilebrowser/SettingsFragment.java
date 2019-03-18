package org.trv.alex.remotefilebrowser;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PREF_URL = "pref_URL";

    private static final String HTTP_SCHEME = "http://";
    private static final String HTTP_SEPARATOR = "/";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        String value = getPreferenceScreen().getSharedPreferences().getString(PREF_URL, "");
        findPreference(PREF_URL).setSummary(value);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PREF_URL.equals(key)) {
            String value = sharedPreferences.getString(key, "");
            Preference preference = findPreference(key);
            boolean saveFixed = false;
            if (!value.matches("https?://.*$")) {
                value = HTTP_SCHEME + value;
                saveFixed = true;
            }
            if (!value.matches(".*/$")) {
                value = value + HTTP_SEPARATOR;
                saveFixed = true;
            }
            if (saveFixed) {
                sharedPreferences
                        .edit()
                        .putString(key, value)
                        .apply();
            }
            preference.setSummary(value);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
