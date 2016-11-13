package com.inpen.shuffle.songListScreens;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.inpen.shuffle.model.AudioItem;
import com.inpen.shuffle.model.database.MediaContract;

import java.util.ArrayList;
import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Abhishek on 11/13/2016.
 */

public class SongListPresenter implements SongListScreenContract.SongListViewInteractionListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private LoaderManager mLoaderManager;
    private SongListScreenContract.SongListView mSongListView;
    private Context mContext;
    private String mPlaylistFilter;

    @Override
    public void initialize(@NonNull LoaderManager loaderManager,
                           @NonNull Context context,
                           @NonNull SongListScreenContract.SongListView view,
                           @NonNull String playlistFilter) {
        mLoaderManager = checkNotNull(loaderManager);
        mSongListView = checkNotNull(view);
        mContext = checkNotNull(context);

        mPlaylistFilter = (playlistFilter);

        mLoaderManager.initLoader(0, null, this);
    }

    private void updateViews(List<AudioItem> itemList) {
        mSongListView.showData(itemList);
    }

    @Override
    public void onItemClicked(AudioItem item) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (mPlaylistFilter != null && !mPlaylistFilter.equals("")) {
            return new CursorLoader(mContext,
                    MediaContract.MediaEntry.buildSongByPlaylistUri(mPlaylistFilter),
                    PLAYLIST_CURSOR_COLUMNS,
                    null, null, null);
        } else {
            return new CursorLoader(mContext,
                    MediaContract.MediaEntry.CONTENT_URI,
                    PLAYLIST_CURSOR_COLUMNS,
                    null, null, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data == null || !data.moveToFirst()) {
            updateViews(new ArrayList<AudioItem>());
            return;
        }

        data.moveToFirst();

        List<AudioItem> itemList = new ArrayList<>();

        do {
            AudioItem item = new AudioItem(
                    data.getString(COLUMN_INDEX_SONG_ID),
                    data.getString(COLUMN_INDEX_PATH),
                    data.getString(COLUMN_INDEX_TITLE),
                    data.getString(COLUMN_INDEX_ALBUM),
                    data.getString(COLUMN_INDEX_ARTIST),
                    data.getString(COLUMN_INDEX_ALBUM_ART),
                    Long.parseLong(data.getString(COLUMN_INDEX_DURATION))
            );
            itemList.add(item);
        } while (data.moveToNext());

        updateViews(itemList);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
