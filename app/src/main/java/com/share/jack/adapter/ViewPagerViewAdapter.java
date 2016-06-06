package com.share.jack.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/23 14:14
 * Copyright:1.0
 */
public class ViewPagerViewAdapter extends PagerAdapter {

    private ArrayList<View> list = null;

    public ViewPagerViewAdapter(ArrayList<View> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    //移除view
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(list.get(position));
    }

    //从View集合中获取对应索引的元素, 并添加到ViewPager中
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(list.get(position));
        return list.get(position);
    }

    //是否将显示的ViewPager页面与instantiateItem返回的对象进行关联这个方法是必须实现的
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
