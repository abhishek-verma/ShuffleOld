package com.inpen.shuffle.mainscreen;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inpen.shuffle.R;
import com.inpen.shuffle.utils.CustomTypes;

import java.util.ArrayList;
import java.util.List;

public class AlbumsFragment extends Fragment implements MainScreenContract.ItemsView {

    MainScreenContract.ItemsFragmentListener mActionsListener;
    private ItemsAdapter mItemsAdapter;

    public AlbumsFragment() {
        // Required empty public constructor
    }

    public static AlbumsFragment newInstance() {
        return new AlbumsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mItemsAdapter = new ItemsAdapter(new ArrayList<Item>(0), new ItemsAdapter.ItemsListener() {
            @Override
            public void onItemClicked(Item item, boolean selectState) {
                mActionsListener.setItemSelected(item, selectState);
            }

            @Override
            public void onAllSelectAllItemClicked(List<Item> itemList, boolean selectState) {
                mActionsListener.setAllItemsSelected(itemList, selectState);
            }

        });

        mActionsListener = new ItemsPresenter(getLoaderManager(),
                getContext(),
                this,
                CustomTypes.ItemType.ALBUM_ID);

    }

    @Override
    public void onResume() {
        super.onResume();

        mActionsListener.initialize();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_albums, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void setProgressIndicator(boolean active) {

    }

    @Override
    public void showItems(List<Item> itemList) {
        mItemsAdapter.replaceData(itemList);
    }

}
