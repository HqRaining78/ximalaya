package com.hq.ximalaya.interfaces;

import com.hq.ximalaya.base.BasePresenter;
import com.ximalaya.ting.android.opensdk.model.album.Album;

public interface ISubscriptionPresenter extends BasePresenter<ISubscriptionCallback> {
    void addSubscription(Album album);
    void deleteSubscription(Album album);

    void getSubscriptionList();
}
