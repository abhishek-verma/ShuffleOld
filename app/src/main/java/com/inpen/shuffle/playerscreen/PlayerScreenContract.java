package com.inpen.shuffle.playerscreen;

import android.content.Context;

import com.inpen.shuffle.model.AudioItem;
import com.inpen.shuffle.utils.CustomTypes;

import java.util.List;

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

        void showLiked(boolean enabled);

        void showDisliked(boolean enabled);

        void setSeekBarProgress(int progress);
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

        void scheduleSeekBarUpdate(boolean enable);
    }

    interface SuggestionsView {

        void showData(List<AudioItem> audioItemList);

        Context getActivityContext();

    }

    interface SuggestionViewActionsListener {

        void initialize(Context context, SuggestionsView view);

        void onItemClicked(AudioItem item);
    }
}
