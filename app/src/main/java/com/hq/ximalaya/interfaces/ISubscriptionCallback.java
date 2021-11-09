package com.hq.ximalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

public interface ISubscriptionCallback {
    void onAddResult(boolean isSuccess);
    void onDeleteResult(boolean isSuccess);
    void onSubscriptionLoaded(List<Album> result);

    void onSubFull();
}
