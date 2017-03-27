package ru.jxt.usbtoreasyclient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class SheduledService {

    public void startAsync(final Context context, final long delay, final boolean firstCheck) {

        new AsyncTask<Void, Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                Network mNetwork = Network.getInstance();
                mNetwork.loadCookie(context);

                if(mNetwork.cookieExists()) {
                    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(context, StartServiceReceiver.class);
                    intent.setAction(MainActivity.START_UPDATESERVICE);
                    PendingIntent pi = PendingIntent.getBroadcast(context, MainActivity.repeatingRequestCode, intent, 0);
                    long triggerAtMills = SystemClock.elapsedRealtime();
                    am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            firstCheck ? triggerAtMills + 100 : triggerAtMills + delay,
                            delay, pi);
                }
                return null;
            }
        }.execute();

    }

    public void stopAsync(final Context context) {
        new AsyncTask<Void, Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(context, StartServiceReceiver.class);
                intent.setAction(MainActivity.START_UPDATESERVICE);
                PendingIntent pi = PendingIntent.getBroadcast(context, MainActivity.repeatingRequestCode, intent, 0);
                am.cancel(pi);
                pi.cancel();
                return null;
            }
        }.execute();
    }

}
