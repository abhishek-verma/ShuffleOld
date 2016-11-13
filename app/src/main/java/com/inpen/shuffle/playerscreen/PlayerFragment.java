package com.inpen.shuffle.playerscreen;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.inpen.shuffle.R;
import com.inpen.shuffle.model.AudioItem;
import com.inpen.shuffle.utils.CustomTypes;
import com.inpen.shuffle.utils.LogHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class PlayerFragment extends Fragment implements PlayerScreenContract.PlayerView {

    private static final String LOG_TAG = LogHelper.makeLogTag(PlayerFragment.class);
    public static String EXTRA_IS_NEW_LAUNCH_KEY = "new_launch";

    @BindView(R.id.songTitle)
    TextView mTitleTextView;
    @BindView(R.id.artistName)
    TextView mArtistTextView;
    @BindView(R.id.albumArt)
    ImageView mAlbumArt;
    @BindView(R.id.seekBar)
    SeekBar mSeekbar;
    @BindView(R.id.likeButton)
    ImageButton mLikeBtn;
    @BindView(R.id.dislikeButton)
    ImageButton mDislikeBtn;
    @BindView(R.id.shareButton)
    ImageButton mShareBtn;
    @BindView(R.id.songDetailsParent)
    LinearLayout mSongDetailParent;
    @BindView(R.id.playerControlsParent)
    LinearLayout mPlayerControlParent;
    CustomTypes.PlayerViewState mPlayerState = CustomTypes.PlayerViewState.PAUSED;
    private PlayerScreenContract.PlayerActionsListener mPresenter = null;
    SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mPresenter.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mPresenter.scheduleSeekBarUpdate(false);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mPresenter.scheduleSeekBarUpdate(true);
        }
    };
    private boolean mIsNewLaunch = false;
    private Context mContext;

    public PlayerFragment() {
        // Required empty public constructor
    }

    public static PlayerFragment newInstance(boolean isNewLaunch) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_IS_NEW_LAUNCH_KEY, isNewLaunch);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && savedInstanceState == null) {
            mIsNewLaunch = getArguments().getBoolean(EXTRA_IS_NEW_LAUNCH_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        ButterKnife.setDebug(true);
        ButterKnife.bind(this, view);

        mSeekbar.setOnSeekBarChangeListener(mSeekBarChangeListener);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPresenter == null)
            mPresenter = new PlayerFragmentPresenter(getContext(),
                    this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.onStop();
        mPresenter = null;
        mIsNewLaunch = false;
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        mContext = context;
    }

    ///////////////////////////////////////////////////////////////////////////
    // View listeners
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @OnClick(R.id.albumArt)
    public void albumArtClicked() {
        setState(CustomTypes.PlayerViewState.PAUSED);
        mPresenter.onScreenClicked();
    }

    @OnClick(R.id.playButton)
    public void playButtonClicked() {
        setState(CustomTypes.PlayerViewState.PLAYING);
        mPresenter.play();
    }

    @OnClick(R.id.prevButton)
    public void prevButtonClicked() {
        setState(CustomTypes.PlayerViewState.PLAYING);
        mPresenter.playPrevious();
    }

    @OnClick(R.id.nextButton)
    public void nextButtonClicked() {
        setState(CustomTypes.PlayerViewState.PLAYING);
        mPresenter.playNext();
    }

    @OnClick(R.id.likeButton)
    public void likeButtonClicked() {
        mPresenter.onLiked();
    }

    @OnClick(R.id.dislikeButton)
    public void dislikeButtonClicked() {
        mPresenter.onDisliked();
    }


    ///////////////////////////////////////////////////////////////////////////
    // Implementation for Contract Player view
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void showData(PlayerAudioItemEnvelop playerAudioItemEnvelop) {
        if (playerAudioItemEnvelop == null || playerAudioItemEnvelop.mAudioItem == null) {
            return;
        }

        AudioItem audioItem = playerAudioItemEnvelop.mAudioItem;

        mTitleTextView.setText(audioItem.getmTitle());
        mArtistTextView.setText(audioItem.getmArtist());

        if (playerAudioItemEnvelop.isLiked) {
            mLikeBtn.setAlpha(1f);
        } else if (playerAudioItemEnvelop.isDisliked) {
            mLikeBtn.setAlpha(1f);
        }

        Glide.with(this).load(audioItem.getmAlbumArt()).placeholder(R.drawable.ic_loading_circle).into(mAlbumArt);

        mSeekbar.setMax((int) audioItem.getmDuration());

        getActivity().startPostponedEnterTransition();
    }

    @Override
    public void showLiked(boolean enabled) {
        if (enabled) {
            mLikeBtn.setAlpha(1f);
            mDislikeBtn.setAlpha(0.4f);
        } else {
            mLikeBtn.setAlpha(0.4f);
        }
    }

    @Override
    public void showDisliked(boolean enabled) {
        if (enabled) {
            mLikeBtn.setAlpha(0.4f);
            mDislikeBtn.setAlpha(1f);
        } else {
            mDislikeBtn.setAlpha(0.4f);
        }
    }

    @Override
    public CustomTypes.PlayerViewState getPlayerState() {
        return mPlayerState;
    }

    @Override
    public boolean getIsNewLaunch() {
        return mIsNewLaunch;
    }

    @Override
    public void setState(CustomTypes.PlayerViewState playerViewState) {
        mPlayerState = playerViewState;

        switch (mPlayerState) {
            case PAUSED:
                showPaused();
                break;
            case PLAYING:
                showPlaying();
                break;
        }
    }

    private void showPaused() {
        mSongDetailParent.setVisibility(View.GONE);
        mPlayerControlParent.setVisibility(View.VISIBLE);
    }

    public void showPlaying() {
        mSongDetailParent.setVisibility(View.VISIBLE);
        mPlayerControlParent.setVisibility(View.GONE);
    }

    @Override
    public void setSeekBarProgress(int progress) {
        mSeekbar.setProgress(progress);
    }

    @Override
    public Context getActivityContext() {
        return mContext;
    }
}
