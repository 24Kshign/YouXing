package com.share.jack.widget;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;

import com.share.jack.swingtravel.R;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/26 10:07
 * Copyright:1.0
 */
public class SelectPicPopupWindow extends PopupWindow {


    private Button btn_take_photo, btn_pick_photo, btn_cancel;
    private View mMenuView;

    public SelectPicPopupWindow(Activity context, View.OnClickListener itemsOnClick) {
        super(context);
        //利用LayoutInflater得到view
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.pop_select_pic, null);
        btn_take_photo = (Button) mMenuView.findViewById(R.id.item_popupwindows_camera);
        btn_pick_photo = (Button) mMenuView.findViewById(R.id.item_popupwindows_photo);
        btn_cancel = (Button) mMenuView.findViewById(R.id.item_popupwindows_cancel);
        //取消按钮
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //销毁弹出框
                dismiss();
            }
        });
        //设置按钮监听
        btn_pick_photo.setOnClickListener(itemsOnClick);
        btn_take_photo.setOnClickListener(itemsOnClick);
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.mypopwindow_anim_style);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                //获取是当前View相对于它的父类容器的顶部位置距离
                int height = mMenuView.findViewById(R.id.ll_popup).getTop();
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
        mMenuView.setOnKeyListener(new View.OnKeyListener() {
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
