package com.inpen.shuffle.playback;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import com.inpen.shuffle.model.Audio;
import com.inpen.shuffle.model.QueueRepository;
import com.inpen.shuffle.utils.LogHelper;

import java.io.IOException;
import java.util.List;

// Not Used
public class MediaPlayerService extends Service
        implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {


    private static final String LOG_TAG = LogHelper.makeLogTag(MediaPlayerService.class);
    // Binder given to clients
    private final IBinder iBinder = new LocalBinder();
    private MediaPlayer mMediaPlayer;
    //Used to pause/resume MediaPlayer
    private int mResumePosition;
    private AudioManager mAudioManager;

    private QueueRepository mQueueRepository;

    //List of available Audio files
    private List<Audio> mAudioList;
    private int mAudioIndex = -1;
    private Audio mActiveAudio; //an object of the currently playing audio

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            mQueueRepository = QueueRepository.getInstance();
            mQueueRepository.loadQueue(this);

            mAudioList = mQueueRepository.mPlayingQueue;
            mAudioIndex = mQueueRepository.mCurrentTrackIndex;
            mActiveAudio = mAudioList.get(mAudioIndex);

        } catch (NullPointerException e) {
            stopSelf();
        }

        //Request audio focus
        if (requestAudioFocus() == false) {
            //Could not gain focus
            stopSelf();
        }

        if (mActiveAudio != null)
            initMediaPlayer();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            stopMedia();
            mMediaPlayer.release();
        }

        if (mQueueRepository != null)
            mQueueRepository.clearCachedAudioPlaylist(this);

        //TODO remove receivers and remove notification

        removeAudioFocus();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed.
        stopMedia();
        //stop the service
        stopSelf();
    }

    //Handle errors
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation.
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                LogHelper.e(LOG_TAG, "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                LogHelper.e(LOG_TAG, "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                LogHelper.e(LOG_TAG, "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Invoked when the media source is ready for playback.
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.

        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mMediaPlayer == null) initMediaPlayer();
                else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mMediaPlayer.reset();

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mMediaFile location
            mMediaPlayer.setDataSource(mActiveAudio.getmSourcePath());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mMediaPlayer.prepareAsync();
    }

    private boolean requestAudioFocus() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }


    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.abandonAudioFocus(this);
    }

    private void playMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mMediaPlayer == null) return;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mResumePosition = mMediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(mResumePosition);
            mMediaPlayer.start();
        }
    }

    public class LocalBinder extends Binder {

        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // Broadcast recievers and regestering methods
    ///////////////////////////////////////////////////////////////////////////

    // I don't know what this is for
//    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            //Get the new media index form SharedPreferences
//            mAudioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
//            if (mAudioIndex != -1 && mAudioIndex < mAudioList.size()) {
//                //index is in a valid range
//                activeAudio = audioList.get(audioIndex);
//            } else {
//                stopSelf();
//            }
//
//            //A PLAY_NEW_AUDIO action received
//            //reset mediaPlayer to play the new Audio
//            stopMedia();
//            mediaPlayer.reset();
//            initMediaPlayer();
//            updateMetaData();
//            buildNotification(PlaybackStatus.PLAYING);
//        }
//    };
//
//    private void registerPlayNewAudioReciever() {
//        //Register playNewMedia receiver
//        IntentFilter filter = new IntentFilter(PlayerActivity.BROADCAST_PLAY_NEW_AUDIO);
//        registerReceiver(playNewAudio, filter);
//    }

}
