package com.hq.ximalaya.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hq.ximalaya.PlayerActivity;
import com.hq.ximalaya.R;
import com.hq.ximalaya.adapters.DetailListAdapter;
import com.hq.ximalaya.base.BaseApplication;
import com.hq.ximalaya.interfaces.IHistoryCallback;
import com.hq.ximalaya.presenters.HistoryPresenter;
import com.hq.ximalaya.presenters.PlayPresenter;
import com.hq.ximalaya.views.ConfirmCheckBoxDialog;
import com.hq.ximalaya.views.UILoader;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class HistoryFragment extends BaseFragment implements IHistoryCallback, DetailListAdapter.ItemClickListener, DetailListAdapter.ItemLongClickListener, ConfirmCheckBoxDialog.OnDialogActionClickListener {

    private UILoader mUiLoader;
    private DetailListAdapter mTrackAdapter;
    private HistoryPresenter mHistoryPresenter;
    private Track mCurrentHistoryItem = null;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        FrameLayout rootView = (FrameLayout) layoutInflater.inflate(R.layout.fragment_history, container, false);

        if (mUiLoader == null) {
            mUiLoader = new UILoader(BaseApplication.getAppContext()) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView(container);
                }

                @Override
                public View getEmptyView() {
                    View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view, this, false);
                    TextView tipView = emptyView.findViewById(R.id.empty_tip_text);
                    tipView.setText("没有历史记录呀～");
                    return emptyView;                }
            };
        } else {
            if (mUiLoader.getParent() instanceof ViewGroup) {
                ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
            }
        }

        mHistoryPresenter = HistoryPresenter.getInstance();
        mHistoryPresenter.registerViewCallback(this);
        mUiLoader.updateUIStatus(UILoader.UIStates.LOADING);
        mHistoryPresenter.listHistories();
        rootView.addView(mUiLoader);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHistoryPresenter != null) {
            mHistoryPresenter.unRegisterViewCallback(this);
        }
    }

    private View createSuccessView(ViewGroup container) {
        View successView = LayoutInflater.from(container.getContext()).inflate(R.layout.item_history, container, false);
        RecyclerView historyList = successView.findViewById(R.id.history_list);
        historyList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 5);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 5);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });
        historyList.setLayoutManager(new LinearLayoutManager(container.getContext()));
        mTrackAdapter = new DetailListAdapter();
        historyList.setAdapter(mTrackAdapter);
        mTrackAdapter.setItemClickListener(this);
        mTrackAdapter.setItemLongClickListener(this);
        return successView;
    }


    @Override
    public void onHistoryLoaded(List<Track> tracks) {
        if (tracks == null || tracks.isEmpty()) {
            mUiLoader.updateUIStatus(UILoader.UIStates.EMPTY);
        } else {
            if (mTrackAdapter != null) {
                mTrackAdapter.setData(tracks);
                mUiLoader.updateUIStatus(UILoader.UIStates.SUCCESS);
            }
        }
    }

    @Override
    public void onItemClick(List<Track> detailData, int position) {
        PlayPresenter playPresenter = PlayPresenter.getPlayPresenter();
        playPresenter.setPlayList(detailData, position);
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(Track track) {
        this.mCurrentHistoryItem = track;

        ConfirmCheckBoxDialog dialog = new ConfirmCheckBoxDialog(getContext());
        dialog.setOnDialogActionClickListener(this);
        dialog.show();
    }

    @Override
    public void onCancelSubClick() {

    }

    @Override
    public void onGiveUpClick(boolean checked) {
        if (mCurrentHistoryItem != null && mHistoryPresenter != null) {
            if (!checked) {
                mHistoryPresenter.delHistory(mCurrentHistoryItem);
            } else {
                mHistoryPresenter.clearHistories();
            }
        }
    }
}
