package com.inpen.shuffle.songListScreens;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.inpen.shuffle.R;
import com.inpen.shuffle.utils.StaticStrings;

public class SongsActivity extends AppCompatActivity {

    public static final String EXTRA_PLAYLIST_FILTER_KEY = "playlist_filter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);

        String playlistFilter = getIntent().hasExtra(EXTRA_PLAYLIST_FILTER_KEY) ?
                getIntent().getStringExtra(EXTRA_PLAYLIST_FILTER_KEY) : "";

        if (playlistFilter.equals(StaticStrings.PlAYLIST_NAME_LIKED)) {
            setTitle(getString(R.string.liked_songs_activity_title));
        } else if (playlistFilter.equals(StaticStrings.PlAYLIST_NAME_DISLIKED)) {
            setTitle(getString(R.string.disliked_songs_activity_title));
        } else {
            setTitle(getString(R.string.all_songs_activity_title));
        }

        if (savedInstanceState == null) {

            SongListFragment songListFragment = SongListFragment.newInstance(playlistFilter);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.songListFragmentContainer, songListFragment).commit();

        }
    }
}
