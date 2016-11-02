package com.inpen.shuffle.mainscreen;

import com.inpen.shuffle.model.database.MediaContract;

import java.util.List;

/**
 * Created by Abhishek on 10/25/2016.
 */

public interface MainScreenContract {

    interface ActivityActionsListener {

        void scanMedia();

        void shuffleAndPlay(List<String> ItemIdList);

    }

    interface ItemsView {

        void setProgressIndicator(boolean active);

        void showItems(List<Item> itemList);

    }

    interface ItemsFragmentListener {


        String[] ALBUMS_QUERY_CURSOR_COLUMNS = {
                "DISTINCT " + MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM_ID,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM_ART
        };

        String[] ARTISTS_QUERY_CURSOR_COLUMNS = {
                "DISTINCT " + MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ARTIST_ID,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ARTIST,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM_ART
        };

        String[] FOLDERS_QUERY_CURSOR_COLUMNS = {
                "DISTINCT " + MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_PATH,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_FOLDER,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM_ART
        };

        String[] PLAYLISTS_QUERY_CURSOR_COLUMNS = {
                "DISTINCT " + MediaContract.PlaylistsEntry.TABLE_NAME + "." + MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM_ART
        };

        void initialize();

        void openItemDetail(String itemId);

        void setItemSelected(Item item, boolean selectState);

        void setAllItemsSelected(List<Item> itemList, boolean selectState);

    }

}
