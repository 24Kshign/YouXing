package com.share.jack.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.share.jack.adapter.ViewPagerViewAdapter;
import com.share.jack.swingtravel.R;

import java.util.ArrayList;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/23 12:23
 * Copyright:1.0
 */
public class ShopFragment extends Fragment implements View.OnClickListener {

    private Button mBtnLeft;
    private Button mBtnRight;
    private Fragment mShopFoodFragment;
    private Fragment mShopRestFragment;
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_shop, container, false);
        initeView();
        setSelect(0);
        return mView;
    }

    private void initeView() {
        mBtnLeft = (Button) mView.findViewById(R.id.fs_btn_left);
        mBtnRight = (Button) mView.findViewById(R.id.fs_btn_right);

        mBtnLeft.setOnClickListener(this);
        mBtnRight.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        resetImage();
        switch (v.getId()) {
            case R.id.fs_btn_left:
                setSelect(0);
                break;
            case R.id.fs_btn_right:
                setSelect(1);
                break;
        }
    }

    public void setSelect(int select) {
        FragmentManager fm = ShopFragment.this.getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();    //创建一个事务
        hideFragment(transaction);     //我们先把所有的Fragment隐藏了，然后下面再开始处理具体要显示的Fragment
        // 设置图片和文字的亮色
        switch (select) {
            case 0:
                if (mShopFoodFragment == null) {
                    mShopFoodFragment = new ShopFoodFragment();
                    transaction.add(R.id.fs_fl_layout, mShopFoodFragment);    //将Fragment添加到Activity中
                } else {
                    transaction.show(mShopFoodFragment);          //将当前fragment显示到界面
                }
                mBtnLeft.setBackgroundResource(R.drawable.fs_top_left_bg_pressed);   //设置选中的背景为亮色
                mBtnLeft.setTextColor(Color.WHITE);     //设置选中字体颜色为白色
                break;
            case 1:
                if (mShopRestFragment == null) {
                    mShopRestFragment = new ShopRestFragment();
                    transaction.add(R.id.fs_fl_layout, mShopRestFragment);    //将Fragment添加到Activity中
                } else {
                    transaction.show(mShopRestFragment);          //将当前fragment显示到界面
                }
                mBtnRight.setBackgroundResource(R.drawable.fs_top_right_bg_pressed);
                mBtnRight.setTextColor(Color.WHITE);
                break;
        }
        transaction.commitAllowingStateLoss();   //提交事务
    }

    /**
     * 隐藏所有的fragment
     *
     * @param transaction
     */
    private void hideFragment(FragmentTransaction transaction) {
        if (mShopFoodFragment != null) {
            transaction.hide(mShopFoodFragment);
        }
        if (mShopRestFragment != null) {
            transaction.hide(mShopRestFragment);
        }
    }

    // 将背景及文字变为暗色
    private void resetImage() {
        mBtnLeft.setBackgroundResource(R.drawable.fs_top_left_bg_normal);
        mBtnLeft.setTextColor(Color.BLACK);
        mBtnRight.setBackgroundResource(R.drawable.fs_top_right_bg_normal);
        mBtnRight.setTextColor(Color.BLACK);
    }
}
