package com.hq.ximalaya.presenters;

import com.hq.ximalaya.data.XimalayaApi;
import com.hq.ximalaya.interfaces.IAlbumDetailPresenter;
import com.hq.ximalaya.interfaces.IAlbumDetailViewCallback;
import com.hq.ximalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;

import java.util.ArrayList;
import java.util.List;

public class AlbumDetailPresenter implements IAlbumDetailPresenter {
    private static final String TAG = "AlbumDetailPresenter";
    private Album mTargetAlbum = null;
    private List<IAlbumDetailViewCallback> mCallbacks = new ArrayList<>();
    private long mCurrentAlbumId = -1;
    private int mCurrentPageIndex = 0;
    private List<Track> mTracks = new ArrayList<>();

    private AlbumDetailPresenter() {

    }
    private static AlbumDetailPresenter sInstance = null;
    public static AlbumDetailPresenter getInstance() {
        if (sInstance == null) {
            synchronized (AlbumDetailPresenter.class) {
                if (sInstance == null) {
                    sInstance = new AlbumDetailPresenter();
                }
            }
        }
        return sInstance;
    }


    @Override
    public void getAlbumDetail(long albumId, int page) {
        this.mCurrentAlbumId = albumId;
        this.mCurrentPageIndex = page;
        mTracks.clear();

        doLoaded(false);
    }

    private void doLoaded(final boolean isLoadMore) {
        XimalayaApi.getInstance().getAlbumDetail(new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
                if (trackList != null) {
                    List<Track> tracks = trackList.getTracks();
                    if (isLoadMore) {
                        mTracks.addAll(mTracks.size() - 1, tracks);
                        int size = tracks.size();
                        handlerLoaderMoreResult(size);
                    } else {
                        mTracks.addAll(0, tracks);
                    }
                    handlerAlbumDetailResult(tracks);
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if (isLoadMore) {
                    mCurrentPageIndex--;
                }
                LogUtil.d(TAG, "error ---> " + errorCode + " msg ----> " + errorMsg);
                handlerError(errorCode, errorMsg);
            }
        }, mCurrentAlbumId, mCurrentPageIndex);
    }

    private void handlerLoaderMoreResult(int size) {
        for (IAlbumDetailViewCallback callback : mCallbacks) {
            callback.onLoaderMoreFinish(size);
        }
    }


    private void handlerError(int errorCode, String errorMsg) {
        for (IAlbumDetailViewCallback callback : mCallbacks) {
            callback.onNetworkError(errorCode, errorMsg);
        }
    }

    private void handlerAlbumDetailResult(List<Track> tracks) {
        for (IAlbumDetailViewCallback callback : mCallbacks) {
            callback.onDetailListLoaded(tracks);
        }
    }

    @Override
    public void pull2RefreshMore() {
        mCurrentPageIndex = 1;
        mTracks.clear();
        doLoaded(false);
    }

    @Override
    public void loadMore() {
        mCurrentPageIndex++;
        doLoaded(true);
    }

    @Override
    public void registerViewCallback(IAlbumDetailViewCallback callback) {
        if (!mCallbacks.contains(callback)) {
            mCallbacks.add(callback);
            if (mTargetAlbum != null) {
                callback.onAlbumLoaded(mTargetAlbum);
            }
        }
    }

    @Override
    public void unRegisterViewCallback(IAlbumDetailViewCallback callback) {
        if (mCallbacks.contains(callback)) {
            mCallbacks.remove(callback);
        }
    }

    public void setTargetAlbum(Album targetAlbum) {
        this.mTargetAlbum = targetAlbum;
    }

}
