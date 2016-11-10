package com.inpen.shuffle.playerscreen;

import com.inpen.shuffle.model.AudioItem;

/**
 * Created by Abhishek on 11/8/2016.
 */

public class PlayerAudioItemEnvelop {
    public AudioItem mAudioItem;
    public boolean isLiked;

    public boolean isDisliked;

    public PlayerAudioItemEnvelop(AudioItem mAudioItem) {
        this.mAudioItem = mAudioItem;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public void setDisliked(boolean disliked) {
        isDisliked = disliked;
    }

}

