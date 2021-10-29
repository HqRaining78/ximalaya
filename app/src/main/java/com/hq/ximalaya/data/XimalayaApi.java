package com.hq.ximalaya.data;

import com.hq.ximalaya.utils.Constants;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;
import com.ximalaya.ting.android.opensdk.model.album.SearchAlbumList;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.model.word.HotWordList;
import com.ximalaya.ting.android.opensdk.model.word.SuggestWords;

import java.util.HashMap;
import java.util.Map;

public class XimalayaApi {
    private XimalayaApi() {

    }
    private static XimalayaApi _sInstance;

    public static XimalayaApi getInstance() {
        if (_sInstance == null) {
            synchronized (XimalayaApi.class) {
                if (_sInstance == null) {
                    _sInstance = new XimalayaApi();
                }
            }
        }
        return _sInstance;
    }

    public void getRecommendList(IDataCallBack<GussLikeAlbumList> callBack) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.LIKE_COUNT, Constants.RECOMMEND_COUNT + "");
        CommonRequest.getGuessLikeAlbum(map, callBack);
    }

    public void getAlbumDetail(IDataCallBack<TrackList> callBack, long albumId, int pageIndex) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.ALBUM_ID, albumId + "");
        map.put(DTransferConstants.SORT, "asc");
        map.put(DTransferConstants.PAGE, pageIndex + "");
        map.put(DTransferConstants.PAGE_SIZE, Constants.TRACK_COUNT + "");
        CommonRequest.getTracks(map, callBack);
    }


    public void searchByKeyword(String keyword, int pageIndex, IDataCallBack<SearchAlbumList> callBack) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.SEARCH_KEY, keyword);
        map.put(DTransferConstants.PAGE, pageIndex + "");
        map.put(DTransferConstants.PAGE_SIZE, Constants.DISPLAY_COUNT + "");
        CommonRequest.getSearchedAlbums(map, callBack);

    }


    public void getHotWords(IDataCallBack<HotWordList> callBack) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.TOP, Constants.COUNT_HOT_WORD + "");
        CommonRequest.getHotWords(map, callBack);
    }

    public void getSuggestWords(String keyword, IDataCallBack<SuggestWords> callBack) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.SEARCH_KEY, keyword);
        CommonRequest.getSuggestWord(map, callBack);
    }

}
