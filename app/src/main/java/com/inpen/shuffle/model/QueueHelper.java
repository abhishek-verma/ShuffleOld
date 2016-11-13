package com.inpen.shuffle.model;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;

import com.inpen.shuffle.model.database.MediaContract.MediaEntry;
import com.inpen.shuffle.model.database.MediaContract.PlaylistsEntry;
import com.inpen.shuffle.model.database.MediaProvider;
import com.inpen.shuffle.utils.CustomTypes;
import com.inpen.shuffle.utils.LogHelper;
import com.inpen.shuffle.utils.StaticStrings;

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
    final static double LIKED_PREF = 0.7/*very_often=0.8, often=0.7, normal= 0.6*/,
            NORMAL_PREF = 0.6,
            DISLIKED_PREF = 0.5/*normal=0.6, less_often=0.5, rarely=0.4, never=0*/;
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
    private Context mContext;

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

    public synchronized List<AudioItem> generateQueue(@Nullable CustomTypes.ItemType selector,
                                                      @Nullable List<String> selectorItems) {


        //TODO temp log, remove
        LogHelper.v(LOG_TAG + "temp", "generateQueue( selectorType: " + selector
                + " selectors: " + selectorItems.toString());

        Cursor cursor = null;
        List<AudioItem> audioItemList;
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
                            .query(PlaylistsEntry.CONTENT_URI,
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
                audioItemList = new ArrayList<>(cursor.getCount());

                do {
                    AudioItem audioItem = new AudioItem(
                            cursor.getString(COLUMN_SONG_ID),
                            cursor.getString(COLUMN_INDEX_PATH),
                            cursor.getString(COLUMN_INDEX_TITLE),
                            cursor.getString(COLUMN_INDEX_ALBUM),
                            cursor.getString(COLUMN_INDEX_ARTIST),
                            cursor.getString(COLUMN_INDEX_ALBUM_ART),
                            Long.parseLong(cursor.getString(COLUMN_INDEX_DURATION)));
                    if (!audioItemList.contains(audioItem))
                        audioItemList.add(audioItem);
                }
                while (cursor.moveToNext());

            } else {
                audioItemList = new ArrayList<>();
            }

            shuffleSongQueue(audioItemList);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        //TODO temp log, remove
        LogHelper.v(LOG_TAG + "temp", "curated audioItemList size: " + audioItemList.size() + "\ncontent: " + audioItemList.toString());
        return audioItemList;
    }

    private void shuffleSongQueue(List<AudioItem> audioItemList) {
        //TODO implement custom shuffle algo
        //THAT TAKES INTO ACCOUNT THE LIKED AND DISLIKED SONGS
        /*
        List<AudioItem> likedSongsList = getLikedSongs();
        List<AudioItem> dislikedSongsList = getDislikedSongs();
        */
        Collections.shuffle(audioItemList);
    }

    private List<AudioItem> getLikedSongs() {
        Cursor cursor = mContext.getContentResolver()
                .query(MediaEntry.buildSongByPlaylistUri(StaticStrings.PlAYLIST_NAME_LIKED),
                        SONGS_QUEUE_CURSOR_COLUMNS,
                        null, null,
                        MediaProvider.mSongsSortOrder);

        List<AudioItem> audioItemList;

        if (cursor != null && cursor.moveToFirst()) {
            audioItemList = new ArrayList<>(cursor.getCount());

            do {
                audioItemList.add(new AudioItem(
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
            assert cursor != null;
            cursor.close();
            return new ArrayList<AudioItem>();
        }

        cursor.close();
        return audioItemList;
    }

    private List<AudioItem> getDislikedSongs() {
        Cursor cursor = mContext.getContentResolver()
                .query(MediaEntry.buildSongByPlaylistUri(StaticStrings.PlAYLIST_NAME_DISLIKED),
                        SONGS_QUEUE_CURSOR_COLUMNS,
                        null, null,
                        MediaProvider.mSongsSortOrder);

        List<AudioItem> audioItemList;

        if (cursor != null && cursor.moveToFirst()) {
            audioItemList = new ArrayList<>(cursor.getCount());

            do {
                audioItemList.add(new AudioItem(
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
            if (cursor != null) {
                cursor.close();
            }
            return new ArrayList<>();
        }

        cursor.close();
        return audioItemList;
    }

//
//    private List<AudioItem> weightedShuffle(List<AudioItem> allSongs,
//                                            List<AudioItem> likedSongs,
//                                            List<AudioItem> dislikedSongs) {
//
//        double pref;
//        int i=0;
//        for(i=(int)(allSongs.size()*0.5); i<allSongs.size(); i++){
//
//            if(likedSongs.contains(allSongs.get(i))) pref = LIKED_PREF;
//            else if(dislikedSongs.contains(allSongs.get()))
//
//            double rand = Math.random();
//            if(pref > NORMAL_PREF) {
//                debugPrint("i: " + i);
//                debugPrint("Value of rand: " + rand);
//                if(rand*pref > 0.5) {
//                    //swap to song to somewhere between
//                    //0 to (int)songs.size()*(1-random*pref)
//                    //ie random()*((int) songs.size()*(1-random*pref)
//
//                    int dest = (int)(
//                            Math.random()
//                                    *(songs.size()*(1-rand*pref))
//                    );
//
//                    debugPrint("Dest of the liked song: " + dest);
//
//                    Collections.swap(songs, i, dest);
//                }
//            }
//        }
//
//        for(i=0; i<(int)(songs.size()*0.6); i++){
//            double pref = songs.get(i).pref;
//            double rand = Math.random();
//            if(pref < NORMAL_PREF) {
//                debugPrint("i: " + i);
//                debugPrint("Value of rand: " + rand);
//
//                if(rand*pref < 0.37) {
//                    //swap to song to somewhere between
//                    //(int)songs.size()*(1-random*pref) to songs.size()
//                    //ie (int)songs.size*(1-random*pref + random()*(random*pref))
//
//                    int dest = (int)(
//                            songs.size()
//                                    * (1-rand*pref + Math.random()*(rand*pref))
//                    );
//
//                    debugPrint("Dest of the disliked song: " + dest);
//
//                    Collections.rotate(songs.subList(i, dest+1), -1);
//                    i--;//so that the counter does not skip the next song
//                }
//            }
//        }
//
//        return allSongs;
//    }
}
