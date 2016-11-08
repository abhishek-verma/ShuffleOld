package com.inpen.shuffle.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inpen.shuffle.mainscreen.Item;
import com.inpen.shuffle.utils.CustomTypes;
import com.inpen.shuffle.utils.LogHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhishek on 10/31/2016.
 */

public class SelectedItemsRepository {

    public static final String UI_STORAGE = "com.inpen.shuffle.UI_STORAGE";
    private static final String LOG_TAG = LogHelper.makeLogTag(SelectedItemsRepository.class);
    private static final String KEY_SELECTED_LIST = "selected_list";
    private static final String KEY_SELECTED_TYPE = "selected_type";

    private static SelectedItemsRepository mSelectedItemsRepositoryInstance;
    public CustomTypes.ItemType mItemType;
    private SharedPreferences mPreferences;
    private List<Item> mSelectedItemList = new ArrayList<>();
    private List<ItemTypeObserver> mItemTypeObserverList;
    private List<IsRepositoryEmptyObserver> mIsRepositoryEmptyObserverList;

    public static SelectedItemsRepository getInstance() {

        LogHelper.v(LOG_TAG, "getInstance()");

        if (mSelectedItemsRepositoryInstance == null) {
            LogHelper.v(LOG_TAG, "new instance created");
            mSelectedItemsRepositoryInstance = new SelectedItemsRepository();
        }
        return mSelectedItemsRepositoryInstance;
    }


    public List<Item> getmSelectedItemList() {
        return mSelectedItemList;
    }

    public CustomTypes.ItemType getmItemType() {
        return mItemType;
    }

    public void loadData(Context context) {

        Gson gson = new Gson();
        String json = getmPreferences(context).getString(KEY_SELECTED_LIST, null);

        Type type = new TypeToken<ArrayList<Audio>>() {
        }.getType();

        mSelectedItemList = gson.fromJson(json, type);

        mItemType = CustomTypes.ItemType.fromInt(getmPreferences(context).getInt(KEY_SELECTED_TYPE, -1));

        if (mSelectedItemList == null) {
            mSelectedItemList = new ArrayList<>();
            mItemType = CustomTypes.ItemType.ALBUM_ID;
        }

    }

    public void saveData(Context context) {

        SharedPreferences.Editor editor = getmPreferences(context).edit();
        Gson gson = new Gson();
        String json = gson.toJson(mSelectedItemList);
        editor.putString(KEY_SELECTED_LIST, json);

        editor.putInt(KEY_SELECTED_TYPE, CustomTypes.ItemType.toInt(mItemType));

        editor.apply();
    }

    public void clearData(Context context) {
        LogHelper.v(LOG_TAG, "clearData()");
        mSelectedItemList.clear();

        notifyIsRepositoryEmptyObservers(true);

        getmPreferences(context).edit().clear().apply();

        LogHelper.v(LOG_TAG, "mSelectedITemLIst.size() : " + mSelectedItemList.size());
    }

    public void setItemType(CustomTypes.ItemType itemType) {
        LogHelper.v(LOG_TAG, "setItemType( " + itemType.toString() + " )");
        mItemType = itemType;

        notifyItemTypeChangedObservers();
    }

    public void addItems(List<Item> items) {
        LogHelper.v(LOG_TAG, "addItems( " + items.toString() + " )");

        if (mSelectedItemList.size() == 0) {
            LogHelper.d("SelectedItemRepository 1st item being inserted of type: " + mItemType);
            notifyIsRepositoryEmptyObservers(false);
        }

        for (Item item : items) {
            if (!mSelectedItemList.contains(item))
                mSelectedItemList.add(item);
        }

        LogHelper.v(LOG_TAG, "mSelectedITemLIst.size() : " + mSelectedItemList.size());
    }

    public void addItem(Item item) {
        LogHelper.v(LOG_TAG, "addItem( " + item.toString() + " )");
        if (mSelectedItemList.size() == 0) {
            LogHelper.d("SelectedItemRepository 1st item being inserted of type: " + mItemType);
            notifyIsRepositoryEmptyObservers(false);
        }

        mSelectedItemList.add(item);

        LogHelper.v(LOG_TAG, "mSelectedITemLIst.size() : " + mSelectedItemList.size());
    }

    public void removeItem(Item item) {
        LogHelper.v(LOG_TAG, "removeItem( " + item.toString() + " )");
        mSelectedItemList.remove(item);

        if (mSelectedItemList.size() == 0) {
            notifyIsRepositoryEmptyObservers(true);
        }

        LogHelper.v(LOG_TAG, "mSelectedITemLIst.size() : " + mSelectedItemList.size());
    }

    private SharedPreferences getmPreferences(Context context) {

        if (mPreferences == null)
            mPreferences = context.getSharedPreferences(UI_STORAGE, Context.MODE_PRIVATE);

        return mPreferences;
    }

    private void notifyItemTypeChangedObservers() {
        if (mItemTypeObserverList == null)
            return;

        for (ItemTypeObserver observer : mItemTypeObserverList) {
            if (observer != null)
                observer.onItemTypeChanged(mItemType);
        }
    }

    private void notifyIsRepositoryEmptyObservers(boolean isEmpty) {
        LogHelper.d("notifyIsRepositoryEmptyObservers isEmpty: " + isEmpty);
        if (mIsRepositoryEmptyObserverList == null)
            return;

        for (IsRepositoryEmptyObserver observer : mIsRepositoryEmptyObserverList) {
            if (observer != null) {
                observer.onEmptyStateChanged(isEmpty);
            }
        }
    }

    public void addItemTypeObserver(ItemTypeObserver observer) {
        if (mItemTypeObserverList == null)
            mItemTypeObserverList = new ArrayList<>();

        if (!mItemTypeObserverList.contains(observer))
            mItemTypeObserverList.add(observer);
    }

    public void addIsRepositoryEmptyObserver(IsRepositoryEmptyObserver observer) {
        if (mIsRepositoryEmptyObserverList == null)
            mIsRepositoryEmptyObserverList = new ArrayList<>();

        if (!mIsRepositoryEmptyObserverList.contains(observer))
            mIsRepositoryEmptyObserverList.add(observer);
    }

    public interface ItemTypeObserver {
        void onItemTypeChanged(CustomTypes.ItemType itemType);

    }

    public interface IsRepositoryEmptyObserver {
        void onEmptyStateChanged(boolean isEmpty);
    }
}
