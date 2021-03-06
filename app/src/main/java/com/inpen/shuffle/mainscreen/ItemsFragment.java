package com.inpen.shuffle.mainscreen;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inpen.shuffle.R;
import com.inpen.shuffle.utils.CustomTypes;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class ItemsFragment extends Fragment implements MainScreenContract.ItemsView {

    private static final String EXTRA_INT_ITEM_TYPE = "item_type";
    public ItemsAdapter mItemsAdapter;
    MainScreenContract.ItemsFragmentListener mActionsListener;
    @BindView(R.id.itemRecyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.emptyView)
    TextView mEmptyView;
    private CustomTypes.ItemType mItemType;

    public ItemsFragment() {
        // Required empty public constructor
    }

    public static ItemsFragment newInstance(@NonNull CustomTypes.ItemType itemType) {

        ItemsFragment f = new ItemsFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_INT_ITEM_TYPE, checkNotNull(CustomTypes.ItemType.toInt(itemType)));

        f.setArguments(args);
        f.setRetainInstance(true);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleArguments();

        mActionsListener = new ItemsPresenter(getLoaderManager(),
                getContext(),
                this,
                mItemType);

    }

    private void handleArguments() {

        Bundle b = getArguments();

        if (b != null) {
            mItemType = CustomTypes.ItemType.fromInt(getArguments().getInt(EXTRA_INT_ITEM_TYPE));
        } else {
            mItemType = CustomTypes.ItemType.ALBUM_ID;
        }

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
        View view = inflater.inflate(R.layout.fragment_items, container, false);

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

        ButterKnife.bind(this, view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Create a grid layout with two columns
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);

        // Create a custom SpanSizeLookup where the first item spans both columns
//        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
//                return position == 0 ? 2 : 1;
//            }
//        });
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mItemsAdapter);

        return view;
    }

    @Override
    public void setProgressIndicator(boolean active) {

    }

    @Override
    public void showItems(List<Item> itemList) {
        mItemsAdapter.replaceData(itemList);

        if (itemList == null || itemList.size() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void clearSelection() {
        if (mItemsAdapter != null) {
            mItemsAdapter.clearSelection();
        }
    }

    @Override
    public void selectItems(List<Item> selectedItemList) {
        mItemsAdapter.selectItems(selectedItemList);
    }
}
