package com.qtzone.backgroundservice;

import android.content.Intent;
import android.util.Log;
import com.getcapacitor.*;

@CapacitorPlugin(name = "BackgroundService")
public class BackgroundServicePlugin extends Plugin {

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");
        Log.i("BG_PLUGIN", value);

        JSObject ret = new JSObject();
        ret.put("value", value);
        call.resolve(ret);
    }

    @PluginMethod
    public void start(PluginCall call) {
        Intent intent = new Intent(getContext(), BackgroundService.class);
        getContext().startForegroundService(intent);
        call.resolve();
    }

    @PluginMethod
    public void stop(PluginCall call) {
        Intent intent = new Intent(getContext(), BackgroundService.class);
        getContext().stopService(intent);
        call.resolve();
    }
}
