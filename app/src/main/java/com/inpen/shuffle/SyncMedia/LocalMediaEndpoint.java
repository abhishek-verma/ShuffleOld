package com.inpen.shuffle.syncmedia;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.inpen.shuffle.model.Audio;
import com.inpen.shuffle.model.database.MediaContract;

import java.util.Vector;

/**
 * Created by Abhishek on 10/25/2016.
 */

public class LocalMediaEndpoint implements MediaEndpoint {

    Context mContext;

    public LocalMediaEndpoint(Context context) {
        mContext = context;
    }

    @Override
    public void syncMedia(Callback callback) {

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        final int COL_INDEX_DATA = 0;
        final int COL_INDEX_TITLE = 1;
        final int COL_INDEX_ALBUM = 2;
        final int COL_INDEX_ALBUM_KEY = 3;
        final int COL_INDEX_ARTIST = 4;
        final int COL_INDEX_ARTIST_KEY = 5;
        final int COL_INDEX_DURATION = 6;
        final int COL_INDEX_ALBUM_ID = 7;
        String[] projection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_KEY,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_KEY,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID
        };

        Cursor cur = mContext.getContentResolver()
                .query(uri, projection, selection, null, sortOrder);

        Vector<ContentValues> cvVector = new Vector<>();
        ContentValues cv;
        if (cur != null && cur.moveToFirst()) {
            do {
                String path = cur.getString(COL_INDEX_DATA);
                String title = cur.getString(COL_INDEX_TITLE);
                String album = cur.getString(COL_INDEX_ALBUM);
                String artist = cur.getString(COL_INDEX_ARTIST);
                long duration = cur.getLong(COL_INDEX_DURATION);
                String albumKey = cur.getString(COL_INDEX_ALBUM_KEY);
                String artistKey = cur.getString(COL_INDEX_ARTIST_KEY);
                String albumArt = getAlbumArtForAlbum(cur.getInt(COL_INDEX_ALBUM_ID));
                cv = new ContentValues();
                cv.put(MediaContract.MediaEntry.COLUMN_SONG_ID, Audio.generateSongID(title, artist, duration));
                cv.put(MediaContract.MediaEntry.COLUMN_PATH, path);
                cv.put(MediaContract.MediaEntry.COLUMN_TITLE, title);
                cv.put(MediaContract.MediaEntry.COLUMN_ALBUM, album);
                cv.put(MediaContract.MediaEntry.COLUMN_ALBUM_KEY, albumKey);
                cv.put(MediaContract.MediaEntry.COLUMN_FOLDER_PATH, MediaContract.MediaEntry.getFolderPathFromFullPath(path));
                cv.put(MediaContract.MediaEntry.COLUMN_ARTIST, artist);
                cv.put(MediaContract.MediaEntry.COLUMN_ARTIST_KEY, artistKey);
                cv.put(MediaContract.MediaEntry.COLUMN_DURATION, Long.toString(duration));
                cv.put(MediaContract.MediaEntry.COLUMN_ALBUM_ART, albumArt);
                cvVector.add(cv);
            } while (cur.moveToNext());
        }

        if (cur != null)
            cur.close();

        int inserted = 0;
        //adding to db
        if (cvVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cvVector.size()];
            cvVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(MediaContract.MediaEntry.CONTENT_URI, cvArray);
        }

        callback.onDataSynced(inserted);
    }


    private String getAlbumArtForAlbum(int albumId) {


        final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(artworkUri, albumId);

        return uri.toString();
        // If above doesn't work, use this instead
//
//        Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
//                new String[]{MediaStore.Audio.AlbumColumns.ALBUM_ID, MediaStore.Audio.AlbumColumns.ALBUM_ART},
//                MediaStore.Audio.Albums._ID + "=?",
//                new String[]{String.valueOf(albumId)},
//                null);
//        if (cursor.moveToFirst()) {
//            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART));
//            return path;
//        }
//        return "";

    }
}
