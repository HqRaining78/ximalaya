package com.hq.ximalaya.interfaces;

import com.hq.ximalaya.base.BasePresenter;
import com.ximalaya.ting.android.opensdk.model.track.Track;

public interface IHistoryPresenter extends BasePresenter<IHistoryCallback> {
    void listHistories();
    void addHistory(Track track);
    void delHistory(Track track);
    void clearHistories();
}
