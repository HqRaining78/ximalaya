package com.hq.ximalaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hq.ximalaya.R;
import com.hq.ximalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.ArrayList;
import java.util.List;

public class RecommendListAdapter extends RecyclerView.Adapter<RecommendListAdapter.RecommendViewHolder> {
    public static final String TAG = "RecommendListAdapter";
    private final List<Album> mAlbumList = new ArrayList<>();
    private OnRecommendItemClickListener mListener;

    @Override
    public RecommendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommend, parent, false);
        return new RecommendViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecommendViewHolder holder, int position) {
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.d(TAG, "click ----->" + v.getTag());
                if (mListener != null) {
                    int index = (int)v.getTag();
                    mListener.onItemClick(index, mAlbumList.get(index));
                }
            }
        });
        holder.setData(mAlbumList.get(position));
    }

    @Override
    public int getItemCount() {
        return mAlbumList.size();
    }

    public void setData(List<Album> albumList) {
        if (albumList != null) {
            mAlbumList.clear();
            mAlbumList.addAll(albumList);
        }
        notifyDataSetChanged();
    }

    public static class RecommendViewHolder extends RecyclerView.ViewHolder {
        public RecommendViewHolder(View itemView) {
            super(itemView);
        }

        public void setData(Album album) {
            ImageView albumCoverIv = itemView.findViewById(R.id.album_cover);
            TextView albumTitleTv = itemView.findViewById(R.id.album_title_tv);
            TextView albumDescTv = itemView.findViewById(R.id.album_description_tv);
            TextView albumPlayCountTv = itemView.findViewById(R.id.album_play_count);
            TextView albumContentCountTv = itemView.findViewById(R.id.album_content_size);

            Glide.with(itemView.getContext()).load(album.getCoverUrlSmall()).into(albumCoverIv);
            albumTitleTv.setText(album.getAlbumTitle());
            albumDescTv.setText(album.getAlbumIntro());
            albumPlayCountTv.setText(album.getPlayCount() + "");
            albumContentCountTv.setText(album.getIncludeTrackCount() + "");
        }
    }

    public void setOnRecommendItemClickListener(OnRecommendItemClickListener listener) {
        this.mListener = listener;
    }
    public interface OnRecommendItemClickListener {
        public void onItemClick(int position, Album album);
    }
}

