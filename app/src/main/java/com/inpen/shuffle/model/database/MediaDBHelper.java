package com.inpen.shuffle.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.inpen.shuffle.model.database.MediaContract.MediaEntry;
import com.inpen.shuffle.model.database.MediaContract.PlaylistsEntry;
import com.inpen.shuffle.utils.LogHelper;
/**
 * Created by Abhishek on 10/21/2016.
 */


public class MediaDbHelper extends SQLiteOpenHelper{
    public static final String DATABASE_NAME = "media.db";
    private static final String LOG_TAG = LogHelper.makeLogTag(MediaDbHelper.class);
    private static final int DATABASE_VERSION = 1;

    public MediaDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //Creating Movie Table
        final String SQL_CREATE_MEDIA_TABLE = "CREATE TABLE " + MediaEntry.TABLE_NAME + " ( " +
                MediaEntry._ID + " INTEGER PRIMARY KEY, " +
                MediaEntry.COLUMN_SONG_ID + " TEXT UNIQUE NOT NULL, " +
                MediaEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MediaEntry.COLUMN_PATH + " TEXT UNIQUE NOT NULL, " +
                MediaEntry.COLUMN_ALBUM + " TEXT NOT NULL, " +
                MediaEntry.COLUMN_ARTIST + " TEXT NOT NULL, " +
                MediaEntry.COLUMN_FOLDER + " TEXT NOT NULL, " +
                MediaEntry.COLUMN_DURATION + " TEXT NOT NULL, " +
                MediaEntry.COLUMN_ALBUM_ART + " TEXT, " +
                MediaEntry.COLUMN_IS_SYNCED + " INTEGER NOT NULL" +
                " )";

        final String SQL_CREATE_PLAYLISTS_TABLE = "CREATE TABLE " + PlaylistsEntry.TABLE_NAME + " ( " +
                PlaylistsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PlaylistsEntry.COLUMN_PLAYLIST_NAME + " TEXT NOT NULL, " +
                PlaylistsEntry.COLUMN_SONG_ID + " TEXT NOT NULL" +
                " )";

        Log.i(LOG_TAG, "onCreate: SQL Query" + SQL_CREATE_MEDIA_TABLE);
        Log.i(LOG_TAG, "onCreate: SQL Query" + SQL_CREATE_PLAYLISTS_TABLE);

        sqLiteDatabase.execSQL(SQL_CREATE_MEDIA_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PLAYLISTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // TODO implement when required
        // although not required in DATABASE_VERSION = 1
    }
}
