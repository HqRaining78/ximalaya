package com.hq.ximalaya.presenters;

import com.hq.ximalaya.base.BaseApplication;
import com.hq.ximalaya.data.HistoryDao;
import com.hq.ximalaya.data.IHistoryDao;
import com.hq.ximalaya.data.IHistoryDaoCallback;
import com.hq.ximalaya.interfaces.IHistoryCallback;
import com.hq.ximalaya.interfaces.IHistoryPresenter;
import com.hq.ximalaya.utils.Constants;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HistoryPresenter implements IHistoryPresenter, IHistoryDaoCallback {

    private final IHistoryDao mHistoryDao;
    private List<IHistoryCallback> mCallbacks = new ArrayList<>();
    private List<Track> mCurrentTracks = new ArrayList<>();
    private Track mAddTrack;

    private HistoryPresenter() {
        mHistoryDao = new HistoryDao();
        mHistoryDao.setCallback(this);
        listHistories();
    }

    private static HistoryPresenter mPresenter = null;
    public static HistoryPresenter getInstance() {
        if (mPresenter == null) {
            synchronized (HistoryPresenter.class) {
                if (mPresenter == null) {
                    mPresenter = new HistoryPresenter();
                }
            }
        }
        return mPresenter;
    }

    @Override
    public void listHistories() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                if (mHistoryDao != null) {
                    mHistoryDao.listHistories();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    private boolean isOutSize = false;
    @Override
    public void addHistory(Track track) {
        if (mCurrentTracks.size() >= Constants.MAX_SUB_COUNT) {
            isOutSize = true;
            delHistory(mCurrentTracks.get(mCurrentTracks.size() - 1));
            this.mAddTrack = track;
        } else {
            doAddHistory(track);
        }
    }

    private void doAddHistory(Track track) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                if (mHistoryDao != null) {
                    mHistoryDao.addHistory(track);
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void delHistory(Track track) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                if (mHistoryDao != null) {
                    mHistoryDao.deleteHistory(track);
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void clearHistories() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                if (mHistoryDao != null) {
                    mHistoryDao.clearHistory();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void registerViewCallback(IHistoryCallback iHistoryCallback) {
        if (!mCallbacks.contains(iHistoryCallback)) {
            mCallbacks.add(iHistoryCallback);
        }
    }

    @Override
    public void unRegisterViewCallback(IHistoryCallback iHistoryCallback) {
        mCallbacks.remove(iHistoryCallback);
    }

    @Override
    public void onHistoryAdd(boolean isSuccess) {
        listHistories();
    }

    @Override
    public void onHistoryDelete(boolean isSuccess) {
        if (isOutSize && mAddTrack != null) {
            isOutSize = false;
            addHistory(mAddTrack);
        } else  {
            listHistories();
        }    }

    @Override
    public void onHistoryLoaded(List<Track> tracks) {
        this.mCurrentTracks = tracks;
        BaseApplication.getHandler().post(new Runnable() {
            @Override
            public void run() {
                for (IHistoryCallback callback : mCallbacks) {
                    callback.onHistoryLoaded(tracks);
                }
            }
        });
    }

    @Override
    public void onHistoryClear(boolean isSuccess) {
        listHistories();
    }
}
