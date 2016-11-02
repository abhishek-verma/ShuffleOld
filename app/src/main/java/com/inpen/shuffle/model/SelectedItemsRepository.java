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


    private static final String TAG = LogHelper.makeLogTag(QueueRepository.class);
    private static final String KEY_SELECTED_LIST = "selected_list";
    private static final String KEY_SELECTED_TYPE = "selected_type";

    private static SelectedItemsRepository mSelectedItemsRepositoryInstance;
    public CustomTypes.ItemType mItemType;
    private SharedPreferences mPreferences;
    private List<Item> mSelectedItemList = new ArrayList<>();

    public static SelectedItemsRepository getInstance() {
        if (mSelectedItemsRepositoryInstance == null) {
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
        mSelectedItemList.clear();
        getmPreferences(context).edit().clear().apply();
    }

    public void setItemType(CustomTypes.ItemType itemType) {
        mItemType = itemType;
    }

    public void addItems(List<Item> items) {

        for (Item item : items) {
            mSelectedItemList.add(item);
        }

    }

    public void addItem(Item item) {
        mSelectedItemList.add(item);
    }

    public void removeItem(Item item) {
        mSelectedItemList.remove(item);
    }

    public List<Item> getSelectedItemList() {
        return mSelectedItemList;
    }

    private SharedPreferences getmPreferences(Context context) {

        if (mPreferences == null)
            mPreferences = context.getSharedPreferences(UI_STORAGE, Context.MODE_PRIVATE);

        return mPreferences;
    }
}
