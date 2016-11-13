package com.inpen.shuffle.playerscreen;

import android.content.Context;

import com.inpen.shuffle.model.AudioItem;
import com.inpen.shuffle.model.QueueRepository;
import com.inpen.shuffle.utils.CustomTypes;

/**
 * Created by Abhishek on 11/13/2016.
 */

public class SuggestionFragmentPresenter implements PlayerScreenContract.SuggestionViewActionsListener {

    private QueueRepository mQueueRepository;
    private PlayerScreenContract.SuggestionsView mView;

    @Override
    public void initialize(Context context, PlayerScreenContract.SuggestionsView view) {

        mView = view;
        mQueueRepository = QueueRepository.getInstance();

        if (mQueueRepository.getState().equals(CustomTypes.RepositoryState.INITIALIZED)) {
            updateViews();
        } else {
            mQueueRepository.loadQueue(context, new QueueRepository.CachedQueueLoadedCallback() {
                @Override
                public void onCachedQueueLoaded() {
                    updateViews();
                }
            });
        }
    }

    private void updateViews() {
        mView.showData(mQueueRepository.mPlayingQueue);
    }

    @Override
    public void onItemClicked(AudioItem item) {
        mQueueRepository.setCurrentQueueItem(item);
    }
}
