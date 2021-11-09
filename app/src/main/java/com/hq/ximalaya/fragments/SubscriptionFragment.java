package com.hq.ximalaya.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hq.ximalaya.DetailActivity;
import com.hq.ximalaya.R;
import com.hq.ximalaya.adapters.AlbumListAdapter;
import com.hq.ximalaya.base.BaseApplication;
import com.hq.ximalaya.interfaces.ISubscriptionCallback;
import com.hq.ximalaya.presenters.AlbumDetailPresenter;
import com.hq.ximalaya.presenters.SubscriptionPresenter;
import com.hq.ximalaya.utils.Constants;
import com.hq.ximalaya.views.ConfirmDialog;
import com.hq.ximalaya.views.UILoader;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class SubscriptionFragment extends BaseFragment implements ISubscriptionCallback, AlbumListAdapter.OnAlbumItemClickListener, AlbumListAdapter.OnAlbumItemLongClickListener, ConfirmDialog.OnDialogActionClickListener {

    private SubscriptionPresenter mSubscriptionPresenter;
    private RecyclerView mSubListView;
    private AlbumListAdapter mAlbumListAdapter;
    private ConfirmDialog mConfirmDialog;
    private Album mCurrentAlbum;
    private UILoader mUiLoader;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        FrameLayout rootView = (FrameLayout) layoutInflater.inflate(R.layout.fragment_subscription, container, false);
        if (mUiLoader == null) {
            mUiLoader = new UILoader(container.getContext()) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView(layoutInflater);
                }

                @Override
                public View getEmptyView() {
                    View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view, this, false);
                    TextView tipView = emptyView.findViewById(R.id.empty_tip_text);
                    tipView.setText("没有内容，赶紧去订阅吧～");
                    return emptyView;
                }
            };

            if (mUiLoader.getParent() instanceof ViewGroup) {
                ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
            }

            rootView.addView(mUiLoader);
        }

        return rootView;
    }

    private View createSuccessView(LayoutInflater layoutInflater) {
        View itemView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.item_subscription, null);

        mSubListView = itemView.findViewById(R.id.subscription_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(itemView.getContext());
        mSubListView.setLayoutManager(linearLayoutManager);
        mAlbumListAdapter = new AlbumListAdapter();
        mAlbumListAdapter.setAlbumItemClickListener(this);
        mAlbumListAdapter.setOnAlbumItemLongClickListener(this);
        mSubListView.setAdapter(mAlbumListAdapter);
        mSubListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 5);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 5);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });

        mSubscriptionPresenter = SubscriptionPresenter.getInstance();
        mSubscriptionPresenter.registerViewCallback(this);
        mSubscriptionPresenter.getSubscriptionList();

        if (mUiLoader != null) {
            mUiLoader.updateUIStatus(UILoader.UIStates.LOADING);
        }
        return itemView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscriptionPresenter != null) {
            mSubscriptionPresenter.unRegisterViewCallback(this);
        }
        if (mAlbumListAdapter != null) {
            mAlbumListAdapter.setAlbumItemClickListener(null);
        }
        if (mConfirmDialog != null) {
            mConfirmDialog.setOnDialogActionClickListener(null);
        }
    }

    @Override
    public void onAddResult(boolean isSuccess) {

    }

    @Override
    public void onDeleteResult(boolean isSuccess) {
        Toast.makeText(getActivity(), isSuccess ? R.string.cancel_sub_success : R.string.cancel_sub_failure, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSubscriptionLoaded(List<Album> result) {
        if (result == null || result.isEmpty()) {
            mUiLoader.updateUIStatus(UILoader.UIStates.EMPTY);
        } else {
            if (mAlbumListAdapter != null) {
                mAlbumListAdapter.setData(result);
            }
            mUiLoader.updateUIStatus(UILoader.UIStates.SUCCESS);
        }
    }

    @Override
    public void onSubFull() {
        Toast.makeText(getActivity(), "订阅数量不得超过" + Constants.MAX_SUB_COUNT, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(int position, Album album) {
        AlbumDetailPresenter.getInstance().setTargetAlbum(album);

        Intent intent = new Intent(getContext(), DetailActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(Album album) {
        this.mCurrentAlbum = album;
        mConfirmDialog = new ConfirmDialog(getActivity());
        mConfirmDialog.setOnDialogActionClickListener(this);
        mConfirmDialog.show();
    }

    @Override
    public void onCancelSubClick() {
        if (mCurrentAlbum != null && mSubscriptionPresenter != null) {
            mSubscriptionPresenter.deleteSubscription(mCurrentAlbum);
        }
    }

    @Override
    public void onGiveUpClick() {

    }
}
