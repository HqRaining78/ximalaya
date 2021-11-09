package com.hq.ximalaya.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hq.ximalaya.R;

public class ConfirmCheckBoxDialog extends Dialog {

    private TextView mCancelBtn;
    private TextView mConfirmBtn;
    private OnDialogActionClickListener mListener = null;
    private CheckBox mCheckBox;

    public ConfirmCheckBoxDialog(@NonNull Context context) {
        this(context, 0);
    }

    public ConfirmCheckBoxDialog(@NonNull Context context, int themeResId) {
        // cancelable 是否点击空白处取消
        this(context, true, null);
    }

    protected ConfirmCheckBoxDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_check_box_confirm);

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
                    boolean checked = mCheckBox.isChecked();
                    mListener.onGiveUpClick(checked);
                    dismiss();
                }
            }
        });
    }

    private void initView() {
        mCancelBtn = findViewById(R.id.dialog_check_box_cancel);
        mConfirmBtn = findViewById(R.id.dialog_check_box_confirm);
        mCheckBox = findViewById(R.id.confirm_checkbox);
    }

    public void setOnDialogActionClickListener(OnDialogActionClickListener listener) {
        this.mListener = listener;
    }

    public interface OnDialogActionClickListener {
        void onCancelSubClick();
        void onGiveUpClick(boolean checked);

    }
}
