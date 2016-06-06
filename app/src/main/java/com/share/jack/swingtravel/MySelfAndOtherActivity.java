package com.share.jack.swingtravel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.RequestParams;
import com.share.jack.bean.PersonalBean;
import com.share.jack.demoutils.UserUtils;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.utils.GlideCircleTransform;
import com.share.jack.utils.YXApplication;
import com.share.jack.utils.YXConstant;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/2/4 15:44
 * Copyright:1.0
 */
public class MySelfAndOtherActivity extends BaseActivity implements View.OnClickListener {

    private final static String TAG = "MySelfAndOtherActivity";

    @Bind(R.id.ltb_rv_left)
    RelativeLayout mRvLeft;
    @Bind(R.id.ltb_tv_left)
    TextView mTvLeft;
    @Bind(R.id.ltb_tv_title)
    TextView mTvTitle;
    @Bind(R.id.amao_lv_image_of_been_gone)
    LinearLayout mLvImageOfBeenGone;       //去过下面的图片，仅在自己浏览时显示
    @Bind(R.id.amao_lv_image_of_want_go)
    LinearLayout mLvImageOfWantGo;        //想去下面的图片，仅在自己浏览时显示
    @Bind(R.id.amao_rv_been_gone)
    RelativeLayout mRvBeenGone;
    @Bind(R.id.amao_rv_want_go)
    RelativeLayout mRvWantGo;
    @Bind(R.id.amao_btn_send)
    Button mBtnSend;          //发送消息，仅在别人浏览时显示
    @Bind(R.id.amao_riv_head)
    ImageView mRivHead;
    @Bind(R.id.amao_tv_nickname)
    TextView mTvNickname;
    @Bind(R.id.amao_tv_location_city)
    TextView mTvLocation;
    @Bind(R.id.amao_iv_gender)
    ImageView mIvGender;

    @Bind(R.id.amao_iv_been_image1)
    ImageView mIvBeenImage1;
    @Bind(R.id.amao_iv_been_image2)
    ImageView mIvBeenImage2;
    @Bind(R.id.amao_iv_want_image1)
    ImageView mIvWantImage1;
    @Bind(R.id.amao_iv_want_image2)
    ImageView mIvWantImage2;

    private String isMeOrOther = null;    //判断是自己的主页还是别人的
    private SharedPreferences sp = null;
    private PersonalBean personalBean = null;

    private String otherUserNama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YXApplication.getInstance().addActivity(this);
        setContentView(R.layout.activity_myself_and_other);
        sp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
        ButterKnife.bind(this);
        isMeOrOther = getIntent().getStringExtra("username");
        personalBean = getIntent().getParcelableExtra(YXConstant.PAR_USER_KEY);
        Log.d(TAG, "isMeOrOther=" + isMeOrOther);
        initeView();
    }

    private void initeView() {
        mRvLeft.setVisibility(View.VISIBLE);
        mTvLeft.setText(getResources().getString(R.string.back));
        mRvLeft.setOnClickListener(this);
        mRvBeenGone.setOnClickListener(this);
        mRvWantGo.setOnClickListener(this);
        if (sp.getString(YXConstant.USER_PHONE, "").equals(isMeOrOther)) { //自己的主页
            mLvImageOfBeenGone.setVisibility(View.VISIBLE);
            mLvImageOfWantGo.setVisibility(View.VISIBLE);
            mBtnSend.setVisibility(View.GONE);
            UserUtils.setCurrentUserAvatar(this, mRivHead);
            mTvNickname.setText(sp.getString(YXConstant.USER_NICKNAME,
                    "YX_" + sp.getString(YXConstant.USER_PHONE, "").substring(5)));
            mTvLocation.setText(sp.getString(YXConstant.USER_LOCATION, ""));
            mTvTitle.setText(getResources().getString(R.string.me));
            if (sp.getString(YXConstant.USER_GENDER, getString(R.string.male)).equals(getString(R.string.male))) {
                mIvGender.setImageResource(R.mipmap.fp_iv_man);
            } else {
                mIvGender.setImageResource(R.mipmap.fp_iv_woman);
            }
            asyncHttpPost();
        } else {      //别人的主页
            mLvImageOfBeenGone.setVisibility(View.GONE);
            mLvImageOfWantGo.setVisibility(View.GONE);
            mBtnSend.setVisibility(View.VISIBLE);
            mBtnSend.setOnClickListener(this);
            mTvTitle.setText("他的");
            if (personalBean != null) {
                mTvLocation.setText(personalBean.getUserLocation());
                mTvNickname.setText(personalBean.getUserNick());
                if (personalBean.getUserSex().equals(getString(R.string.male))) {
                    mIvGender.setImageResource(R.mipmap.fp_iv_man);
                } else {
                    mIvGender.setImageResource(R.mipmap.fp_iv_woman);
                }
                Glide.with(this).load(personalBean.getUserHead())
                        .placeholder(R.mipmap.default_avatar)
                        .error(R.mipmap.default_avatar)
                        .transform(new GlideCircleTransform(this)).into(mRivHead);
            } else {
                getOtherUserInfo(isMeOrOther);
            }
        }
    }

    private void getOtherUserInfo(final String isMeOrOther) {
        String url = "http://115.28.101.140/youxing/Home/User/getUserInfoByPhone";
        RequestParams params = new RequestParams();
        params.put("User_Phone", isMeOrOther);
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {

                        Glide.with(MySelfAndOtherActivity.this).load(jsonObject
                                .getJSONObject("response").getJSONObject("data")
                                .getString("Head")).placeholder(R.mipmap.default_avatar)
                                .error(R.mipmap.ic_launcher)
                                .transform(new GlideCircleTransform(MySelfAndOtherActivity
                                        .this)).into(mRivHead);
                        otherUserNama = jsonObject
                                .getJSONObject("response").getJSONObject("data")
                                .getString("Nickname");
                        mTvNickname.setText(otherUserNama);

                        mTvLocation.setText(jsonObject
                                .getJSONObject("response").getJSONObject("data")
                                .getString("Live"));

                        if (jsonObject
                                .getJSONObject("response").getJSONObject("data")
                                .getString("Sex").equals("男")) {
                            mIvGender.setImageResource(R.mipmap.fp_iv_man);
                        } else {
                            mIvGender.setImageResource(R.mipmap.fp_iv_woman);
                        }
                    } else {
                        UserUtils.setUserAvatar(MySelfAndOtherActivity.this, isMeOrOther, mRivHead);
                        UserUtils.setUserNick("YX_" + isMeOrOther.substring(5), mTvNickname);
                        mTvLocation.setText("浙江-杭州");
                        mIvGender.setImageResource(R.mipmap.fp_iv_man);
                        Log.d(TAG, jsonObject.getJSONObject("response").getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                UserUtils.setUserAvatar(MySelfAndOtherActivity.this, isMeOrOther, mRivHead);
                UserUtils.setUserNick("YX_" + isMeOrOther.substring(5), mTvNickname);
                mTvLocation.setText("浙江-杭州");
                mIvGender.setImageResource(R.mipmap.fp_iv_man);
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    private void asyncHttpPost() {
        String url = "http://115.28.101.140/youxing/Home/User/getUserInfoById";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        setImage(new String(response));
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

    private void setImage(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject jsonObjectImage = jsonObject.getJSONObject("response").getJSONObject("data");
            String[] strBeanImage = jsonObjectImage.getString("Been_Img").split(",");
            String[] strWantImage = jsonObjectImage.getString("WantGo_Img").split(",");
            if (strBeanImage.length == 1) {
                mLvImageOfBeenGone.setVisibility(View.VISIBLE);
                Glide.with(this).load(strBeanImage[0]).placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .centerCrop().into(mIvBeenImage1);
                mIvBeenImage2.setVisibility(View.GONE);
            } else {
                mLvImageOfBeenGone.setVisibility(View.VISIBLE);
                Glide.with(this).load(strBeanImage[0]).placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .centerCrop().into(mIvBeenImage1);
                Glide.with(this).load(strBeanImage[1]).placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .centerCrop().into(mIvBeenImage2);
            }
            if (strWantImage.length == 1) {
                mLvImageOfWantGo.setVisibility(View.VISIBLE);
                Glide.with(this).load(strWantImage[0]).placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .centerCrop().into(mIvWantImage1);
                mIvWantImage2.setVisibility(View.GONE);
            } else {
                mLvImageOfWantGo.setVisibility(View.VISIBLE);
                Glide.with(this).load(strWantImage[0]).placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .centerCrop().into(mIvWantImage1);
                Glide.with(this).load(strWantImage[1]).placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .centerCrop().into(mIvWantImage2);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ltb_rv_left:
                finish();
                break;
            case R.id.amao_btn_send:
                // 进入聊天页面
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("userId", isMeOrOther);
                if (otherUserNama != null) {
                    intent.putExtra("otherUserName", otherUserNama);
                } else {
                    intent.putExtra("otherUserName", "YX_" + isMeOrOther.substring(5));
                }
                startActivity(intent);
                break;
            case R.id.amao_rv_been_gone:
                if (sp.getString(YXConstant.USER_PHONE, "").equals(isMeOrOther)) {    //自己的去过
                    startActivity(new Intent(this, BeenGoneActivity.class));
                } else {
                    startActivity(new Intent(this, OtherPeopleBeenGoneActivity.class)
                            .putExtra("otherId", personalBean.getUserId()));
                }
                break;
            case R.id.amao_rv_want_go:
                if (sp.getString(YXConstant.USER_PHONE, "").equals(isMeOrOther)) {    //自己的想去
                    startActivity(new Intent(this, WantGoActivity.class));
                } else {
                    startActivity(new Intent(this, OtherPeopleWantGoActivity.class)
                            .putExtra("otherId", personalBean.getUserId()));
                }
                break;
        }
    }
}
