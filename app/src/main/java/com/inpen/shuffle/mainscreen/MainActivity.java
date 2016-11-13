package com.inpen.shuffle.mainscreen;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.inpen.shuffle.R;
import com.inpen.shuffle.songListScreens.SongsActivity;
import com.inpen.shuffle.syncmedia.SyncMediaIntentService;
import com.inpen.shuffle.utils.CustomTypes;
import com.inpen.shuffle.utils.LogHelper;
import com.inpen.shuffle.utils.StaticStrings;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements MainScreenContract.MainView,
        NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = LogHelper.makeLogTag(MainActivity.class);
    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final int RC_SIGN_IN = 0;

    MainScreenContract.ActivityActionsListener mActivityActionsListener;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    //Nav panel header views
    private SignInButton mSignInButton;
    private Button mSignOutButton;
    private TextView mGreetingTextView;

    private MyPagerAdapter mFragmentAdapter;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mActivityActionsListener = new MainPresenter(this);
        mActivityActionsListener.init(this);

        mActivityActionsListener.scanMedia(this);

        setUpAdapterAndViewPager();

        setUpNavigationDrawer();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("266113444855-g706c4in8t8va1ohg9jnsq8fe9vird5h.apps.googleusercontent.com")
                //Create string if still dosent work, value: 266113444855-ih2j5a3vrqj7nuioppdm5p5emp88luc2.apps.googleusercontent.com
                //if still dosent work switch to web clients id
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this/*Activity*/, this/*OnConnectionFailedListener*/)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void setUpNavigationDrawer() {

        mNavigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                mToolbar,
                R.string.openDrawer,
                R.string.closeDrawer) {

        };

        //Setting the actionbarToggle to drawer layout
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();


        //Setting up listeners for vies in header
        View header = mNavigationView.getHeaderView(0);

        mSignInButton = (SignInButton) header.findViewById(R.id.signInButton);
        mSignOutButton = (Button) header.findViewById(R.id.signOutButton);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        mGreetingTextView = (TextView) header.findViewById(R.id.navBarGreetingTextV);
        SharedPreferences prefs = getSharedPreferences(StaticStrings.PREF_NAME_USER_SIGN_DATA, MODE_PRIVATE);

        if (prefs.getBoolean(StaticStrings.PREF_IS_SIGNED_IN_BOOLEAN_KEY, false)) {
            mSignInButton.setVisibility(View.GONE);
            mSignOutButton.setVisibility(View.VISIBLE);

            mGreetingTextView.setText(prefs.getString(StaticStrings.PREF_EXTRA_USER_EMAIL_ID_STRING_KEY,
                    getString(R.string.nav_bar_greeting)));

        } else {
            mSignInButton.setVisibility(View.VISIBLE);
            mSignOutButton.setVisibility(View.GONE);
        }
    }

    private void setUpAdapterAndViewPager() {
        //Set up the Adapter
        mFragmentAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOffscreenPageLimit(4);

        //Set up the ViewPager
        mTabLayout.setupWithViewPager(mViewPager);
// TODO        setTabIcons();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                mSignInButton.setVisibility(View.VISIBLE);
                mSignOutButton.setVisibility(View.GONE);

                mGreetingTextView.setText(getString(R.string.nav_bar_greeting));

                getSharedPreferences(StaticStrings.PREF_NAME_USER_SIGN_DATA, MODE_PRIVATE)
                        .edit().clear().apply();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                handleSignInResult(account);
                firebaseAuthWithGoogle(account);

            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                Toast.makeText(MainActivity.this, "Authentication failed! Please try again later.",
                        Toast.LENGTH_SHORT).show();
                LogHelper.e(LOG_TAG, "Login failed! + resultCode: " + requestCode);
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Hey " + acct.getDisplayName() + "!",
                                    Toast.LENGTH_SHORT).show();
                            SyncMediaIntentService.syncMedia(MainActivity.this);
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed! Please try again later.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void handleSignInResult(GoogleSignInAccount account) {
        mSignInButton.setVisibility(View.GONE);
        mSignOutButton.setVisibility(View.VISIBLE);

        mGreetingTextView.setText(account.getDisplayName());

        SharedPreferences.Editor editor = getSharedPreferences(StaticStrings.PREF_NAME_USER_SIGN_DATA, MODE_PRIVATE)
                .edit();

        editor.putBoolean(StaticStrings.PREF_IS_SIGNED_IN_BOOLEAN_KEY, true);

        editor.putString(StaticStrings.PREF_EXTRA_USER_AUTH_ID_STRING_KEY, account.getId());
        editor.putString(StaticStrings.PREF_EXTRA_USER_EMAIL_ID_STRING_KEY, account.getEmail());
        editor.putString(StaticStrings.PREF_EXTRA_USER_DISPLAY_NAME_STRING_KEY, account.getDisplayName());

        editor.apply();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        //Checking if the item is in checked state or not, if not make it in checked state

        //Closing drawer on item click
        mDrawerLayout.closeDrawers();

        Intent songListIntent;

        //Check to see which item was being clicked and perform appropriate action
        switch (menuItem.getItemId()) {
            case R.id.navOptionSongs:
                songListIntent = new Intent(this, SongsActivity.class);
                startActivity(songListIntent);
                break;
            case R.id.navOptionLiked:
                songListIntent = new Intent(this, SongsActivity.class);
                songListIntent.putExtra(SongsActivity.EXTRA_PLAYLIST_FILTER_KEY, StaticStrings.PlAYLIST_NAME_LIKED);
                startActivity(songListIntent);
                break;
            case R.id.navOptionDisliked:
                songListIntent = new Intent(this, SongsActivity.class);
                songListIntent.putExtra(SongsActivity.EXTRA_PLAYLIST_FILTER_KEY, StaticStrings.PlAYLIST_NAME_DISLIKED);
                startActivity(songListIntent);
                break;
            default:
                Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                return false;
        }

        return true;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public AppCompatActivity getActivityContext() {
        return MainActivity.this;
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
//                case 2: TODO implement these later
//                    itemType = CustomTypes.ItemType.FOLDER;
//                    break;
//                case 3:
//                    itemType = CustomTypes.ItemType.PLAYLIST;
//                    break;
                default:
                    itemType = CustomTypes.ItemType.ALBUM_ID;
            }

            return ItemsFragment.newInstance(itemType);
        }

        @Override
        public int getCount() {
            return 2;
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
//                case 2:
//                    title = "FOLDERS";
//                    break;
//                case 3:
//                    title = "PLAYLISTS";
//                    break;
                default:
                    title = "ALBUMS";
            }

            return title;
        }
    }
}
