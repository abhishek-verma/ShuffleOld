package com.inpen.shuffle.songListScreens;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inpen.shuffle.R;
import com.inpen.shuffle.model.AudioItem;

import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class SongListViewAdapter extends RecyclerView.Adapter<SongListViewAdapter.ViewHolder> {

    private final SongListFragment.OnListFragmentInteractionListener mListener;
    private List<AudioItem> mItemList;

    public SongListViewAdapter(List<AudioItem> items, SongListFragment.OnListFragmentInteractionListener listener) {
        mItemList = items;
        mListener = listener;
    }

    public void replaceData(List<AudioItem> itemList) {
        setList(itemList);
        notifyDataSetChanged();
    }

    private void setList(List<AudioItem> itemList) {
        mItemList = checkNotNull(itemList);
    }

    @Override
    public SongListViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_list_item, parent, false);
        return new SongListViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SongListViewAdapter.ViewHolder holder, int position) {
        holder.bind(mItemList.get(position));
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public AudioItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.songTitle);
        }

        public void bind(AudioItem item) {
            mItem = item;
            mTitleView.setText(mItem.getmTitle());

            mTitleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onListFragmentInteraction(mItem);
                    }
                }
            });
        }

    }
}
