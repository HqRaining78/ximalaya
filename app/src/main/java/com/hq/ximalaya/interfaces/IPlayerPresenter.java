package com.hq.ximalaya.interfaces;

import com.hq.ximalaya.base.BasePresenter;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.ArrayList;
import java.util.List;

public interface IPlayerPresenter extends BasePresenter<IPlayerCallback> {
    void play();

    void pause();

    void stop();

    void playPre();

    void playNext();

    void switchPlayMode(XmPlayListControl.PlayMode mode);

    void getPlayList();

    void playByIndex(int index);

    void seekTo(int progress);

    boolean isPlay();

    void reversePlayList();

}
