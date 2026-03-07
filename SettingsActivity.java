package com.example.stockmonitorapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 显示设置片段
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // 加载设置资源
            addPreferencesFromResource(R.xml.preferences);

            // 获取所有偏好设置
            Preference notificationEnabled = findPreference("notification_enabled");
            Preference notificationFrequency = findPreference("notification_frequency");
            Preference notificationType = findPreference("notification_type");
            Preference darkMode = findPreference("dark_mode");

            // 设置偏好变化监听器
            if (notificationEnabled != null) {
                notificationEnabled.setOnPreferenceChangeListener(this);
            }
            if (notificationFrequency != null) {
                notificationFrequency.setOnPreferenceChangeListener(this);
            }
            if (notificationType != null) {
                notificationType.setOnPreferenceChangeListener(this);
            }
            if (darkMode != null) {
                darkMode.setOnPreferenceChangeListener(this);
            }

            // 更新偏好摘要
            updatePreferenceSummary(notificationFrequency);
            updatePreferenceSummary(notificationType);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            // 处理深色模式设置
            if (preference.getKey().equals("dark_mode")) {
                boolean darkModeEnabled = (Boolean) newValue;
                // 应用主题切换
                if (getActivity() != null) {
                    getActivity().recreate(); // 重新创建活动以应用新主题
                }
            }
            
            // 更新偏好摘要
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(newValue.toString());
                if (index >= 0) {
                    preference.setSummary(listPreference.getEntries()[index]);
                }
            }
            return true;
        }

        private void updatePreferenceSummary(Preference preference) {
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(listPreference.getValue());
                if (index >= 0) {
                    preference.setSummary(listPreference.getEntries()[index]);
                }
            }
        }
    }
}