package com.inpen.shuffle.mainscreen;

import android.content.Context;

import com.inpen.shuffle.model.database.MediaContract;
import com.inpen.shuffle.utils.CustomTypes;

import java.util.List;

/**
 * Created by Abhishek on 10/25/2016.
 */

public interface MainScreenContract {

    interface MainView {
        boolean hasPermissions();

        void getPermissions();

        void onFabStateChanged(CustomTypes.MainFabState state);
    }

    interface ActivityActionsListener {

        void scanMedia(Context context);

        void shuffleAndPlay(Context context);

        void init(Context context);

    }

    interface ItemsView {

        void setProgressIndicator(boolean active);

        void showItems(List<Item> itemList);

        void clearSelection();

        void selectItems(List<Item> selectedItemList);
    }

    interface ItemsFragmentListener {


        String[] ALBUMS_QUERY_CURSOR_COLUMNS = {
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM_KEY,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM_ART
        };

        String[] ARTISTS_QUERY_CURSOR_COLUMNS = {
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ARTIST_KEY,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ARTIST,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM_ART
        };

        String[] FOLDERS_QUERY_CURSOR_COLUMNS = {
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_FOLDER_PATH,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM_ART
        };

        String[] PLAYLISTS_QUERY_CURSOR_COLUMNS = {
                MediaContract.PlaylistsEntry.TABLE_NAME + "." + MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM_ART
        };

        void initialize();

        void openItemDetail(String itemId);

        void setItemSelected(Item item, boolean selectState);

        void setAllItemsSelected(List<Item> itemList, boolean selectState);

    }

}
