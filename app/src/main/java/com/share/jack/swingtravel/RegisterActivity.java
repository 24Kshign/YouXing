package com.share.jack.swingtravel;

import android.app.ProgressDialog;
import android.content.Intent;
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
 * Time:2016/1/22 09:09
 * Copyright:1.0
 */

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private static final int MSG_INFO = 0x011;

    @Bind(R.id.ltb_tv_right)
    TextView mTvRight;
    @Bind(R.id.ltb_tv_left)
    TextView mTvLeft;
    @Bind(R.id.ltb_rv_left)
    RelativeLayout mRvLeft;
    @Bind(R.id.ltb_tv_title)
    TextView mTvTitle;
    @Bind(R.id.ar_et_user)
    EditText mEtUser;
    @Bind(R.id.ar_et_pwd)
    EditText mEtPwd;
    @Bind(R.id.ar_et_confirm_code)
    EditText mEtConfirmCode;
    @Bind(R.id.ar_btn_finish)
    Button mBtnFinish;
    @Bind(R.id.ar_btn_confirm_code)
    Button mBtnGetConfirmCode;

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
                        register();
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {    //提示验证码已经发送，等待验证
                        showToast("验证码已经发送");
                    }
                } else {         //校验验证码不正确
                    ((Throwable) data).printStackTrace();
                    int resId = getStringRes(RegisterActivity.this, "smssdk_network_error");
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
        setContentView(R.layout.activity_register);
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
        mTvLeft.setText(getResources().getString(R.string.back));
        mTvTitle.setText(getResources().getString(R.string.register));
        mTvRight.setVisibility(View.GONE);
        enableButtonOrNot(mBtnFinish, mEtUser, mEtConfirmCode, mEtPwd);
        enableButtonOrNot(mBtnGetConfirmCode, mEtUser);
        pd = new ProgressDialog(this);
        mBtnFinish.setOnClickListener(this);
        mRvLeft.setOnClickListener(this);
        mBtnGetConfirmCode.setOnClickListener(this);
    }

    //交互
    private void register() {
        pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(getString(R.string.Is_the_registered));
        pd.show();
        String url = "http://115.28.101.140/youxing/Home/Index/register";
        RequestParams params = new RequestParams();
        params.put("User_Phone", strPhone);
        params.put("User_Password", strPwd);
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                //注册成功回调
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (pd != null && pd.isShowing()) {
                            pd.dismiss();
                        }
                    }
                });
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    Log.d(TAG, "result=" + jsonObject.getString("result"));
                    if (jsonObject.getString("result").equals("success")) {
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class).putExtra("userPhone", strPhone));
                        showToast("注册成功，请登录");
                    } else {
                        if (jsonObject.getString("error").equals("1011")) {
                            showToast(getString(R.string.alerady_has_account));
                        } else {
                            showToast(getString(R.string.Registration_failed) + jsonObject.getJSONObject("response").getString("message"));
                        }
                    }
                } catch (final JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            showToast(getString(R.string.Registration_failed) + e.toString());
                        }
                    });
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                //注册失败回调
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    @Override
    public void onClick(View v) {
        strPhone = mEtUser.getText().toString().trim();
        strPwd = MD5(mEtPwd.getText().toString().trim());
        strIdentify = mEtConfirmCode.getText().toString();
        switch (v.getId()) {
            case R.id.ltb_rv_left:
                this.finish();
                break;
            case R.id.ar_btn_finish:
                hideKeyboard();
                if (!NetUtils.hasNetwork(this)) {
                    showToast(getString(R.string.the_current_network));
                    return;
                }
                SMSSDK.submitVerificationCode("86", strPhone, strIdentify);
                break;
            case R.id.ar_btn_confirm_code:
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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }

    @Override
    public void onBackPressed() {
        cancelToast();
        super.onBackPressed();
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
}
