package com.hq.ximalaya;

import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.hq.ximalaya.adapters.PlayerTrackPagerAdapter;
import com.hq.ximalaya.interfaces.IPlayerCallback;
import com.hq.ximalaya.presenters.PlayPresenter;
import com.hq.ximalaya.utils.LogUtil;
import com.hq.ximalaya.views.SopPopWindow;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerActivity extends BaseActivity implements IPlayerCallback, ViewPager.OnPageChangeListener {
    public static final String TAG = "PlayerActivity";

    private ImageView mControlBtn;
    private PlayPresenter mPlayPresenter;
    private SimpleDateFormat mMinFormat = new SimpleDateFormat("mm:ss");
    private SimpleDateFormat mHourFormat = new SimpleDateFormat("HH:mm:ss");
    private TextView mTotalDuration;
    private TextView mCurrentDuration;
    private SeekBar mProgress;
    private int mCurrentProgress = 0;
    private boolean mIsUserTouch = false;
    private ImageView mPlayPreviousBtn;
    private ImageView mPlayNextBtn;
    private TextView mTrackTitle;
    private String mTrackTitleString;
    private ViewPager mTrackPagerView;
    private PlayerTrackPagerAdapter mTrackPagerAdapter;
    private boolean mIsUserSlidePage = false;
    private ImageView mPlayModeSwitchBtn;
    private ImageView mPlayerListBtn;

    private XmPlayListControl.PlayMode mCurrentModel = XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
    private static Map<XmPlayListControl.PlayMode, XmPlayListControl.PlayMode> sPlayModelsRule = new HashMap<>();
    static {
        sPlayModelsRule.put(XmPlayListControl.PlayMode.PLAY_MODEL_LIST, XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP);
        sPlayModelsRule.put(XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP, XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM);
        sPlayModelsRule.put(XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM, XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP);
        sPlayModelsRule.put(XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP, XmPlayListControl.PlayMode.PLAY_MODEL_LIST);
    }

    private SopPopWindow mPopWindow;
    private ValueAnimator mEnterBgAnimator;
    private ValueAnimator mOutBgAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initView();

        mPlayPresenter = PlayPresenter.getPlayPresenter();
        mPlayPresenter.registerViewCallback(this);
        mPlayPresenter.getPlayList();
        initEvent();
        initBgAnimation();
    }

    private void initBgAnimation() {
        mEnterBgAnimator = ValueAnimator.ofFloat(1.0f, 0.8f);
        mEnterBgAnimator.setDuration(300);
        mEnterBgAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LogUtil.d(TAG, "content value animator ---> " + animation.getAnimatedValue());
                float value = (float) animation.getAnimatedValue();
                updateBgAlpha(value);
            }
        });


        mOutBgAnimator = ValueAnimator.ofFloat(0.8f, 1.0f);
        mOutBgAnimator.setDuration(300);
        mOutBgAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LogUtil.d(TAG, "content value animator ---> " + animation.getAnimatedValue());
                float value = (float) animation.getAnimatedValue();
                updateBgAlpha(value);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayPresenter != null) {
            mPlayPresenter.unRegisterViewCallback(this);
            mPlayPresenter = null;
        }
    }

    private void initEvent() {
        mControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayPresenter.isPlay()) {
                    mPlayPresenter.pause();;
                } else {
                    mPlayPresenter.play();
                }
            }
        });

        mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mCurrentProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsUserTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mIsUserTouch = false;
                mPlayPresenter.seekTo(mCurrentProgress);
            }
        });

        mPlayPreviousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayPresenter != null) {
                    mPlayPresenter.playPre();
                }
            }
        });

        mPlayNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayPresenter != null) {
                    mPlayPresenter.playNext();
                }
            }
        });

        mTrackPagerView.addOnPageChangeListener(this);

        mTrackPagerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mIsUserSlidePage = true;
                    break;
                    case MotionEvent.ACTION_UP:
                        mIsUserSlidePage = false;
                    break;
                }
                return false;
            }
        });


        mPlayModeSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchPlayMode();
            }
        });


        mPlayerListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopWindow.showAtLocation(v, Gravity.BOTTOM, 0 ,0);
                mEnterBgAnimator.start();
            }
        });

        mPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mOutBgAnimator.start();
            }
        });

        mPopWindow.setPlayListItemClickListener(new SopPopWindow.PlayListItemClickListener() {
            @Override
            public void itemClickAction(int index) {
                if (mPlayPresenter != null) {
                    mPlayPresenter.playByIndex(index);
                }
            }
        });

        mPopWindow.setPlayListPlayModeClickListener(new SopPopWindow.PlayListPlayModeClickListener() {
            @Override
            public void onPlayModeClick() {
                switchPlayMode();
            }

            @Override
            public void onOrderClick() {
                if (mPlayPresenter != null) {
                    mPlayPresenter.reversePlayList();
                }
            }
        });
    }

    private void switchPlayMode() {
        XmPlayListControl.PlayMode playMode = sPlayModelsRule.get(mCurrentModel);
        if (mPlayPresenter != null) {
            mPlayPresenter.switchPlayMode(playMode);
        }
    }

    public void updateBgAlpha(float alpha) {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.alpha = alpha;
        window.setAttributes(attributes);
    }

    private void updatePlayModelBtnImage() {
        int resId = R.drawable.selector_player_descend;
        switch (mCurrentModel) {
            case PLAY_MODEL_LIST:
                resId = R.drawable.selector_player_descend;
                break;
            case PLAY_MODEL_LIST_LOOP:
                resId = R.drawable.selector_player_loop;
                break;
            case PLAY_MODEL_RANDOM:
                resId = R.drawable.selector_player_random;
                break;
            case PLAY_MODEL_SINGLE_LOOP:
                resId = R.drawable.selector_player_single_loop;
                break;
        }
        mPlayModeSwitchBtn.setImageResource(resId);
    }

    private void initView() {
        mControlBtn = findViewById(R.id.play_or_pause_btn);
        mTotalDuration = findViewById(R.id.track_duration);
        mCurrentDuration = findViewById(R.id.current_position);
        mProgress = findViewById(R.id.track_seek_bar);
        mPlayPreviousBtn = findViewById(R.id.player_previous);
        mPlayNextBtn = findViewById(R.id.player_next);
        mTrackTitle = findViewById(R.id.track_title);
        if (!TextUtils.isEmpty(mTrackTitleString)) {
            mTrackTitle.setText(mTrackTitleString);
        }
        mTrackPagerView = findViewById(R.id.track_pager_view);
        mTrackPagerAdapter = new PlayerTrackPagerAdapter();
        mTrackPagerView.setAdapter(mTrackPagerAdapter);

        mPlayModeSwitchBtn = findViewById(R.id.player_model_switch_btn);
        mPlayerListBtn = findViewById(R.id.player_list_btn);

        mPopWindow = new SopPopWindow();
    }


    @Override
    public void onPlayStart() {
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_stop);
        }
    }

    @Override
    public void onPlayPause() {
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_play);
        }
    }

    @Override
    public void onPlayStop() {

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
        if (mTrackPagerAdapter != null) {
            mTrackPagerAdapter.setData(list);
        }

        if (mPopWindow != null) {
            mPopWindow.setListData(list);
        }
    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode mode) {
        mCurrentModel = mode;
        mPopWindow.updatePlayModel(mode);
        updatePlayModelBtnImage();
    }

    @Override
    public void onProgressChange(int currentProgress, int total) {
        if (mProgress != null) {
            mProgress.setMax(total);
        }
        String totalDuration = "";
        String currentDuration = "";
        if (total > 1000 * 60 * 60) {
           totalDuration = mHourFormat.format(total);
           currentDuration = mHourFormat.format(currentProgress);
        } else {
            totalDuration = mMinFormat.format(total);
            currentDuration = mMinFormat.format(currentProgress);
        }
        if (mTotalDuration != null) {
            mTotalDuration.setText(totalDuration);
        }
        if (mCurrentDuration != null) {
            mCurrentDuration.setText(currentDuration);
        }


        if (!mIsUserTouch) {
//            int percent = (int)(currentProgress * 1.0 / total * 100);
            if (mProgress != null) {
                mProgress.setProgress(currentProgress);
            }
        }
    }

    @Override
    public void updateListOrder(boolean isReverse) {
        if (mPopWindow != null) {
            mPopWindow.updateOrderIcon(isReverse);
        }
    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }

    @Override
    public void onTrackUpdate(Track track, int position) {
        mTrackTitleString = track.getTrackTitle();
        if (mTrackTitle != null) {
            mTrackTitle.setText(mTrackTitleString);
        }

        if (mTrackPagerView != null) {
            mTrackPagerView.setCurrentItem(position, true);
        }

        if (mPopWindow != null) {
            mPopWindow.setCurrentPlayPosition(position);
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        // 当页面选中的时候，就切换播放内容
        if (mPlayPresenter != null && mIsUserSlidePage) {
            mPlayPresenter.playByIndex(position);
        }
        mIsUserSlidePage = false;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
