package com.evenless.tersicore.tasks;

import com.evenless.tersicore.interfaces.ApiRequestExtraTaskListener;
import com.evenless.tersicore.interfaces.ApiRequestTaskListener;
import com.evenless.tersicore.TaskHandler;

import java.net.URL;

public class ApiGetTask extends GenericGetTask {
    private ApiRequestTaskListener mListener;
    private ApiRequestExtraTaskListener mListenertwo;
    private int mState;

    public ApiGetTask(ApiRequestTaskListener listener,
                      URL url, String token, int state) {
        super(url, token);
        mListener = listener;
        mState=state;
    }

    public ApiGetTask(ApiRequestExtraTaskListener listener,
                      URL url, String token, int state) {
        super(url, token);
        mListenertwo = listener;
        mState=state;
    }

    @Override
    protected void notifyResult(String result, Exception e) {
        super.notifyResult(result, e);
        switch (mState){
            case TaskHandler.ALL_TRACKS: mListener.onRequestComplete(result, e, mToken); break;
            case TaskHandler.TRACKS_LATEST: mListener.onLatestRequestComplete(result,e); break;
            case TaskHandler.PLAYLIST_SINGLE: mListener.onPlaylistSingleRequestComplete(result,e); break;
            case TaskHandler.PLAYLISTS: mListener.onPlaylistsRequestComplete(result,e); break;
            case TaskHandler.SUGGESTIONS: mListener.onSuggestionsRequestComplete(result,e); break;
            case TaskHandler.USERS: mListenertwo.onUsersRequestComplete(result,e); break;
            case TaskHandler.MESSAGES: mListenertwo.onMessagesRequestComplete(result,e); break;
            default: break;
        }
    }
}
