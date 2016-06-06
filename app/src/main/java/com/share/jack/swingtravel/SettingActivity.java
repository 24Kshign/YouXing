package com.share.jack.swingtravel;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.EMCallBack;
import com.loopj.android.http.RequestParams;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.model.DemoHXSDKHelper;
import com.share.jack.utils.DataCleanManager;
import com.share.jack.utils.YXApplication;
import com.share.jack.utils.YXConstant;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/24 15:43
 * Copyright:1.0
 */

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SettingActivity";

    @Bind(R.id.ltb_rv_left)
    RelativeLayout mRvLeft;
    @Bind(R.id.ltb_tv_left)
    TextView mTvLeft;
    @Bind(R.id.ltb_tv_title)
    TextView mTvTitle;
    @Bind(R.id.as_btn_exit)
    Button mBtnExit;
    @Bind(R.id.as_rv_info_setting)
    RelativeLayout mRvInfoSetting;
    @Bind(R.id.as_rv_messege_tip)
    RelativeLayout mRvMessegeTip;
    @Bind(R.id.as_rv_clear_chat_record)
    RelativeLayout mRvClearChatRecord;
    @Bind(R.id.as_tv_num_cache)
    TextView mTvNumCache;
    @Bind(R.id.as_rv_clear_cache)
    RelativeLayout mRvClearCache;
    @Bind(R.id.as_rv_set_pwd)
    RelativeLayout mRvSetPwd;

    private ProgressDialog pd = null;
    private SharedPreferences sp = null;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    showToast("清除聊天记录成功");
                    pd.dismiss();
                    break;
                case 1:
                    mTvNumCache.setVisibility(View.GONE);
                    showToast("清除缓存成功");
                    pd.dismiss();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YXApplication.getInstance().addActivity(this);
        setContentView(R.layout.activity_setting);
        sp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
        ButterKnife.bind(this);
        initeView();
    }

    private void initeView() {
        mRvLeft.setVisibility(View.VISIBLE);
        mTvLeft.setText(getResources().getString(R.string.back));
        mTvTitle.setText(getResources().getString(R.string.setting));
        try {
            Log.d(TAG, "cache=" + DataCleanManager.getTotalCacheSizeDouble(this));
            if (DataCleanManager.getTotalCacheSizeDouble(this) >= 102400) {
                mTvNumCache.setVisibility(View.VISIBLE);
                mTvNumCache.setText(DataCleanManager.getTotalCacheSize(this));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBtnExit.setOnClickListener(this);
        mRvLeft.setOnClickListener(this);
        mRvInfoSetting.setOnClickListener(this);
        mRvMessegeTip.setOnClickListener(this);
        mRvClearChatRecord.setOnClickListener(this);
        mRvClearCache.setOnClickListener(this);
        mRvSetPwd.setOnClickListener(this);
        if (sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {   //没有登陆过
            mBtnExit.setEnabled(false);
            mBtnExit.setBackgroundResource(R.color.btnLoginGreenDisable);
        } else {
            mBtnExit.setEnabled(true);
            mBtnExit.setBackgroundResource(R.drawable.btn_exit);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.as_btn_exit:
                showExitDialog();
                break;
            case R.id.ltb_rv_left:
                finish();
                break;
            case R.id.as_rv_info_setting:
                if (sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {   //没有登陆过
                    startActivity(new Intent(this, LoginActivity.class).putExtra("isLoging", "jack"));
                } else {
                    startActivity(new Intent(this, InfoSettingActivity.class));
                }
                break;
            case R.id.as_rv_messege_tip:
                if (sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {   //没有登陆过
                    startActivity(new Intent(this, LoginActivity.class).putExtra("isLoging", "jack"));
                } else {
                    startActivity(new Intent(this, MessegeTipActivity.class));
                }
                break;
            case R.id.as_rv_clear_chat_record:
                pd = new ProgressDialog(this);
                pd.setMessage("正在清除....");
                pd.setCanceledOnTouchOutside(false);
                pd.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mHandler.sendEmptyMessage(0);
                    }
                }).start();
                break;
            case R.id.as_rv_clear_cache:
                pd = new ProgressDialog(this);
                pd.setMessage("正在清除....");
                pd.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DataCleanManager.clearAllCache(SettingActivity.this);
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mHandler.sendEmptyMessage(1);
                    }
                }).start();
                break;
            case R.id.as_rv_set_pwd:
                if (sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {    //没有登陆过
                    startActivity(new Intent(this, LoginActivity.class).putExtra("isLoging", "jack"));
                } else {
                    startActivity(new Intent(this, SetPwdActivity.class));
                }
                break;
        }
    }

    //退出提示框
    private void showExitDialog() {
        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.show();       //弹窗显示
        Window window = dlg.getWindow();
        // 设置窗口的内容页面,dialog_sex.xml文件中定义view内容
        window.setContentView(R.layout.dialog_exit);
        Button btnSure = (Button) window.findViewById(R.id.de_btn_sure);
        Button btnCancel = (Button) window.findViewById(R.id.de_btn_cancel);
        btnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SettingActivity--->", "exit application");
                loginToLoginActivity();
                dlg.cancel();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.cancel();
            }
        });
    }

    private void loginToLoginActivity() {
        pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(getString(R.string.Are_logged_out));
        pd.show();
        String url = "http://115.28.101.140/youxing/Home/User/logout";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        Log.d(TAG, "token=" + sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));

        RequestUtils.ClientGet(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                //上传性别成功回调
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        logout();
                    } else {
                        showToast("退出失败" + jsonObject.getJSONObject("response").getString("message"));
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

    //退出当前账号
    private void logout() {
        pd = new ProgressDialog(this);
        pd.setMessage("正在退出服务器...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        DemoHXSDKHelper.getInstance().logout(true, new EMCallBack() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        pd.dismiss();
                        // 重新显示登陆页面
                        SharedPreferences sp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);   //保存是否已经登陆
                        SharedPreferences.Editor editor = sp.edit();
                        editor.clear();
                        editor.commit();
                        finish();
                        startActivity(new Intent(SettingActivity.this, LoginActivity.class));
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
}
