package com.hq.ximalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

public interface IPlayerCallback {
    void onPlayStart();
    void onPlayPause();
    void onPlayStop();
    void onPlayError();

    void nextPlay();
    void onPrePlay();

    void onListLoaded(List<Track> list);

    void onPlayModeChange(XmPlayListControl.PlayMode mode);

    void onProgressChange(int currentProgress, int total);

    void onAdLoading();
    void onAdFinished();

    void onTrackUpdate(Track track, int position);

    void updateListOrder(boolean isReverse);
}
