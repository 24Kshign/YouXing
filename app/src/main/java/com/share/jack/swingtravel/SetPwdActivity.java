package com.share.jack.swingtravel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.utils.YXApplication;
import com.share.jack.utils.YXConstant;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/25 10:07
 * Copyright:1.0
 */

public class SetPwdActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SetPwdActivity";

    @Bind(R.id.ltb_rv_left)
    RelativeLayout mRvLeft;
    @Bind(R.id.ltb_tv_left)
    TextView mTvLeft;
    @Bind(R.id.ltb_tv_title)
    TextView mTvTitle;
    @Bind(R.id.asp_et_pwd)
    EditText mEtPwd;
    @Bind(R.id.asp_et_new_pwd)
    EditText mEtNewPwd;
    @Bind(R.id.asp_btn_finish)
    Button mBtnFinish;

    private ProgressDialog pd = null;
    private String strOldPwd;
    private String strNewPwd;
    private SharedPreferences sp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YXApplication.getInstance().addActivity(this);
        setContentView(R.layout.activity_set_pwd);
        sp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
        ButterKnife.bind(this);
        initeView();
    }

    private void initeView() {
        mRvLeft.setVisibility(View.VISIBLE);
        mTvTitle.setText(getResources().getString(R.string.info_setting));
        mTvLeft.setText(getResources().getString(R.string.back));
        mRvLeft.setOnClickListener(this);
        pd = new ProgressDialog(this);
        enableButtonOrNot(mBtnFinish, mEtPwd, mEtNewPwd);
        mBtnFinish.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        strOldPwd = MD5(mEtPwd.getText().toString().trim());
        strNewPwd = MD5(mEtNewPwd.getText().toString().trim());
        switch (v.getId()) {
            case R.id.ltb_rv_left:
                finish();
                break;
            case R.id.asp_btn_finish:
                hideKeyboard();
                pd.setMessage("密码正在重设...");
                pd.show();
                pd.setCanceledOnTouchOutside(false);
                reSetPwd();
                break;
        }
    }

    private void reSetPwd() {
        String url = "http://115.28.101.140/youxing/Home/User/editPassword";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        Log.d(TAG, sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("User_oldPassword", strOldPwd);
        params.put("User_newPassword", strNewPwd);

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
                        YXApplication.getInstance().setPassword(strNewPwd);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(YXConstant.USER_PWD, strNewPwd);
                        editor.commit();
                        showToast("密码修改成功,清放心使用");
                    } else {
                        showToast("修改密码失败" + jsonObject.getJSONObject("response").getString("message"));
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

    @Override
    public void onBackPressed() {
        cancelToast();
        super.onBackPressed();
    }
}
