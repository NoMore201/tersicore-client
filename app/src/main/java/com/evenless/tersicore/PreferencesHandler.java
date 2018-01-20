package com.evenless.tersicore;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.ArraySet;

import com.evenless.tersicore.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PreferencesHandler {
    private static final String PREF_SERVER_KEY = "TersicoreServer";
    private static final String PREF_TOKEN_KEY = "TersicoreToken";
    private static final String PREF_CACHE_SIZE_KEY = "CacheSize";
    private static final String PREF_OFFLINE = "Offline";
    private static final String PREF_USER = "User";
    public static boolean offline;

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static int getCacheSize(Context ctx) {
        return Integer.parseInt(getSharedPreferences(ctx).getString(PREF_CACHE_SIZE_KEY, "512"));
    }

    public static Set<String> getServer(Context ctx) {
        return getSharedPreferences(ctx).getStringSet(PREF_SERVER_KEY, new HashSet<String>());
    }

    public static boolean getOffline(Context ctx) {
        return getSharedPreferences(ctx).getBoolean(PREF_OFFLINE, false);
    }

    public static String getToken(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_TOKEN_KEY, "0651863bf5d902262b17c4621ec340544ff016752543d99a92d7d22872d8a455");
    }

    public static void setServer(Context ctx, String str){
        Set<String> users = getServer(ctx);
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        users.add(str);
        editor.putStringSet(PREF_SERVER_KEY, users);
        editor.apply();
    }

    public static void setToken(Context ctx, String str){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_TOKEN_KEY, str);
        editor.apply();
    }

    public static void setCacheSize(Context ctx, String cacheSize) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_CACHE_SIZE_KEY, cacheSize);
        editor.apply();
    }

    public static void setOffline(Context ctx, boolean is) {
        offline = is;
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(PREF_OFFLINE, is);
        editor.apply();
    }

    public static void setUser(Context ctx, String u){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER, u);
        editor.apply();
    }

    public static String getUsername(Context ctx){
        return getSharedPreferences(ctx).getString(PREF_USER, "guest");
    }
}
