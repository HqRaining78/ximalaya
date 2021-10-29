package com.hq.ximalaya.data;

import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

public interface ISubDaoCallback {
    void onAddResult(boolean isSuccess);

    void onDeleteResult(boolean isSuccess);

    void onSubListLoaded(List<Album> result);
}
