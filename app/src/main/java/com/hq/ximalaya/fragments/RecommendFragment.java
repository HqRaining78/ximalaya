package com.hq.ximalaya.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hq.ximalaya.DetailActivity;
import com.hq.ximalaya.R;
import com.hq.ximalaya.adapters.RecommendListAdapter;
import com.hq.ximalaya.interfaces.IRecommendCallback;
import com.hq.ximalaya.presenters.AlbumDetailPresenter;
import com.hq.ximalaya.presenters.RecommendPresenter;
import com.hq.ximalaya.views.UILoader;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class RecommendFragment extends BaseFragment implements IRecommendCallback, UILoader.OnRetryClickListener, RecommendListAdapter.OnRecommendItemClickListener {
    private static final String TAG = "RecommendFragment";
    private View mRootView;
    private RecyclerView mRecommendListView;
    private RecommendListAdapter mRecommendListAdapter;
    private RecommendPresenter mRecommendPresenter;
    private UILoader mUiLoader;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        mUiLoader = new UILoader(getContext()) {
            @Override
            protected View getSuccessView(ViewGroup container) {
                return createSuccessView(layoutInflater, container);
            }
        };

        mRecommendPresenter = RecommendPresenter.getInstance();
        mRecommendPresenter.registerViewCallback(this);
        mRecommendPresenter.getRecommendList();

        if (mUiLoader.getParent() instanceof ViewGroup) {
            ((ViewGroup)mUiLoader.getParent()).removeView(mUiLoader);
        }

        mUiLoader.setOnRetryClickListener(this);
        return mUiLoader;
    }

    private View createSuccessView(LayoutInflater layoutInflater, ViewGroup container) {
        mRootView = layoutInflater.inflate(R.layout.fragment_recommend, container, false);

        mRecommendListView = mRootView.findViewById(R.id.recommend_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecommendListView.setLayoutManager(linearLayoutManager);
        mRecommendListAdapter = new RecommendListAdapter();
        mRecommendListAdapter.setOnRecommendItemClickListener(this);
        mRecommendListView.setAdapter(mRecommendListAdapter);

        mRecommendListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 5);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 5);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });


        return mRootView;
    }

    @Override
    public void onRecommendListLoaded(List<Album> result) {
        mRecommendListAdapter.setData(result);
        mUiLoader.updateUIStatus(UILoader.UIStates.SUCCESS);
    }

    @Override
    public void onLoaderMore(List<Album> result) {
    }

    @Override
    public void onRefreshMore(List<Album> result) {

    }

    @Override
    public void onNetworkError() {
        mUiLoader.updateUIStatus(UILoader.UIStates.NETWORK_ERROR);
    }

    @Override
    public void onEmpty() {
        mUiLoader.updateUIStatus(UILoader.UIStates.EMPTY);
    }

    @Override
    public void onLoading() {
        mUiLoader.updateUIStatus(UILoader.UIStates.LOADING);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRecommendPresenter != null) {
            mRecommendPresenter.unRegisterViewCallback(this);
        }
    }

    @Override
    public void onRetryClick() {
        if (mRecommendPresenter != null) {
            mRecommendPresenter.getRecommendList();
        }
    }

    @Override
    public void onItemClick(int position, Album album) {
        AlbumDetailPresenter.getInstance().setTargetAlbum(album);
        Intent i = new Intent(getContext(), DetailActivity.class);
        startActivity(i);
    }
}
