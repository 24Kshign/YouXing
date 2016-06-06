package com.share.jack.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.util.NetUtils;
import com.loopj.android.http.RequestParams;
import com.share.jack.adapter.FragmentFoodAdapter;
import com.share.jack.adapter.FragmentRestAdapter;
import com.share.jack.adapter.ViewPagerViewAdapter;
import com.share.jack.bean.FragmentFoodBean;
import com.share.jack.bean.FragmentRestBean;
import com.share.jack.bean.ShopBean;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.swingtravel.ArticleActivity;
import com.share.jack.swingtravel.MainActivity;
import com.share.jack.swingtravel.R;
import com.share.jack.utils.YXConstant;
import com.share.jack.widget.MyNoScrollListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/23 12:23
 * Copyright:1.0
 */
public class ShopRestFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
        , ViewPager.OnPageChangeListener, AdapterView.OnItemClickListener {

    private static final String TAG = "ShopRestFragment";
    private static final int REFRESH_COMPLETE = 0X115;
    private static final int PAGER_SLIDE = 0X116;
    private View mView;
    private SwipeRefreshLayout mSwipRefresh;
    private MyNoScrollListView mListView;
    private FragmentRestAdapter mAdapter;
    private List<FragmentRestBean> mDatas = null;
    private Button mBtnSearch;
    private EditText mEtSearch;
    private TextView mTvLocationCity;
    private TextView mTvTemperature;
    private ImageView mIvTemperature;
    private View mHeadView = null;
    private ViewPager mPager;
    private ArrayList<View> mList;        //布局的集合
    private LayoutInflater mInflater;
    private int currentItem = 0;
    private ScheduledExecutorService scheduledExecutorService;
    private boolean isFirst = true;    //判断是否是第一次进入
    private InputMethodManager inputMethodManager;
    private SharedPreferences sp = null;
    private int pageNum = 1;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_COMPLETE:
                    if (NetUtils.hasNetwork(getActivity())) {
                        if (mAdapter == null) {
                            YXConstant.showToast(getActivity(), "无最新数据");
                        } else {
                            mAdapter.notifyDataSetChanged();
                            YXConstant.showToast(getActivity(), "刷新完成");
                        }
                    } else {
                        YXConstant.showToast(getActivity(), "当前网络不可用");
                    }
                    mSwipRefresh.setRefreshing(false);
                    break;
                case PAGER_SLIDE:
                    mPager.setCurrentItem(currentItem);
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_shop_rest, container, false);
        sp = getActivity().getSharedPreferences(YXConstant.WEATHER_INFO, getActivity().MODE_PRIVATE);
        ((MainActivity) this.getActivity()).registerMyTouchListener(mTouchListener);
        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        initeView();
        return mView;
    }

    private void initeView() {
        mSwipRefresh = (SwipeRefreshLayout) mView.findViewById(R.id.fsr_swipe_ly);
        mListView = (MyNoScrollListView) mView.findViewById(R.id.fsr_listview);
        mHeadView = getActivity().getLayoutInflater().inflate(R.layout.item_shop_food_head, null);
        mPager = (ViewPager) mHeadView.findViewById(R.id.isfh_viewpager);
        mList = new ArrayList<View>();
        mInflater = LayoutInflater.from(getActivity());
        View viewSearch = mInflater.inflate(R.layout.view_search, null);
        View viewWeather = mInflater.inflate(R.layout.view_weather, null);
        mList.add(viewWeather);
        mList.add(viewSearch);
        mBtnSearch = (Button) viewSearch.findViewById(R.id.vs_btn_city_and_search);
        mEtSearch = (EditText) viewSearch.findViewById(R.id.vs_et_want_place);
        mTvLocationCity = (TextView) viewWeather.findViewById(R.id.vw_tv_city);
        mTvTemperature = (TextView) viewWeather.findViewById(R.id.vw_tv_degree);
        mIvTemperature = (ImageView) viewWeather.findViewById(R.id.vw_iv_degree);
        Log.d(TAG, "WEATHER_CITY2=" + sp.getString(YXConstant.WEATHER_CITY, "杭州"));
        mTvLocationCity.setText(sp.getString(YXConstant.WEATHER_CITY, "杭州"));
        String temp = sp.getString(YXConstant.WEATHER_DEGREE, "9") + "℃/";
        mTvTemperature.setText(temp);
        if (YXConstant.AisContainB(sp.getString(YXConstant.WEATHER, "晴"), "晴")) {
            mIvTemperature.setImageResource(R.mipmap.iv_sunny);
        } else if (YXConstant.AisContainB(sp.getString(YXConstant.WEATHER, "晴"), "云")) {
            mIvTemperature.setImageResource(R.mipmap.iv_cloudy);
        } else if (YXConstant.AisContainB(sp.getString(YXConstant.WEATHER, "晴"), "阴")) {
            mIvTemperature.setImageResource(R.mipmap.iv_overcast);
        } else if (YXConstant.AisContainB(sp.getString(YXConstant.WEATHER, "晴"), "雨")) {
            mIvTemperature.setImageResource(R.mipmap.iv_rain);
        } else if (YXConstant.AisContainB(sp.getString(YXConstant.WEATHER, "晴"), "雪")) {
            mIvTemperature.setImageResource(R.mipmap.iv_overcast);
        }
        mPager.setAdapter(new ViewPagerViewAdapter(mList));
        mPager.addOnPageChangeListener(this);
        mPager.setCurrentItem(300);
        mListView.addHeaderView(mHeadView);
        asyncHttpPost();
        mSwipRefresh.setOnRefreshListener(this);
        mSwipRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        if (mAdapter == null) {
            mListView.setAdapter(null);
        }
        mListView.setOnItemClickListener(this);
        mPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        mSwipRefresh.setEnabled(false);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mSwipRefresh.setEnabled(true);
                        break;
                }
                return false;
            }
        });
        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEtSearch.getText().toString().equals("")) {
                    YXConstant.showToast(getActivity(), "请输入要搜索的内容");
                    return;
                }
                YXConstant.showToast(getActivity(), mEtSearch.getText().toString());
                mEtSearch.setText("");
            }
        });
    }

    public List<FragmentRestBean> getJsonDatas(String jsonString) {
        List<FragmentRestBean> list = new ArrayList<FragmentRestBean>();
        JSONObject jsonObject;
        FragmentRestBean bean;
        try {
            jsonObject = new JSONObject(jsonString);
            if (jsonObject.getString("result").equals("success")) {
                JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("data");     //json数组中包含很多json对象
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    bean = new FragmentRestBean();
                    bean.mImageUrl = jsonObject.getString("Main_Img");
                    bean.mNickName = jsonObject.getString("Nickname") + "：";
                    bean.mContent = jsonObject.getString("Content");
                    bean.mUserId = jsonObject.getString("User_Id");
                    bean.mUserHead = jsonObject.getString("User_Head");
                    bean.mRecommentId = jsonObject.getString("Recomment_id");
                    bean.mOtherImage = jsonObject.getString("Other_Img");
                    bean.mTitle = jsonObject.getString("Title");
                    bean.mReadNum = jsonObject.getString("Readnum");
                    bean.mTime = jsonObject.getString("Time");
                    bean.mLocation = jsonObject.getString("Range");
                    bean.mLongitude = jsonObject.getString("Longitude");
                    bean.mLatitude = jsonObject.getString("Latitude");
                    bean.mPraiseNum = jsonObject.getString("Zannum");
                    bean.mCommentNum = jsonObject.getString("Commentnum");
                    list.add(bean);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void asyncHttpPost() {
        String url = "http://115.28.101.140/youxing/Home/Recomment/getRestList";
        mDatas = new ArrayList<FragmentRestBean>();
        RequestParams params = new RequestParams();
        params.put("Location", String.valueOf(pageNum++));
        params.put("City_Id", "0571");         //测试用
        RequestUtils.ClientGet(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                mDatas = getJsonDatas(new String(response));
                mAdapter = new FragmentRestAdapter(getActivity(), mDatas);
                mListView.setAdapter(mAdapter);
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                YXConstant.showToast(getActivity(), getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "position=" + position);
        ShopBean shopBean = new ShopBean();
        Bundle bundle = new Bundle();
        Intent intent = new Intent(getActivity(), ArticleActivity.class);
        FragmentRestBean bean = mDatas.get(position - 1);
        shopBean.setNickName(bean.mNickName);
        shopBean.setUserId(bean.mUserId);
        shopBean.setUserHead(bean.mUserHead);
        shopBean.setRecommentId(bean.mRecommentId);
        shopBean.setMainImage(bean.mImageUrl);
        shopBean.setOtherImage(bean.mOtherImage);
        shopBean.setContent(bean.mContent);
        shopBean.setTitle(bean.mTitle);
        shopBean.setReadNum(bean.mReadNum);
        shopBean.setTime(bean.mTime);
        shopBean.setLocation(bean.mLocation);
        shopBean.setLongitude(bean.mLongitude);
        shopBean.setLatitude(bean.mLatitude);
        shopBean.setPraiseNum(bean.mPraiseNum);
        shopBean.setCommentNum(bean.mCommentNum);
        bundle.putParcelable(YXConstant.PAR_KEY, shopBean);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.d("ShopRest----->", "position=" + mPager.getCurrentItem());
        currentItem = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    @Override
    public void onStart() {
        super.onStart();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        //每隔2秒钟切换一张图片
        scheduledExecutorService.scheduleWithFixedDelay(new ViewPagerTask(), 2, 2, TimeUnit.SECONDS);
        pageNum = 1;
    }

    //切换图片
    private class ViewPagerTask implements Runnable {
        @Override
        public void run() {
            currentItem = (currentItem + 1) % 2;
            if (isFirst) {
                mHandler.sendEmptyMessage(PAGER_SLIDE);
                mHandler.obtainMessage().sendToTarget();
                isFirst = false;
            }
        }
    }

    /**
     * Fragment中，注册
     * 接收MainActivity的Touch回调的对象
     * 重写其中的onTouchEvent函数，并进行该Fragment的逻辑处理
     */
    private MainActivity.MyTouchListener mTouchListener = new MainActivity.MyTouchListener() {
        @Override
        public void onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (getActivity().getCurrentFocus() != null && getActivity().getCurrentFocus()
                        .getWindowToken() != null) {
                    inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus()
                            .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }
    };

    @Override
    public void onRefresh() {
        mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 2000);
    }

    @Override
    public void onResume() {
        super.onResume();
        isFirst = true;
        pageNum = 1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pageNum = 1;
    }
}
