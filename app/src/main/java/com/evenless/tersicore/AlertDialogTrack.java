package com.evenless.tersicore;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.evenless.tersicore.activities.MainActivity;
import com.evenless.tersicore.model.Playlist;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackResources;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by McPhi on 30/12/2017.
 */

public class AlertDialogTrack {
    public static final String[] playOptions = {
            "Play now (Destroy queue)",
            "Play now (Maintain queue)",
            "Add To Current Playling list",
            "Play After",
            "Add To Playlist"
    };

    public static final String[] playOptionsPlus = {
            "Play now (Destroy queue)",
            "Play now (Maintain queue)",
            "Add To Current Playling list",
            "Play After"
    };

    public static void CreateDialogTrack (Context ct, final Track toP, MediaPlayerService srv) {

        final Context ctx = ct;
        final List<Track> toPlay = new ArrayList<>();
        final MediaPlayerService mService = srv;
        toPlay.add(toP);
        final Intent dd = new Intent(ctx, MainActivity.class);
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Play options")
                .setItems(playOptions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int which) {
                        // Add to playlist
                        if(which==4){
                            AlertDialog.Builder bd = new AlertDialog.Builder(ctx);
                            bd.setTitle("Playlists");
                            final List<Playlist> asd = DataBackend.getMyPlaylists(PreferencesHandler.getUsername(ctx));
                            CharSequence[] data = new CharSequence[asd.size()+1];
                            data[0]="NEW PLAYLIST";
                            int i = 1;
                            for(Playlist p : asd) {
                                data[i] = p.name;
                                i++;
                            }
                            bd.setItems(data, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int wh) {
                                    if(wh==0){
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                                        builder.setTitle("Name the New Playlist");
                                        final EditText input = new EditText(ctx);
                                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                                        builder.setView(input);
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Playlist p = DataBackend.createNewPlaylist(input.getText().toString(), toPlay, PreferencesHandler.getUsername(ctx));
                                                updatePlaylistOnServer(ctx, p);
                                            }
                                        });
                                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });

                                        builder.show();
                                    }
                                    else
                                        updatePlaylistOnServer(ctx, DataBackend.addToPlaylist(asd.get(wh-1), toPlay));
                                }});
                            bd.create().show();
                        } else {
                            Log.e("Dialog", PreferencesHandler.getPreferredQuality(ctx) + "");
                            boolean askResource = PreferencesHandler.getPreferredQuality(ctx) == 6;
                            if(!askResource)
                                playSong(which, mService, toPlay, ctx);
                            else{
                                AlertDialog.Builder bd = new AlertDialog.Builder(ctx);
                                bd.setTitle("Resources");
                                CharSequence[] data = new CharSequence[toPlay.get(0).resources.size()];
                                int i=0;
                                for(TrackResources p : toPlay.get(0).resources) {
                                    String toShow = "";
                                    if(p.isDownloaded)
                                        toShow += "Offline\n";
                                    else
                                        toShow += p.server + "\n";
                                    data[i]=toShow + p.codec + " " + p.sample_rate / 1000 +
                                            "Khz " + p.bitrate / 1000 + "kbps";
                                    i++;
                                }
                                bd.setItems(data, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int w) {
                                        Map<String, Integer> resfav = new HashMap<>();
                                        resfav.put(toPlay.get(0).uuid, w);
                                        playSong(which, mService, toPlay, ctx, resfav);
                                    }
                                });
                                bd.show();
                            }
                        }
                    }
                });
        builder.create().show();
    }

    private static void playSong(int which, MediaPlayerService mService, List<Track> toPlay, Context ctx){
        Intent dd = new Intent(ctx, MainActivity.class);
        // TODO: use enum rather than integers
        switch (which) {
            // play now clean queue
            case 0:
                mService.updatePlaylist(toPlay, 0, false);
                ctx.startActivity(dd);
                break;
            // play now keep queue
            case 1:
                mService.seekToTrack(mService.append(toPlay));
                ctx.startActivity(dd);
                break;
            // add to current playlist
            case 2:
                mService.append(toPlay);
                ctx.startActivity(dd);
                break;
            // play after
            case 3:
                mService.appendAfterCurrent(toPlay);
                ctx.startActivity(dd);
                break;
            default:
                break;
        }
    }

    private static void playSong(int which, MediaPlayerService mService, List<Track> toPlay, Context ctx, Map<String, Integer> pref){
        Intent dd = new Intent(ctx, MainActivity.class);
        // TODO: use enum rather than integers
        switch (which) {
            // play now clean queue
            case 0:
                mService.updatePlaylist(toPlay, 0, false, pref);
                ctx.startActivity(dd);
                break;
            // play now keep queue
            case 1:
                mService.seekToTrack(mService.append(toPlay, pref));
                ctx.startActivity(dd);
                break;
            // add to current playlist
            case 2:
                mService.append(toPlay, pref);
                ctx.startActivity(dd);
                break;
            // play after
            case 3:
                mService.appendAfterCurrent(toPlay, pref);
                ctx.startActivity(dd);
                break;
            default:
                break;
        }
    }

    public static void updatePlaylistOnServer(Context ctx, Playlist p) {
        try {
            for(String ss : PreferencesHandler.getServer(ctx))
                TaskHandler.setPlaylist(ss, null, p);
        } catch (MalformedURLException e) {
            Toast.makeText(ctx, "There can be errors in synchronizing with server", Toast.LENGTH_LONG);
        }
    }
}
