package com.share.jack.swingtravel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.share.jack.adapter.InteractionAdapter;
import com.share.jack.bean.InteractionBean;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.utils.YXApplication;
import com.share.jack.utils.YXConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/2/10 23:39
 * Copyright:1.0
 */

public class InteractionListActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "InteractionListActivity";

    @Bind(R.id.ltb_rv_left)
    RelativeLayout mRvLeft;
    @Bind(R.id.ltb_tv_left)
    TextView mTvLeft;
    @Bind(R.id.ltb_tv_title)
    TextView mTvTitle;
    @Bind(R.id.ail_list)
    ListView mListView;

    private InteractionAdapter mAdapter;
    private List<InteractionBean> mDatas = new ArrayList<>();
    private SharedPreferences sp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YXApplication.getInstance().addActivity(this);
        setContentView(R.layout.activity_interaction_list);
        sp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
        ButterKnife.bind(this);
        initeView();
    }

    private void initeView() {
        mRvLeft.setVisibility(View.VISIBLE);
        mTvLeft.setText(getResources().getString(R.string.back));
        mTvTitle.setText(getResources().getString(R.string.interaction));
        mRvLeft.setOnClickListener(this);
        asyncHttpPost();
        if (mAdapter == null) {
            mListView.setAdapter(null);
        }
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 进入聊天页面
                Intent intent = new Intent(InteractionListActivity.this, ChatActivity.class);
                // it is single chat
                intent.putExtra("userId", mDatas.get(position).phone);
                startActivity(intent);
            }
        });
        // 注册上下文菜单
        registerForContextMenu(mListView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ltb_rv_left:
                finish();
                break;
        }
    }

    private void asyncHttpPost() {
        String url = "http://115.28.101.140/youxing/Home/User/getFriendsList";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        Log.d(TAG, "token=" + sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                mDatas = getJsonDatas(new String(response));
                mAdapter = new InteractionAdapter(InteractionListActivity.this, mDatas);
                mListView.setAdapter(mAdapter);
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    public List<InteractionBean> getJsonDatas(String jsonString) {
        List<InteractionBean> list = new ArrayList<InteractionBean>();
        JSONObject jsonObject;
        InteractionBean bean;
        try {
            jsonObject = new JSONObject(jsonString);
            if (jsonObject.getString("result").equals("success")) {
                JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("data");     //json数组中包含很多json对象
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    bean = new InteractionBean();
                    bean.imgHeadUrl = jsonObject.getString("Head");
                    bean.nickName = jsonObject.getString("Remarks");
                    bean.phone = jsonObject.getString("Phone");
                    list.add(bean);
                }
            } else {
                Log.d(TAG, jsonObject.getJSONObject("response").getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.menu_remove, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d(TAG, "position=" + ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position);
        int pos = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
        removeItemFromServer(pos);
        return true;
    }

    private void removeItemFromServer(final int pos) {
        String url = "http://115.28.101.140/youxing/Home/User/deleteFriendList";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        Log.d(TAG, "token=" + sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Friend_Id", "");
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        mDatas.remove(pos);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        showToast("删除失败" + jsonObject.getJSONObject("response").getString("message"));
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
}
