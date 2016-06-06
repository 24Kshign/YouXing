package com.share.jack.swingtravel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.util.NetUtils;
import com.loopj.android.http.RequestParams;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.utils.YXApplication;
import com.share.jack.utils.YXConstant;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import static com.mob.tools.utils.R.getStringRes;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/22 16:18
 * Copyright:1.0
 */

public class ForgetPwdActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ForgetPwdActivity";

    private static final int MSG_INFO = 0x010;

    @Bind(R.id.ltb_tv_right)
    TextView mTvRight;
    @Bind(R.id.ltb_tv_left)
    TextView mTvLeft;
    @Bind(R.id.ltb_rv_left)
    RelativeLayout mRvLeft;
    @Bind(R.id.ltb_tv_title)
    TextView mTvTitle;
    @Bind(R.id.afp_et_user)
    EditText mEtUser;
    @Bind(R.id.afp_et_confirm_code)
    EditText mEtConfirmCode;
    @Bind(R.id.afp_et_new_pwd)
    EditText mEtNewPwd;
    @Bind(R.id.afp_btn_confirm_code)
    Button mBtnGetConfirmCode;
    @Bind(R.id.afp_btn_finish)
    Button mBtnFinish;

    private TimeCount time = null;
    private ProgressDialog pd = null;
    private String strPhone;
    private String strPwd;
    private EventHandler eventHandler;
    private String strIdentify;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_INFO) {
                int event = msg.arg1;
                int result = msg.arg2;
                Object data = msg.obj;
                Log.e("event", "event=" + event);
                if (result == SMSSDK.RESULT_COMPLETE) {
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {     // 验证码验证成功
                        Log.d(TAG, "Success To Send Msg");
                        setPwd();
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {    //提示验证码已经发送，等待验证
                        showToast("验证码已经发送");
                    }
                } else {         //校验验证码不正确
                    ((Throwable) data).printStackTrace();
                    int resId = getStringRes(ForgetPwdActivity.this, "smssdk_network_error");
                    if (resId > 0) {
                        showToast("验证码错误");
                        Log.d(TAG, String.valueOf(resId));
                    }
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YXApplication.getInstance().addActivity(this);
        setContentView(R.layout.activity_forget_pwd);
        ButterKnife.bind(this);
        time = new TimeCount(60000, 1000);
        initeView();         //初始化控件
        initeSDK();
    }

    private void initeSDK() {
        SMSSDK.initSDK(this, YXConstant.SMSAPP_KEY, YXConstant.SMSAPP_SECRET, true);
        SMSSDK.registerEventHandler(eventHandler);
        eventHandler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = mHandler.obtainMessage();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                msg.what = MSG_INFO;
                mHandler.sendMessage(msg);
            }
        };
        // 注册回调监听接口
        SMSSDK.registerEventHandler(eventHandler);
    }

    private void initeView() {
        mRvLeft.setVisibility(View.VISIBLE);
        mTvRight.setVisibility(View.GONE);
        mTvLeft.setText(getResources().getString(R.string.back));
        mTvTitle.setText(getResources().getString(R.string.find_pwd));
        enableButtonOrNot(mBtnFinish, mEtUser, mEtNewPwd, mEtConfirmCode);
        enableButtonOrNot(mBtnGetConfirmCode, mEtUser);
        mBtnFinish.setOnClickListener(this);
        mBtnGetConfirmCode.setOnClickListener(this);
        mRvLeft.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        strPhone = mEtUser.getText().toString().trim();
        strPwd = MD5(mEtNewPwd.getText().toString().trim());
        strIdentify=mEtConfirmCode.getText().toString();
        switch (v.getId()) {
            case R.id.ltb_rv_left:
                this.finish();
                break;
            case R.id.afp_btn_confirm_code:
                if (!isMobileNO(strPhone)) {
                    showToast("格式错误");
                    return;
                } else if (!NetUtils.hasNetwork(this)) {
                    showToast("请检查网络连接");
                    return;
                }
                //通过SDK发送短信验证
                SMSSDK.getVerificationCode("86", strPhone);
                //让按钮变成不可点击并且显示倒计时（正在获取）
                time.start();
                mBtnGetConfirmCode.setClickable(false);
                mBtnGetConfirmCode.setEnabled(false);
                break;
            case R.id.afp_btn_finish:
                hideKeyboard();
                if (strPhone.isEmpty() || strPwd.isEmpty()) {
                    showToast("账号或密码不能为空");
                    return;
                }
                SMSSDK.submitVerificationCode("86", strPhone, strIdentify);
                break;
        }
    }

    //重新设置密码
    private void setPwd() {
        pd = new ProgressDialog(this);
        pd.setMessage("正在修改，请稍后...");
        pd.show();
        String url = "http://115.28.101.140/youxing/Home/User/findPassword";
        RequestParams params = new RequestParams();
        params.put("User_Phone", strPhone);
        params.put("User_Password", strPwd);

        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                //上传性别成功回调
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        showToast("找回密码成功，请重新登陆");
                        startActivity(new Intent(ForgetPwdActivity.this, LoginActivity.class).putExtra("userPhone", strPhone));
                        finish();
                    } else {
                        Log.d(TAG, "找回密码失败:" + jsonObject.getJSONObject("response").getString("message"));
                        showToast("找回密码失败" + jsonObject.getJSONObject("response").getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                //上传性别失败回调
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    //改变验证码中的时间
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mBtnGetConfirmCode.setClickable(false);
            mBtnGetConfirmCode.setEnabled(false);
            mBtnGetConfirmCode.setText(millisUntilFinished / 1000 + "s" + "后重发");
        }

        @Override
        public void onFinish() {
            mBtnGetConfirmCode.setClickable(true);
            mBtnGetConfirmCode.setEnabled(true);
            mBtnGetConfirmCode.setText("重新获取");
        }
    }

    @Override
    public void onBackPressed() {
        cancelToast();
        super.onBackPressed();
    }
}
