package com.inpen.shuffle.mainscreen;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.inpen.shuffle.model.QueueRepository;
import com.inpen.shuffle.model.SelectedItemsRepository;
import com.inpen.shuffle.playback.MusicService;
import com.inpen.shuffle.playerscreen.PlayerActivity;
import com.inpen.shuffle.syncmedia.SyncMediaIntentService;
import com.inpen.shuffle.utils.CustomTypes;
import com.inpen.shuffle.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Abhishek on 10/27/2016.
 */

public class MainPresenter implements MainScreenContract.ActivityActionsListener, SelectedItemsRepository.IsRepositoryEmptyObserver {

    private static final String LOG_TAG = LogHelper.makeLogTag(MainPresenter.class);
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
    public void shuffleAndPlay(final Context context) {

        // Getting list of selected items ids as String
        List<String> selectorItems = new ArrayList<>();

        for (Item item : mSelectedItemsRepository.getmSelectedItemList()) {
            selectorItems.add(item.getId());
        }

        // updateViews queueRepo
        QueueRepository queueRepository = QueueRepository.getInstance();
        queueRepository.initializeQueue(mSelectedItemsRepository.getmItemType(),
                selectorItems, context, new QueueRepository.QueueRepositoryInitializedCallback() {
                    @Override
                    public void onPlayingQueueReady(boolean success) {

                        if (success) {
                            context.stopService(new Intent(context, MusicService.class));

                            LogHelper.v(LOG_TAG, "onPlayingQueueReady! starting player activity.");
                            //Starting player activity
                            Intent playerActivityIntent = new Intent(context, PlayerActivity.class);
                            playerActivityIntent.putExtra(PlayerActivity.EXTRA_BOOLEAN_NEW_LAUNCH, true);
                            context.startActivity(playerActivityIntent);
                        } else {
                            LogHelper.e(LOG_TAG, "Something wrong happened while curating queue!");
                        }
                    }
                });

    }

    @Override
    public void onEmptyStateChanged(boolean isEmpty) {
        if (!isEmpty)
            mMainView.onFabStateChanged(CustomTypes.MainFabState.SHUFFLE);
        else
            mMainView.onFabStateChanged(CustomTypes.MainFabState.HIDDEN);

    }
}
