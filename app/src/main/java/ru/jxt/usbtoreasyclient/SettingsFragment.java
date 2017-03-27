package ru.jxt.usbtoreasyclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;


public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

        ListPreference list_preference_1 = (ListPreference) findPreference("list_preference_1");
        list_preference_1.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String nv = (String) newValue;

                ListPreference mListPreference = (ListPreference) preference;
                mListPreference.setSummary(mListPreference.getEntries()[mListPreference.findIndexOfValue(nv)]);

                //if(isLogin()) { //это излишняя проверка, тк пункта Настройка в окне Логина нет
                    SharedPreferences mSharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(getActivity());
                    if(mSharedPreferences.getBoolean("switch_preference_1", false)) {
                        int min = Integer.parseInt((String) newValue);
                        Context context = getActivity().getApplicationContext();
                        SheduledService mSheduledService = new SheduledService();
                        mSheduledService.stopAsync(context);
                        mSheduledService.startAsync(context, min * 60 * 1000, false);
                    }
                //}
                return true;
            }
        });

        SwitchPreference switch_preference_1 = (SwitchPreference) findPreference("switch_preference_1");
        switch_preference_1.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                //if(isLogin()) {
                    boolean state = (boolean) newValue;
                    Context context = getActivity().getApplicationContext();

                    if (state) {
                        SharedPreferences mSharedPreferences =
                                PreferenceManager.getDefaultSharedPreferences(context);
                        String minuts = mSharedPreferences.getString("list_preference_1", "30");
                        new SheduledService().startAsync(context, Integer.parseInt(minuts) * 60 * 1000, false);
                    } else {
                        new SheduledService().stopAsync(context);
                    }
                //}

                return true;
            }
        });

    }

//    boolean isLogin() {
//        SharedPreferences mSharedPreferences =
//                PreferenceManager.getDefaultSharedPreferences(getActivity());
//        return mSharedPreferences.getBoolean("login", false);
//    }

}
