package com.hq.ximalaya.data;

import com.ximalaya.ting.android.opensdk.model.track.Track;

public interface IHistoryDao {
    void setCallback(IHistoryDaoCallback callback);

    void addHistory(Track track);

    void deleteHistory(Track track);

    void clearHistory();

    void listHistories();
}
