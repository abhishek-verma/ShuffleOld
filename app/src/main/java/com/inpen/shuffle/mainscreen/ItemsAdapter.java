package com.inpen.shuffle.mainscreen;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.inpen.shuffle.customviews.ItemView;
import com.inpen.shuffle.utils.LogHelper;

import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Abhishek on 11/1/2016.
 */
public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemViewHolder> {

    private static final String LOG_TAG = LogHelper.makeLogTag(ItemsAdapter.class);
    private final ItemsListener mItemsListener;
    private List<Item> mItemList;
    private int mSelectedItemCount = 0;

    public ItemsAdapter(List<Item> itemList, ItemsListener itemsListener) {
        mItemList = itemList;
        mItemsListener = itemsListener;
    }

    public void replaceData(List<Item> tasks) {
        setList(tasks);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        LogHelper.v(LOG_TAG, "clearSelection()", new Throwable());
        if (mSelectedItemCount == 0) {
            return;
        }

        for (Item item : mItemList)
            item.setSelected(false);

        mSelectedItemCount = 0;

        LogHelper.v(LOG_TAG, "mSelectedItemCount: " + mSelectedItemCount);
        notifyDataSetChanged();
    }

    public void selectItems(List<Item> selectedItemList) {
        LogHelper.v(LOG_TAG, "selectItems( selectedIemList:" + selectedItemList.toString() + " )");

        mSelectedItemCount = Math.min(selectedItemList.size(), mItemList.size() - 1);

        for (Item itemIterator : mItemList) {
            if (selectedItemList.contains(itemIterator))
                itemIterator.setSelected(true);

        }
        notifyDataSetChanged();
        LogHelper.v(LOG_TAG, "mSelectedItemCount: " + mSelectedItemCount);
    }

    private void setList(List<Item> tasks) {
        mItemList = checkNotNull(tasks);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemView v = new ItemView(parent.getContext());

        ItemViewHolder vh = new ItemViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.bind(mItemList.get(position));
    }

    private boolean toggleSelectionForItem(Item item) {
        LogHelper.v(LOG_TAG, "toggleSelectionForItem- title: " + item.getTitle());

        item.setSelected(!item.isSelected());

        if (item.isSelected()) {
            mSelectedItemCount++;
        } else {
            mSelectedItemCount--;
        }
        boolean allItemsSelected = mSelectedItemCount == mItemList.size() - 1;//-1 because 1st item (selectall item) is not included here

        if (mItemList.get(0).isSelected() != allItemsSelected) {
            //Set selectAllItems to be selected is all items are selected else deselected
            mItemList.get(0).setSelected(allItemsSelected);
            notifyItemChanged(0);
        }

        notifyItemChanged(mItemList.indexOf(item));


        LogHelper.v(LOG_TAG, "mSelectedItemCount: " + mSelectedItemCount);
        return item.isSelected();
    }

    private boolean toggleSelectionForAllItems() {
        LogHelper.d(LOG_TAG, "toggleSelectionForItems");
        boolean selectAll;

        selectAll = !mItemList.get(0).isSelected();

        for (Item item : mItemList)
            item.setSelected(selectAll);

        if (selectAll)
            mSelectedItemCount = mItemList.size() - 1;
        else
            mSelectedItemCount = 0;

        notifyDataSetChanged();

        LogHelper.d(LOG_TAG, "mSelectedItemCount: " + mSelectedItemCount);
        return selectAll;
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public interface ItemsListener {

        void onItemClicked(Item item, boolean selectState);

        void onAllSelectAllItemClicked(List<Item> item, boolean selectState);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ItemView mItemView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mItemView = (ItemView) itemView;
        }

        @Override
        public void onClick(View view) {

            ItemView itemView = (ItemView) view;

            LogHelper.v(LOG_TAG, "view with itemId: " + itemView.getItem().getId() + " clicked! ");

            if (itemView.getItem().getId().equals(Item.SELECT_ALL_ITEM_VIEW_ID)) {
                boolean selectState = toggleSelectionForAllItems();
                mItemsListener.onAllSelectAllItemClicked(mItemList, selectState);
            } else {
                boolean selectState = toggleSelectionForItem(itemView.getItem());
                mItemsListener.onItemClicked(((ItemView) view).getItem(), selectState);
            }
        }

        public void bind(Item item) {
            mItemView.setItem(item);
            mItemView.setOnClickListener(this);
        }
    }
}
