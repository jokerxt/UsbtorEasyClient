package ru.jxt.usbtoreasyclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class NotificationService extends Service {

    private int notificationId = 305;
    public final static int NETWORK_JOB_ID = 355;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Context context = getApplicationContext();

        if(isNetworkConnected())
            new LoadAndNotificationNewPostsAndMessagesCounts().execute();
        else {
            new SheduledService().stopAsync(context);
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.M) { //для 7.0 и выше юзаем job
                JobScheduler js = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                JobInfo job = new JobInfo.Builder(
                        NETWORK_JOB_ID,
                        new ComponentName(context, WhenNetworkOnJobService.class))
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .build();
                js.schedule(job);
            }
            else {
                SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                mSharedPreferences.edit().putBoolean("ready_for_update", true).apply();
            }
        }

        return Service.START_NOT_STICKY;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public class LoadAndNotificationNewPostsAndMessagesCounts extends AsyncTask<Void, Void, String[]> {

        private Network mNetwork;
        private String newMessagesCount = "";
        private String newPostsCount = "";

        @Override
        protected void onPreExecute() {
            mNetwork = Network.getInstance();
        }

        @Override
        protected String[] doInBackground(Void... params) {

            //загружаем cookies, если есть
            mNetwork.loadCookie(getApplicationContext());

            //проверяем есть ли cookie и можно ли по ним загрузить валидную страницу
            Document doc;
            if(mNetwork.cookieExists()
                    && (doc = mNetwork.getValidCookieDocument(MainActivity.usbtorNewPostsPage)) != null) {
                Elements newMessages = doc.select("strong.itemCount");
                Elements newPosts = doc.select("h1.ipsType_pagetitle.left");

                if(newMessages != null && !newMessages.isEmpty()) {
                    newMessagesCount = newMessages.select("span.Total").text();
                    newMessagesCount = newMessagesCount.split("\\s+")[0];
                }

                if(newPosts != null && !newPosts.isEmpty()) {
                    newPostsCount = newPosts.text();
                    newPostsCount = newPostsCount.split("\\s+")[2];
                }
            }

            if(newMessagesCount.isEmpty() && newPostsCount.isEmpty())
                return null;

            return new String[] {newMessagesCount, newPostsCount};
        }

        @Override
        protected void onPostExecute(String[] aString) {
            if(aString != null) {
                String newMessagesCount = aString[0];
                String newPostsCount = aString[1];

                String messages =  newMessagesCount.isEmpty() ? "" : "Новые сообщения: " + newMessagesCount;
                String posts = newPostsCount.isEmpty() ? "" : "Новые посты: " + newPostsCount;

                String message = messages.isEmpty() ? posts
                        : (posts.isEmpty() ? messages
                            : messages + "\r\n" + posts);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                SharedPreferences mSharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean vibrate = mSharedPreferences.getBoolean("switch_preference_2", true);
                boolean sound = mSharedPreferences.getBoolean("switch_preference_3", true);

                int defaults =
                        (sound ? Notification.DEFAULT_SOUND : 0) |
                        (vibrate ? Notification.DEFAULT_VIBRATE : 0);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getApplicationContext())
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent)
                                .setSmallIcon(R.drawable.ic_menu_share)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                                .setContentText(message)
                                .setDefaults(defaults)
                                .setContentTitle("Usbtor ждет!");

                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                mNotificationManager.notify(notificationId, mBuilder.build());
            }
        }
    }
}
