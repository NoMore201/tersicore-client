package com.evenless.tersicore;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.ArraySet;
import android.util.Log;

import com.evenless.tersicore.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PreferencesHandler {
    private static final String PREF_CACHE_SIZE_KEY = "CacheSize";
    private static final String PREF_QUALITY_WIFI_KEY = "PreferredQualityWiFi";
    private static final String PREF_QUALITY_DATA_KEY = "PreferredQualityData";
    private static final String PREF_PRIOR_OFFLINE = "PreferOffline";
    private static final String PREF_DATA_PROTECTION = "DataProtection";
    private static final String PREF_OFFLINE = "Offline";
    private static final String PREF_USER = "User";
    private static final String PREF_LAST_UPDATE = "Lup";
    public static boolean offline;

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static int getCacheSize(Context ctx) {
        return Integer.parseInt(getSharedPreferences(ctx).getString(PREF_CACHE_SIZE_KEY, "512"));
    }

    public static List<String> getServer(Context ctx) {
        return DataBackend.getServers();
    }

    public static boolean getOffline(Context ctx) {
        return getSharedPreferences(ctx).getBoolean(PREF_OFFLINE, false);
    }


    public static void setCacheSize(Context ctx, String cacheSize) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_CACHE_SIZE_KEY, cacheSize);
        editor.apply();
        editor.commit();
    }

    public static void setOffline(Context ctx, boolean is) {
        offline = is;
        for(String ss : PreferencesHandler.getServer(ctx))
            try {
                TaskHandler.setUser(ss, null,
                        new User(PreferencesHandler.getUsername(ctx), !is));
            } catch (Exception e){
                e.printStackTrace();
            }
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(PREF_OFFLINE, is);
        editor.apply();
        editor.commit();
    }

    public static void setUser(Context ctx, String u){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER, u);
        editor.apply();
        editor.commit();
    }

    public static String getUsername(Context ctx){
        return getSharedPreferences(ctx).getString(PREF_USER, "guest");
    }

    public static void deleteServer(Context ctx, String s) {
        DataBackend.deleteToken(s);
    }

    public static int getPreferredQuality(Context ctx) {
        ConnectivityManager connManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected()) {
                return Integer.parseInt(getSharedPreferences(ctx).getString(PREF_QUALITY_WIFI_KEY, "1"));
            } else
                return Integer.parseInt(getSharedPreferences(ctx).getString(PREF_QUALITY_DATA_KEY, "1"));
        } catch (Exception e){
            return Integer.parseInt(getSharedPreferences(ctx).getString(PREF_QUALITY_DATA_KEY, "1"));
        }
    }

    public static boolean getPreferOffline(Context ctx) {
        return getSharedPreferences(ctx).getBoolean(PREF_PRIOR_OFFLINE, true);
    }

    public static boolean getDataProtection(Context ctx) {
        return getSharedPreferences(ctx).getBoolean(PREF_DATA_PROTECTION, true);
    }

    public static long getLastUpdate(Context ctx){
        return getSharedPreferences(ctx).getLong(PREF_LAST_UPDATE, new Date().getTime()/1000);
    }

    public static void setLastUpdate(Context ctx){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putLong(PREF_LAST_UPDATE, (new Date().getTime()/1000));
        editor.apply();
        editor.commit();
    }
}
