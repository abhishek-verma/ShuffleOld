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
        LoaderManager.LoaderCallbacks<Cursor>, SelectedItemsRepository.ItemTypeObserver {

    private LoaderManager mLoaderManager;
    private MainScreenContract.ItemsView mItemsView;
    private Context mContext;
    private CustomTypes.ItemType mItemType;
    private SelectedItemsRepository mSelectedItemsRepository;
    private boolean isActive = false;

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
        mSelectedItemsRepository = SelectedItemsRepository.getInstance();
        mSelectedItemsRepository.addItemTypeObserver(this);
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        mLoaderManager.initLoader(0, null, this);
    }

    @Override
    public void openItemDetail(String itemId) {
        // open a bottom sheet with details
    }

    @Override
    public void setItemSelected(Item item, boolean selectState) {

        checkAndSetRepositoryType();

        // call the selecteditemrepo select add item or remove item depending on isselected
        if (selectState) {
            mSelectedItemsRepository.addItem(item);
        } else
            mSelectedItemsRepository.removeItem(item);
    }

    @Override
    public void setAllItemsSelected(List<Item> itemList, boolean selectState) {

        checkAndSetRepositoryType();

        if (selectState) {
            mSelectedItemsRepository.addItems(itemList);
        } else {
            mSelectedItemsRepository.clearData(mContext);
        }
    }

    public void checkAndSetRepositoryType() {
        if (mSelectedItemsRepository.getmItemType() != null && !mSelectedItemsRepository.getmItemType().equals(mItemType)) {
            mSelectedItemsRepository.clearData(mContext);
            mSelectedItemsRepository.setItemType(mItemType);
        } else if (mSelectedItemsRepository.getmItemType() == null) {
            mSelectedItemsRepository.setItemType(mItemType);
        }

        isActive = true;
    }

    // This method is used inside method onLoadFinished to check
    // add items To a list also supplied as parameters
    private final void addItem(List<Item> itemList, Item item) {

        if (!itemList.contains(item)) {
            itemList.add(item);
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

        if (data == null || !data.moveToFirst()) {
            mItemsView.showItems(new ArrayList<Item>());
            return;
        }

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
                    addItem(itemList, item);
                } while (data.moveToNext());
                break;
            case ARTIST_ID:
                do {
                    Item item = new Item(
                            data.getString(0),//artist id
                            data.getString(1),//artist title
                            data.getString(2),//Album art
                            false);
                    addItem(itemList, item);
                } while (data.moveToNext());
                break;
            case FOLDER:
                do {
                    Item item = new Item(
                            data.getString(0),//folder path
                            MediaContract.MediaEntry.getSongFolderFromFolderPath(data.getString(0)),//folder name from folder path
                            data.getString(1),//Album art
                            false);
                    addItem(itemList, item);
                } while (data.moveToNext());
                break;
            case PLAYLIST:
                do {
                    Item item = new Item(
                            data.getString(0),//playlist name
                            data.getString(0),//playlist name
                            data.getString(1),//Album art
                            false);
                    addItem(itemList, item);
                } while (data.moveToNext());
                break;
        }

        mItemsView.showItems(itemList);

        if (mSelectedItemsRepository.mItemType != null
                && mSelectedItemsRepository.getmItemType().equals(mItemType)) {
            mItemsView.selectItems(mSelectedItemsRepository.getmSelectedItemList());
        }

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public void onItemTypeChanged(CustomTypes.ItemType itemType) {
        if (!itemType.equals(mItemType)) {
            if (isActive) {
                isActive = false;
                mItemsView.clearSelection();
            }
        }
    }

}
