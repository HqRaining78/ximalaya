package com.hq.ximalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

public interface IRecommendCallback {
    void onRecommendListLoaded(List<Album> result);

    void onLoaderMore(List<Album> result);

    void onRefreshMore(List<Album> result);

    void onNetworkError();

    void onEmpty();

    void onLoading();
}
