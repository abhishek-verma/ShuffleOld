package com.inpen.shuffle.utils;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.util.Log;

import com.inpen.shuffle.model.Audio;
import com.inpen.shuffle.model.database.MediaContract.MediaEntry;
import com.inpen.shuffle.model.database.MediaContract.PlaylistsEntry;
import com.inpen.shuffle.model.database.MediaProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Abhishek on 10/22/2016.
 */

public class QueueHelper {

    public static final int COLUMN_INDEX_TITLE = 0;
    public static final int COLUMN_INDEX_ALBUM = 1;
    public static final int COLUMN_INDEX_ARTIST = 2;
    public static final int COLUMN_INDEX_ALBUM_ART = 3;
    public static final int COLUMN_INDEX_DURATION = 4;
    public static final int COLUMN_INDEX_PATH = 5;
    public static final int COLUMN_SONG_ID = 6;
    public static final int COLUMN_PLAYLIST_NAME = 7;
    private static final String[] SONGS_QUEUE_CURSOR_COLUMNS = {
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_TITLE,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_ALBUM,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_ARTIST,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_ALBUM_ART,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_DURATION,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_PATH,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_SONG_ID
    };
    private static final String[] SONGS_FOR_PLAYLISTS_QUEUE_CURSOR_COLUMNS = {
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_TITLE,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_ALBUM,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_ARTIST,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_ALBUM_ART,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_DURATION,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_PATH,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_SONG_ID,
            PlaylistsEntry.TABLE_NAME + "." + PlaylistsEntry.COLUMN_PLAYLIST_NAME
    };
    public static String PlAYLIST_LIKED = "liked";
    public static String PlAYLIST_DISLIKED = "disliked";
    private static Context mContext;

    public QueueHelper(Context context) {
        mContext = context;
    }

    private static String getStringFromSelectorItems(String[] selectors) {
        StringBuffer s = new StringBuffer();


        for (int i = 0; i < selectors.length; i++) {
            if (i != 0) s.append(",");

            s.append('"').append(selectors[i]).append('"');
        }

        Log.i("temp", s.toString());

        return s.toString();
    }

    public synchronized List<Audio> generateQueue(MediaSelectorType selector,
                                                  @Nullable String[] selectorItems) {

        Cursor cursor = null;
        //context.getContentResolver().query()

        switch (selector) {
            case NONE: {
                cursor = mContext.getContentResolver()
                        .query(MediaEntry.CONTENT_URI,
                                SONGS_QUEUE_CURSOR_COLUMNS,
                                null,
                                null,
                                MediaProvider.mSongsSortOrder);
                break;
            }
            case ALBUM: {
                cursor = mContext.getContentResolver()
                        .query(MediaEntry.CONTENT_URI,
                                SONGS_QUEUE_CURSOR_COLUMNS,
                                MediaEntry.COLUMN_ALBUM
                                        + " IN ("
                                        + getStringFromSelectorItems(selectorItems)
                                        + ")",
                                null,
                                MediaProvider.mSongsSortOrder);
                break;
            }
            case ARTIST: {
                cursor = mContext.getContentResolver()
                        .query(MediaEntry.CONTENT_URI,
                                SONGS_QUEUE_CURSOR_COLUMNS,
                                MediaEntry.COLUMN_ARTIST
                                        + " IN ("
                                        + getStringFromSelectorItems(selectorItems)
                                        + ")",
                                null,
                                MediaProvider.mSongsSortOrder);
                break;
            }
            case PATH: {
                cursor = mContext.getContentResolver()
                        .query(MediaEntry.CONTENT_URI,
                                SONGS_QUEUE_CURSOR_COLUMNS,
                                MediaEntry.COLUMN_PATH
                                        + " IN ("
                                        + getStringFromSelectorItems(selectorItems)
                                        + ")",
                                null,
                                MediaProvider.mSongsSortOrder);
                break;
            }
            case PLAYLIST: {
                cursor = mContext.getContentResolver()
                        .query(MediaEntry.buildSongByPlaylistUri(),
                                SONGS_FOR_PLAYLISTS_QUEUE_CURSOR_COLUMNS,
                                PlaylistsEntry.COLUMN_PLAYLIST_NAME
                                        + " IN ("
                                        + getStringFromSelectorItems(selectorItems)
                                        + ")",
                                null,
                                MediaProvider.mSongsSortOrder);
                break;
            }
        }
        List<Audio> audioList;

        if (cursor != null && cursor.moveToFirst()) {
            audioList = new ArrayList<>(cursor.getCount());

            do {
                Audio audio = new Audio(
                        cursor.getString(COLUMN_SONG_ID),
                        cursor.getString(COLUMN_INDEX_PATH),
                        cursor.getString(COLUMN_INDEX_TITLE),
                        cursor.getString(COLUMN_INDEX_ALBUM),
                        cursor.getString(COLUMN_INDEX_ARTIST),
                        cursor.getString(COLUMN_INDEX_ALBUM_ART),
                        Long.parseLong(cursor.getString(COLUMN_INDEX_DURATION)));
                if (!audioList.contains(audio))
                    audioList.add(audio);
            }
            while (cursor.moveToNext());

        } else {
            return new ArrayList<>();
        }

        shuffleSongQueue(audioList);
        return audioList;
    }

    private void shuffleSongQueue(List<Audio> audioList) {
        //TODO implement custom shuffle algo
        List<Audio> likedSongsList = getLikedSongs();
        List<Audio> dislikedSongsList = getDislikedSongs();
        Collections.shuffle(audioList);
    }

    private List<Audio> getLikedSongs() {
        Cursor cursor = mContext.getContentResolver()
                .query(MediaEntry.buildSongByPlaylistUri(),
                        SONGS_QUEUE_CURSOR_COLUMNS,
                        PlaylistsEntry.COLUMN_PLAYLIST_NAME
                                + " = ? ",
                        new String[]{PlAYLIST_LIKED},
                        MediaProvider.mSongsSortOrder);

        List<Audio> audioList;

        if (cursor != null && cursor.moveToFirst()) {
            audioList = new ArrayList<>(cursor.getCount());

            do {
                audioList.add(new Audio(
                        cursor.getString(COLUMN_SONG_ID),
                        cursor.getString(COLUMN_INDEX_PATH),
                        cursor.getString(COLUMN_INDEX_TITLE),
                        cursor.getString(COLUMN_INDEX_ALBUM),
                        cursor.getString(COLUMN_INDEX_ARTIST),
                        cursor.getString(COLUMN_INDEX_ALBUM_ART),
                        Long.parseLong(cursor.getString(COLUMN_INDEX_DURATION))
                ));
            }
            while (cursor.moveToNext());

        } else {
            return new ArrayList<Audio>();
        }

        return audioList;
    }

    private List<Audio> getDislikedSongs() {
        Cursor cursor = mContext.getContentResolver()
                .query(MediaEntry.buildSongByPlaylistUri(),
                        SONGS_QUEUE_CURSOR_COLUMNS,
                        PlaylistsEntry.COLUMN_PLAYLIST_NAME
                                + " = ? ",
                        new String[]{PlAYLIST_DISLIKED},
                        MediaProvider.mSongsSortOrder);

        List<Audio> audioList;

        if (cursor != null && cursor.moveToFirst()) {
            audioList = new ArrayList<>(cursor.getCount());

            do {
                audioList.add(new Audio(
                        cursor.getString(COLUMN_SONG_ID),
                        cursor.getString(COLUMN_INDEX_PATH),
                        cursor.getString(COLUMN_INDEX_TITLE),
                        cursor.getString(COLUMN_INDEX_ALBUM),
                        cursor.getString(COLUMN_INDEX_ARTIST),
                        cursor.getString(COLUMN_INDEX_ALBUM_ART),
                        Long.parseLong(cursor.getString(COLUMN_INDEX_DURATION))
                ));
            }
            while (cursor.moveToNext());

        } else {
            return new ArrayList<>();
        }

        return audioList;
    }

    public enum MediaSelectorType {
        NONE, ALBUM, ARTIST, PLAYLIST, PATH
    }
}
