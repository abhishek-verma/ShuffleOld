package com.inpen.shuffle.playerscreen;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import com.inpen.shuffle.R;
import com.inpen.shuffle.playback.MusicService;
import com.inpen.shuffle.utils.LogHelper;

public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_BOOLEAN_NEW_LAUNCH = "new_launch";
    private static final String LOG_TAG = LogHelper.makeLogTag(PlayerActivity.class);
    private MusicService mMusicService;
    private boolean mIsServiceBound = false;
    private boolean mIsNewLaunch;
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
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mIsNewLaunch = getIntent().getBooleanExtra(EXTRA_BOOLEAN_NEW_LAUNCH, false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Binding the service
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unbinding the service
        unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mIsServiceBound) {
//            unbindService(serviceConnection);
//            // service is active
//            // TODO remove this since service should be active even when activity closed
//            mMusicService.stopSelf();
//        }
    }

    private void playAudio() {
        mMusicService.play();
    }

    public MusicService getMusicService() {
        return mMusicService;
    }

    public boolean isServiceBound() {
        return mIsServiceBound;
    }
}
