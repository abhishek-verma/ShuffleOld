package com.inpen.shuffle.mainscreen;

/**
 * Created by Abhishek on 10/27/2016.
 */

public class Item {

    public static String SELECT_ALL_ITEM_VIEW_ID = "select_all";
    public static String SELECT_ALL_ITEM_VIEW_TITLE = "SELECT ALL";
    public static String TOGGLE_ALL_ITEM_VIEW_TITLE = "TOGGLE ALL";//TODO change title to this if first item is selected
    public static String SELECT_ALL_ITEM_VIEW_IMAGEPATH = "";

    private String mId;
    private String mImagePath;
    private String mTitle;

    private boolean mIsSelected = false;

    public Item(String id, String title, String imagePath, boolean isSelected) {
        mId = id;
        mTitle = title;
        mImagePath = imagePath;
        mIsSelected = isSelected;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public void setImagePath(String imagePath) {
        this.mImagePath = imagePath;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setSelected(boolean selected) {
        mIsSelected = selected;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Item) {
            Item item = (Item) obj;

            return item.getId().equals(mId)
                    && item.getTitle().equals(mTitle);
        }
        return super.equals(obj);
    }
}
