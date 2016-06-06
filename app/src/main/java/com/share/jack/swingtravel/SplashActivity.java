package com.share.jack.swingtravel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.easemob.EMCallBack;
import com.loopj.android.http.RequestParams;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.model.DemoHXSDKHelper;
import com.share.jack.utils.YXApplication;
import com.share.jack.utils.YXConstant;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/21 15:34
 * Copyright:1.0
 */

//加载动画界面
public class SplashActivity extends BaseActivity {

    private static final String TAG = "SplashActivity";

    private boolean isFirst;
    // Handler:跳转至不同页面
    private final static int SWITCH_MAIN_ACTIVITY = 1000;    //跳转到主页
    private final static int SWITCH_GUIDE_ACTIVITY = 1001;  //跳转到引导界面
    private final static int SWITCH_MAIN_ACTIVITY_NOT_LOGIN = 1002;
    private SharedPreferences sp = null;
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SWITCH_MAIN_ACTIVITY_NOT_LOGIN:
                    Intent mIntent = new Intent();
                    mIntent.setClass(SplashActivity.this, MainActivity.class);
                    SplashActivity.this.startActivity(mIntent);
                    SplashActivity.this.finish();
                    break;
                case SWITCH_MAIN_ACTIVITY:
                    login();
                    break;
                case SWITCH_GUIDE_ACTIVITY:
                    mIntent = new Intent();
                    mIntent.setClass(SplashActivity.this, GuideActivity.class);
                    SplashActivity.this.startActivity(mIntent);
                    SplashActivity.this.finish();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void login() {
        String url = "http://115.28.101.140/youxing/Home/Index/checkToken";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    Log.d(TAG, jsonObject.getString("result"));
                    if (jsonObject.getString("result").equals("success")) {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    } else {
                        showToast("账号在别处登陆，请重新登陆");
                        JumpToLogin();
                    }
                } catch (final JSONException e) {
                    e.printStackTrace();
                    JumpToLogin();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                JumpToLogin();
            }
        });
    }

    private void JumpToLogin() {
        DemoHXSDKHelper.getInstance().logout(true, new EMCallBack() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        // 重新显示登陆页面
                        SharedPreferences sp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);   //保存是否已经登陆
                        SharedPreferences.Editor editor = sp.edit();
                        editor.clear();
                        editor.commit();
                        finish();
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        YXApplication.getInstance().finishAllActivity();
                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("unbind devicetokens failed");
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        cancelToast();
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //让此界面全屏显示，覆盖通知栏
        getWindow().setFlags(WindowManager.LayoutParams.TYPE_STATUS_BAR
                , WindowManager.LayoutParams.TYPE_STATUS_BAR);
        setContentView(R.layout.activity_splash);
        sp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
        isFirst = isFirstEnter(SplashActivity.this, SplashActivity.this.getClass().getName());
        if (isFirst)
            mHandler.sendEmptyMessageDelayed(SWITCH_GUIDE_ACTIVITY, 2500);
        else {
            if (sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
                mHandler.sendEmptyMessageDelayed(SWITCH_MAIN_ACTIVITY_NOT_LOGIN, 2500);
            } else {
                YXApplication.getInstance().setmUserId(String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
                YXApplication.getInstance().setmUserToken(sp.getString(YXConstant.USER_TOKEN, ""));
                mHandler.sendEmptyMessageDelayed(SWITCH_MAIN_ACTIVITY, 10);
            }
        }
    }

    private boolean isFirstEnter(Context context, String className) {
        if (context == null || className == null || "".equalsIgnoreCase(className)) {
            return false;
        }
        String mResultStr = context.getSharedPreferences(YXConstant.SHAREDPREFERENCES_NAME
                , Context.MODE_WORLD_READABLE).getString(YXConstant.KEY_GUIDE_ACTIVITY, "");   //取得所有类名 如 com.my.MainActivity
        if (mResultStr.equalsIgnoreCase("false"))
            return false;
        else
            return true;
    }
}
