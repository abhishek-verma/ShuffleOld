package com.inpen.shuffle.mainscreen;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.inpen.shuffle.R;
import com.inpen.shuffle.utils.CustomTypes;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements MainScreenContract.MainView {

    private static final int PERMISSION_REQUEST_CODE = 0;
    MainScreenContract.ActivityActionsListener mActivityActionsListener;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    private MyPagerAdapter mFragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        mActivityActionsListener = new MainPresenter(this);
        mActivityActionsListener.init(this);

        mActivityActionsListener.scanMedia(this);

        //Set up the Adapter
        mFragmentAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOffscreenPageLimit(4);

        //Set up the ViewPager
        mTabLayout.setupWithViewPager(mViewPager);
// TODO        setTabIcons();

    }

    @Override
    public boolean hasPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void getPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to read the contacts
            }

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onFabStateChanged(CustomTypes.MainFabState state) {
        //TODO animate changes

        switch (state) {
            case HIDDEN:
                fab.setVisibility(View.GONE);
                break;
            case SHUFFLE:
                fab.setVisibility(View.VISIBLE);
                fab.setImageResource(R.drawable.ic_shuffle);
                break;
            case PLAYER:
                fab.setVisibility(View.VISIBLE);
                fab.setImageResource(R.drawable.ic_player);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        mActivityActionsListener.scanMedia(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.fab)
    public void onFabClick() {
        mActivityActionsListener.shuffleAndPlay(this);
    }

    public void setTabIcons() {
        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_albums);
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_artists);
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_folders);
        mTabLayout.getTabAt(3).setIcon(R.drawable.ic_playlists);
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            CustomTypes.ItemType itemType;
//            = CustomTypes.ItemType.ALBUM_ID;

            switch (position) {
                case 0:
                    itemType = CustomTypes.ItemType.ALBUM_ID;
                    break;
                case 1:
                    itemType = CustomTypes.ItemType.ARTIST_ID;
                    break;
                case 2:
                    itemType = CustomTypes.ItemType.FOLDER;
                    break;
                case 3:
                    itemType = CustomTypes.ItemType.PLAYLIST;
                    break;
                default:
                    itemType = CustomTypes.ItemType.ALBUM_ID;
            }

            return ItemsFragment.newInstance(itemType);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title;

            switch (position) {
                case 0:
                    title = "ALBUMS";
                    break;
                case 1:
                    title = "ARTISTS";
                    break;
                case 2:
                    title = "FOLDERS";
                    break;
                case 3:
                    title = "PLAYLISTS";
                    break;
                default:
                    title = "ALBUMS";
            }

            return title;
        }
    }
}
