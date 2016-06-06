package com.share.jack.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.RequestParams;
import com.share.jack.demoutils.ImageUtils;
import com.share.jack.demoutils.UserUtils;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.swingtravel.BeenGoneActivity;
import com.share.jack.swingtravel.LoginActivity;
import com.share.jack.swingtravel.R;
import com.share.jack.swingtravel.RegisterActivity;
import com.share.jack.swingtravel.SettingActivity;
import com.share.jack.swingtravel.WantGoActivity;
import com.share.jack.utils.GlideCircleTransform;
import com.share.jack.utils.YXConstant;
import com.share.jack.widget.RoundImageView;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/23 12:23
 * Copyright:1.0
 */
public class PersonalFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "PersonalFragment";
    private View mView;

    @Bind(R.id.ltb_rv_left)
    RelativeLayout mRvLeft;
    @Bind(R.id.ltb_tv_title)
    TextView mTvTitle;
    @Bind(R.id.ltb_btn_right)
    Button mBtnRight;
    @Bind(R.id.fp_rv_been_gone)
    RelativeLayout mRvBeenGone;
    @Bind(R.id.fp_rv_want_go)
    RelativeLayout mRvWantGo;
    @Bind(R.id.fp_iv_head)
    ImageView mIvHead;
    @Bind(R.id.fp_tv_location_city)
    TextView mTvLocation;
    @Bind(R.id.fp_tv_nickname)
    TextView mTvNickname;
    @Bind(R.id.fp_iv_gender)
    ImageView mIvGender;
    @Bind(R.id.fp_rv_not_login)
    RelativeLayout mRvNotLogin;
    @Bind(R.id.fp_riv_head)
    RoundImageView mRivHead;
    @Bind(R.id.fp_lv_bottom)
    LinearLayout mLvBottom;
    @Bind(R.id.lnl_btn_login)
    Button mBtnGoLogin;
    @Bind(R.id.lnl_btn_register)
    Button mBtnGoRegister;

    @Bind(R.id.fp_iv_been_gone_image1)
    ImageView mIvBeenImage1;
    @Bind(R.id.fp_iv_been_gone_image2)
    ImageView mIvBeenImage2;
    @Bind(R.id.fp_iv_want_go_image1)
    ImageView mIvWantImage1;
    @Bind(R.id.fp_iv_want_go_image2)
    ImageView mIvWantImage2;

    @Bind(R.id.fp_lv_image_of_been_gone)
    LinearLayout mLvBeanGone;
    @Bind(R.id.fp_lv_image_of_want_go)
    LinearLayout mLvWantGo;

    private SharedPreferences sp = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_personal, null);
        ButterKnife.bind(this, mView);
        sp = getActivity().getSharedPreferences(YXConstant.USER, getActivity().MODE_PRIVATE);
        mRvLeft.setVisibility(View.GONE);
        mBtnRight.setVisibility(View.VISIBLE);
        mBtnRight.setBackgroundResource(R.drawable.ltb_btn_right);
        mTvTitle.setText(getResources().getString(R.string.me));
        mBtnRight.setOnClickListener(this);
        if (sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
            mRvNotLogin.setVisibility(View.VISIBLE);
            mLvBottom.setVisibility(View.GONE);
            mBtnGoLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), LoginActivity.class).putExtra("isLoging", "jack"));
                }
            });
            mBtnGoRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), RegisterActivity.class));
                }
            });
        } else {
            initeView();
            mRvNotLogin.setVisibility(View.GONE);
            mLvBottom.setVisibility(View.VISIBLE);
            Log.d(TAG, "location=" + sp.getString(YXConstant.USER_LOCATION, "浙江省-杭州市"));
            mTvLocation.setText(sp.getString(YXConstant.USER_LOCATION, "浙江省-杭州市"));
            Log.d(TAG, "nickname=" + sp.getString(YXConstant.USER_NICKNAME, ""));
            mTvNickname.setText(sp.getString(YXConstant.USER_NICKNAME, "YX" + YXConstant.USER_PHONE.substring(5)));
            if (sp.getString(YXConstant.USER_GENDER, getString(R.string.male)).equals(getString(R.string.male))) {
                mIvGender.setImageResource(R.mipmap.fp_iv_man);
            } else {
                mIvGender.setImageResource(R.mipmap.fp_iv_woman);
            }
            if (!sp.getString(YXConstant.USER_HEAD, "").isEmpty()) {
                mRivHead.setVisibility(View.VISIBLE);
                mIvHead.setVisibility(View.GONE);
                mRivHead.setImageBitmap(ImageUtils.stringtoBitmap(sp.getString(YXConstant.USER_HEAD, "")));
            } else {
                mRivHead.setVisibility(View.GONE);
                mIvHead.setVisibility(View.VISIBLE);
                UserUtils.setCurrentUserAvatar(getActivity(), mIvHead);
            }
        }
        return mView;
    }

    private void initeView() {
        mRvBeenGone.setOnClickListener(this);
        mRvWantGo.setOnClickListener(this);
        asyncHttpPost();
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
                mLvBeanGone.setVisibility(View.VISIBLE);
                Glide.with(this).load(strBeanImage[0]).placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .centerCrop().into(mIvBeenImage1);
                mIvBeenImage2.setVisibility(View.GONE);
            } else {
                mLvBeanGone.setVisibility(View.VISIBLE);
                Glide.with(this).load(strBeanImage[0]).placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .centerCrop().into(mIvBeenImage1);
                Glide.with(this).load(strBeanImage[1]).placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .centerCrop().into(mIvBeenImage2);
            }
            if (strWantImage.length == 1) {
                mLvWantGo.setVisibility(View.VISIBLE);
                Glide.with(this).load(strWantImage[0]).placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .centerCrop().into(mIvWantImage1);
                mIvWantImage2.setVisibility(View.GONE);
            } else {
                mLvWantGo.setVisibility(View.VISIBLE);
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
            case R.id.ltb_btn_right:
                if (sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
                    startActivity(new Intent(getActivity(), LoginActivity.class).putExtra("isLoging", "jack"));
                } else {
                    startActivity(new Intent(getActivity(), SettingActivity.class));
                }
                break;
            case R.id.fp_rv_been_gone:
                startActivity(new Intent(getActivity(), BeenGoneActivity.class));
                break;
            case R.id.fp_rv_want_go:
                startActivity(new Intent(getActivity(), WantGoActivity.class));
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
            mTvLocation.setText(sp.getString(YXConstant.USER_LOCATION, "杭州"));
            mTvNickname.setText(sp.getString(YXConstant.USER_NICKNAME, "YX_" + sp.getString(YXConstant.USER_PHONE, "")));
            if (sp.getString(YXConstant.USER_GENDER, getString(R.string.male)).equals(getString(R.string.male))) {
                mIvGender.setImageResource(R.mipmap.fp_iv_man);
            } else {
                mIvGender.setImageResource(R.mipmap.fp_iv_woman);
            }
            if (!sp.getString(YXConstant.USER_HEAD, "").isEmpty()) {
                mRivHead.setVisibility(View.VISIBLE);
                mIvHead.setVisibility(View.GONE);
                mRivHead.setImageBitmap(ImageUtils.stringtoBitmap(sp.getString(YXConstant.USER_HEAD, "")));
            } else {
                mRivHead.setVisibility(View.GONE);
                mIvHead.setVisibility(View.VISIBLE);
                UserUtils.setCurrentUserAvatar(getActivity(), mIvHead);
            }
        }
    }
}
