package com.share.jack.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/29 09:31
 * Copyright:1.0
 */
public class MyScrollView extends ScrollView {

    private ScrollListener mListener;
    //ScrollView正在向上滑动
    public static final int SCROLL_UP = 0x01;
    //ScrollView正在向下滑动
    public static final int SCROLL_DOWN = 0x10;
    public static final int SCROLL_STOP = 0x11;
    //最小的滑动距离
    private static final int SCROLLLIMIT = 10;
    //对外提供的接口
    public static interface ScrollListener {
        public void myScrollview(int scroll);
    }

    public MyScrollView(Context context) {
        super(context, null);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (oldt > t && oldt - t > SCROLLLIMIT) {     // 向下
            if (mListener != null)
                mListener.myScrollview(SCROLL_DOWN);
        } else if (oldt < t && t - oldt > SCROLLLIMIT) {   // 向上
            if (mListener != null)
                mListener.myScrollview(SCROLL_UP);
        } else {
            if (mListener != null)
                mListener.myScrollview(SCROLL_STOP);
        }
    }

    public void setScrollListener(ScrollListener mListener) {
        this.mListener = mListener;
    }
}
