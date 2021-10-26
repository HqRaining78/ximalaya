package com.hq.ximalaya.interfaces;

import com.hq.ximalaya.base.BasePresenter;

public interface IRecommendPresenter extends BasePresenter<IRecommendCallback> {
// 获取推荐内容
    void getRecommendList();

    // 下拉刷新更多内容
    void pull2RefreshMore();

    void loadMore();

}
