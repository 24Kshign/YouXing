package com.share.jack.swingtravel;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.share.jack.controller.HXSDKHelper;
import com.share.jack.model.DemoHXSDKModel;
import com.share.jack.utils.YXApplication;
import com.share.jack.widget.UISwitchButton;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/25 10:07
 * Copyright:1.0
 */

public class MessegeTipActivity extends BaseActivity implements View.OnClickListener
        , CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MessegeTipActivity";

    @Bind(R.id.ltb_rv_left)
    RelativeLayout mRvLeft;

    @Bind(R.id.ltb_tv_left)
    TextView mTvLeft;

    @Bind(R.id.ltb_tv_title)
    TextView mTvTitle;

    @Bind(R.id.amt_usb_voice)
    UISwitchButton mUbVoice;

    @Bind(R.id.amt_usb_voice_comment)
    UISwitchButton mUbVoiceComment;

    @Bind(R.id.amt_usb_voice_ait)
    UISwitchButton mUbVoiceAit;

    @Bind(R.id.amt_usb_voice_chat_messege)
    UISwitchButton mUbVoiceChatMessege;

    @Bind(R.id.amt_usb_shake)
    UISwitchButton mUbShake;

    @Bind(R.id.amt_usb_shake_comment)
    UISwitchButton mUbShakeComment;

    @Bind(R.id.amt_usb_shake_ait)
    UISwitchButton mUbShakeAit;

    @Bind(R.id.amt_usb_shake_chat_messege)
    UISwitchButton mUbShakeChatMessege;

    @Bind(R.id.amt_lv_all_voice)
    LinearLayout mLvAllVoice;

    @Bind(R.id.amt_lv_all_shake)
    LinearLayout mLvAllShake;

    private EMChatOptions chatOptions;
    private DemoHXSDKModel model;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YXApplication.getInstance().addActivity(this);
        setContentView(R.layout.activity_messege_tip);
        ButterKnife.bind(this);
        initeView();
    }

    private void initeView() {
        mRvLeft.setVisibility(View.VISIBLE);
        mTvTitle.setText(getResources().getString(R.string.info_setting));
        mTvLeft.setText(getResources().getString(R.string.back));

        chatOptions = EMChatManager.getInstance().getChatOptions();
        model = (DemoHXSDKModel) HXSDKHelper.getInstance().getModel();

        mRvLeft.setOnClickListener(this);
        mUbVoice.setOnCheckedChangeListener(this);
        mUbVoiceComment.setOnCheckedChangeListener(this);
        mUbVoiceAit.setOnCheckedChangeListener(this);
        mUbVoiceChatMessege.setOnCheckedChangeListener(this);
        mUbShake.setOnCheckedChangeListener(this);
        mUbShakeComment.setOnCheckedChangeListener(this);
        mUbShakeAit.setOnCheckedChangeListener(this);
        mUbShakeChatMessege.setOnCheckedChangeListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ltb_rv_left:
                finish();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.amt_usb_voice:
                if (isChecked) {
                    mLvAllVoice.setVisibility(View.VISIBLE);
                } else {
                    mLvAllVoice.setVisibility(View.GONE);
                }
                break;
            case R.id.amt_usb_voice_comment:
                break;
            case R.id.amt_usb_voice_ait:
                break;
            case R.id.amt_usb_voice_chat_messege:
                break;
            case R.id.amt_usb_shake:
                if (isChecked) {
                    mLvAllShake.setVisibility(View.VISIBLE);
                } else {
                    mLvAllShake.setVisibility(View.GONE);
                }
                break;
            case R.id.amt_usb_shake_comment:
                break;
            case R.id.amt_usb_shake_ait:
                break;
            case R.id.amt_usb_shake_chat_messege:
                break;
        }
    }
}
