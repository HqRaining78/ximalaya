package com.hq.ximalaya.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;


import androidx.appcompat.widget.AppCompatImageView;

public class RoundRectImageView extends AppCompatImageView {
    private float mRoundRatio = 0.1f;
    private Path mPath;

    public RoundRectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mPath == null) {
            mPath = new Path();
            mPath.addRoundRect(new RectF(0, 0 , getWidth(), getHeight()), mRoundRatio * getWidth(), mRoundRatio * getHeight(), Path.Direction.CW);
        }
        canvas.save();
        canvas.clipPath(mPath);
        super.onDraw(canvas);
        canvas.restore();
    }
}
