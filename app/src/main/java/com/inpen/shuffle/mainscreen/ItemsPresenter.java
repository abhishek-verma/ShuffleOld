package com.inpen.shuffle.mainscreen;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.inpen.shuffle.model.SelectedItemsRepository;
import com.inpen.shuffle.model.database.MediaContract;
import com.inpen.shuffle.utils.CustomTypes;

import java.util.ArrayList;
import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Abhishek on 10/27/2016.
 */

public class ItemsPresenter implements
        MainScreenContract.ItemsFragmentListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private LoaderManager mLoaderManager;
    private MainScreenContract.ItemsView mItemsView;
    private Context mContext;
    private CustomTypes.ItemType mItemType;
    private SelectedItemsRepository mSelectedItemsRepository;

    public ItemsPresenter(@NonNull LoaderManager loaderManager,
                          @NonNull Context context,
                          @NonNull MainScreenContract.ItemsView itemsView,
                          @NonNull CustomTypes.ItemType itemType) {
        mLoaderManager = checkNotNull(loaderManager);
        mItemsView = checkNotNull(itemsView);
        mContext = checkNotNull(context);
        mItemType = checkNotNull(itemType);
    }

    @Override
    public void initialize() {
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        mLoaderManager.initLoader(0, null, this);
        mSelectedItemsRepository = SelectedItemsRepository.getInstance();
    }

    @Override
    public void openItemDetail(String itemId) {
        // open a bottom sheet with details
    }

    @Override
    public void setItemSelected(Item item, boolean selectState) {
        if (!mSelectedItemsRepository.getmItemType().equals(mItemType)) {
            mSelectedItemsRepository.clearData(mContext);
            mSelectedItemsRepository.setItemType(mItemType);
        }

        // call the selecteditemrepo select add item or remove item depending on isselected
        if (selectState) {
            mSelectedItemsRepository.addItem(item);
        } else
            mSelectedItemsRepository.removeItem(item);
    }

    @Override
    public void setAllItemsSelected(List<Item> itemList, boolean selectState) {
        if (!mSelectedItemsRepository.getmItemType().equals(mItemType)) {
            mSelectedItemsRepository.clearData(mContext);
            mSelectedItemsRepository.setItemType(mItemType);
        }

        if (selectState) {
            mSelectedItemsRepository.clearData(mContext);
            mSelectedItemsRepository.addItems(itemList);
        } else {
            mSelectedItemsRepository.clearData(mContext);
        }
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        mItemsView.setProgressIndicator(true);

        switch (mItemType) {
            case ALBUM_ID:
                return new CursorLoader(mContext,
                        MediaContract.MediaEntry.CONTENT_URI,
                        ALBUMS_QUERY_CURSOR_COLUMNS,
                        null, null, null);
            case ARTIST_ID:
                return new CursorLoader(mContext,
                        MediaContract.MediaEntry.CONTENT_URI,
                        ARTISTS_QUERY_CURSOR_COLUMNS,
                        null, null, null);
            case FOLDER:
                return new CursorLoader(mContext,
                        MediaContract.MediaEntry.CONTENT_URI,
                        FOLDERS_QUERY_CURSOR_COLUMNS,
                        null, null, null);
            case PLAYLIST:
                return new CursorLoader(mContext,
                        MediaContract.PlaylistsEntry.CONTENT_URI,
                        PLAYLISTS_QUERY_CURSOR_COLUMNS,
                        null, null, null);

        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data == null || !data.moveToFirst())
            return;

        data.moveToFirst();

        List<Item> itemList = new ArrayList<>();

        itemList.add(new Item(Item.SELECT_ALL_ITEM_VIEW_ID,
                Item.SELECT_ALL_ITEM_VIEW_TITLE,
                Item.SELECT_ALL_ITEM_VIEW_IMAGEPATH,
                false));

        switch (mItemType) {
            case ALBUM_ID:
                do {
                    Item item = new Item(
                            data.getString(0),//album id
                            data.getString(1),//album title
                            data.getString(2),//Album art
                            false);
                    itemList.add(item);
                } while (data.moveToNext());
                break;
            case ARTIST_ID:
                do {
                    Item item = new Item(
                            data.getString(0),//artist id
                            data.getString(1),//artist title
                            data.getString(2),//Album art
                            false);
                    itemList.add(item);
                } while (data.moveToNext());
                break;
            case FOLDER:
                do {
                    Item item = new Item(
                            data.getString(0),//folder path
                            data.getString(1),//folder name
                            data.getString(2),//Album art
                            false);
                    itemList.add(item);
                } while (data.moveToNext());
                break;
            case PLAYLIST:
                do {
                    Item item = new Item(
                            data.getString(0),//playlist name
                            data.getString(0),//playlist name
                            data.getString(1),//Album art
                            false);
                    itemList.add(item);
                } while (data.moveToNext());
                break;
        }

        mItemsView.showItems(itemList);
        data.close();
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

}
