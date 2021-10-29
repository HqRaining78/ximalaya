package com.hq.ximalaya;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hq.ximalaya.adapters.DetailListAdapter;
import com.hq.ximalaya.interfaces.IAlbumDetailViewCallback;
import com.hq.ximalaya.interfaces.IPlayerCallback;
import com.hq.ximalaya.interfaces.ISubscriptionCallback;
import com.hq.ximalaya.presenters.AlbumDetailPresenter;
import com.hq.ximalaya.presenters.PlayPresenter;
import com.hq.ximalaya.presenters.SubscriptionPresenter;
import com.hq.ximalaya.utils.ImageBlur;
import com.hq.ximalaya.views.RoundRectImageView;
import com.hq.ximalaya.views.UILoader;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class DetailActivity extends BaseActivity implements IAlbumDetailViewCallback, UILoader.OnRetryClickListener, DetailListAdapter.ItemClickListener, IPlayerCallback, ISubscriptionCallback {
    private ImageView mLargerCover;
    private RoundRectImageView mSmallCover;
    private TextView mAlbumTitle;
    private TextView mAlbumAuthor;
    private AlbumDetailPresenter mDetailPresenter;
    private int mCurrentPage = 1;
    private RecyclerView mDetailList;
    private DetailListAdapter mDetailListAdapter;
    private FrameLayout mDetailListContainer;
    private UILoader mUiLoader;
    private long mCurrentId = -1;
    private ImageView mPlayControl;
    private TextView mPlayControlTips;
    private PlayPresenter mPlayPresenter;
    private List<Track> mCurrentTracks;
    public static final int DEFAULT_PLAY_INDEX = 0;
    private SmartRefreshLayout mRefreshLayout;
    private String mTrackTitle = null;
    private TextView mSubBtn;
    private SubscriptionPresenter mSubscriptionPresenter;
    private Album mCurrentAlbum = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        initView();
        initPresenter();
        updateSubState();
        updatePlayState(mPlayPresenter.isPlay());
        initEvent();
    }

    private void updateSubState() {
        if (mSubscriptionPresenter != null) {
            boolean isSub = mSubscriptionPresenter.isSub(mCurrentAlbum);
            mSubBtn.setText(isSub ? R.string.cancel_sub_tips_text : R.string.sub_tips_text);
        }
    }

    private void initPresenter() {
        mDetailPresenter = AlbumDetailPresenter.getInstance();
        mDetailPresenter.registerViewCallback(this);
        mPlayPresenter = PlayPresenter.getPlayPresenter();
        mPlayPresenter.registerViewCallback(this);
        mSubscriptionPresenter = SubscriptionPresenter.getInstance();
        mSubscriptionPresenter.registerViewCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayPresenter != null) {
            mPlayPresenter.unRegisterViewCallback(this);
            mPlayPresenter = null;
        }

        if (mSubscriptionPresenter != null) {
            mSubscriptionPresenter.unRegisterViewCallback(this);
            mSubscriptionPresenter = null;
        }

        if (mDetailPresenter != null) {
            mDetailPresenter.unRegisterViewCallback(this);
            mDetailPresenter = null;
        }
    }

    private void updatePlayState(boolean play) {
        if (mPlayControl != null && mPlayControlTips != null) {
            mPlayControl.setImageResource(play ? R.drawable.selector_player_pause : R.drawable.selector_player_play);

            if (!play) {
                mPlayControlTips.setText(R.string.pause_tips_text);
            } else {
                if (!TextUtils.isEmpty(mTrackTitle)) {
                    mPlayControlTips.setText(mTrackTitle);
                }
            }
        }
    }

    private void initEvent() {
        mPlayControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayPresenter != null) {
                    boolean hasPlayList = mPlayPresenter.hasPlayList();
                    if (hasPlayList) {
                        handleNoPlayList();
                    } else {
                        handlePlayControl();
                    }
                }
            }
        });


        mSubBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });
    }

    private void handleNoPlayList() {
        mPlayPresenter.setPlayList(mCurrentTracks, DEFAULT_PLAY_INDEX);
    }

    private void handlePlayControl() {
        if (mPlayPresenter.isPlay()) {
            mPlayPresenter.pause();
        } else {
            mPlayPresenter.play();
        }
    }

    private void initView() {
        mDetailListContainer = findViewById(R.id.detail_list_container);
        if (mUiLoader == null) {
            mUiLoader = new UILoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView(container);
                }
            };
            mDetailListContainer.removeAllViews();
            mDetailListContainer.addView(mUiLoader);
            mUiLoader.setOnRetryClickListener(this);
        }

        mLargerCover = findViewById(R.id.cover_bg);
        mSmallCover = findViewById(R.id.viv_small_cover);
        mAlbumTitle = findViewById(R.id.tv_album_title);
        mAlbumAuthor = findViewById(R.id.tv_album_author);
        mPlayControl = findViewById(R.id.detail_play_control);
        mPlayControlTips = findViewById(R.id.play_control_tv);
        mPlayControlTips.setSelected(true);

        mSubBtn = findViewById(R.id.detail_sub_btn);

    }

    private View createSuccessView(ViewGroup container) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_detail_list, container, false);
        mDetailList = view.findViewById(R.id.album_detail_list);
        mRefreshLayout = view.findViewById(R.id.refreshLayout);
        
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mDetailList.setLayoutManager(linearLayoutManager);
        // 设置适配器
        mDetailListAdapter = new DetailListAdapter();
        mDetailList.setAdapter(mDetailListAdapter);
        mDetailList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 2);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 2);
                outRect.left = UIUtil.dip2px(view.getContext(), 2);
                outRect.right = UIUtil.dip2px(view.getContext(), 2);
            }
        });
        mDetailListAdapter.setItemClickListener(this);

        mRefreshLayout.setRefreshHeader(new ClassicsHeader(this));
        mRefreshLayout.setRefreshFooter(new ClassicsFooter(this));
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                if (mDetailPresenter != null) {
                    mDetailPresenter.pull2RefreshMore();
                }
            }
        });
        mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                if (mDetailPresenter != null) {
                    mDetailPresenter.loadMore();
                }
            }
        });
        return view;
    }

    @Override
    public void onDetailListLoaded(List<Track> result) {
        if (mRefreshLayout !=null && mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh();
        }
        if (mRefreshLayout !=null && mRefreshLayout.isLoading()) {
            mRefreshLayout.finishLoadMore();
        }
        mCurrentTracks = result;
        if (mUiLoader != null) {
            if (result == null || result.size() == 0) {
                mUiLoader.updateUIStatus(UILoader.UIStates.EMPTY);
            } else {
                mUiLoader.updateUIStatus(UILoader.UIStates.SUCCESS);
            }
        }
        mDetailListAdapter.setData(result);
    }

    @Override
    public void onAlbumLoaded(Album album) {
        mCurrentAlbum = album;
        mCurrentId = album.getId();
        if (mDetailPresenter != null) {
            mDetailPresenter.getAlbumDetail(album.getId(), mCurrentPage);
        }
        if (mUiLoader != null) {
            mUiLoader.updateUIStatus(UILoader.UIStates.LOADING);
        }
        if (mAlbumTitle != null) {
            mAlbumTitle.setText(album.getAlbumTitle());
        }
        if (mAlbumAuthor != null) {
            mAlbumAuthor.setText(album.getAnnouncer().getNickname());
        }
        if (mLargerCover != null) {
            Picasso.with(this).load(album.getCoverUrlLarge()).into(mLargerCover, new Callback() {
                @Override
                public void onSuccess() {
                    Drawable drawable = mLargerCover.getDrawable();
                    if (drawable != null) {
                        ImageBlur.makeBlur(mLargerCover, DetailActivity.this);
                    }
                }

                @Override
                public void onError() {
                }
            });
        }

        if (mSmallCover != null) {
            Picasso.with(this).load(album.getAnnouncer().getAvatarUrl()).into(mSmallCover);
        }

    }

    @Override
    public void onNetworkError(int errorCode, String errorMsg) {
        if (mUiLoader != null) {
            mUiLoader.updateUIStatus(UILoader.UIStates.NETWORK_ERROR);
        }
    }

    @Override
    public void onLoaderMoreFinish(int size) {
        if (size > 0) {

        } else {

        }
    }

    @Override
    public void onRefreshFinish(int size) {

    }


    @Override
    public void onRetryClick() {
        if (mDetailPresenter != null) {
            mDetailPresenter.getAlbumDetail(mCurrentId, mCurrentPage);
        }
    }

    @Override
    public void onItemClick(List<Track> detailData, int position) {
        PlayPresenter playPresenter = PlayPresenter.getPlayPresenter();
        playPresenter.setPlayList(detailData, position);
        Intent i = new Intent(this, PlayerActivity.class);
        startActivity(i);
    }

    @Override
    public void onPlayStart() {
        updatePlayState(true);
    }

    @Override
    public void onPlayPause() {
        updatePlayState(false);
    }

    @Override
    public void onPlayStop() {
       updatePlayState(false);
    }

    @Override
    public void onPlayError() {

    }

    @Override
    public void nextPlay() {

    }

    @Override
    public void onPrePlay() {

    }

    @Override
    public void onListLoaded(List<Track> list) {

    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode mode) {

    }

    @Override
    public void onProgressChange(int currentProgress, int total) {

    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }

    @Override
    public void onTrackUpdate(Track track, int position) {
        if (track != null) {
            mTrackTitle = track.getTrackTitle();
            if (!TextUtils.isEmpty(mTrackTitle) && mPlayControlTips != null) {
                mPlayControlTips.setText(mTrackTitle);
            }
        }
    }

    @Override
    public void updateListOrder(boolean isReverse) {

    }

    @Override
    public void onAddResult(boolean isSuccess) {

    }

    @Override
    public void onDeleteResult(boolean isSuccess) {

    }

    @Override
    public void onSubscriptionLoaded(List<Album> result) {

    }
}
