package com.evenless.tersicore.tasks;

import com.evenless.tersicore.interfaces.ApiRequestTaskListener;
import com.evenless.tersicore.TaskHandler;

import java.net.URL;

public class ApiGetTask extends GenericGetTask {
    private ApiRequestTaskListener mListener;
    private int mState;

    public ApiGetTask(ApiRequestTaskListener listener,
                      URL url, String token, int state) {
        super(url, token);
        mListener = listener;
        mState=state;
    }

    @Override
    protected void notifyResult(String result, Exception e) {
        super.notifyResult(result, e);
        switch (mState){
            case TaskHandler.ALL_TRACKS: mListener.onRequestComplete(result, e); break;
            case TaskHandler.TRACKS_LATEST: mListener.onLatestRequestComplete(result,e); break;
            case TaskHandler.PLAYLIST_SINGLE: mListener.onPlaylistSingleRequestComplete(result,e); break;
            case TaskHandler.PLAYLISTS: mListener.onPlaylistsRequestComplete(result,e); break;
            case TaskHandler.SUGGESTIONS: mListener.onSuggestionsRequestComplete(result,e); break;
            default: break;
        }
    }
}
