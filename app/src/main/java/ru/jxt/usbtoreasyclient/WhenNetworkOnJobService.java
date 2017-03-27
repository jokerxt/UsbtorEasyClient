package ru.jxt.usbtoreasyclient;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

@TargetApi(23)
public class WhenNetworkOnJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        Context context = getApplicationContext();

        if(isNetworkConnected(context)) {
            int minuts = getMinutsForUpdate(context);
            if (minuts != 0)
                new SheduledService().startAsync(context, minuts * 60 * 1000, true);

            JobScheduler js = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            js.cancel(NotificationService.NETWORK_JOB_ID);
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    int getMinutsForUpdate(Context context) {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(mSharedPreferences.getBoolean("switch_preference_1", false)) {
            String minuts = mSharedPreferences.getString("list_preference_1", "30");
            return Integer.parseInt(minuts);
        }
        return 0;
    }

    boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

}
