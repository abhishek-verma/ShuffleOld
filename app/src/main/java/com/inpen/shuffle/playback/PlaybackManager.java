/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.inpen.shuffle.playback;

import android.content.Context;
import android.content.res.Resources;
import android.os.SystemClock;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.inpen.shuffle.model.Audio;
import com.inpen.shuffle.model.QueueRepository;
import com.inpen.shuffle.utils.LogHelper;

// The class is kept for future implementations
/**
 * Manage the interactions among the container service, the queue manager and the actual playback.
 */
public class PlaybackManager implements Playback.Callback {

    private static final String TAG = LogHelper.makeLogTag(PlaybackManager.class);
    // Action to thumbs up a media item
    private static final String CUSTOM_ACTION_THUMBS_UP = "com.inpen.shuffle.THUMBS_UP";

    private QueueRepository mQueueRepository;
    private Resources mResources;
    private Playback mPlayback;
    private PlaybackServiceCallback mServiceCallback;
    private MediaSessionCallback mMediaSessionCallback;

    public PlaybackManager(PlaybackServiceCallback serviceCallback, Resources resources,
                           QueueRepository queueRepository,
                           Playback playback, Context context) {
        mQueueRepository = queueRepository;
        mServiceCallback = serviceCallback;
        mResources = resources;
        mMediaSessionCallback = new MediaSessionCallback(context);
        mPlayback = playback;
        mPlayback.setCallback(this);
    }

    public Playback getPlayback() {
        return mPlayback;
    }

    public MediaSessionCompat.Callback getMediaSessionCallback() {
        return mMediaSessionCallback;
    }

    /**
     * Handle a request to play music
     */
    public void handlePlayRequest() {
        LogHelper.d(TAG, "handlePlayRequest: mState=" + mPlayback.getState());
        Audio currentMusic = mQueueRepository.getCurrentMusic();

        if (currentMusic != null) {
            mServiceCallback.onPlaybackStart();
            mPlayback.play(currentMusic);
        }
    }

    /**
     * Handle a request to pause music
     */
    public void handlePauseRequest() {
        LogHelper.d(TAG, "handlePauseRequest: mState=" + mPlayback.getState());
        if (mPlayback.isPlaying()) {
            mPlayback.pause();
            mServiceCallback.onPlaybackStop();
        }
    }

    /**
     * Handle a request to stop music
     *
     * @param withError Error message in case the stop has an unexpected cause. The error
     *                  message will be set in the PlaybackState and will be visible to
     *                  MediaController clients.
     */
    public void handleStopRequest(String withError) {
        LogHelper.d(TAG, "handleStopRequest: mState=" + mPlayback.getState() + " error=", withError);
        mPlayback.stop(true);
        mServiceCallback.onPlaybackStop();
        updatePlaybackState(withError);
    }


    /**
     * Update the current media player state, optionally showing an error message.
     *
     * @param error if not null, error message to present to the user.
     */
    public void updatePlaybackState(String error) {
        LogHelper.d(TAG, "updatePlaybackState, playback state=" + mPlayback.getState());
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }

        //noinspection ResourceType
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

        setCustomAction(stateBuilder);
        int state = mPlayback.getState();

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }
        //noinspection ResourceType
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        // Set the activeQueueItemId if the current index is valid.
        // TODO implement when using lists for car or stuff
//        Audio currentMusic = mQueueRepository.getCurrentMusic();
//        if (currentMusic != null) {
//            stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
//        }

        mServiceCallback.onPlaybackStateUpdated(stateBuilder.build());

        if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED) {
            mServiceCallback.onNotificationRequired();
        }
    }

    private void setCustomAction(PlaybackStateCompat.Builder stateBuilder) {
        Audio currentMusic = mQueueRepository.getCurrentMusic();
        if (currentMusic == null) {
            return;
        }
        // Set appropriate "Favorite" icon on Custom action:
        String musicId = currentMusic.getmSongID();
        if (musicId == null) {
            return;
        }
        // TODO Use section to display like button
//
//        int favoriteIcon = mMusicProvider.isFavorite(musicId) ?
//                R.drawable.ic_star_on : R.drawable.ic_star_off;
//        LogHelper.d(TAG, "updatePlaybackState, setting Favorite custom action of music ",
//                musicId, " current favorite=", mMusicProvider.isFavorite(musicId));
//        Bundle customActionExtras = new Bundle();
//        WearHelper.setShowCustomActionOnWear(customActionExtras, true);
//        stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
//                CUSTOM_ACTION_THUMBS_UP, mResources.getString(R.string.favorite), favoriteIcon)
//                .setExtras(customActionExtras)
//                .build());
    }

    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY |
//                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
//                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
//                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    /**
     * Implementation of the Playback.Callback interface
     */
    @Override
    public void onCompletion() {
//        // The media player finished playing the current song, so we go ahead
//        // and play the next.
//        if (mQueueRepository.skipQueuePosition(1)) {
//            handlePlayRequest();
//            //TODO Update metadata and tell music service that it's time to switch to next music
//            //Or better approach, add listener pattern inside QueueRepo and notify service and activity etc
//            //mQueueManager.updateMetadata();
//        } else {
//            // If skipping was not possible, we stop and release the resources:
//            handleStopRequest(null);
//        }
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        //TODO implement or remove
//        LogHelper.d(TAG, "setCurrentMediaId", mediaId);
//        mQueueManager.setQueueFromMusic(mediaId);
    }

    public interface PlaybackServiceCallback {
        void onPlaybackStart();

        void onNotificationRequired();

        void onPlaybackStop();

        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }

    //If possible implement this in Player service
    private class MediaSessionCallback extends MediaSessionCompat.Callback {


        private final Context mContext;

        public MediaSessionCallback(Context context) {
            mContext = context;
        }

        @Override
        public void onPlay() {
            LogHelper.d(TAG, "play");
            if (mQueueRepository.getCurrentMusic() == null) {
                mQueueRepository.loadQueue(mContext);
            }
            handlePlayRequest();
        }

        @Override
        public void onSeekTo(long position) {
            LogHelper.d(TAG, "onSeekTo:", position);
            mPlayback.seekTo((int) position);
        }

        @Override
        public void onPause() {
            LogHelper.d(TAG, "pause. current state=" + mPlayback.getState());
            handlePauseRequest();
        }

        @Override
        public void onStop() {
            LogHelper.d(TAG, "stop. current state=" + mPlayback.getState());
            handleStopRequest(null);
        }

        @Override
        public void onSkipToNext() {
            LogHelper.d(TAG, "skipToNext");
//            if (mQueueRepository.skipQueuePosition(1)) {
//                handlePlayRequest();
//            } else {
//                handleStopRequest("Cannot skip");
//            }
        }

        @Override
        public void onSkipToPrevious() {
//            if (mQueueRepository.skipQueuePosition(-1)) {
//                handlePlayRequest();
//            } else {
//                handleStopRequest("Cannot skip");
//            }
        }

//        TODO implement this for like action
//        @Override
//        public void onCustomAction(@NonNull String action, Bundle extras) {
//            if (CUSTOM_ACTION_THUMBS_UP.equals(action)) {
//                LogHelper.i(TAG, "onCustomAction: favorite for current track");
//                MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
//                if (currentMusic != null) {
//                    String mediaId = currentMusic.getDescription().getMediaId();
//                    if (mediaId != null) {
//                        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
//                        mMusicProvider.setFavorite(musicId, !mMusicProvider.isFavorite(musicId));
//                    }
//                }
//                // playback state needs to be updated because the "Favorite" icon on the
//                // custom action will change to reflect the new favorite state.
//                updatePlaybackState(null);
//            } else {
//                LogHelper.e(TAG, "Unsupported action: ", action);
//            }
//        }

    }
}
