package com.evenless.tersicore.interfaces;


import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackSuggestion;

import java.net.URL;
import java.util.ArrayList;

public interface SuggestionsTaskListener {
    void onSuggestionsCompleted(ArrayList<TrackSuggestion> result);
}
