package com.hq.ximalaya.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hq.ximalaya.base.BaseApplication;
import com.hq.ximalaya.utils.Constants;
import com.ximalaya.ting.android.opensdk.model.album.Announcer;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

public class HistoryDao implements IHistoryDao {

    private final XimalayaDBHelper mDbHelper;
    private IHistoryDaoCallback mCallback;
    private Object mLock = new Object();

    public HistoryDao() {
        mDbHelper = new XimalayaDBHelper(BaseApplication.getAppContext());
    }
    
    @Override
    public void setCallback(IHistoryDaoCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void addHistory(Track track) {
        synchronized (mLock) {
            SQLiteDatabase database = null;
            boolean isSuccess = false;
            try {
                database = mDbHelper.getWritableDatabase();
                database.delete(Constants.HISTORY_TB_NAME, Constants.HISTORY_TRACK_ID + "=?", new String[]{track.getDataId() + ""});

                database.beginTransaction();
                ContentValues values = new ContentValues();
                values.put(Constants.HISTORY_TRACK_ID, track.getDataId());
                values.put(Constants.HISTORY_TITLE, track.getTrackTitle());
                values.put(Constants.HISTORY_PLAY_COUNT, track.getPlayCount());
                values.put(Constants.HISTORY_DURATION, track.getDuration());
                values.put(Constants.HISTORY_UPDATE_TIME, track.getUpdatedAt());
                values.put(Constants.HISTORY_COVER, track.getCoverUrlLarge());
                values.put(Constants.HISTORY_AUTHOR, track.getAnnouncer().getNickname());
                values.put(Constants.HISTORY_ORDER_NUM, track.getOrderNum());

                database.insert(Constants.HISTORY_TB_NAME, null, values);
                database.setTransactionSuccessful();
                isSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
                isSuccess = false;
            } finally {
                if (database != null) {
                    database.endTransaction();
                    database.close();
                }

                if (mCallback != null) {
                    mCallback.onHistoryAdd(isSuccess);
                }
            }
        }
    }

    @Override
    public void deleteHistory(Track track) {
        synchronized (mLock) {
            SQLiteDatabase db = null;
            boolean isDeleteSuccess = false;
            try {
                db = mDbHelper.getWritableDatabase();
                db.beginTransaction();
                int delete = db.delete(Constants.HISTORY_TB_NAME, Constants.HISTORY_TRACK_ID + "=?", new String[]{track.getDataId() + ""});
                db.setTransactionSuccessful();
                isDeleteSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
                isDeleteSuccess = false;
            } finally {
                if (db != null) {
                    db.endTransaction();
                    db.close();
                }
                if (mCallback != null) {
                    mCallback.onHistoryDelete(isDeleteSuccess);
                }
            }
        }
    }

    @Override
    public void clearHistory() {
        synchronized (mLock) {
            SQLiteDatabase db = null;
            boolean isDeleteSuccess = false;
            try {
                db = mDbHelper.getWritableDatabase();
                db.beginTransaction();
                int delete = db.delete(Constants.HISTORY_TB_NAME, null, null);
                db.setTransactionSuccessful();
                isDeleteSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
                isDeleteSuccess = false;
            } finally {
                if (db != null) {
                    db.endTransaction();
                    db.close();
                }
                if (mCallback != null) {
                    mCallback.onHistoryClear(isDeleteSuccess);
                }
            }
        }
    }

    @Override
    public void listHistories() {
        synchronized (mLock) {
            SQLiteDatabase readableDatabase = null;
            List<Track> trackList = new ArrayList<>();
            try {
                readableDatabase = mDbHelper.getReadableDatabase();
                readableDatabase.beginTransaction();
                Cursor cursor = readableDatabase.query(Constants.HISTORY_TB_NAME, null, null, null, null, null, "_id desc");

                while (cursor.moveToNext()) {
                    Track track = new Track();
                    //封面图片
                    int coverIndex = cursor.getColumnIndex(Constants.HISTORY_COVER);
                    String coverUrl = cursor.getString(coverIndex);
                    track.setCoverUrlLarge(coverUrl);
                    track.setCoverUrlMiddle(coverUrl);
                    track.setCoverUrlSmall(coverUrl);

                    int authorIndex = cursor.getColumnIndex(Constants.HISTORY_AUTHOR);
                    String authorName = cursor.getString(authorIndex);

                    Announcer announcer = new Announcer();
                    announcer.setNickname(authorName);
                    track.setAnnouncer(announcer);

                    //
                    int titleIndex = cursor.getColumnIndex(Constants.HISTORY_TITLE);
                    String title = cursor.getString(titleIndex);
                    track.setTrackTitle(title);

                    //
                    int numberIndex = cursor.getColumnIndex(Constants.HISTORY_ORDER_NUM);
                    int orderNumber = cursor.getInt(numberIndex);
                    track.setOrderNum(orderNumber);
                    //
                    int durationIndex = cursor.getColumnIndex(Constants.HISTORY_DURATION);
                    int duration = cursor.getInt(durationIndex);
                    track.setDuration(duration);
                    //
                    int playIndex = cursor.getColumnIndex(Constants.HISTORY_PLAY_COUNT);
                    int playCount = cursor.getInt(playIndex);
                    track.setPlayCount(playCount);
                    //
                    int albumIndex = cursor.getColumnIndex(Constants.HISTORY_TRACK_ID);
                    int albumId = cursor.getInt(albumIndex);
                    track.setDataId(albumId);

                    int updateTimeIndex = cursor.getColumnIndex(Constants.HISTORY_UPDATE_TIME);
                    int updateTime = cursor.getInt(updateTimeIndex);
                    track.setUpdatedAt(updateTime);

                    trackList.add(track);
                }
                cursor.close();
                readableDatabase.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (readableDatabase != null) {
                    readableDatabase.endTransaction();
                    readableDatabase.close();
                }
                if (mCallback != null) {
                    mCallback.onHistoryLoaded(trackList);
                }
            }
        }
    }
}
