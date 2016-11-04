package com.inpen.shuffle.customviews;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inpen.shuffle.R;
import com.inpen.shuffle.mainscreen.Item;

/**
 * Created by Abhishek on 11/2/2016.
 */

public class ItemView extends FrameLayout {
    private Item mItem;

    private TextView mTitleTextView;
    private RelativeLayout mParentView;


    public ItemView(Context context) {
        super(context);

        initView();
    }

    private void initView() {
        mParentView = (RelativeLayout) inflate(getContext(), R.layout.item_view_layout, null);
        mTitleTextView = (TextView) mParentView.findViewById(R.id.itemTitle);
        addView(mParentView);
    }

    public Item getItem(){
        return mItem;
    }

    public void setItem(Item item) {
        mItem = item;

        if(mTitleTextView!=null)
            mTitleTextView.setText(mItem.getTitle());

        if(mItem.isSelected()){
            mParentView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.black));
        } else {
            mParentView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey));
        }
    }

}
