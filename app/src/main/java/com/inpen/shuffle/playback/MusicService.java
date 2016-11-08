package com.inpen.shuffle.playback;

/**
 * Created by Abhishek on 11/7/2016.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;

import com.inpen.shuffle.R;
import com.inpen.shuffle.mainscreen.MainActivity;
import com.inpen.shuffle.model.Audio;
import com.inpen.shuffle.model.QueueRepository;
import com.inpen.shuffle.playerscreen.PlayerActivity;
import com.inpen.shuffle.utils.LogHelper;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Matteo on 11/06/2015.
 */
public class MusicService extends Service implements Playback.Callback {

    public static final String BROADCAST_CURRENT_SONG_CHANGED = "CURRENT_SONG_CHANGED";
    public static final String BROADCAST_PLAYBACK_STATE_CHANGED = "PLAYBACK_STATE_CHANGED";
    public static final String BROADCAST_EXTRA_PLAYBACK_STATE_KEY = "PLAYBACK_STATE";
    public static final String INTENT_ACTION_PLAY = "ACTION_PLAY";
    public static final String INTENT_ACTION_PAUSE = "ACTION_PAUSE";
    public static final String INTENT_ACTION_NEXT = "ACTION_NEXT";
    public static final String INTENT_ACTION_PREV = "ACTION_PREV";
    public static final String INTENT_ACTION_STOP = "ACTION_STOP";
    private static final String LOG_TAG = LogHelper.makeLogTag(MusicService.class);
    private static final int NOTIFICATION_ID = 1;
    private final IBinder mMusicBinder = new MusicBinder();
    private QueueRepository mQueueRepository;
    private Playback mPlayback;

    @Override
    public void onCreate() {
        super.onCreate();
        mQueueRepository = QueueRepository.getInstance();
        mQueueRepository.loadQueue(this);

        mPlayback = new Playback(this);
        mPlayback.setCallback(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            handleIntent(intent);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mPlayback.stop(true);
        super.onDestroy();
    }

    private void handleIntent(Intent intent) {
        switch (intent.getAction()) {
            case INTENT_ACTION_PLAY:
                play();
                break;
            case INTENT_ACTION_PAUSE:
                pause();
                break;
            case INTENT_ACTION_PREV:
                playPrev();
                break;
            case INTENT_ACTION_NEXT:
                playNext();
                break;
            case INTENT_ACTION_STOP:
                stop();
                break;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Playback control methods
    ///////////////////////////////////////////////////////////////////////////

    public void play() {
        Audio currentMusic = mQueueRepository.getCurrentMusic();

        if (currentMusic != null) {
            mPlayback.play(currentMusic);
        }

        buildNotification();
    }

    public void pause() {
        if (mPlayback.isPlaying()) {
            mPlayback.pause();
        }

        buildNotification();
    }

    public boolean isPlaying() {
        return mPlayback.isPlaying();
    }

    public void seekTo(int position) {
        mPlayback.seekTo(position);
    }


    public void playPrev() {
        mQueueRepository.skipQueuePosition(-1);
        play();
    }

    public void playNext() {
        mQueueRepository.skipQueuePosition(+1);
        play();
    }

    public void stop() {
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        mPlayback.pause();
    }

    public int getState() {
        return mPlayback.getState();
    }

    private void buildNotification() {
        // http://stackoverflow.com/questions/24465587/change-notifications-action-icon-dynamically

        // Initializing the media style (no text on notification buttons)
        NotificationCompat.MediaStyle mediaStyle = new NotificationCompat.MediaStyle();

        // Building the notification settings
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        builder.setSmallIcon(R.drawable.ic_shuffle);
        builder.setContentTitle(mQueueRepository.getCurrentMusic().getmTitle());
        builder.setContentText(mQueueRepository.getCurrentMusic().getmArtist());
        builder.setLargeIcon(getLargeIcon());
        builder.setShowWhen(false);
        builder.setStyle(mediaStyle);

        // Setting the notification default intent (starting MainActivity)
        Intent resultIntent = new Intent(getApplicationContext(), PlayerActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        // Adding the notification controls actions
        builder.addAction(createAction
                (R.drawable.ic_skip_prev, "Previous", INTENT_ACTION_PREV));
        if (isPlaying()) {
            builder.addAction(createAction
                    (R.drawable.ic_pause, "Pause", INTENT_ACTION_PAUSE));
        } else {
            builder.addAction(createAction
                    (R.drawable.ic_play, "Play", INTENT_ACTION_PLAY));
        }
        builder.addAction(createAction
                (R.drawable.ic_skip_next, "Next", INTENT_ACTION_NEXT));

        // Setting the notification delete intent
        Intent stopIntent = new Intent(getApplicationContext(), MusicService.class);
        stopIntent.setAction(INTENT_ACTION_STOP);
        PendingIntent deleteIntent = PendingIntent.
                getService(getApplicationContext(), 1, stopIntent, 0);
        builder.setDeleteIntent(deleteIntent);

        // Setting the lock screen notification
        mediaStyle.setShowActionsInCompactView(1, 2);

        // Setting the notification manager
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // Getting the bitmap from Picasso
    // http://stackoverflow.com/questions/26888247/easiest-way-to-use-picasso-in-notification-icon
    private Bitmap getLargeIcon() {
        Bitmap bitmap = null;
        try {
            try {
                bitmap = new AsyncTask<Void, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        try {
                            return Picasso.with(getApplicationContext())
                                    .load(mQueueRepository.getCurrentMusic().getmAlbumArt())
                                    .resize(200, 200)
                                    .placeholder(R.drawable.ic_shuffle)
                                    .error(R.drawable.ic_shuffle)
                                    .get();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (bitmap != null) {
            return bitmap;
        } else {
            return BitmapFactory.
                    decodeResource(getResources(), R.drawable.ic_shuffle);
        }
    }

    private Action createAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent =
                PendingIntent.getService(getApplicationContext(), NOTIFICATION_ID, intent, 0);
        return new Action.Builder(icon, title, pendingIntent).build();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Callbacks from Playback class
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onCompletion() {
        playNext();
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        Intent intent = new Intent(BROADCAST_PLAYBACK_STATE_CHANGED);
        intent.putExtra(BROADCAST_EXTRA_PLAYBACK_STATE_KEY, mPlayback.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onError(String error) {

    }

    @Override
    public void setCurrentMediaId(String mediaId) {

    }

    ///////////////////////////////////////////////////////////////////////////
    // Binding related
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    /*
     * Binding settings
     */
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

}
