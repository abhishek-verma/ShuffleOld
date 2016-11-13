package com.inpen.shuffle.songListScreens;

import android.content.Context;
import android.support.v4.app.LoaderManager;

import com.inpen.shuffle.model.AudioItem;
import com.inpen.shuffle.model.database.MediaContract;

import java.util.List;

/**
 * Created by Abhishek on 11/13/2016.
 */

public interface SongListScreenContract {

    interface SongListView {
        void showData(List<AudioItem> audioItemList);
    }

    interface SongListViewInteractionListener {


        String[] PLAYLIST_CURSOR_COLUMNS = new String[]{
                MediaContract.MediaEntry.COLUMN_SONG_ID,
                MediaContract.MediaEntry.COLUMN_TITLE,
                MediaContract.MediaEntry.COLUMN_ALBUM_ART,
                MediaContract.MediaEntry.COLUMN_DURATION,
                MediaContract.MediaEntry.COLUMN_ALBUM,
                MediaContract.MediaEntry.COLUMN_ARTIST,
                MediaContract.MediaEntry.COLUMN_PATH
        };

        int COLUMN_INDEX_SONG_ID = 0;
        int COLUMN_INDEX_TITLE = 1;
        int COLUMN_INDEX_ALBUM_ART = 2;
        int COLUMN_INDEX_DURATION = 3;
        int COLUMN_INDEX_ALBUM = 4;
        int COLUMN_INDEX_ARTIST = 5;
        int COLUMN_INDEX_PATH = 6;

        void initialize(LoaderManager loaderManager, Context context, SongListView view, String playlistFilter);

        void onItemClicked(AudioItem item);
    }

}
