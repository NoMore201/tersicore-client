package com.evenless.tersicore;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesHandler {
    private static final String PREF_SERVER_KEY = "TersicoreServer";
    private static final String PREF_TOKEN_KEY = "TersicoreToken";
    private static final String PREF_CACHE_SIZE_KEY = "CacheSize";

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static int getCacheSize(Context ctx) {
        return Integer.parseInt(getSharedPreferences(ctx).getString(PREF_CACHE_SIZE_KEY, "512"));
    }

    public static String getServer(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_SERVER_KEY, null);
    }

    public static String getToken(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_TOKEN_KEY, null);
    }

    public static void setServer(Context ctx, String str){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_SERVER_KEY, str);
        editor.apply();
    }

    public static void setCacheSize(Context ctx, String cacheSize) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_CACHE_SIZE_KEY, cacheSize);
        editor.apply();
    }
}
