package com.hq.ximalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

public interface IAlbumDetailViewCallback {
    void onDetailListLoaded(List<Track> result);

    void onAlbumLoaded(Album album);

    void onNetworkError(int errorCode, String errorMsg);

    void onLoaderMoreFinish(int size);

    void onRefreshFinish(int size);
}
