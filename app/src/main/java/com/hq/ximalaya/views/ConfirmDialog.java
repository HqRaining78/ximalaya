package com.hq.ximalaya.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.DialogCompat;

import com.hq.ximalaya.R;

public class ConfirmDialog extends Dialog {

    private TextView mCancelBtn;
    private TextView mConfirmBtn;
    private OnDialogActionClickListener mListener = null;

    public ConfirmDialog(@NonNull Context context) {
        this(context, 0);
    }

    public ConfirmDialog(@NonNull Context context, int themeResId) {
        // cancelable 是否点击空白处取消
        this(context, true, null);
    }

    protected ConfirmDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirm);

        initView();
        initListener();
    }

    private void initListener() {
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCancelSubClick();
                    dismiss();
                }
            }
        });

        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onGiveUpClick();
                    dismiss();
                }
            }
        });
    }

    private void initView() {
        mCancelBtn = findViewById(R.id.dialog_cancel_sub);
        mConfirmBtn = findViewById(R.id.dialog_confirm_sub);

    }

    public void setOnDialogActionClickListener(OnDialogActionClickListener listener) {
        this.mListener = listener;
    }

    public interface OnDialogActionClickListener {
        void onCancelSubClick();
        void onGiveUpClick();

    }
}
