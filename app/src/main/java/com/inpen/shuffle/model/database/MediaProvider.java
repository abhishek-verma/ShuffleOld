package com.inpen.shuffle.model.database;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.inpen.shuffle.model.database.MediaContract.MediaEntry;
import com.inpen.shuffle.model.database.MediaContract.PlaylistsEntry;

/**
 * Created by Abhishek on 10/21/2016.
 */

public class MediaProvider extends ContentProvider {

    public static final String mSongsSortOrder =
            MediaEntry.COLUMN_TITLE + " ASC";
    public static final String mAlbumsSortOrder =
            MediaEntry.COLUMN_ALBUM + " ASC";
    public static final String mArtistsSortOrder =
            MediaEntry.COLUMN_ARTIST + " ASC";
    public static final String mPlaylistsOrder =
            PlaylistsEntry.COLUMN_PLAYLIST_NAME + " ASC";


    private static final int SONGS = 100;
    private static final int SONGS_BY_PLAYLIST = 101;
    private static final int PLAYLISTS = 200;
    private static final int SONG_BY_ID = 10;

    // The URI Matcher used by this content provider
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final SQLiteQueryBuilder songsJoinPlaylistQueryBuilder;

    static {
        songsJoinPlaylistQueryBuilder = new SQLiteQueryBuilder();

        songsJoinPlaylistQueryBuilder.setTables(
                MediaEntry.TABLE_NAME + " CROSS JOIN " +
                        PlaylistsEntry.TABLE_NAME +
                        " ON " + MediaEntry.TABLE_NAME +
                        "." + MediaEntry.COLUMN_SONG_ID +
                        " = " + PlaylistsEntry.TABLE_NAME +
                        "." + PlaylistsEntry.COLUMN_SONG_ID);

        songsJoinPlaylistQueryBuilder.setDistinct(true);
    }


    private MediaDbHelper mOpenHelper;

    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MediaContract.CONTENT_AUTHORITY;

        matcher.addURI(authority,
                MediaContract.PATH_MEDIA,
                SONGS);
        matcher.addURI(authority,
                MediaContract.PATH_MEDIA + "/" + MediaContract.PATH_BY_PLAYLIST + "/*",
                SONGS_BY_PLAYLIST);
        matcher.addURI(authority,
                MediaContract.PATH_PLAYLISTS,
                PLAYLISTS);
        matcher.addURI(authority,
                MediaContract.PATH_MEDIA + "/*",
                SONG_BY_ID);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new MediaDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case SONGS:
            case SONGS_BY_PLAYLIST:
                return MediaEntry.CONTENT_TYPE;
            case PLAYLISTS:
                return PlaylistsEntry.CONTENT_TYPE;
            case SONG_BY_ID:
                return MediaEntry.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor = null;

        switch (sUriMatcher.match(uri)) {
            case SONGS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        true,
                        MediaEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder, null
                );
                break;

            case SONGS_BY_PLAYLIST:
                retCursor = songsJoinPlaylistQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        PlaylistsEntry.TABLE_NAME + "." + PlaylistsEntry.COLUMN_PLAYLIST_NAME + "=?",
                        new String[]{MediaEntry.getPlaylistNameFromUri(uri)},
                        null, null,
                        sortOrder);
                break;

            case PLAYLISTS:
                //Still using query builder because in some cases albumart is needed for respective playlists
                // thus songs table is also required
                retCursor = songsJoinPlaylistQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder
                );
                break;

            case SONG_BY_ID:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MediaEntry.TABLE_NAME,
                        projection,
                        MediaEntry.COLUMN_SONG_ID + "=?",
                        new String[]{MediaEntry.getSongIdFromUri(uri)},
                        null, null,
                        sortOrder
                );
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        if (retCursor != null) retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public synchronized Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {

            case SONGS: {
                long _id = db.insertWithOnConflict(MediaEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE);
                if (_id > 0)
                    returnUri = MediaEntry.buildSongWithIdUri(
                            values.getAsString(MediaEntry.COLUMN_SONG_ID));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PLAYLISTS: {
                long _id = db.insertWithOnConflict(PlaylistsEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE);
                if (_id > 0)
                    returnUri = MediaEntry.buildSongWithIdUri(
                            values.getAsString(PlaylistsEntry.COLUMN_SONG_ID));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case SONGS:
                rowsDeleted = db.delete(
                        MediaEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case PLAYLISTS:
                rowsDeleted = db.delete(
                        PlaylistsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case SONGS:
                rowsUpdated = db.update(MediaEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }


    @Override
    public synchronized int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;

        switch (match) {
            case SONGS: {
                db.beginTransaction();
                // removing all previous songs
                db.delete(MediaEntry.TABLE_NAME, null, null);
                try {
                    for (ContentValues val : values) {
                        long _id
                                = db.insertWithOnConflict(MediaEntry.TABLE_NAME,
                                null,
                                val,
                                SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1)
                            returnCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }

            case PLAYLISTS: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(PlaylistsEntry.TABLE_NAME,
                                null,
                                value,
                                SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1)
                            returnCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }

            default: {
                return super.bulkInsert(uri, values);
            }
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }


    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
