package com.evenless.tersicore.interfaces;

public interface ApiRequestTaskListener {
    void onRequestComplete(String response, Exception e, String token);
    void onLatestRequestComplete(String response, Exception e);
    void onPlaylistSingleRequestComplete(String result, Exception e);
    void onPlaylistsRequestComplete(String result, Exception e);
    void onSuggestionsRequestComplete(String result, Exception e);
}
