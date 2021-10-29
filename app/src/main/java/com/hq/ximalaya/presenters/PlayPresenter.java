package com.hq.ximalaya.presenters;

import android.content.Context;
import android.content.SharedPreferences;

import com.hq.ximalaya.data.XimalayaApi;
import com.hq.ximalaya.base.BaseApplication;
import com.hq.ximalaya.interfaces.IPlayerCallback;
import com.hq.ximalaya.interfaces.IPlayerPresenter;
import com.hq.ximalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.advertis.Advertis;
import com.ximalaya.ting.android.opensdk.model.advertis.AdvertisList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.advertis.IXmAdsStatusListener;
import com.ximalaya.ting.android.opensdk.player.constants.PlayerConstants;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP;

public class PlayPresenter implements IPlayerPresenter, IXmAdsStatusListener, IXmPlayerStatusListener {
    public static final String TAG = "PlayPresenter";
    private final XmPlayerManager mPlayerManager;
    private final List<IPlayerCallback> mIPlayerCallbacks = new ArrayList<>();
    private Track mCurrentTrack;
    private int mCurrentIndex = 0;
    private final SharedPreferences mPlayModeSp;
    private XmPlayListControl.PlayMode mCurrentModel = PLAY_MODEL_LIST;
    private boolean mIsReverse = false;

    public static final int PLAY_MODEL_LIST_INT = 0;
    public static final int PLAY_MODEL_LIST_LOOP_INT = 1;
    public static final int PLAY_MODEL_RANDOM_INT = 2;
    public static final int PLAY_MODEL_SINGLE_LOOP_INT = 3;
    public static final String PLAY_MODE_SP_NAME = "PlayMod";
    public static final String PLAY_MODE_SP_KEY = "currentPlayMode";
    private int mCurrentProgressPosition = 0;
    private int mProgressDuration = 0;

    private PlayPresenter() {
        mPlayerManager = XmPlayerManager.getInstance(BaseApplication.getAppContext());
        mPlayerManager.addAdsStatusListener(this);
        mPlayerManager.addPlayerStatusListener(this);
        mPlayModeSp = BaseApplication.getAppContext().getSharedPreferences(PLAY_MODE_SP_NAME, Context.MODE_PRIVATE);

    }

    private static  PlayPresenter sPlayPresenter;

    public static PlayPresenter getPlayPresenter() {
        if (sPlayPresenter == null) {
            synchronized (PlayPresenter.class) {
                if (sPlayPresenter == null) {
                    sPlayPresenter = new PlayPresenter();
                }
            }
        }
        return sPlayPresenter;
    }

    private boolean isPlayListSet = false;
    public void setPlayList(List<Track> list, int playIndex) {
        mCurrentIndex = playIndex;

        if (mPlayerManager != null) {
            mPlayerManager.setPlayList(list, playIndex);
            isPlayListSet = true;

            mCurrentTrack = list.get(playIndex);
        }


    }

    public boolean hasPlayList() {
        return isPlayListSet;
    }


    @Override
    public void play() {
        if (isPlayListSet) {
            mPlayerManager.play();
        }
    }

    @Override
    public boolean isPlay() {
        return mPlayerManager.isPlaying();
    }

    @Override
    public void pause() {
        if (mPlayerManager != null) {
            LogUtil.d(TAG, "play status -----> " + mPlayerManager.getPlayerStatus());
            mPlayerManager.pause();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void playPre() {
        if (mPlayerManager != null) {
            mPlayerManager.playPre();
        }
    }

    @Override
    public void playNext() {
        if (mPlayerManager != null) {
            mPlayerManager.playNext();
        }
    }

    @Override
    public void reversePlayList() {
        List<Track> playList = mPlayerManager.getPlayList();
        Collections.reverse(playList);
        mIsReverse = !mIsReverse;

        mCurrentIndex = playList.size() - mCurrentIndex - 1;
        mPlayerManager.setPlayList(playList, mCurrentIndex);
        mCurrentTrack = (Track)mPlayerManager.getCurrSound();
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onListLoaded(playList);
            iPlayerCallback.onTrackUpdate(mCurrentTrack, mCurrentIndex);
            iPlayerCallback.updateListOrder(mIsReverse);
        }
    }

    @Override
    public void playByAlbumId(long id) {
        XimalayaApi.getInstance().getAlbumDetail(new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
                List<Track> tracks = trackList.getTracks();
                if (tracks != null && tracks.size() > 0) {
                    mPlayerManager.setPlayList(tracks, 0);
                    isPlayListSet = true;

                    mCurrentIndex = 0;
                    mCurrentTrack = tracks.get(0);
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        }, id, 1);
    }

    @Override
    public void switchPlayMode(XmPlayListControl.PlayMode mode) {
        if (mPlayerManager != null) {
            mCurrentModel = mode;
            mPlayerManager.setPlayMode(mode);
            for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
                iPlayerCallback.onPlayModeChange(mode);
            }
            SharedPreferences.Editor edit = mPlayModeSp.edit();
            edit.putInt(PLAY_MODE_SP_KEY, getIntByPlayMode(mode));
            edit.apply();
        }
    }

    private int getIntByPlayMode(XmPlayListControl.PlayMode mode) {
        switch(mode) {
            case PLAY_MODEL_SINGLE_LOOP:
                return PLAY_MODEL_SINGLE_LOOP_INT;
            case PLAY_MODEL_LIST_LOOP:
                return PLAY_MODEL_LIST_LOOP_INT;
            case PLAY_MODEL_RANDOM:
                return PLAY_MODEL_RANDOM_INT;
            case PLAY_MODEL_LIST:
                return PLAY_MODEL_LIST_INT;
        }

        return PLAY_MODEL_LIST_INT;
    }

    private XmPlayListControl.PlayMode getModeByInt(int index) {
        switch(index) {
            case PLAY_MODEL_SINGLE_LOOP_INT:
                return PLAY_MODEL_SINGLE_LOOP;
            case PLAY_MODEL_LIST_LOOP_INT:
                return PLAY_MODEL_LIST_LOOP;
            case PLAY_MODEL_RANDOM_INT:
                return PLAY_MODEL_RANDOM;
            case PLAY_MODEL_LIST_INT:
                return PLAY_MODEL_LIST;
        }
        return PLAY_MODEL_LIST;
    }

    @Override
    public void getPlayList() {
        if (mPlayerManager != null) {
           List<Track> playList = mPlayerManager.getPlayList();
            for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
                iPlayerCallback.onListLoaded(playList);
            }
        }
//        mPlayerManager.getCommonTrackList();
    }

    @Override
    public void playByIndex(int index) {
        if (mPlayerManager != null) {
            mPlayerManager.play(index);
        }
    }

    @Override
    public void seekTo(int progress) {
        mPlayerManager.seekTo(progress);
    }

    @Override
    public void registerViewCallback(IPlayerCallback iPlayerCallback) {
        iPlayerCallback.onTrackUpdate(mCurrentTrack, mCurrentIndex);
        iPlayerCallback.onProgressChange(mCurrentProgressPosition, mProgressDuration);
        handlePlayState(iPlayerCallback);
        int index = mPlayModeSp.getInt(PLAY_MODE_SP_KEY, PLAY_MODEL_LIST_INT);
        XmPlayListControl.PlayMode playMode = getModeByInt(index);
        mCurrentModel = playMode;
        iPlayerCallback.onPlayModeChange(playMode);
        if (!mIPlayerCallbacks.contains(iPlayerCallback)) {
            mIPlayerCallbacks.add(iPlayerCallback);
        }
    }

    private void handlePlayState(IPlayerCallback iPlayerCallback) {
        int playerStatus = mPlayerManager.getPlayerStatus();
        if (PlayerConstants.STATE_STARTED == playerStatus) {
            iPlayerCallback.onPlayStart();
        } else {
            iPlayerCallback.onPlayPause();
        }
    }

    @Override
    public void unRegisterViewCallback(IPlayerCallback iPlayerCallback) {
        mIPlayerCallbacks.remove(iPlayerCallback);
    }

    @Override
    public void onStartGetAdsInfo() {

    }

    // 广告相关的回调
    @Override
    public void onGetAdsInfo(AdvertisList advertisList) {
        LogUtil.d(TAG, "onGetAdsInfo...");
    }

    @Override
    public void onAdsStartBuffering() {
        LogUtil.d(TAG, "onAdsStartBuffering...");
    }

    @Override
    public void onAdsStopBuffering() {
        LogUtil.d(TAG, "onAdsStopBuffering...");
    }

    @Override
    public void onStartPlayAds(Advertis advertis, int i) {
        LogUtil.d(TAG, "onStartPlayAds...");
    }

    @Override
    public void onCompletePlayAds() {
        LogUtil.d(TAG, "onCompletePlayAds...");
    }

    @Override
    public void onError(int i, int i1) {
        LogUtil.d(TAG, "onError.....> " + i + "-----> " + i1);
    }

    // 播放相关的回调
    @Override
    public void onPlayStart() {
        LogUtil.d(TAG, "onPlayStart...");
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onPlayStart();
        }
    }

    @Override
    public void onPlayPause() {
        LogUtil.d(TAG, "onPlayPause...");
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onPlayPause();
        }
    }

    @Override
    public void onPlayStop() {
        LogUtil.d(TAG, "onPlayStop...");
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onPlayStop();
        }
    }

    @Override
    public void onSoundPlayComplete() {
        LogUtil.d(TAG, "onSoundPlayComplete...");

    }

    @Override
    public void onSoundPrepared() {
        LogUtil.d(TAG, "onSoundPrepared...");

        LogUtil.d(TAG, "onSoundPrepared status -----> " + mPlayerManager.getPlayerStatus());
        mPlayerManager.setPlayMode(mCurrentModel);
        if (mPlayerManager.getPlayerStatus() == PlayerConstants.STATE_PREPARED) {
            mPlayerManager.play();
        }

    }

    @Override
    public void onSoundSwitch(PlayableModel lastModel, PlayableModel curModel) {
        LogUtil.d(TAG, "onSoundSwitch...");

//        if (curModel.getKind().equals("track")) {
//            Track currentTrack = (Track)curModel;
//            LogUtil.d(TAG, "title...>>> " + currentTrack.getTrackTitle());
//        }
        mCurrentIndex = mPlayerManager.getCurrentIndex();
        if (curModel instanceof Track) {
            Track currentTrack = (Track)curModel;
            LogUtil.d(TAG, "title...>>> " + currentTrack.getTrackTitle());
            mCurrentTrack = currentTrack;
            for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
                iPlayerCallback.onTrackUpdate(mCurrentTrack, mCurrentIndex);
            }
        }
    }

    @Override
    public void onBufferingStart() {
        LogUtil.d(TAG, "onBufferingStart...");

    }

    @Override
    public void onBufferingStop() {
        LogUtil.d(TAG, "onBufferingStop...");

    }

    @Override
    public void onBufferProgress(int i) {
        LogUtil.d(TAG, "onBufferProgress... > " + i);

    }

    @Override
    public void onPlayProgress(int i, int i1) {
        this.mCurrentProgressPosition = i;
        this.mProgressDuration = i1;
        LogUtil.d(TAG, "onPlayProgress... i-" + i + "------> " + i1);
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onProgressChange(i, i1);
        }
    }

    @Override
    public boolean onError(XmPlayerException e) {
        LogUtil.d(TAG, "onError... exception ---> " + e);
        return false;
    }
}
