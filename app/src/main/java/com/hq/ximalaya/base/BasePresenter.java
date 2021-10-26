package com.hq.ximalaya.base;

import com.hq.ximalaya.interfaces.IRecommendCallback;

public interface BasePresenter<T> {

    void registerViewCallback(T t);

    void unRegisterViewCallback(T t);
}
