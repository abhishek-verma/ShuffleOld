package com.inpen.shuffle.playerscreen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.inpen.shuffle.R;
import com.inpen.shuffle.mainscreen.MainActivity;
import com.inpen.shuffle.model.QueueRepository;
import com.inpen.shuffle.utils.LogHelper;

public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_BOOLEAN_NEW_LAUNCH = "new_launch";
    private static final String LOG_TAG = LogHelper.makeLogTag(PlayerActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (!QueueRepository.hasCachedQueue(this)) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        if (savedInstanceState == null) {
            PlayerFragment playerFragment
                    = PlayerFragment.newInstance(getIntent().
                    getBooleanExtra(PlayerActivity.EXTRA_BOOLEAN_NEW_LAUNCH,
                            getIntent().hasExtra(EXTRA_BOOLEAN_NEW_LAUNCH) &&
                                    getIntent().getExtras().getBoolean(EXTRA_BOOLEAN_NEW_LAUNCH)));

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.playerFragmentContainer, playerFragment).commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
