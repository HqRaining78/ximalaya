package com.hq.ximalaya.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hq.ximalaya.R;

public class HistoryFragment extends BaseFragment{
    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        View rootView = layoutInflater.inflate(R.layout.fragment_history, container, false);

        getRecommendData();

        return rootView;
    }

    private void getRecommendData() {
    }
}