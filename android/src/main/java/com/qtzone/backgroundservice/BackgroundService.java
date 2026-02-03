package com.qtzone.backgroundservice;

import android.app.*;
import android.content.*;
import android.location.Location;
import android.os.*;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.*;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class BackgroundService extends Service {

    private static final String TAG = "BG_SERVICE";
    private static final String CHANNEL_ID = "bg_service_channel";

    // ⏱ 5 minutes
    private static final long INTERVAL = 5 * 60 * 1000;

    // 📍 minimum movement to trigger API (meters)
    private static final float MIN_DISTANCE_METERS = 30f;

    private FusedLocationProviderClient locationClient;
    private Handler handler;
    private Runnable task;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        startForeground(1, buildNotification());

        locationClient = LocationServices.getFusedLocationProviderClient(this);
        handler = new Handler(Looper.getMainLooper());

        Log.i(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTimer();
        return START_STICKY;
    }

    /**
     * Start 5-minute timer
     */
    private void startTimer() {
        if (task != null) return;

        task = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Timer tick (5 min)");
                checkLocationAndSend();
                handler.postDelayed(this, INTERVAL);
            }
        };

        handler.post(task); // start immediately
    }

    /**
     * Get location and compare
     */
    private void checkLocationAndSend() {
        try {
            locationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location == null) {
                            Log.e(TAG, "Location null");
                            return;
                        }

                        double lat = location.getLatitude();
                        double lng = location.getLongitude();

                        if (shouldSendLocation(lat, lng)) {
                            Log.i(TAG, "Location changed → API call");
                            saveLastLocation(lat, lng);
                            hitLocationApi(lat, lng);
                        } else {
                            Log.i(TAG, "Location unchanged → skip");
                        }
                    });

        } catch (SecurityException e) {
            Log.e(TAG, "Location permission missing", e);
        }
    }

    /**
     * Compare with last sent location
     */
    private boolean shouldSendLocation(double lat, double lng) {
        SharedPreferences prefs =
                getSharedPreferences("bg_service_prefs", MODE_PRIVATE);

        if (!prefs.contains("last_lat")) return true;

        float lastLat = prefs.getFloat("last_lat", 0);
        float lastLng = prefs.getFloat("last_lng", 0);

        float[] result = new float[1];
        Location.distanceBetween(
                lastLat, lastLng,
                lat, lng,
                result
        );

        return result[0] >= MIN_DISTANCE_METERS;
    }

    /**
     * Save last sent location
     */
    private void saveLastLocation(double lat, double lng) {
        SharedPreferences prefs =
                getSharedPreferences("bg_service_prefs", MODE_PRIVATE);

        prefs.edit()
                .putFloat("last_lat", (float) lat)
                .putFloat("last_lng", (float) lng)
                .apply();
    }

    /**
     * API call
     */
    private void hitLocationApi(double lat, double lng) {
        try {
            String customerId = getCustomerId();
            if (customerId == null) {
                Log.e(TAG, "Customer ID missing");
                return;
            }

            JSONObject json = new JSONObject();
            json.put("lat", lat);
            json.put("lng", lng);
            json.put("customerId", customerId);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url("https://snatchit-api.qztbox.com/customer/notification/locationNotification")
                    .post(body)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    Log.i(TAG, "API success: " + response.code());
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "API error", e);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "API exception", e);
        }
    }

    private String getCustomerId() {
        SharedPreferences prefs =
                getSharedPreferences("bg_service_prefs", MODE_PRIVATE);
        return prefs.getString("customer_id", null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && task != null) {
            handler.removeCallbacks(task);
        }
        Log.i(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification buildNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Location Service")
                    .setContentText("Checking location every 5 minutes")
                    .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                    .build();
        }
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Background Location Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class)
                    .createNotificationChannel(channel);
        }
    }
}
