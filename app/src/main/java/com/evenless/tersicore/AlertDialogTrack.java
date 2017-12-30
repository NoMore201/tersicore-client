package com.evenless.tersicore;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import com.evenless.tersicore.activities.MainActivity;
import com.evenless.tersicore.model.Playlist;
import com.evenless.tersicore.model.Track;

import java.util.ArrayList;
import java.util.List;

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

    public static void CreateDialogTrack (Context ct, Track toP, MediaPlayerService srv) {

        final Context ctx = ct;
        final List<Track> toPlay = new ArrayList<>();
        final MediaPlayerService mService = srv;
        toPlay.add(toP);
        final Intent dd = new Intent(ctx, MainActivity.class);
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Play options")
                .setItems(playOptions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent dd = new Intent(ctx, MainActivity.class);
                        // TODO: use enum rather than integers
                        switch (which){
                            // play now clean queue
                            case 0: mService.updatePlaylist(toPlay, 0, false);  ctx.startActivity(dd); break;
                            // play now keep queue
                            case 1: mService.seekToTrack(mService.append(toPlay)); ctx.startActivity(dd); break;
                            // add to current playlist
                            case 2: mService.append(toPlay); ctx.startActivity(dd); break;
                            // play after
                            case 3: mService.appendAfterCurrent(toPlay); ctx.startActivity(dd); break;
                            // Add to Playlist
                            case 4: AlertDialog.Builder bd = new AlertDialog.Builder(ctx);
                                bd.setTitle("Playlists");
                                final List<Playlist> asd = DataBackend.getPlaylists();
                                for(Playlist p : asd)
                                    if(!p.uploader.equalsIgnoreCase("me"))
                                        asd.remove(p);

                                CharSequence[] data = new CharSequence[asd.size()+1];
                                data[0]="NEW PLAYLIST";
                                int i = 1;
                                for(Playlist p : asd) {
                                    data[i] = p.name;
                                    i++;
                                }
                                bd.setItems(data, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which==0){
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                                            builder.setTitle("Name the New Playlist");
                                            final EditText input = new EditText(ctx);
                                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                                            builder.setView(input);
                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    DataBackend.createNewPlaylist(input.getText().toString(), toPlay);
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
                                            DataBackend.addToPlaylist(asd.get(which-1), toPlay);
                                    }});
                                bd.create().show();
                            default: break;
                        }
                    }
                });
        builder.create().show();
    }
}
