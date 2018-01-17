package com.evenless.tersicore;

public interface ApiRequestTaskListener {
    void onRequestComplete(String response, Exception e);
    void onLatestRequestComplete(String response, Exception e);
    void onPlaylistSingleRequestComplete(String result, Exception e);
    void onPlaylistsRequestComplete(String result, Exception e);
    void onSuggestionsRequestComplete(String result, Exception e);
}
