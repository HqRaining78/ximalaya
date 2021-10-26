package com.hq.ximalaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hq.ximalaya.R;
import com.hq.ximalaya.base.BaseApplication;
import com.hq.ximalaya.views.SopPopWindow;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class PlayerListAdapter extends RecyclerView.Adapter<PlayerListAdapter.PlayerListHolder> {
   private List<Track> mData = new ArrayList<>();
   private  int playingIndex = 0;
    private SopPopWindow.PlayListItemClickListener mOnItemClickListener;

    @NonNull
    @Override
    public PlayerListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_play_list, parent, false);
        return new PlayerListHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerListHolder holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.itemClickAction(position);
                }
            }
        });

        Track track = mData.get(position);
        TextView trackTitleTv = holder.itemView.findViewById(R.id.track_title_tv);
        trackTitleTv.setText(track.getTrackTitle());
        trackTitleTv.setTextColor(BaseApplication.getAppContext().getColor(playingIndex == position ? R.color.second_color : R.color.play_list_text_color));

        ImageView playingIconView = holder.itemView.findViewById(R.id.play_icon_iv);
        playingIconView.setVisibility(playingIndex == position ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<Track> data) {
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void setCurrentPlayPosition(int position) {
        playingIndex = position;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(SopPopWindow.PlayListItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }


    public static class PlayerListHolder extends RecyclerView.ViewHolder {
        public PlayerListHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}

