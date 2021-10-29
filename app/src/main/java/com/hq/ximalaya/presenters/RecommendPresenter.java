package com.hq.ximalaya.presenters;

import com.hq.ximalaya.data.XimalayaApi;
import com.hq.ximalaya.interfaces.IRecommendCallback;
import com.hq.ximalaya.interfaces.IRecommendPresenter;
import com.hq.ximalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;

import java.util.ArrayList;
import java.util.List;

public class RecommendPresenter implements IRecommendPresenter {
    public static final String TAG = "RecommendPresenter";
    private List<IRecommendCallback> mCallbacks = new ArrayList<>();
    private List<Album> mCurrentRecommend = null;

    private RecommendPresenter() {}
    private static RecommendPresenter sInstance = null;
    public static RecommendPresenter getInstance() {
        if (sInstance == null) {
            synchronized (RecommendPresenter.class) {
                if (sInstance == null) {
                    sInstance = new RecommendPresenter();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void getRecommendList() {
        updateLoading();
        getRecommendData();
    }

    // 获取推荐内容
    private void getRecommendData() {
        XimalayaApi.getInstance().getRecommendList(new IDataCallBack<GussLikeAlbumList>() {
            @Override
            public void onSuccess(GussLikeAlbumList gussLikeAlbumList) {
                if (gussLikeAlbumList != null) {
                    List<Album> albumList = gussLikeAlbumList.getAlbumList();
                    LogUtil.d(TAG, "count ====> " + albumList.size());
                    handlerRecommendResult(albumList);
                }
            }

            @Override
            public void onError(int i, String s) {
                LogUtil.d(TAG, "error ====> " + i + "msg ====> " + s);
                handlerError();
            }
        });

    }

    private void handlerError() {
        if (mCallbacks != null) {
            for (IRecommendCallback callback : mCallbacks) {
                callback.onNetworkError();
            }
        }
    }

    private void handlerRecommendResult(List<Album> albumList) {
        // 通知UI更新
        if (albumList != null) {
            if (albumList.size() == 0) {
                if (mCallbacks != null) {
                    for (IRecommendCallback callback : mCallbacks) {
                        callback.onEmpty();
                    }
                }
            } else {
                if (mCallbacks != null) {
                    for (IRecommendCallback callback : mCallbacks) {
                        callback.onRecommendListLoaded(albumList);
                    }
                    this.mCurrentRecommend = albumList;
                }
            }
        }

    }

    private void updateLoading() {
        for (IRecommendCallback callback : mCallbacks) {
            callback.onLoading();
        }
    }

    public List<Album> getCurrentRecommend() {
        return mCurrentRecommend;
    }

    @Override
    public void pull2RefreshMore() {

    }

    @Override
    public void loadMore() {

    }

    @Override
    public void registerViewCallback(IRecommendCallback callback) {
        if (mCallbacks != null && !mCallbacks.contains(callback)) {
            mCallbacks.add(callback);
        }
    }

    @Override
    public void unRegisterViewCallback(IRecommendCallback callback) {
        if (mCallbacks != null) {
            mCallbacks.remove(callback);
        }
    }
}
