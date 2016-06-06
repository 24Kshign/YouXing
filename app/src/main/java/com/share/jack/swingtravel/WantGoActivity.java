package com.share.jack.swingtravel;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.util.NetUtils;
import com.loopj.android.http.RequestParams;
import com.share.jack.adapter.WantGoAdapter;
import com.share.jack.bean.WantGoBean;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.utils.DisplayUtil;
import com.share.jack.utils.YXApplication;
import com.share.jack.utils.YXConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import swingtravel.jack.share.com.swipelistview.SwipeMenu;
import swingtravel.jack.share.com.swipelistview.SwipeMenuCreator;
import swingtravel.jack.share.com.swipelistview.SwipeMenuItem;
import swingtravel.jack.share.com.swipelistview.SwipeMenuListView;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/26 19:34
 * Copyright:1.0
 */

public class WantGoActivity extends BaseActivity implements View.OnClickListener, AbsListView.OnScrollListener {

    private static final String TAG = "WantGoActivity";

    @Bind(R.id.ltb_rv_left)
    RelativeLayout mRvLeft;
    @Bind(R.id.ltb_tv_left)
    TextView mTvLeft;
    @Bind(R.id.ltb_tv_title)
    TextView mTvTitle;
    @Bind(R.id.awg_listview)
    SwipeMenuListView mListView;

    private View mFooterView;     //上拉加载更多的view
    private Handler mHandler = new Handler();
    private boolean isLoading;// 表示是否正在加载
    private final int MAX_COUNT = 50;// 表示服务器上总共有MAX_COUNT条数据
    private final int EACH_COUNT = 10;// 表示每次加载的条数

    private WantGoAdapter mAdapter = null;
    private List<WantGoBean> mDatas = new ArrayList<WantGoBean>();
    private SharedPreferences sp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YXApplication.getInstance().addActivity(this);
        setContentView(R.layout.activity_want_go);
        ButterKnife.bind(this);
        sp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
        mFooterView = getLayoutInflater().inflate(R.layout.item_foot, null);
        mListView.addFooterView(mFooterView);// 设置列表底部视图
        initeView();
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                //创建一个菜单条
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                // 设置菜单的背景
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                // 宽度菜单的宽度是一定要有的，否则不显示菜单
                deleteItem.setWidth(DisplayUtil.dip2px(WantGoActivity.this, 120f));
                // 菜单标题
                deleteItem.setIcon(R.mipmap.ic_delete);
                //添加到menu
                menu.addMenuItem(deleteItem);
            }
        };
        mListView.setMenuCreator(creator);
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        deleteMenuFromServer(position);
                        break;
                }
                // false : 会关闭菜单; true ：不会关闭菜单
                return false;
            }
        });
        asyncHttpPost();
        if (mAdapter == null) {
            mListView.setAdapter(null);
            mListView.removeFooterView(mFooterView);
        }
        if (mDatas.size() > 10) {
            // 设置setOnScrollListener会自动调用onscroll方法。
            mListView.setOnScrollListener(this);
        }
    }

    private void initeView() {
        mRvLeft.setVisibility(View.VISIBLE);
        mTvLeft.setText(getResources().getString(R.string.back));
        mTvTitle.setText(getResources().getString(R.string.want_go));
        mRvLeft.setOnClickListener(this);
    }

    //从服务器端删除item
    private void deleteMenuFromServer(final int position) {
        String url = "http://115.28.101.140/youxing/Home/Recomment/deleteWantGo";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Recomment_Id", mDatas.get(position).mRecommentId);
        Log.d(TAG, "recommentId=" + mDatas.get(position).mRecommentId);
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        mDatas.remove(position);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, jsonObject.getJSONObject("response").getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ltb_rv_left:
                finish();
                break;
        }
    }

    //把得到的json字符串进行解析
    public List<WantGoBean> getJsonDatas(String jsonString) {
        List<WantGoBean> list = new ArrayList<WantGoBean>();
        JSONObject jsonObject;
        WantGoBean bean;
        try {
            jsonObject = new JSONObject(jsonString);
            if (jsonObject.getString("result").equals("success")) {
                JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("data");     //json数组中包含很多json对象
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    bean = new WantGoBean();
                    bean.mImgHeadUrl = jsonObject.getString("Wantgo_head");
                    bean.mNickName = jsonObject.getString("Wantgo_nickname");
                    bean.mTime = jsonObject.getString("Time");
                    bean.mRecommentId = jsonObject.getString("Recomment_Id");
                    bean.mImgUrl = jsonObject.getString("Main_Img");
                    bean.mTitle = jsonObject.getString("Title");
                    bean.mContent = jsonObject.getString("Content");
                    list.add(bean);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    //请求数据

    private void asyncHttpPost() {
        String url = "http://115.28.101.140/youxing/Home/Recomment/getWantGo";
        mDatas = new ArrayList<WantGoBean>();
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        //自己封装的Async-http类
        RequestUtils.ClientGet(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                mDatas = getJsonDatas(new String(response));
                mAdapter = new WantGoAdapter(WantGoActivity.this, mDatas);
                mListView.setAdapter(mAdapter);
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                if (mListView.getFooterViewsCount() != 0) {
                    mListView.removeFooterView(mFooterView);
                }
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.d("onScrollStateChanged", scrollState + "");
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem + visibleItemCount == totalItemCount && !isLoading) {
            // isLoading = true 表示正在加载，加载完毕设置isLoading =false；
            Log.i("onScroll", "firstVisibleItem" + firstVisibleItem
                    + " visibleItemCount" + visibleItemCount
                    + " totalItemCount" + totalItemCount);
            isLoading = true;
            // 如果服务端还有数据，则继续加载更多，否则隐藏底部的加载更多
            if (totalItemCount <= MAX_COUNT) {
                // 等待2秒之后才加载，模拟网络等待时间为2s
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        if (NetUtils.hasNetwork(WantGoActivity.this)) {
                            loadMoreData();
                        }
                    }
                }, 2000);
            } else {
                if (mListView.getFooterViewsCount() != 0) {
                    mListView.removeFooterView(mFooterView);
                }
            }
        }
    }

    //上拉加载更多
    private void loadMoreData() {
        String url = "http://115.28.101.140/youxing/Home/Recomment/getWantGo";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        //自己封装的Async-http类
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                int count = 0;
                if (mAdapter != null) {
                    count = mAdapter.getCount();
                }
                JSONObject jsonObject;
                WantGoBean bean;
                try {
                    jsonObject = new JSONObject(new String(response));
                    JSONArray jsonArray = jsonObject.getJSONArray("data");     //json数组中包含很多json对象
                    for (int i = 0; i < EACH_COUNT; i++) {
                        if (count + i < MAX_COUNT) {
                            jsonObject = jsonArray.getJSONObject(i);
                            bean = new WantGoBean();
                            bean.mImgUrl = jsonObject.getString("picBig");
                            bean.mTitle = "钟声悠然传来" + i;
                            bean.mContent = jsonObject.getString("description");
                            bean.mTime = "2016-1-26  21:" + (i + 1);
                            mAdapter.addNewsItem(bean);
                        } else {
                            mListView.removeView(mFooterView);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    isLoading = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                if (mListView.getFooterViewsCount() != 0) {
                    mListView.removeFooterView(mFooterView);
                }
                isLoading = false;
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    @Override
    public void onBackPressed() {
        cancelToast();
        super.onBackPressed();
    }
}
