package com.isgm.camreport.utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utilities {
    public static void saveToPreference(Activity activity, String key, String value) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static void saveIntToPreference(Activity activity, String key, int value) {
        SharedPreferences sharedpreferences =
                PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        sharedpreferences.edit().putInt(key, value).apply();
    }

    public static String getFromPreference(Activity activity, String value) {
        SharedPreferences sharedpreferences =
                PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        return sharedpreferences.getString(value, "");
    }


    public static int getIntFromPreference(Activity activity, String key) {
        SharedPreferences sharedpreferences =
                PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        return sharedpreferences.getInt(key, 0);
    }

    public static boolean getAllPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (activity.getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    activity.getApplicationContext().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) ==
                            PackageManager.PERMISSION_GRANTED &&
                    activity.getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED &&
                    activity.getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED)
                return true;
            else {
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
                return false;
            }
        }
        return true;
    }

    public static String nowTimeFormat1() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
    }

    public static String nowTimeFormat2() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
    }

    public static String nowTimeFormat3() {
        return new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new Date());
    }

    public static String nowTimeFormat4() {
        return new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
    }

    public static boolean haveNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network[] networks = connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network network : networks) {
                networkInfo = connectivityManager.getNetworkInfo(network);
                if (networkInfo != null) {
                    if (networkInfo.getTypeName().equalsIgnoreCase("WIFI"))
                        if (networkInfo.isConnected()) haveConnectedWifi = true;
                    if (networkInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                        if (networkInfo.isConnected()) haveConnectedMobile = true;
                }
            }
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
