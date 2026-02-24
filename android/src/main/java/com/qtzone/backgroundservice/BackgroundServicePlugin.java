package com.qtzone.backgroundservice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.getcapacitor.*;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.LocationServices;
import androidx.activity.result.ActivityResult;
import com.getcapacitor.annotation.ActivityCallback;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.location.LocationManager;
import netscape.javascript.JSObject;
import android.app.Activity;

@CapacitorPlugin(name = "BackgroundService", permissions = {
        @Permission(alias = "location", strings = { Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION }),
        @Permission(alias = "backgroundLocation", strings = { Manifest.permission.ACCESS_BACKGROUND_LOCATION })
})
public class BackgroundServicePlugin extends Plugin {
    private BroadcastReceiver gpsReceiver;

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
        if (getPermissionState("location") != PermissionState.GRANTED) {
            requestPermissionForAlias("location", call, "internalLocationCallback");
        } else {
            handleBackgroundAndSettings(call);
        }
    }

    @PermissionCallback
    private void internalLocationCallback(PluginCall call) {
        if (getPermissionState("location") == PermissionState.GRANTED) {
            handleBackgroundAndSettings(call);
        } else {
            JSObject ret = new JSObject();
            ret.put("value", "denied");
            call.resolve(ret);
        }
    }

    private void handleBackgroundAndSettings(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && getPermissionState("backgroundLocation") != PermissionState.GRANTED) {
            requestPermissionForAlias("backgroundLocation", call, "internalBackgroundCallback");
        } else {
            checkAndEnableSettings(call);
        }
    }

    @PermissionCallback
    private void internalBackgroundCallback(PluginCall call) {
        if (getPermissionState("backgroundLocation") == PermissionState.GRANTED) {
            checkAndEnableSettings(call);
        } else {
            JSObject ret = new JSObject();
            ret.put("value", "denied");
            call.resolve(ret);
        }
    }

    private void checkAndEnableSettings(PluginCall call) {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(getContext());
        client.checkLocationSettings(builder.build())
                .addOnSuccessListener(getActivity(), locationSettingsResponse -> {
                    JSObject ret = new JSObject();
                    ret.put("value", "granted");
                    call.resolve(ret);
                })
                .addOnFailureListener(getActivity(), e -> {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        int statusCode = apiException.getStatusCode();
                        if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) apiException;
                                resolvable.startResolutionForResult(getActivity(), 12345);
                                saveCall(call);
                            } catch (Exception sendEx) {
                                call.reject("Error showing settings dialog");
                            }
                        } else {
                            JSObject ret = new JSObject();
                            ret.put("value", "denied");
                            call.resolve(ret);
                        }
                    } else {
                        call.reject("Unknown error checking settings");
                    }
                });
    }

    @Override
    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        super.handleOnActivityResult(requestCode, resultCode, data);
        PluginCall savedCall = getSavedCall();
        if (savedCall == null)
            return;

        if (requestCode == 12345) {
            JSObject ret = new JSObject();
            if (resultCode == Activity.RESULT_OK) {
                ret.put("value", "granted");
            } else {
                ret.put("value", "denied");
            }
            savedCall.resolve(ret);
            freeSavedCall();
        }
    }

    @Override
    public void load() {
        super.load();

        gpsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {

                    LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

                    boolean gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

                    JSObject data = new JSObject();
                    data.put("gpsEnabled", gpsEnabled);

                    notifyListeners("gpsStatusChange", data);
                }
            }
        };

        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);

        getContext().registerReceiver(gpsReceiver, filter);
    }

    @Override
    protected void handleOnDestroy() {
        super.handleOnDestroy();
        if (gpsReceiver != null) {
            getContext().unregisterReceiver(gpsReceiver);
        }
    }

}
