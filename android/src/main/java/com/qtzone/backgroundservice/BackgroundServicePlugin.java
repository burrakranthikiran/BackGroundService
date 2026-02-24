package com.qtzone.backgroundservice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import com.getcapacitor.*;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "BackgroundService")
public class BackgroundServicePlugin extends Plugin {

    @PluginMethod
    public void echo(PluginCall call) {
        String customerId = call.getString("value");

        if (customerId == null || customerId.isEmpty()) {
            call.reject("customerId is required");
            return;
        }

        // 🔐 Store permanently
        SharedPreferences prefs = getContext()
                .getSharedPreferences("bg_service_prefs", Context.MODE_PRIVATE);

        prefs.edit()
                .putString("customer_id", customerId)
                .apply();

        Log.i("BG_PLUGIN", "Saved customerId: " + customerId);

        JSObject ret = new JSObject();
        ret.put("value", customerId);
        call.resolve(ret);
    }

    @PluginMethod
    public void start(PluginCall call) {
        Log.i("BG_PLUGIN", "Working");
        Intent intent = new Intent(getContext(), BackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().startForegroundService(intent);
        }
        call.resolve();
    }

    @PluginMethod
    public void stop(PluginCall call) {
        Intent intent = new Intent(getContext(), BackgroundService.class);
        getContext().stopService(intent);
        call.resolve();
    }

    @PluginMethod
    public void checkLocationPermission(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (getContext().checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                call.resolve("granted");
            } else {    
                call.resolve("denied");
            }
        } else {
            call.resolve("granted");
        }
    }

    @PluginMethod
    public void requestLocationPermission(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (getContext().checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                call.resolve("granted");
            } else {
                getContext().requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION});
                call.resolve("pending");
            }
        } else {
            call.resolve("granted");
        }
    }
}
