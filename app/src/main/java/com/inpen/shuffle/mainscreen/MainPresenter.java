package com.inpen.shuffle.mainscreen;

import android.content.Context;
import android.support.annotation.NonNull;

import com.inpen.shuffle.model.QueueRepository;
import com.inpen.shuffle.model.SelectedItemsRepository;
import com.inpen.shuffle.syncmedia.SyncMediaIntentService;
import com.inpen.shuffle.utils.CustomTypes;

import java.util.ArrayList;
import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Abhishek on 10/27/2016.
 */

public class MainPresenter implements MainScreenContract.ActivityActionsListener, SelectedItemsRepository.IsRepositoryEmptyObserver {

    private MainScreenContract.MainView mMainView;
    private SelectedItemsRepository mSelectedItemsRepository;


    public MainPresenter(@NonNull MainScreenContract.MainView mainView) {
        mMainView = checkNotNull(mainView);
    }

    public void init(Context context) {
        mSelectedItemsRepository = SelectedItemsRepository.getInstance();
        mSelectedItemsRepository.addIsRepositoryEmptyObserver(this);


        if (mSelectedItemsRepository.getmSelectedItemList().size() != 0) {
            mMainView.onFabStateChanged(CustomTypes.MainFabState.SHUFFLE);
        } else if (false) {
            //TODO if player service active/running set state as player
            mMainView.onFabStateChanged(CustomTypes.MainFabState.PLAYER);
        } else {
            mMainView.onFabStateChanged(CustomTypes.MainFabState.HIDDEN);
        }


    }


    @Override
    public void scanMedia(Context context) {

        if (mMainView.hasPermissions())
            SyncMediaIntentService.syncMedia(context);
        else
            mMainView.getPermissions();
    }


    @Override
    public void shuffleAndPlay(Context context) {

        // Getting list of selected items ids as String
        List<String> selectorItems = new ArrayList<>();

        for (Item item : mSelectedItemsRepository.getmSelectedItemList()) {
            selectorItems.add(item.getId());
        }

        // initialize queueRepo
        QueueRepository queueRepository = QueueRepository.getInstance();
        queueRepository.initializeQueue(mSelectedItemsRepository.getmItemType(),
                selectorItems, context);

        // launch player activity on callback

        // start service

    }

    @Override
    public void onEmptyStateChanged(boolean isEmpty) {
        if (!isEmpty)
            mMainView.onFabStateChanged(CustomTypes.MainFabState.SHUFFLE);
        else
            mMainView.onFabStateChanged(CustomTypes.MainFabState.HIDDEN);

    }
}
