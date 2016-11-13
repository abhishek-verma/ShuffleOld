package com.inpen.shuffle.songListScreens;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inpen.shuffle.R;
import com.inpen.shuffle.model.AudioItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class SongListFragment extends Fragment implements SongListScreenContract.SongListView {

    @BindView(R.id.emptyView)
    public TextView mEmptyView;
    private SongListScreenContract.SongListViewInteractionListener mPresenter = new SongListPresenter();
    private SongListViewAdapter mSongListAdapter;
    private RecyclerView mRecyclerView;

    public SongListFragment() {
    }

    public static SongListFragment newInstance(String playlistFilter) {
        SongListFragment fragment = new SongListFragment();
        Bundle args = new Bundle();
        args.putString(SongsActivity.EXTRA_PLAYLIST_FILTER_KEY, playlistFilter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song_list, container, false);

        ButterKnife.bind(this, view);

        mSongListAdapter = new SongListViewAdapter(
                new ArrayList<AudioItem>(),
                new SongListFragment.OnListFragmentInteractionListener() {
                    @Override
                    public void onListFragmentInteraction(AudioItem item) {
                        mPresenter.onItemClicked(item);
                    }
                });

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            mRecyclerView.setAdapter(mSongListAdapter);
        } else {
            mRecyclerView = (RecyclerView) view.findViewById(R.id.songList);
            Context context = view.getContext();
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            mRecyclerView.setAdapter(mSongListAdapter);
        }

        mPresenter.initialize(getLoaderManager(),
                getContext(),
                this,
                getArguments().getString(SongsActivity.EXTRA_PLAYLIST_FILTER_KEY));
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void showData(List<AudioItem> audioItemList) {
        mSongListAdapter.replaceData(audioItemList);


        if (audioItemList == null || audioItemList.size() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    public interface OnListFragmentInteractionListener {

        void onListFragmentInteraction(AudioItem item);

    }
}
