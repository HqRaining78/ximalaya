package com.hq.ximalaya.presenters;

import com.hq.ximalaya.data.XimalayaApi;
import com.hq.ximalaya.interfaces.ISearchCallback;
import com.hq.ximalaya.interfaces.ISearchPresenter;
import com.hq.ximalaya.utils.Constants;
import com.hq.ximalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.SearchAlbumList;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.HotWordList;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;
import com.ximalaya.ting.android.opensdk.model.word.SuggestWords;

import java.util.ArrayList;
import java.util.List;

public class SearchPresenter implements ISearchPresenter {
    public static final String TAG = "SearchPresenter";
    List<ISearchCallback> mCallbacks = new ArrayList<>();
    private String mCurrentKeyword = null;
    private final XimalayaApi mXimalayaApi;
    private int mCurrentPage = 1;
    private List<Album> searchResult = new ArrayList<>();
    private boolean mIsLoadMore = false;

    private SearchPresenter() {
        mXimalayaApi = XimalayaApi.getInstance();
    }
    private static SearchPresenter sInstance = null;
    public static SearchPresenter getInstance() {
        if (sInstance == null) {
            synchronized (SearchPresenter.class) {
                if (sInstance == null) {
                    sInstance = new SearchPresenter();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void doSearch(String keyword) {
        searchResult.clear();
        mCurrentPage = 1;
        this.mCurrentKeyword = keyword;
        search(keyword);
    }

    private void search(String keyword) {
        mXimalayaApi.searchByKeyword(keyword, mCurrentPage, new IDataCallBack<SearchAlbumList>() {
            @Override
            public void onSuccess(SearchAlbumList searchAlbumList) {
                List<Album> albums = searchAlbumList.getAlbums();
                searchResult.addAll(albums);

                if (albums != null) {
                    if (mIsLoadMore) {
                        for (ISearchCallback callback : mCallbacks) {
                            callback.onLoadMoreResult(searchResult, albums.size() > 0);
                        }
                        mIsLoadMore = false;
                    } else {
                        for (ISearchCallback callback : mCallbacks) {
                            callback.onSearchResultLoaded(searchResult);
                        }
                    }

                }
            }

            @Override
            public void onError(int i, String s) {
                for (ISearchCallback callback : mCallbacks) {
                    if (mIsLoadMore) {
                        callback.onLoadMoreResult(searchResult, false);
                        mIsLoadMore = false;
                        mCurrentPage--;
                    } else {
                        callback.onError(i, s);
                    }
                }
            }
        });
    }

    @Override
    public void reSearch() {
        search(mCurrentKeyword);
    }

    @Override
    public void loadMore() {
        if (searchResult.size() < Constants.DISPLAY_COUNT) {
            for (ISearchCallback callback : mCallbacks) {
                callback.onLoadMoreResult(searchResult, false);
            }
        } else {
            mIsLoadMore = true;
            mCurrentPage++;
            search(mCurrentKeyword);
        }
    }

    @Override
    public void getHotWord() {
        mXimalayaApi.getHotWords(new IDataCallBack<HotWordList>() {
            @Override
            public void onSuccess(HotWordList hotWordList) {
                List<HotWord> wordList = hotWordList.getHotWordList();
                for (ISearchCallback callback : mCallbacks) {
                    callback.onHotWordLoaded(wordList);
                }
            }
            @Override
            public void onError(int i, String s) {
                LogUtil.d(TAG, "errorCode---> + " + i + "errorMSg ---> " + s);
            }
        });
    }

    @Override
    public void getRecommendWord(String keyword) {
        mXimalayaApi.getSuggestWords(keyword, new IDataCallBack<SuggestWords>() {
            @Override
            public void onSuccess(SuggestWords suggestWords) {
                if (suggestWords != null) {
                    List<QueryResult> keyWordList = suggestWords.getKeyWordList();
                    for (ISearchCallback callback : mCallbacks) {
                        callback.onRecommendWordLoaded(keyWordList);
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                LogUtil.d(TAG, "error----> " + i + "errorMsg ----> " + s);
            }
        });
    }

    @Override
    public void registerViewCallback(ISearchCallback iSearchCallback) {
        if (!mCallbacks.contains(iSearchCallback)) {
            mCallbacks.add(iSearchCallback);
        }
    }

    @Override
    public void unRegisterViewCallback(ISearchCallback iSearchCallback) {
        mCallbacks.remove(iSearchCallback);
    }
}
