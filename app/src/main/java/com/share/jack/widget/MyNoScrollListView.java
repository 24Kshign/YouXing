package com.share.jack.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/24 14:00
 * Copyright:1.0
 */
public class MyNoScrollListView extends ListView {

    public MyNoScrollListView(Context context) {
        super(context);
    }

    public MyNoScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyNoScrollListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
