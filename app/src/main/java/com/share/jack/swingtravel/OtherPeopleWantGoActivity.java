package com.share.jack.swingtravel;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.util.NetUtils;
import com.loopj.android.http.RequestParams;
import com.share.jack.adapter.WantGoAdapter;
import com.share.jack.bean.WantGoBean;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.utils.YXConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by 程 on 2016/4/17.
 * E-mails：17764576259@163.com.
 * Welcome to YouXing.
 */
public class OtherPeopleWantGoActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener
        , View.OnClickListener {

    private static final String TAG = "OtherPeopleWantActivity";
    private static final int REFRESH_COMPLETE = 0X010;

    @Bind(R.id.ltb_rv_left)
    RelativeLayout mRvLeft;
    @Bind(R.id.ltb_tv_left)
    TextView mTvLeft;
    @Bind(R.id.ltb_tv_title)
    TextView mTvTitle;

    @Bind(R.id.aopwg_swipe)
    SwipeRefreshLayout mSwipe;
    @Bind(R.id.aopwg_listview)
    ListView mListView;

    private WantGoAdapter mAdapter = null;
    private List<WantGoBean> mDatas = new ArrayList<WantGoBean>();
    private String mId = null;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_COMPLETE:
                    if (NetUtils.hasNetwork(OtherPeopleWantGoActivity.this)) {
                        if (mAdapter == null) {
                            showToast("无最新数据");
                        } else {
                            mAdapter.notifyDataSetChanged();
                            showToast("刷新完成");
                        }
                    } else {
                        showToast("当前网络不可用");
                    }
                    mSwipe.setRefreshing(false);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_people_want_go);
        ButterKnife.bind(this);
        mId = getIntent().getStringExtra("otherId");
        initeView();
        asyncHttpPost();
        if (mAdapter == null) {
            mListView.setAdapter(null);
        }
    }

    private void initeView() {
        mRvLeft.setVisibility(View.VISIBLE);
        mTvLeft.setText(getResources().getString(R.string.back));
        mTvTitle.setText("他的" + getResources().getString(R.string.want_go));
        mRvLeft.setOnClickListener(this);
        mSwipe.setOnRefreshListener(this);
        mSwipe.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
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
        params.put("User_ID", String.valueOf(mId));
        //自己封装的Async-http类
        RequestUtils.ClientGet(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                mDatas = getJsonDatas(new String(response));
                mAdapter = new WantGoAdapter(OtherPeopleWantGoActivity.this, mDatas);
                mListView.setAdapter(mAdapter);
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    @Override
    public void onRefresh() {
        mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 2000);
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
