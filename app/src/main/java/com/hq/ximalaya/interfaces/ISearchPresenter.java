package com.hq.ximalaya.interfaces;

import com.hq.ximalaya.base.BasePresenter;

public interface ISearchPresenter extends BasePresenter<ISearchCallback> {
    void doSearch(String keyword);

    void reSearch();

    void loadMore();

    void getHotWord();

    void getRecommendWord(String keyword);
}
