package ru.jxt.usbtoreasyclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

public class StartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context = context.getApplicationContext();
        String action = intent.getAction();

        if(action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            startSheduledNotification(context, false);
        }
        else if(action.equals(MainActivity.START_UPDATESERVICE)) {
            context.startService(new Intent(context.getApplicationContext(), NotificationService.class));
        }
        else if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            //это не работает на Android 7.0 и выше - используется JobSheduler
            //собственно если ready_for_update - true, то надо сразу проверить

            SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            if(isNetworkConnected(context) && mSharedPreferences.getBoolean("ready_for_update", false)) {
                mSharedPreferences.edit().putBoolean("ready_for_update", false).apply();
                startSheduledNotification(context, true);
            }
        }
    }

    boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    void startSheduledNotification(Context context, boolean flag) {
        int minuts = getMinutsForUpdate(context);
        if (minuts != 0)
            new SheduledService().startAsync(context, minuts * 60 * 1000, flag);
    }

    int getMinutsForUpdate(Context context) {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(mSharedPreferences.getBoolean("switch_preference_1", false)) {
            String minuts = mSharedPreferences.getString("list_preference_1", "30");
            return Integer.parseInt(minuts);
        }
        return 0;
    }

}
