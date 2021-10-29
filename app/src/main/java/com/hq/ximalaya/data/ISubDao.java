package com.hq.ximalaya.data;

import com.ximalaya.ting.android.opensdk.model.album.Album;

public interface ISubDao {
    void setCallback(ISubDaoCallback callback);
    void addAlbum(Album album);
    void delAlbum(Album album);
    void listAlbums();
}
