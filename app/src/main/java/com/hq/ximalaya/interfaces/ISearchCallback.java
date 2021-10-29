package com.hq.ximalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;

import java.util.List;

public interface ISearchCallback {
    void onSearchResultLoaded(List<Album> result);

    void onHotWordLoaded(List<HotWord> result);

    void onLoadMoreResult(List<Album> result, boolean isOk);

    void onRecommendWordLoaded(List<QueryResult> keyWordList);

    void onError(int errorCode, String errorMsg);
}
