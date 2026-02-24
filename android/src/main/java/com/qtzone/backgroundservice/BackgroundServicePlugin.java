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
}
