package com.evenless.tersicore.activities;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.evenless.tersicore.interfaces.ApiRequestTaskListener;
import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.model.Track;
import com.google.gson.Gson;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;

import me.crosswall.lib.coverflow.core.Utils;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity
        implements ApiRequestTaskListener {

    private static MediaPlayerService mService;
    private boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }

    };

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };


    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MediaPlayerService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBound = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new DataSyncPreferenceFragment()).commit();
    }

    @Override
    public void onRequestComplete(String response, Exception e, String ss) {
        if(e!=null)
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        else {
            if(mBound)
                mService.reset();
            Track[] listTracks = new Gson().fromJson(response, Track[].class);
            DataBackend.insertTracks(new ArrayList<>(Arrays.asList(listTracks)), DataBackend.getServer(ss));
            Toast.makeText(this, "Rebuild Complete", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLatestRequestComplete(String response, Exception e) {
        // Do Nothing
    }

    @Override
    public void onPlaylistSingleRequestComplete(String result, Exception e) {
        // Never Called
    }

    @Override
    public void onPlaylistsRequestComplete(String result, Exception e) {
        // Never Called
    }

    @Override
    public void onSuggestionsRequestComplete(String result, Exception e) {
        // Never called
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            Preference button = findPreference("updateservers");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        DataBackend.removeAll();
                        for(String ss : PreferencesHandler.getServer(preference.getContext()))
                            TaskHandler.getTracks((ApiRequestTaskListener) preference.getContext(), ss);
                    } catch (MalformedURLException e) {
                        Toast.makeText(preference.getContext(), "There was an  error with a non valid server", Toast.LENGTH_LONG).show();
                    }
                    //code for what you want it to do
                    return true;
                }
            });

            Preference butt = findPreference("manageservers");
            butt.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(preference.getContext(), Servers.class);
                    startActivity(i);
                    //code for what you want it to do
                    return true;
                }
            });

            Preference bcache = findPreference("deleteCached");
            bcache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    deleteCache(preference.getContext());
                    Toast.makeText(preference.getContext(), "Cache Deleted", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            Preference dcache = findPreference("deletedownloaded");
            dcache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    TaskHandler.removeAllFiles();
                    DataBackend.removeOfflineTracks();
                    Toast.makeText(preference.getContext(), "All File Deleted", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            Preference cacheSize = findPreference("CacheSize");

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(cacheSize);
            bindPreferenceSummaryToValue(findPreference("PreferredQualityWiFi"));
            bindPreferenceSummaryToValue(findPreference("PreferredQualityData"));

            cacheSize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    PreferencesHandler.setCacheSize(preference.getContext(), (String) newValue);
                    bindPreferenceSummaryToValue(preference);
                    mService.changeProxyCacheSize(Integer.parseInt((String) newValue));
                    return false;
                }
            });
        }
    }

    public static void deleteCache(Context ctx) {
        try {
            File dir = ctx.getExternalCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
