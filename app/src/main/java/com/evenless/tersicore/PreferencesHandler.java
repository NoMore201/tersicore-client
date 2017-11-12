package com.evenless.tersicore;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesHandler {
    private static final String PREF_SERVER_KEY = "TersicoreServer";
    private static final String PREF_TOKEN_KEY = "TersicoreToken";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static String getServer(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_SERVER_KEY, null);
    }

    public static String getToken(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_TOKEN_KEY, null);
    }

    public static void setServer(Context ctx, String str){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(str, PREF_SERVER_KEY);
        editor.commit();
    }
}
