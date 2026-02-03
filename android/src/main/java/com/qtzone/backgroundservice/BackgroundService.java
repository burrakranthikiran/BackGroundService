package com.qtzone.backgroundservice;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class BackgroundService extends Service {

    private static final String CHANNEL_ID = "bg_service_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("BG_SERVICE", "Service created");
        createNotificationChannel();
        startForeground(1, getNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("BG_SERVICE", "Service running in background");

        // 🔁 Your background logic here
        // Example: start location, timer, API call, socket, etc.

        return START_STICKY; // 👈 survives app kill
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("BG_SERVICE", "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification getNotification() {
        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("App running")
                .setContentText("Background service active")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Background Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
