package com.inpen.shuffle.mainscreen;

import java.util.List;

/**
 * Created by Abhishek on 10/27/2016.
 */

public class ItemsPresenter implements MainScreenContract.ItemsFragmentListener {
    @Override
    public void initialize() {
        // get a loader
    }

    @Override
    public void shuffleAndPlay(List<String> ItemIdList) {
        // initialize queueRepo
        // launch player activity on callback
        // start service
        //
    }

    @Override
    public void openItemDetail(String itemId) {
        // open a bottom sheet with details
    }
}
