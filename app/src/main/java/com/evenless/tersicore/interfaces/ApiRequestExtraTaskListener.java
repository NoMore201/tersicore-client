package com.evenless.tersicore.interfaces;

public interface ApiRequestExtraTaskListener extends ApiRequestTaskListener {
    void onMessagesRequestComplete(String response, Exception e);
    void onUsersRequestComplete(String response, Exception e);
}
