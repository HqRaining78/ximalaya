package com.hq.ximalaya.views;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hq.ximalaya.R;
import com.hq.ximalaya.adapters.PlayerListAdapter;
import com.hq.ximalaya.base.BaseApplication;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;

public class SopPopWindow extends PopupWindow {

    private final View mPopView;
    private View mCloseBtn;
    private RecyclerView mTrackList;
    private PlayerListAdapter mPlayerListAdapter;
    private TextView mPlayModeTv;
    private ImageView mPlayModeIv;
    private View mPlayModeContainer;
    private PlayListPlayModeClickListener mPlayModeListener = null;
    private View mOrderBtnContainer;
    private TextView mPlayOrderTv;
    private ImageView mPlayOrderIv;

    public SopPopWindow() {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setOutsideTouchable(true);
        mPopView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.pop_play_list, null);
        setContentView(mPopView);

        // 设置窗口进入、退出的动画
        setAnimationStyle(R.style.pop_animation);

        initView();
        initEvent();
    }

    private void initEvent() {
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mPlayModeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayModeListener != null) {
                    mPlayModeListener.onPlayModeClick();
                }
            }
        });

        mOrderBtnContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayModeListener != null) {
                    mPlayModeListener.onOrderClick();
                }
            }
        });
    }


    private void initView() {
        mCloseBtn = mPopView.findViewById(R.id.play_list_close_btn);
        mTrackList = mPopView.findViewById(R.id.play_list_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(BaseApplication.getAppContext());
        mTrackList.setLayoutManager(layoutManager);
        mPlayerListAdapter = new PlayerListAdapter();
        mTrackList.setAdapter(mPlayerListAdapter);

        mPlayModeTv = mPopView.findViewById(R.id.play_list_play_mode_tv);
        mPlayModeIv = mPopView.findViewById(R.id.play_list_play_mode_iv);
        mPlayModeContainer = mPopView.findViewById(R.id.play_list_play_mode_container);

        mOrderBtnContainer = mPopView.findViewById(R.id.play_list_order_container);
        mPlayOrderTv = mPopView.findViewById(R.id.play_list_order_tv);
        mPlayOrderIv = mPopView.findViewById(R.id.play_list_order_iv);

    }

    public void setListData(List<Track> data) {
        if (mPlayerListAdapter != null) {
            mPlayerListAdapter.setData(data);
        }
    }

    public void setCurrentPlayPosition(int position) {
        if (mPlayerListAdapter != null) {
            mPlayerListAdapter.setCurrentPlayPosition(position);
            mTrackList.scrollToPosition(position);
        }
    }
    
    public void setPlayListItemClickListener(PlayListItemClickListener listener) {
        mPlayerListAdapter.setOnItemClickListener(listener);
    }

    public void updatePlayModel(XmPlayListControl.PlayMode mode) {
        updatePlayModelBtnImage(mode);
    }

    public void updateOrderIcon(boolean isReverse) {
        int textId = !isReverse ? R.string.play_order_ascend : R.string.play_order_descend;
        mPlayOrderTv.setText(textId);
        mPlayOrderIv.setImageResource(!isReverse ? R.drawable.selector_player_asec : R.drawable.selector_player_descend);

    }

    private void updatePlayModelBtnImage(XmPlayListControl.PlayMode playMode) {
        int resId = R.drawable.selector_player_descend;
        int textId = R.string.play_mode_order_text;
        switch (playMode) {
            case PLAY_MODEL_LIST:
                resId = R.drawable.selector_player_descend;
                textId = R.string.play_mode_order_text;
                break;
            case PLAY_MODEL_LIST_LOOP:
                resId = R.drawable.selector_player_loop;
                textId = R.string.play_mode_list_play_text;
                break;
            case PLAY_MODEL_RANDOM:
                resId = R.drawable.selector_player_random;
                textId = R.string.play_mode_random_text;
                break;
            case PLAY_MODEL_SINGLE_LOOP:
                resId = R.drawable.selector_player_single_loop;
                textId = R.string.play_mode_single_play_text;
                break;
        }
        mPlayModeIv.setImageResource(resId);
        mPlayModeTv.setText(textId);
    }

    public interface PlayListItemClickListener {
        void itemClickAction(int index);
    }

    public void setPlayListPlayModeClickListener(PlayListPlayModeClickListener listener) {
        mPlayModeListener = listener;
    }

    public interface PlayListPlayModeClickListener {
        void onPlayModeClick();
        void onOrderClick();
    }

    
}
