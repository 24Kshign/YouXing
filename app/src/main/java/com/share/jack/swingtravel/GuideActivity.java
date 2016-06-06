package com.share.jack.swingtravel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/21 15:37
 * Copyright:1.0
 */

//引导界面，只在用户第一次安装时才有
public class GuideActivity extends BaseActivity {

    private static final String TAG = "GuideActivity";

    @Bind(R.id.ag_lv_dot)
    LinearLayout mLvDot;
    @Bind(R.id.ag_iv_dot1)
    ImageView mIvDot1;
    @Bind(R.id.ag_iv_dot2)
    ImageView mIvDot2;
    @Bind(R.id.ag_iv_dot3)
    ImageView mIvDot3;
    @Bind(R.id.ag_viewpager)
    ViewPager mViewpager;
    // Viewpager的初始化需要一个适配器
    private PagerAdapter mAdapter = null;
    // PagerAdapter的初始化需要一个数据集合
    private List<View> mViews = new ArrayList<View>();
    private LayoutInflater mInflater = null;
    private View guide1;
    private View guide2;
    private View guide3;
    private View guide4;

    private static final String SHAREDPREFERENCES_NAME = "my_pref";
    private static final String KEY_GUIDE_ACTIVITY = "guide_activity";
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        ButterKnife.bind(this);
        if (isFirst) {
            mIvDot1.setImageResource(R.drawable.dot_guide_focus);
        }
        initeView();
        initeListener();
    }

    private void initeView() {
        // 初始化tab01， tab02, tab03, tab04的xml文件
        mInflater = LayoutInflater.from(this);
        guide1 = mInflater.inflate(R.layout.fragment_guide_first, null);
        guide2 = mInflater.inflate(R.layout.fragment_guide_second, null);
        guide3 = mInflater.inflate(R.layout.fragment_guide_third, null);
        guide4 = mInflater.inflate(R.layout.fragment_guide_four, null);
        // 将布局都加进List集合中
        mViews.add(guide1);
        mViews.add(guide2);
        mViews.add(guide3);
        mViews.add(guide4);
        // 初始化适配器mAdapter
        mAdapter = new PagerAdapter() {
            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mViews.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view = mViews.get(position);
                container.addView(view);
                return view;
            }

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return mViews.size();
            }
        };
        mViewpager.setAdapter(mAdapter);
        Button btnInside = (Button) guide4.findViewById(R.id.fgf_btn_to_login);
        btnInside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGuided();
                startActivity(new Intent(GuideActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void initeListener() {
        mViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int currentItem = mViewpager.getCurrentItem();        //得到当前显示的是在哪一个fragment
                isFirst = false;
                resetImage();
                Log.d(TAG, "currentItem=" + currentItem);
                switch (currentItem) {
                    case 0:
                        mViewpager.setCurrentItem(0);
                        mIvDot1.setImageResource(R.drawable.dot_guide_focus);       //选中之后就改变底部小圆点的颜色
                        break;
                    case 1:
                        mViewpager.setCurrentItem(1);
                        mIvDot2.setImageResource(R.drawable.dot_guide_focus);
                        break;
                    case 2:
                        mViewpager.setCurrentItem(2);
                        mIvDot3.setImageResource(R.drawable.dot_guide_focus);
                        break;
                    case 3:
                        mViewpager.setCurrentItem(3);
                        mLvDot.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        isFirst = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isFirst = false;
    }

    private void setGuided() {
        SharedPreferences settings = getSharedPreferences(SHAREDPREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_GUIDE_ACTIVITY, "false");
        editor.commit();
    }

    //设置下面的圆点为未选中的颜色
    private void resetImage() {
        mLvDot.setVisibility(View.VISIBLE);
        mIvDot1.setImageResource(R.drawable.dot_guide_normal);
        mIvDot2.setImageResource(R.drawable.dot_guide_normal);
        mIvDot3.setImageResource(R.drawable.dot_guide_normal);
    }
}
