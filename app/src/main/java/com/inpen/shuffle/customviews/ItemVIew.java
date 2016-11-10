package com.inpen.shuffle.customviews;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.inpen.shuffle.R;
import com.inpen.shuffle.mainscreen.Item;

/**
 * Created by Abhishek on 11/2/2016.
 */

public class ItemView extends FrameLayout {
    private float mAspectRatio = 1f;

    private Item mItem;

    private CardView mParentView;
    private TextView mTitleTextView;
    private SquareImageView mAlbumArtView;
    private SquareImageView mPlaceholderView;
    private View mSelectedHighlighterView;

    private int mWidth;
    private int mHeight;

    public ItemView(Context context) {
        super(context);

        initView();
    }

    private void initView() {
        mParentView = (CardView) inflate(getContext(), R.layout.item_view_layout, null);

        mTitleTextView = (TextView) mParentView.findViewById(R.id.itemTitle);
        mAlbumArtView = (SquareImageView) mParentView.findViewById(R.id.albumArt);
        mPlaceholderView = (SquareImageView) mParentView.findViewById(R.id.placeholderView);
        mSelectedHighlighterView = mParentView.findViewById(R.id.selectedHighlighterView);
        addView(mParentView);
    }

    public Item getItem() {
        return mItem;
    }

    public void setItem(Item item) {
        mItem = item;

        if (mTitleTextView != null)
            mTitleTextView.setText(mItem.getTitle());

        if (mAlbumArtView != null) {
            if (mItem.getImagePath().equals(""))
                Glide.with(getContext()).load(R.drawable.all_music).
                        into(mAlbumArtView);
            else
                Glide.with(getContext()).load(mItem.getImagePath())
                        .error(R.drawable.ph_album_art)
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
//                                mAlbumArtView.setImageDrawable(null);
                                mAlbumArtView.setVisibility(INVISIBLE);
                                mPlaceholderView.setVisibility(VISIBLE);
                                return true;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                                mPlaceholderView.setImageDrawable(null);
                                mPlaceholderView.setVisibility(INVISIBLE);
                                mAlbumArtView.setVisibility(VISIBLE);
                                return false;
                            }
                        })
                        .into(mAlbumArtView);
        }

        if (mItem.isSelected()) {
            mTitleTextView.setBackgroundColor(getResources().getColor(R.color.grey));
//            mSelectedHighlighterView.setVisibility(VISIBLE);
        } else {
            mTitleTextView.setBackgroundColor(getResources().getColor(R.color.lt_grey));
//            mSelectedHighlighterView.setVisibility(INVISIBLE);
        }

    }

}
