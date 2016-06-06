package com.share.jack.swingtravel;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.share.jack.adapter.CommentListAdapter;
import com.share.jack.bean.CommentListBean;
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
import butterknife.OnClick;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/2/11 21:56
 * Copyright:1.0
 */

public class CommentListActivity extends BaseActivity {

    private static final String TAG = "CommentListActivity";

    @Bind(R.id.ltb_rv_left)
    RelativeLayout mRvLeft;
    @Bind(R.id.ltb_tv_left)
    TextView mTvLeft;
    @Bind(R.id.ltb_tv_title)
    TextView mTvTitle;
    @Bind(R.id.acl_list)
    ListView mListView;

    private SharedPreferences sp = null;
    private CommentListAdapter mAdapter;
    private List<CommentListBean> mDatas = new ArrayList<CommentListBean>();
    private int pageNum = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_list);
        ButterKnife.bind(this);
        sp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
        initeView();
    }

    private void initeView() {
        mRvLeft.setVisibility(View.VISIBLE);
        mTvLeft.setText(getResources().getString(R.string.back));
        mTvTitle.setText(getResources().getString(R.string.comment));
        asyncHttpPost();
        if (mAdapter == null) {
            mListView.setAdapter(null);
        }
    }

    private void asyncHttpPost() {
        String url = "http://115.28.101.140/youxing/Home/Comment/getUserCommentList";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Location", String.valueOf(pageNum++));
        RequestUtils.ClientGet(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                mDatas = getJsonDatas(new String(response));
                mAdapter = new CommentListAdapter(CommentListActivity.this, mDatas);
                mListView.setAdapter(mAdapter);
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    private List<CommentListBean> getJsonDatas(String result) {
        List<CommentListBean> list = new ArrayList<CommentListBean>();
        JSONObject jsonObject;
        CommentListBean bean;
        try {
            jsonObject = new JSONObject(result);
            if (jsonObject.getString("result").equals("success")) {
                JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("data");     //json数组中包含很多json对象
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    bean = new CommentListBean();
                    bean.mImageHeadUrl = jsonObject.getString("Author_Head");
                    bean.mNickname = jsonObject.getString("Author_Nick");
                    bean.mTime = jsonObject.getString("Time");
                    bean.mReplyContent = jsonObject.getString("Comment");
                    bean.mImageUrl = jsonObject.getString("Main_Img");
                    bean.mArticleTitle = jsonObject.getString("Title");
                    bean.mArticleAuthor = jsonObject.getString("Article_Author");
                    bean.mArticleContent = jsonObject.getString("Content");
                    list.add(bean);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    @OnClick(R.id.ltb_rv_left)
    public void back(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageNum = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        pageNum = 0;
    }
}
