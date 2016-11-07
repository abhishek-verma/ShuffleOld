package com.inpen.shuffle.playerscreen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.inpen.shuffle.R;
import com.inpen.shuffle.playback.MusicService;

public class PlayerActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playAudio();
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
        Intent playerIntent = new Intent(this, MusicService.class);
        startService(playerIntent);
    }
}
