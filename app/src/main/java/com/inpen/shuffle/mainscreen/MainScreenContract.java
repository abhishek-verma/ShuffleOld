package com.inpen.shuffle.mainscreen;

import java.util.List;

/**
 * Created by Abhishek on 10/25/2016.
 */

public interface MainScreenContract {

    interface ViewItems {

        void setProgressIndicator(boolean active);

        void showItems(List<Item> itemList);

        enum Item {
            ALBUM_ID, ARTIST_ID, PLAYLIST, FOLDER
        }

    }


    interface ActivityActionsListener {

        void scanMedia();

        void shuffleAndPlay(List<String> ItemIdList);

    }

    interface ItemsFragmentListener {

        void initialize();

        void openItemDetail(String itemId);

    }

}
