package com.addon.tsasaa.photogallerysearch;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;

public class PollService extends IntentService {
    private static final String TAG = "PollService";
    //private static final int POLL_INTERVAL = 1000 * 60; // 60 seconds
    private static final long POLL_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (!isNetworkAvailableAndConnected()) {
            return;
        }

        //Log.i(TAG, "Received an intent: " + intent);

        /////////////////////// Checking for new results //////////////////////
        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getLastResultId(this);
        List<GalleryItem> items;

        if (query == null) {
            items = new URLtoString().fetchRecentPhotos();
        } else {
            items = new URLtoString().searchPhotos(query);
        }

        if (items.size() == 0) {
            return;
        }

        String resultId = items.get(0).getId(); // new result ID here
        if (resultId.equals(lastResultId)) {
            Log.i(TAG, "Got an old result: " + resultId);
        } else {
            Log.i(TAG, "Got a new result: " + resultId);
            ////////////////////// Notification //////////////////////////////////

            Resources resources = getResources();
            Intent intent1 = PhotoGalleryActivity.newIntent(this);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_titleString))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_titleString))
                    .setContentText(resources.getString(R.string.new_pictures_textString))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
            NotificationManagerCompat notificationMC = NotificationManagerCompat.from(this);
            notificationMC.notify(0, notification);

            ////////////////////// Notification //////////////////////////////////
        }

        QueryPreferences.setLastResultId(this, resultId);

        ////////////////////// Checking for new results //////////////////////
    }

    ////////////////////////// INTENT /////////////////////////
    public static Intent newIntent(Context context) { // anyone wants to start this service should use this
        return new Intent(context, PollService.class);
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE); // Network is available or not

        boolean isNetworkAvailable = (cm.getActiveNetworkInfo() != null);
        boolean isNetworkingConnected = isNetworkAvailable &&
                cm.getActiveNetworkInfo().isConnected(); // current network is fully connected or not

        return isNetworkingConnected;
    }

    public static void setServiceAlarm(Context context, boolean isOn) { // adding alarm method executes our service with time interval
        Intent intent = PollService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        // context - with which you send to send the intent, request code (distinguish from others, intent to send, flags

        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) { // set alarm or cancel
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), POLL_INTERVAL, pendingIntent);
                    // time at which to start the alarm, the time interval to repeat the alarm, pending intent to fire when the alarm goes off
        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public static boolean isServiceAlarmOn(Context context) { // Whether the alarm is on or not
        Intent intent = PollService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }
}
