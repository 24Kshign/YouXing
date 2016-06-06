package com.share.jack.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.share.jack.swingtravel.R;

/**
 * Created by 程 on 2016/4/16.
 */
public class MyProgressDialog extends Dialog {

    private static MyProgressDialog mProgressDialog;

    public MyProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public static MyProgressDialog createProgrssDialog(Context context) {
        mProgressDialog = new MyProgressDialog(context, R.style.Custom_Progress);
        mProgressDialog.setContentView(R.layout.dialog_custom_progressdialog);
        // 按返回键是否取消
        mProgressDialog.setCancelable(false);
        // 设置居中
        mProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        WindowManager.LayoutParams lp = mProgressDialog.getWindow().getAttributes();
        // 设置背景层透明度
        lp.dimAmount = 0.2f;
        mProgressDialog.getWindow().setAttributes(lp);
        return mProgressDialog;
    }

    /**
     * 当窗口焦点改变时调用
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        if (mProgressDialog == null) {
            return;
        }
        ImageView imageView = (ImageView) findViewById(R.id.spinnerImageView);
        // 获取ImageView上的动画背景
        AnimationDrawable spinner = (AnimationDrawable) imageView.getBackground();
        // 开始动画
        spinner.start();
    }

    public MyProgressDialog setMessege(String msg) {
        TextView loadingTextView = (TextView) mProgressDialog.findViewById(R.id.message);
        if (!TextUtils.isEmpty(msg)) {
            loadingTextView.setText(msg);
        } else {
            loadingTextView.setText("加载中....");
        }
        return mProgressDialog;
    }
}
