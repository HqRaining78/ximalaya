package com.hq.ximalaya.interfaces;

import com.hq.ximalaya.base.BasePresenter;

public interface IAlbumDetailPresenter extends BasePresenter<IAlbumDetailViewCallback> {

    void getAlbumDetail(long albumId, int page);

    // 下拉刷新更多内容
    void pull2RefreshMore();

    void loadMore();

}
