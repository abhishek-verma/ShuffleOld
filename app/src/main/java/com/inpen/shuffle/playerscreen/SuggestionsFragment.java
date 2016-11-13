package com.inpen.shuffle.playerscreen;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inpen.shuffle.R;
import com.inpen.shuffle.model.AudioItem;

import java.util.ArrayList;
import java.util.List;

public class SuggestionsFragment extends Fragment implements PlayerScreenContract.SuggestionsView {

    private PlayerScreenContract.SuggestionViewActionsListener mPresenter = new SuggestionFragmentPresenter();
    private SuggestionsViewAdapter mSuggestionsAdapter;

    public SuggestionsFragment() {
    }

    public static SuggestionsFragment newInstance() {
        SuggestionsFragment fragment = new SuggestionsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suggestions, container, false);

        mSuggestionsAdapter = new SuggestionsViewAdapter(
                new ArrayList<AudioItem>(),
                new OnListFragmentInteractionListener() {
                    @Override
                    public void onListFragmentInteraction(AudioItem item) {
                        mPresenter.onItemClicked(item);
                    }
                });

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(mSuggestionsAdapter);
        } else {
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.suggestionItemList);
            Context context = view.getContext();
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(mSuggestionsAdapter);
        }

        mPresenter.initialize(getContext(), this);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Context getActivityContext() {
        return getContext();
    }

    @Override
    public void showData(List<AudioItem> audioItemList) {
        mSuggestionsAdapter.replaceData(audioItemList);
    }

    public interface OnListFragmentInteractionListener {

        void onListFragmentInteraction(AudioItem item);

    }
}
