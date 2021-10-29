package com.hq.ximalaya.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.hq.ximalaya.utils.Constants;

public class XimalayaDBHelper extends SQLiteOpenHelper {

    public XimalayaDBHelper(@Nullable Context context) {
        // name 数据库名称 factory 游标 version 版本
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION_CODE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String s = "create table " + Constants.SUB_TB_NAME + "(" + Constants.SUB_ID + " integer primary key autoincrement, " + Constants.SUB_COVER_URL + " varchar, " + Constants.SUB_TITLE + " varchar, "+ Constants.SUB_DESCRIPTION +" varchar, "+ Constants.SUB_PLAY_COUNT +" integer, " + Constants.SUB_TRACKS_COUNT + " integer, " + Constants.SUB_AUTHOR_NAME + " varchar, " + Constants.SUB_ALBUM_ID + " integer)";
        db.execSQL(s);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
