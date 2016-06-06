package com.share.jack.widget;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.share.jack.adapter.CustomPopAndListViewAdapter;
import com.share.jack.swingtravel.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/21 10:44
 * Copyright:1.0
 */
public class MyCustomPopAndListView extends PopupWindow {

    private View mainView;
    private ListView mListview;
    private CustomPopAndListViewAdapter mAdapter;
    private List<String> dates = new ArrayList<String>();

    public MyCustomPopAndListView(Activity mActivity, AdapterView.OnItemClickListener popItemClickListener
            , int parmWidth, int parmHeight) {
        super(mActivity);
        //窗口布局
        mainView = LayoutInflater.from(mActivity).inflate(R.layout.item_custom_pop_listview, null);
        mListview = (ListView) mainView.findViewById(R.id.icpl_listview);
        mAdapter = new CustomPopAndListViewAdapter(mActivity, getDates());
        mListview.setAdapter(mAdapter);
        if (popItemClickListener != null) {
            mListview.setOnItemClickListener(popItemClickListener);
        }
        setContentView(mainView);
        //设置宽度
        setWidth(parmWidth);
        //设置高度
        setHeight(parmHeight);
        //设置隐藏动画
        setAnimationStyle(R.style.AnimTools);
        //设置背景透明
        setBackgroundDrawable(new ColorDrawable(0));
    }

    public List<String> getDates() {
        for (int i = 0; i < 20; i++) {
            dates.add("杭州师范大学" + (i % 9));
        }
        return dates;
    }
}
