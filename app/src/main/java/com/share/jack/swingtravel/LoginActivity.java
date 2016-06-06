package com.share.jack.swingtravel;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.util.NetUtils;
import com.loopj.android.http.RequestParams;
import com.share.jack.controller.HXSDKHelper;
import com.share.jack.db.UserDao;
import com.share.jack.demo.Constant;
import com.share.jack.demoutils.ImageUtils;
import com.share.jack.domain.User;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.model.DemoHXSDKHelper;
import com.share.jack.utils.YXApplication;
import com.share.jack.utils.YXConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/20 19:18
 * Copyright:1.0
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    @Bind(R.id.ltb_tv_right)
    TextView mTvRight;
    @Bind(R.id.ltb_rv_left)
    RelativeLayout mRvLeft;
    @Bind(R.id.ltb_tv_title)
    TextView mTvTitle;
    @Bind(R.id.al_btn_login)
    Button mBtnLogin;
    @Bind(R.id.al_et_user)
    EditText mEtUser;
    @Bind(R.id.al_et_pwd)
    EditText mEtPwd;
    @Bind(R.id.al_tv_forget_pwd)
    TextView mTvForgetPwd;

    private String strPhone;
    private String strPwd;
    private String strNotLogin = null;
    private ProgressDialog pd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YXApplication.getInstance().addActivity(this);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        strNotLogin = getIntent().getStringExtra("isLoging");
        strPhone = getIntent().getStringExtra("userPhone");
        initeView();      //初始化事件
    }

    private void initeView() {
        mRvLeft.setVisibility(View.GONE);
        mTvRight.setVisibility(View.VISIBLE);
        mTvTitle.setText(getResources().getString(R.string.login));
        mTvRight.setText(getResources().getString(R.string.register));
        mTvRight.setOnClickListener(this);
        pd = new ProgressDialog(this);
        if (strPhone != null) {
            mEtUser.setText(strPhone);
            mEtUser.setSelection(strPhone.length());
            enableButtonOrNot(mBtnLogin, mEtPwd);
        } else {
            enableButtonOrNot(mBtnLogin, mEtUser, mEtPwd);
        }
        mBtnLogin.setOnClickListener(this);
        mTvForgetPwd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        strPhone = mEtUser.getText().toString().trim();
        strPwd = MD5(mEtPwd.getText().toString().trim());
        switch (v.getId()) {
            case R.id.ltb_tv_right:
                hideKeyboard();
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.al_btn_login:
                hideKeyboard();
                if (!NetUtils.hasNetwork(this)) {
                    showToast(getString(R.string.the_current_network));
                    return;
                }
                login();
                break;
            case R.id.al_tv_forget_pwd:
                hideKeyboard();
                startActivity(new Intent(this, ForgetPwdActivity.class));
                break;
        }
    }

    private void login() {
        pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(getString(R.string.Is_landing));
        pd.show();
        String url = "http://115.28.101.140/youxing/Home/Index/login";
        RequestParams params = new RequestParams();
        params.put("User_Phone", strPhone);
        params.put("User_Password", strPwd);

        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                //登陆成功回调
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
                    Log.d(TAG, jsonObject.getString("result"));
                    if (jsonObject.getString("result").equals("success")) {
                        loginToMain(new String(response));
                    } else {
                        showToast(getString(R.string.Login_failed) + jsonObject.getJSONObject("response").getString("message"));
                    }
                } catch (final JSONException e) {
                    e.printStackTrace();
                    showToast(getString(R.string.Login_failed) + e.toString());
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                //登陆失败回调
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    private void loginToMain(final String result) {
        pd = new ProgressDialog(LoginActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("登录服务器....");
        pd.show();
        // 调用sdk登陆方法登陆聊天服务器
        EMChatManager.getInstance().login(strPhone, strPwd, new EMCallBack() {
            @Override
            public void onSuccess() {
                // 登陆成功，保存用户名密码
                YXApplication.getInstance().setUserName(strPhone);
                YXApplication.getInstance().setPassword(strPwd);
                try {
                    // ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
                    // ** manually load all local groups and
                    EMChatManager.getInstance().loadAllConversations();
                    // 处理好友和群组
                    initializeContacts();
                } catch (Exception e) {
                    e.printStackTrace();
                    // 取好友或者群聊失败，不让进入主页面
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (pd != null && pd.isShowing()) {
                                pd.dismiss();
                            }
                            DemoHXSDKHelper.getInstance().logout(true, null);
                            showToast(getResources().getString(R.string.login_failure_failed));
                        }
                    });
                    return;
                }
                JSONObject jsonObjec = null;
                int id = 0;
                String token = null;
                try {
                    jsonObjec = new JSONObject(result).getJSONObject("response").getJSONObject("data");
                    id = jsonObjec.getInt("uid");
                    token = jsonObjec.getString("token");
                } catch (final JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (pd != null && pd.isShowing()) {
                                pd.dismiss();
                            }
                            DemoHXSDKHelper.getInstance().logout(true, null);
                            showToast(getString(R.string.Login_failed) + e.toString());
                        }
                    });
                }
                if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
                    if (pd != null && pd.isShowing()) {
                        pd.dismiss();
                    }
                }
                Message msg = mHandler.obtainMessage();
                msg.arg1 = id;
                msg.obj = token;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onProgress(int progress, String status) {
            }

            @Override
            public void onError(final int code, final String message) {
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                showToast(getString(R.string.Login_failed) + message);
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                int id = msg.arg1;
                String token = (String) msg.obj;
                getUserInfo(id, token);
            }
        }
    };

    private void getUserInfo(final int id, final String token) {
        pd = new ProgressDialog(LoginActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("正在获取信息....");
        pd.show();
        String url = "http://115.28.101.140/youxing/Home/User/getUserInfoById";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(id));
        Log.d(TAG, "userId=" + id);
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                //获取信息成功回调
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
                    if (jsonObject.getString("result").equals("success")) {
                        SharedPreferences spUser = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
                        final SharedPreferences.Editor userEditor = spUser.edit();
                        userEditor.putString(YXConstant.USER_PHONE, strPhone);
                        userEditor.putString(YXConstant.USER_PWD, strPwd);
                        Log.d(TAG, "token=" + token);
                        userEditor.putString(YXConstant.USER_TOKEN, token);
                        final JSONObject infoJson = jsonObject.getJSONObject("response").getJSONObject("data");
                        if (!infoJson.getString("Uid").isEmpty()) {
                            userEditor.putInt(YXConstant.USER_ID, infoJson.getInt("Uid"));
                        }
                        if (!infoJson.getString("Nickname").isEmpty()) {
                            Log.d(TAG, "nickname=" + infoJson.getString("Nickname"));
                            userEditor.putString(YXConstant.USER_NICKNAME, infoJson.getString("Nickname"));
                        } else {
                            userEditor.putString(YXConstant.USER_NICKNAME, "YX_" + strPhone.substring(5));
                        }
                        if (!infoJson.getString("Sex").isEmpty()) {
                            Log.d(TAG, "sex=" + infoJson.getString("Sex"));
                            userEditor.putString(YXConstant.USER_GENDER, infoJson.getString("Sex"));
                        } else {
                            userEditor.putString(YXConstant.USER_GENDER, getString(R.string.male));
                        }
                        if (!infoJson.getString("Live").isEmpty()) {
                            Log.d(TAG, "location=" + infoJson.getString("Live"));
                            userEditor.putString(YXConstant.USER_LOCATION, infoJson.getString("Live"));
                        } else {
                            userEditor.putString(YXConstant.USER_LOCATION, "浙江省—杭州市");
                        }
                        if (!infoJson.getString("Head").isEmpty()) {
                            Log.d(TAG, "head=" + infoJson.getString("Head"));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        new MyAsyncTaskTest().execute(infoJson.getString("Head"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            userEditor.putString(YXConstant.USER_HEAD, ImageUtils.bitmaptoString(BitmapFactory.decodeResource(getResources(), R.mipmap.default_avatar)));
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                        userEditor.commit();
                    } else {
                        showToast("获取用户信息失败" + jsonObject.getJSONObject("response").getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                //登陆失败回调
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    class MyAsyncTaskTest extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return getBitmap(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            SharedPreferences spp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
            SharedPreferences.Editor eeditor = spp.edit();
            eeditor.putString(YXConstant.USER_HEAD, ImageUtils.bitmaptoString(bitmap));
            eeditor.commit();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private Bitmap getBitmap(String url) {
        Bitmap bm = null;
        URLConnection conn;
        InputStream is;
        try {
            conn = new URL(url).openConnection();
            is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            is.close();
            bis.close();
            return bm;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            if (strNotLogin == null) {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                YXApplication.getInstance().finishAllActivity();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        cancelToast();
        super.onBackPressed();
    }
}
