package com.share.jack.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.share.jack.swingtravel.R;


/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2015/12/11 16:42
 * Copyright:1.0
 */
public class MyEditPopupwindow extends PopupWindow {

    private Button btnSend;

    private EditText etComment;

    private View mPopView;

    public MyEditPopupwindow(Context context, View.OnClickListener itemOnClick) {
        super(context);
        //利用LayoutInflater得到view
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPopView = inflater.inflate(R.layout.item_edittext_pop, null);
        btnSend = (Button) mPopView.findViewById(R.id.iep_btn_send);
        etComment = (EditText) mPopView.findViewById(R.id.iep_et_text);
        btnSend.setOnClickListener(itemOnClick);

        //设置SelectPicPopupWindow的View
        this.setContentView(mPopView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置小键盘的状态
        this.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        this.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x00000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mPopView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int height = mPopView.findViewById(R.id.iep_rv_popup).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
        //按返回键销毁弹出窗口
        mPopView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
    }
}
