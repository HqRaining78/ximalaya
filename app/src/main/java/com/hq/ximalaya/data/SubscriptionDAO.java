package com.hq.ximalaya.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;

import com.hq.ximalaya.base.BaseApplication;
import com.hq.ximalaya.utils.Constants;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.Announcer;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionDAO implements ISubDao {
    private static final SubscriptionDAO ourInstance = new SubscriptionDAO();
    private final XimalayaDBHelper mXimalayaDBHelper;
    private ISubDaoCallback mCallback = null;

    public static SubscriptionDAO getInstance() {
        return ourInstance;
    }

    private SubscriptionDAO() {
        mXimalayaDBHelper = new XimalayaDBHelper(BaseApplication.getAppContext());
    }

    @Override
    public void setCallback(ISubDaoCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void addAlbum(Album album) {
        SQLiteDatabase database = null;
        try {
            database = mXimalayaDBHelper.getWritableDatabase();
            database.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(Constants.SUB_COVER_URL, album.getCoverUrlLarge());
            contentValues.put(Constants.SUB_TITLE, album.getAlbumTitle());
            contentValues.put(Constants.SUB_DESCRIPTION, album.getAlbumIntro());
            contentValues.put(Constants.SUB_TRACKS_COUNT, album.getIncludeTrackCount());
            contentValues.put(Constants.SUB_PLAY_COUNT, album.getPlayCount());
            contentValues.put(Constants.SUB_AUTHOR_NAME, album.getAnnouncer().getNickname());
            contentValues.put(Constants.SUB_ALBUM_ID, album.getId());

            database.insert(Constants.SUB_TB_NAME, null, contentValues);
            database.setTransactionSuccessful();
            if (mCallback != null) {
                mCallback.onAddResult(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mCallback != null) {
                mCallback.onAddResult(false);
            }
        } finally {
            if (database != null) {
                database.endTransaction();
                database.close();
            }
        }
    }

    @Override
    public void delAlbum(Album album) {
        SQLiteDatabase database = null;
        try {
            database = mXimalayaDBHelper.getWritableDatabase();
            database.beginTransaction();
            database.delete(Constants.SUB_TB_NAME, Constants.SUB_ALBUM_ID + "=? ", new String[]{album.getId() + ""});
            database.setTransactionSuccessful();
            if (mCallback != null) {
                mCallback.onDeleteResult(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mCallback != null) {
                mCallback.onDeleteResult(false);
            }
        } finally {
            if (database != null) {
                database.endTransaction();
                database.close();
            }
        }
    }

    @Override
    public void listAlbums() {
        SQLiteDatabase database = null;
        List<Album> result = new ArrayList<>();
        try {
            database = mXimalayaDBHelper.getWritableDatabase();
            database.beginTransaction();
            Cursor query = database.query(Constants.SUB_TB_NAME, null, null, null, null, null, null);
            while (query.moveToNext()) {                Album album = new Album();
                int coverUrlIndex = query.getColumnIndex(Constants.SUB_COVER_URL);
                String coverUrl = query.getString(coverUrlIndex);
                album.setCoverUrlLarge(coverUrl);

                int titleIndex = query.getColumnIndex(Constants.SUB_TITLE);
                String title = query.getString(titleIndex);
                album.setAlbumTitle(title);

                int descriptionIndex = query.getColumnIndex(Constants.SUB_DESCRIPTION);
                String description = query.getString(descriptionIndex);
                album.setAlbumIntro(description);

                int tracksCountIndex = query.getColumnIndex(Constants.SUB_TRACKS_COUNT);
                long tracksCount = query.getLong(tracksCountIndex);
                album.setIncludeTrackCount(tracksCount);

                int playCountIndex = query.getColumnIndex(Constants.SUB_PLAY_COUNT);
                long playCount = query.getLong(playCountIndex);
                album.setPlayCount(playCount);

                int authorIndex = query.getColumnIndex(Constants.SUB_AUTHOR_NAME);
                String authorName = query.getString(authorIndex);
                Announcer announcer = new Announcer();
                announcer.setNickname(authorName);

                int albumIdIndex = query.getColumnIndex(Constants.SUB_ALBUM_ID);
                long albumId = query.getLong(albumIdIndex);
                album.setId(albumId);
            }
            query.close();
            database.setTransactionSuccessful();
            if (mCallback != null) {
                mCallback.onSubListLoaded(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.endTransaction();
                database.close();
            }
        }
    }
}
