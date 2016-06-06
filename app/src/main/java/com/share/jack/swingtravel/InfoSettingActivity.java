package com.share.jack.swingtravel;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.share.jack.controller.HXSDKHelper;
import com.share.jack.demoutils.ImageUtils;
import com.share.jack.demoutils.UserUtils;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.model.DemoHXSDKHelper;
import com.share.jack.utils.ControlKeyboardUtils;
import com.share.jack.utils.YXApplication;
import com.share.jack.utils.YXConstant;
import com.share.jack.widget.CityPicker;
import com.share.jack.widget.CustomNickNameDialog;
import com.share.jack.widget.RoundImageView;
import com.share.jack.widget.SelectPicPopupWindow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/25 10:07
 * Copyright:1.0
 */

public class InfoSettingActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "InfoSettingActivity";

    @Bind(R.id.ltb_rv_left)
    RelativeLayout mRvLeft;
    @Bind(R.id.ltb_tv_left)
    TextView mTvLeft;
    @Bind(R.id.ltb_tv_title)
    TextView mTvTitle;
    @Bind(R.id.ais_rv_set_head)
    RelativeLayout mRvSetHead;
    @Bind(R.id.ais_rv_set_gender)
    RelativeLayout mRvSetGender;
    @Bind(R.id.ais_rv_set_location)
    RelativeLayout mRvSetLocation;
    @Bind(R.id.ais_tv_nickname)
    TextView mTvSetNickName;
    @Bind(R.id.ais_tv_gender)
    TextView mTvGender;
    @Bind(R.id.ais_riv_head)
    RoundImageView mRivHead;
    @Bind(R.id.ais_tv_location)
    TextView mTvLocation;
    @Bind(R.id.ais_iv_nickname)
    ImageView mIvGender;

    //调用摄像头拍照和调用图库的请求码
    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;  // 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;   // 从相册中选择
    private static final int PHOTO_REQUEST_RESULT = 3;   // 结果
    private SelectPicPopupWindow mSetHeadPop = null;
    private SharedPreferences sp = null;
    private String gender = null;
    private String nickname = null;
    private String location = null;
    private ProgressDialog pd = null;
    private File tempFile = null;
    private File imgFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YXApplication.getInstance().addActivity(this);
        setContentView(R.layout.activity_info_setting);
        sp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
        pd = new ProgressDialog(this);
        ButterKnife.bind(this);

        tempFile = new File(Environment.getExternalStorageDirectory() + "/com.share.jack.swingtravel");
        if (!tempFile.exists()) {
            tempFile.mkdir();
        }
        imgFile = new File(tempFile, "avater.png");
        mTvSetNickName.setText(sp.getString(YXConstant.USER_NICKNAME, "YX" + YXConstant.USER_PHONE.substring(5)));
        mTvGender.setText(sp.getString(YXConstant.USER_GENDER, getString(R.string.male)));
        mTvLocation.setText(sp.getString(YXConstant.USER_LOCATION, "浙江省—杭州市"));
        Log.d(TAG, "head=" + sp.getString(YXConstant.USER_HEAD, ""));
        if (!sp.getString(YXConstant.USER_HEAD, "").isEmpty()) {
            mRivHead.setImageBitmap(ImageUtils.stringtoBitmap(sp.getString(YXConstant.USER_HEAD, "")));
        } else {
            mRivHead.setImageResource(R.mipmap.default_avatar);
        }
        if (mTvGender.getText().toString().equals(getString(R.string.male))) {
            mIvGender.setImageResource(R.mipmap.fp_iv_man);
        } else {
            mIvGender.setImageResource(R.mipmap.fp_iv_woman);
        }
        initeView();
    }

    private void initeView() {
        mRvLeft.setVisibility(View.VISIBLE);
        mTvTitle.setText(getResources().getString(R.string.info_setting));
        mTvLeft.setText(getResources().getString(R.string.back));
        gender = mTvGender.getText().toString();
        location = mTvLocation.getText().toString();
        nickname = mTvSetNickName.getText().toString();

        mRvLeft.setOnClickListener(this);
        mTvSetNickName.setOnClickListener(this);
        mRvSetHead.setOnClickListener(this);
        mRvSetGender.setOnClickListener(this);
        mRvSetLocation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ltb_rv_left:
                finish();
                break;
            case R.id.ais_tv_nickname:
                showChangeNickNameDialog();
                break;
            case R.id.ais_rv_set_head:
                showSetHeadPop();
                break;
            case R.id.ais_rv_set_gender:
                showGenderSelector();
                break;
            case R.id.ais_rv_set_location:
                showSelectCity();
                break;
        }
    }

    //修改昵称
    private void showChangeNickNameDialog() {
        CustomNickNameDialog dialog = new CustomNickNameDialog(this, new CustomNickNameDialog.OnCustomDialogListener() {
            @Override
            public void setEt(EditText et_name) {
                et_name.requestFocus();
                et_name.setText(nickname);
                if (nickname.length() != 0) {
                    et_name.setSelection(nickname.length());
                }
            }

            @Override
            public void back(String name) {
                if (nickname.equals(name) || name.isEmpty()) {
                    mTvSetNickName.setText(nickname);
                    return;
                }
                updateNickNameToServer(name);
            }
        });
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
    }

    //上传昵称到服务器
    private void updateNickNameToServer(final String s) {
        pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("正在修改...");
        pd.show();
        String url = "http://115.28.101.140/youxing//Home/User/modifyUserInfo";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        Log.d(TAG, "id=" + String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        Log.d(TAG, "token=" + sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("User_NickName", s);

        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                //上传昵称成功回调
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        showToast("修改成功");
                        mTvSetNickName.setText(s);
                        SharedPreferences spNick = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
                        SharedPreferences.Editor editor = spNick.edit();
                        editor.putString(YXConstant.USER_NICKNAME, s);
                        editor.commit();
                    } else {
                        showToast("修改失败" + jsonObject.getJSONObject("response").getString("message"));
                        mTvSetNickName.setText(nickname);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                //上传昵称失败回调
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    //修改用户的环信头像
    private void updateUserInfo(final Bitmap bmp) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String avatarUrl = ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getUserProfileManager().uploadUserAvatar(Bitmap2Bytes(bmp));
                if (avatarUrl == null) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Log.d(TAG, getString(R.string.fail_to_link_server));
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "更新成功");
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (ControlKeyboardUtils.isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    hideKeyboard();
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    //设置头像
    private void showSetHeadPop() {
        mSetHeadPop = new SelectPicPopupWindow(this, new ItemSetHeadOnClickListener());
        //显示窗口位置
        mSetHeadPop.showAtLocation(InfoSettingActivity.this.findViewById(R.id.ais_lv_main),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }

    private void showSelectCity() {
        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.show();
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = dlg.getWindow().getAttributes();
        lp.width = (int) (display.getWidth()); //设置宽度
        dlg.getWindow().setAttributes(lp);
        Window window = dlg.getWindow();
        window.setContentView(R.layout.dialog_city);
        final CityPicker cityPicker = (CityPicker) window.findViewById(R.id.dc_cp_city);
        TextView tvFinish = (TextView) window.findViewById(R.id.dc_tv_finish);
        tvFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cityPicker.getCity().equals("市辖区") || cityPicker.getCity().equals("市")
                        || cityPicker.getCity().equals("县")) {
                    if (cityPicker.getCounty().equals("县") || cityPicker.getCounty().equals("市辖区")) {
                        showToast("错误的选择，请重新选择");
                        return;
                    }
                    mTvLocation.setText(cityPicker.getProvince() + "—" + cityPicker.getCounty());
                } else {
                    mTvLocation.setText(cityPicker.getProvince() + "—" + cityPicker.getCity());
                }
                dlg.cancel();
                if (mTvLocation.getText().toString().equals(location)) {
                    return;
                }
                updateLocationInfoToServer(mTvLocation.getText().toString());
            }
        });
    }

    //上传所在地到服务器
    private void updateLocationInfoToServer(final String s) {
        pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("正在修改...");
        pd.show();
        String url = "http://115.28.101.140/youxing//Home/User/modifyUserInfo";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        Log.d(TAG, "id=" + String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        Log.d(TAG, "token=" + sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("User_Location", s);

        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                //上传所在地成功回调
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        showToast("修改成功");
                        SharedPreferences spLocation = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
                        SharedPreferences.Editor editor = spLocation.edit();
                        editor.putString(YXConstant.USER_LOCATION, s);
                        editor.commit();
                        Log.d(TAG, "location=" + sp.getString(YXConstant.USER_LOCATION, ""));
                    } else {
                        showToast("修改失败" + jsonObject.getJSONObject("response").getString("message"));
                        mTvLocation.setText(location);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                //上传所在地失败回调
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    //设置性别
    private void showGenderSelector() {
        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.show();       //弹窗显示
        Window window = dlg.getWindow();
        // 设置窗口的内容页面,dialog_sex.xml文件中定义view内容
        window.setContentView(R.layout.dialog_sex);
        LinearLayout lv_title = (LinearLayout) window.findViewById(R.id.ds_lv_title);   //头部
        lv_title.setVisibility(View.VISIBLE);
        TextView tv_title = (TextView) window.findViewById(R.id.ds_tv_title);
        tv_title.setText("性别");       //设置头部
        // 为确认按钮添加事件,执行退出应用操作
        TextView tv_man = (TextView) window.findViewById(R.id.ds_tv_man);
        tv_man.setText(getString(R.string.male));
        tv_man.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mTvGender.setText(getString(R.string.male));
                mIvGender.setImageResource(R.mipmap.fp_iv_man);
                dlg.cancel();      //弹窗消失
                Log.d(TAG, "location=" + location);
                if (gender.equals(getString(R.string.male))) {
                    return;
                }
                updateGenderToServer(getString(R.string.male));
            }
        });
        TextView tv_woman = (TextView) window.findViewById(R.id.ds_tv_woman);
        tv_woman.setText(getString(R.string.female));
        tv_woman.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mTvGender.setText(getString(R.string.female));
                mIvGender.setImageResource(R.mipmap.fp_iv_woman);
                dlg.cancel();   //弹窗消失
                Log.d(TAG, "location=" + location);
                if (gender.equals(getString(R.string.female))) {
                    return;
                }
                updateGenderToServer(getString(R.string.female));
            }
        });
    }

    //上传性别到服务器
    private void updateGenderToServer(final String s) {
        pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("正在修改...");
        pd.show();
        String url = "http://115.28.101.140/youxing//Home/User/modifyUserInfo";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        Log.d(TAG, "id=" + String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        Log.d(TAG, "token=" + sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("User_Gender", s);

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
                        showToast("修改成功");
                        mTvGender.setText(s);
                        SharedPreferences spGender = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
                        SharedPreferences.Editor editor = spGender.edit();
                        editor.putString(YXConstant.USER_GENDER, s);
                        editor.commit();
                    } else {
                        showToast("修改失败" + jsonObject.getJSONObject("response").getString("message"));
                        mTvGender.setText(gender);
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


    //自定义Pop中按钮的点击事件
    private class ItemSetHeadOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.item_popupwindows_camera:
                    Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);   //打开系统照相机
                    intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imgFile));
                    startActivityForResult(intentCamera, PHOTO_REQUEST_TAKEPHOTO);
                    mSetHeadPop.dismiss();
                    break;
                case R.id.item_popupwindows_photo:
                    Intent intentGallery = new Intent(Intent.ACTION_PICK, null);
                    intentGallery.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intentGallery, PHOTO_REQUEST_GALLERY);
                    mSetHeadPop.dismiss();
                    break;
                case R.id.item_popupwindows_cancel:
                    mSetHeadPop.dismiss();
                    break;
            }
        }

    }

    /**
     * 调用照相机或调用图库结束后会返回此方法
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        返回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PHOTO_REQUEST_TAKEPHOTO:
                pd.dismiss();
                //用户点击了拍照按钮，从data中取出数据
                startImageZoom(Uri.fromFile(imgFile));
                break;
            case PHOTO_REQUEST_GALLERY:
                if (data == null) {
                    pd.dismiss();
                    return;
                }
                //data参数将会包含一个uri——统一资源标识符
                //该uri就是我们选择的图片所对应的uri
                startImageZoom(data.getData());
                break;
            case PHOTO_REQUEST_RESULT:
                if (data == null) {
                    return;
                }
                updateHeadToServer(data);     //上传图片到服务器
                break;
        }
    }

    private void saveBitmapToSd(Bitmap bm) {
        if (!tempFile.exists()) {
            tempFile.mkdir();
        }
        File img = new File(tempFile.getAbsolutePath() + "avater.png");
        if (img.exists()) {
            img.delete();
        }
        try {
            FileOutputStream fos = new FileOutputStream(img);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateHeadToServer(Intent picdata) {
        Bundle bundle = picdata.getExtras();
        if (bundle == null) {
            return;
        }
        pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("正在修改...");
        pd.show();
        String url = "http://115.28.101.140/youxing//Home/User/modifyUserInfo";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        Log.d(TAG, "id=" + String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        Log.d(TAG, "token=" + sp.getString(YXConstant.USER_TOKEN, ""));
        final Bitmap bitmap = bundle.getParcelable("data");
        saveBitmapToSd(bitmap);
        params.put("User_Head", ImageUtils.bitmaptoString(bitmap));
        Log.d(TAG, "head=" + ImageUtils.bitmaptoString(bitmap));

        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                //上传头像成功回调
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        updateUserInfo(bitmap);
                        mRivHead.setImageBitmap(bitmap);
                        showToast("修改成功");
                        SharedPreferences spHead = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
                        SharedPreferences.Editor editor = spHead.edit();
                        editor.putString(YXConstant.USER_HEAD, ImageUtils.bitmaptoString(bitmap));
                        editor.commit();
                    } else {
                        showToast("更新头像失败" + jsonObject.getJSONObject("response").getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                //上传头像失败回调
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    //调用系统的裁剪
    private void startImageZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP"); //跳转到系统裁剪界面
        intent.setDataAndType(uri, "image/*");   //同时设置数据和类型
        intent.putExtra("crop", "true");   //true表示在显示的view中是可裁剪的
        intent.putExtra("aspectX", 1);   //要裁剪的宽高比
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 150);   //输入图片的宽高
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTO_REQUEST_RESULT);
    }

    @Override
    public void onBackPressed() {
        cancelToast();
        super.onBackPressed();
    }
}
