package com.inpen.shuffle.playerscreen;

import android.content.Context;

import com.inpen.shuffle.utils.CustomTypes;

/**
 * Created by Abhishek on 11/8/2016.
 */

public interface PlayerScreenContract {

    interface PlayerActivityView {

    }

    interface ActivityActionsListener {

    }

    interface PlayerView {

        void showData(PlayerAudioItemEnvelop playerAudioItemEnvelop);

        CustomTypes.PlayerViewState getPlayerState();

        boolean getIsNewLaunch();

        void setState(CustomTypes.PlayerViewState playerViewState);

        Context getActivityContext();
    }

    interface PlayerActionsListener {

        void updateViews();

        void play();

        void pause();

        void seekTo(int position);

        void playNext();

        void playPrevious();

        void onScreenClicked();

        void onLiked();

        void onDisliked();

        void onStop();

    }

    interface SuggestionsView {

    }

    interface SuggestionViewActionsListener {

    }
}
