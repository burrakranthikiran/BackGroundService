package com.qtzone.backgroundservice;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.location.*;
import com.squareup.okhttp.*;

import org.json.JSONObject;

public class BackgroundService extends Service {

    private static final String CHANNEL_ID = "bg_service_channel";
    private FusedLocationProviderClient locationClient;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(1, notification());
        locationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("BG_SERVICE", "Service started");

        fetchLocationAndHitApi(); // 🔥 THIS replaces Ionic function

        return START_STICKY;
    }

    private void fetchLocationAndHitApi() {
        try {
            locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Log.e("BG_SERVICE", "Location null");
                        return;
                    }

                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    Log.i("BG_SERVICE", "Lat: " + lat + ", Lng: " + lng);

                    hitLocationApi(lat, lng);
                });

        } catch (SecurityException e) {
            Log.e("BG_SERVICE", "Location permission missing", e);
        }
    }

    private void hitLocationApi(double lat, double lng) {
        try {
            String customerId = getCustomerId();

            JSONObject json = new JSONObject();
            json.put("lat", lat);
            json.put("lng", lng);
            json.put("customerId", customerId);

            RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                .url("https://YOUR_API/locationNotification")
                .post(body)
                .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request req, IOException e) {
                    Log.e("BG_SERVICE", "API error", e);
                }

                @Override
                public void onResponse(Response response) {
                    Log.i("BG_SERVICE", "API success: " + response.code());
                }
            });

        } catch (Exception e) {
            Log.e("BG_SERVICE", "API exception", e);
        }
    }

    private String getCustomerId() {
        SharedPreferences prefs =
            getSharedPreferences("bg_service_prefs", MODE_PRIVATE);
        return prefs.getString("customer_id", null);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification notification() {
        return new Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText("Sending location in background")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Background Location",
                NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class)
                .createNotificationChannel(channel);
        }
    }
}
