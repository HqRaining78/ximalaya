package com.hq.ximalaya.data;

import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

public interface IHistoryDaoCallback {
    void onHistoryAdd(boolean isSuccess);
    void onHistoryDelete(boolean isSuccess);
    void onHistoryLoaded(List<Track> tracks);
    void onHistoryClear(boolean isSuccess);

}
