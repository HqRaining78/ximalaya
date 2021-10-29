package com.hq.ximalaya;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.hq.ximalaya.adapters.AlbumListAdapter;
import com.hq.ximalaya.adapters.SearchRecommendAdapter;
import com.hq.ximalaya.interfaces.ISearchCallback;
import com.hq.ximalaya.presenters.AlbumDetailPresenter;
import com.hq.ximalaya.presenters.SearchPresenter;
import com.hq.ximalaya.utils.Constants;
import com.hq.ximalaya.views.FlowTextLayout;
import com.hq.ximalaya.views.UILoader;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchActivity extends BaseActivity implements ISearchCallback, SearchRecommendAdapter.ItemClickListener, AlbumListAdapter.OnAlbumItemClickListener {

    private View mBackBtn;
    private EditText mInputBox;
    private View mSearchBtn;
    private FrameLayout mResultContainer;
    private SearchPresenter mSearchPresenter;
    private UILoader mContent;
    private RecyclerView mResultListView;
    private AlbumListAdapter mAlbumListAdapter;
    private FlowTextLayout mFlowTextLayout;
    private InputMethodManager mInputMethodManager;
    private ImageView mDeleteBtn;
    private RecyclerView mSearchRecommendList;
    private SearchRecommendAdapter mRecommendAdapter;
    private SmartRefreshLayout mRefreshLayout;
    private boolean mIsNeedSuggestion = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initView();
        initEvent();
        initPresenter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSearchPresenter != null) {
            mSearchPresenter.unRegisterViewCallback(this);
            mSearchPresenter = null;
        }
    }

    private void initPresenter() {
        mInputMethodManager = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        mSearchPresenter = SearchPresenter.getInstance();
        mSearchPresenter.registerViewCallback(this);
        mSearchPresenter.getHotWord();
    }

    private void initEvent() {
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mInputBox.getText().toString().trim();
                if (TextUtils.isEmpty(text)) {
                    return;
                }
                if (mSearchPresenter != null) {
                    mSearchPresenter.doSearch(text);
                    if (mContent != null) {
                        mContent.updateUIStatus(UILoader.UIStates.LOADING);
                    }
                }
            }
        });

        mInputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    mSearchPresenter.getHotWord();
                    mDeleteBtn.setVisibility(View.GONE);
                } else {
                    mDeleteBtn.setVisibility(View.VISIBLE);
                    if (mIsNeedSuggestion) {
                        getSuggestionWord(s.toString());
                    } else {
                        mIsNeedSuggestion = true;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

            mFlowTextLayout.setClickListener(new FlowTextLayout.ItemClickListener() {
                @Override
                public void onItemClick(String text) {
                    mIsNeedSuggestion = false;
                    switch2Search(text);
                }
            });


            mDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mInputBox.setText("");
                }
            });


        if (mRecommendAdapter != null) {
            mRecommendAdapter.setItemClickListener(this);
        }

        mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (mSearchPresenter != null) {
                    mSearchPresenter.loadMore();
                }
            }
        });


mAlbumListAdapter.setAlbumItemClickListener(this);

    }

    private void getSuggestionWord(String keyword) {
        if (mSearchPresenter != null) {
            mSearchPresenter.getRecommendWord(keyword);
        }
    }

    private void initView() {
        mBackBtn = findViewById(R.id.search_back);
        mInputBox = findViewById(R.id.search_input);
        mSearchBtn = findViewById(R.id.search_btn);
        mResultContainer = findViewById(R.id.search_container);
        mDeleteBtn = findViewById(R.id.search_input_delete);
        mDeleteBtn.setVisibility(View.GONE);
        mInputBox.postDelayed(new Runnable() {
            @Override
            public void run() {
                mInputBox.requestFocus();
                mInputMethodManager.showSoftInput(mInputBox, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 500);

        if (mContent == null) {
            mContent = new UILoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView();
                }
            };

            if (mContent.getParent() instanceof ViewGroup) {
                ((ViewGroup) mContent.getParent()).removeView(mContent);
            }
            mResultContainer.addView(mContent);
            mContent.setOnRetryClickListener(new UILoader.OnRetryClickListener() {
                @Override
                public void onRetryClick() {
                    if (mSearchPresenter != null) {
                        mSearchPresenter.reSearch();
                        mContent.updateUIStatus(UILoader.UIStates.LOADING);
                    }
                }
            });

        }

    }

    private View createSuccessView() {
        View resultView = LayoutInflater.from(this).inflate(R.layout.search_result_layout, null);
        mRefreshLayout = resultView.findViewById(R.id.search_result_refresh_layout);
        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setRefreshFooter(new BallPulseFooter(this));
        mResultListView = resultView.findViewById(R.id.result_list_view);
        mFlowTextLayout = resultView.findViewById(R.id.recommend_hot_word_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mResultListView.setLayoutManager(layoutManager);
        mAlbumListAdapter = new AlbumListAdapter();
        mResultListView.setAdapter(mAlbumListAdapter);

        mResultListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 5);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 5);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });


        mSearchRecommendList = resultView.findViewById(R.id.search_recommend_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mSearchRecommendList.setLayoutManager(linearLayoutManager);
        mRecommendAdapter = new SearchRecommendAdapter();
        mSearchRecommendList.setAdapter(mRecommendAdapter);



        return resultView;
    }


    @Override
    public void onSearchResultLoaded(List<Album> result) {
        handleSearchResult(result);
        mInputMethodManager.hideSoftInputFromWindow(mInputBox.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void handleSearchResult(List<Album> result) {
        hideSuccessView();
        mRefreshLayout.setVisibility(View.VISIBLE);
        if (result != null) {
            if (result.size() == 0) {
                if (mContent != null) {
                    mContent.updateUIStatus(UILoader.UIStates.EMPTY);
                }
            } else {
                mAlbumListAdapter.setData(result);
                mContent.updateUIStatus(UILoader.UIStates.SUCCESS);
            }
        }
    }

    @Override
    public void onHotWordLoaded(List<HotWord> result) {
        hideSuccessView();
        mFlowTextLayout.setVisibility(View.VISIBLE);
        if (mContent != null) {
            mContent.updateUIStatus(UILoader.UIStates.SUCCESS);
        }
        List<String> hotWords = new ArrayList<>();
        for (HotWord hotWord : result) {
            String searchWord = hotWord.getSearchword();
            hotWords.add(searchWord);
        }
        Collections.sort(hotWords);
        mFlowTextLayout.setTextContents(hotWords);

    }

    @Override
    public void onLoadMoreResult(List<Album> result, boolean isOk) {
        if (mRefreshLayout != null) {
            mRefreshLayout.finishLoadMore();
        }
        if (isOk) {
            handleSearchResult(result);
        } else {
            Toast.makeText(SearchActivity.this, "没有更多内容了～", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRecommendWordLoaded(List<QueryResult> keyWordList) {
        if (mRecommendAdapter != null) {
            mRecommendAdapter.setData(keyWordList);
        }
        hideSuccessView();
        mSearchRecommendList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        if (mContent != null) {
            mContent.updateUIStatus(UILoader.UIStates.NETWORK_ERROR);
        }
        if (mContent != null) {
            mContent.updateUIStatus(UILoader.UIStates.SUCCESS);
        }


    }


    private void hideSuccessView() {
        mSearchRecommendList.setVisibility(View.GONE);
        mRefreshLayout.setVisibility(View.GONE);
        mFlowTextLayout.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(String keyword) {
        mIsNeedSuggestion = false;
        switch2Search(keyword);
    }

    private void switch2Search(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            return;
        }
        mInputBox.setText(keyword);
        mInputBox.setSelection(keyword.length());
        if (mSearchPresenter != null) {
            mSearchPresenter.doSearch(keyword);
        }
    }

    @Override
    public void onItemClick(int position, Album album) {
        AlbumDetailPresenter.getInstance().setTargetAlbum(album);
        Intent i = new Intent(this, DetailActivity.class);
        startActivity(i);
    }
}