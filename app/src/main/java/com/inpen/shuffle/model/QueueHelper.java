package com.inpen.shuffle.model;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;

import com.inpen.shuffle.model.database.MediaContract.MediaEntry;
import com.inpen.shuffle.model.database.MediaContract.PlaylistsEntry;
import com.inpen.shuffle.model.database.MediaProvider;
import com.inpen.shuffle.utils.CustomTypes;
import com.inpen.shuffle.utils.LogHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueueHelper {

    public static final int COLUMN_INDEX_TITLE = 0;
    public static final int COLUMN_INDEX_ALBUM = 1;
    public static final int COLUMN_INDEX_ARTIST = 2;
    public static final int COLUMN_INDEX_ALBUM_ART = 3;
    public static final int COLUMN_INDEX_DURATION = 4;
    public static final int COLUMN_INDEX_PATH = 5;
    public static final int COLUMN_SONG_ID = 6;
    public static final int COLUMN_INDEX_ALBUM_ID = 7;
    public static final int COLUMN_INDEX_ARTIST_ID = 8;
    public static final int COLUMN_PLAYLIST_NAME = 7;//coz we dont use album_id and artist_id columns for songs_for_playlist
    private static final String LOG_TAG = LogHelper.makeLogTag(QueueHelper.class);
    private static final String[] SONGS_QUEUE_CURSOR_COLUMNS = {
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_TITLE,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_ALBUM,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_ARTIST,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_ALBUM_ART,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_DURATION,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_PATH,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_SONG_ID,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_ALBUM_KEY,
            MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_ARTIST_KEY
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

    private static String getStringFromSelectorItems(List<String> selectors) {
        StringBuffer s = new StringBuffer();


        for (int i = 0; i < selectors.size(); i++) {
            if (i != 0) s.append(",");

            s.append('"').append(selectors.get(i)).append('"');
        }

        return s.toString();
    }

    public synchronized List<Audio> generateQueue(@Nullable CustomTypes.ItemType selector,
                                                  @Nullable List<String> selectorItems) {


        //TODO temp log, remove
        LogHelper.v(LOG_TAG + "temp", "generateQueue( selectorType: " + selector
                + " selectors: " + selectorItems.toString());

        Cursor cursor = null;
        List<Audio> audioList;
        //context.getContentResolver().query()

        try {
            switch (selector) {
                case ALBUM_ID: {
                    cursor = mContext.getContentResolver()
                            .query(MediaEntry.CONTENT_URI,
                                    SONGS_QUEUE_CURSOR_COLUMNS,
                                    MediaEntry.COLUMN_ALBUM_KEY
                                            + " IN ("
                                            + getStringFromSelectorItems(selectorItems)
                                            + ")",
                                    null,
                                    MediaProvider.mSongsSortOrder);
                    break;
                }
                case ARTIST_ID: {
                    cursor = mContext.getContentResolver()
                            .query(MediaEntry.CONTENT_URI,
                                    SONGS_QUEUE_CURSOR_COLUMNS,
                                    MediaEntry.COLUMN_ARTIST_KEY
                                            + " IN ("
                                            + getStringFromSelectorItems(selectorItems)
                                            + ")",
                                    null,
                                    MediaProvider.mSongsSortOrder);
                    break;
                }
                case FOLDER: {
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
//            default: {
//                // shuffle all songs
//                cursor = mContext.getContentResolver()
//                        .query(MediaEntry.CONTENT_URI,
//                                SONGS_QUEUE_CURSOR_COLUMNS,
//                                null,
//                                null,
//                                MediaProvider.mSongsSortOrder);
//                break;
//            }
            }

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
                audioList = new ArrayList<>();
            }

            shuffleSongQueue(audioList);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        //TODO temp log, remove
        LogHelper.v(LOG_TAG + "temp", "curated audioList size: " + audioList.size() + "\ncontent: " + audioList.toString());
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

}
