package com.inpen.shuffle.playerscreen;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.PlaybackStateCompat;

import com.inpen.shuffle.model.QueueRepository;
import com.inpen.shuffle.playback.MusicService;
import com.inpen.shuffle.utils.CustomTypes;
import com.inpen.shuffle.utils.LogHelper;
import com.inpen.shuffle.utils.PlaylistHelper;
import com.inpen.shuffle.utils.StaticStrings;

/**
 * Created by Abhishek on 11/9/2016.
 */

public class PlayerFragmentPresenter implements PlayerScreenContract.PlayerActionsListener, QueueRepository.CurrentItemIndexChangedObserver {

    private static final String LOG_TAG = LogHelper.makeLogTag(PlayerFragmentPresenter.class);
    private final PlayerScreenContract.PlayerView mView;
    QueueRepository mQueueRepository;

    private MusicService mMusicService;
    private boolean mIsServiceBound = false;
    private boolean mIsNewLaunch = false;


    /**
     * Setting the MusicService Binding
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Saving an istance of the binded service
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            mMusicService = binder.getService();
            mIsServiceBound = true;
            LogHelper.d(LOG_TAG, "PLayerService connected!");

            if (mIsNewLaunch) {
                mMusicService.play();
                mIsNewLaunch = false;
                mView.setState(CustomTypes.PlayerViewState.PLAYING);
            } else {
                if (mMusicService.isPlaying())
                    mView.setState(CustomTypes.PlayerViewState.PLAYING);
                else
                    mView.setState(CustomTypes.PlayerViewState.PAUSED);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsServiceBound = false;
        }
    };

    /**
     * Setting the broadcast receiver to intercept broadcast msg from MusicService
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            int playbackState = i.getIntExtra(MusicService.BROADCAST_EXTRA_PLAYBACK_STATE_KEY, PlaybackStateCompat.STATE_PLAYING);

            switch (playbackState) {
                case PlaybackStateCompat.STATE_PAUSED:
                case PlaybackStateCompat.STATE_STOPPED:
                case PlaybackStateCompat.STATE_NONE:
                    mView.setState(CustomTypes.PlayerViewState.PAUSED);
                    break;
                default:
                    mView.setState(CustomTypes.PlayerViewState.PLAYING);
            }
        }
    };


    public PlayerFragmentPresenter(Context context,
                                   PlayerScreenContract.PlayerView view) {
        mQueueRepository = QueueRepository.getInstance();

        mView = view;
        mIsNewLaunch = mView.getIsNewLaunch();

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

        mQueueRepository.addCurrentItemIndexChangedObserver(this);

        //Register Broadcast receiver for PlayerState
        LocalBroadcastManager
                .getInstance((mView.getActivityContext()))
                .registerReceiver(mBroadcastReceiver, new IntentFilter(MusicService.BROADCAST_PLAYBACK_STATE_CHANGED));

        // Binding the service
        Intent intent = new Intent(mView.getActivityContext(), MusicService.class);
        mView.getActivityContext().startService(intent);
        mView.getActivityContext().bindService(intent, mServiceConnection, 0);

    }

    @Override
    public void updateViews() {
        final PlayerAudioItemEnvelop playerAudioItemEnvelop
                = new PlayerAudioItemEnvelop(mQueueRepository.getCurrentMusic());


        mView.showLiked(false);
        mView.showDisliked(false);

        PlaylistHelper.isAudioInPlaylist(StaticStrings.PlAYLIST_NAME_LIKED,
                mQueueRepository.getCurrentMusic().getmSongID(),
                mView.getActivityContext(),
                new PlaylistHelper.Callback() {
                    @Override
                    public void isInPlaylist(boolean result) {
                        if (result) {
                            mView.showLiked(true);
                        }
                    }
                });
        PlaylistHelper.isAudioInPlaylist(StaticStrings.PlAYLIST_NAME_DISLIKED,
                mQueueRepository.getCurrentMusic().getmSongID(),
                mView.getActivityContext(),
                new PlaylistHelper.Callback() {
                    @Override
                    public void isInPlaylist(boolean result) {
                        if (result) {
                            mView.showDisliked(true);
                        }
                    }
                });

        mView.showData(playerAudioItemEnvelop);

    }

    @Override
    public void play() {
        if (mIsServiceBound)
            mMusicService.play();
    }

    @Override
    public void pause() {
        if (mIsServiceBound)
            mMusicService.pause();
    }

    @Override
    public void seekTo(int position) {
        if (mIsServiceBound)
            mMusicService.seekTo(position);
    }

    @Override
    public void playNext() {
        if (mIsServiceBound)
            mMusicService.playNext();
    }

    @Override
    public void playPrevious() {
        if (mIsServiceBound)
            mMusicService.playPrev();
    }

    @Override
    public void onScreenClicked() {
        pause();
    }

    @Override
    public void onLiked() {
        final String audioId = mQueueRepository.getCurrentMusic().getmSongID();
        LogHelper.d(LOG_TAG, audioId + " liked!");

        PlaylistHelper.isAudioInPlaylist(StaticStrings.PlAYLIST_NAME_LIKED,
                audioId,
                mView.getActivityContext(),
                new PlaylistHelper.Callback() {
                    @Override
                    public void isInPlaylist(boolean result) {
                        if (result) {
                            PlaylistHelper.removeAudioFromPlaylist(StaticStrings.PlAYLIST_NAME_LIKED, audioId, mView.getActivityContext());
                            mView.showLiked(false);
                        } else {
                            PlaylistHelper.removeAudioFromPlaylist(StaticStrings.PlAYLIST_NAME_DISLIKED, audioId, mView.getActivityContext());
                            PlaylistHelper.insertAudioIntoPlaylist(StaticStrings.PlAYLIST_NAME_LIKED, audioId, mView.getActivityContext());
                            mView.showLiked(true);
                        }
                    }
                });
    }

    @Override
    public void onDisliked() {
        final String audioId = mQueueRepository.getCurrentMusic().getmSongID();
        LogHelper.d(LOG_TAG, audioId + " disliked!");

        PlaylistHelper.isAudioInPlaylist(StaticStrings.PlAYLIST_NAME_DISLIKED,
                audioId,
                mView.getActivityContext(),
                new PlaylistHelper.Callback() {
                    @Override
                    public void isInPlaylist(boolean result) {
                        if (result) {
                            PlaylistHelper.removeAudioFromPlaylist(StaticStrings.PlAYLIST_NAME_DISLIKED, audioId, mView.getActivityContext());
                            mView.showDisliked(false);
                        } else {
                            PlaylistHelper.removeAudioFromPlaylist(StaticStrings.PlAYLIST_NAME_LIKED, audioId, mView.getActivityContext());
                            PlaylistHelper.insertAudioIntoPlaylist(StaticStrings.PlAYLIST_NAME_DISLIKED, audioId, mView.getActivityContext());
                            mView.showDisliked(true);
                        }
                    }
                });

    }

    @Override
    public void onStop() {

        // Unbinding the service
        if (mIsServiceBound)
            mView.getActivityContext().unbindService(mServiceConnection);


        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(mView.getActivityContext()).unregisterReceiver(mBroadcastReceiver);
        }

        mQueueRepository.removeCurrentItemIndexChangedObserver(this);
    }

    @Override
    public void onQueueIndexChanged() {
        updateViews();
    }
}
