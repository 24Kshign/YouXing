package com.share.jack.swingtravel;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.share.jack.demoutils.ImageUtils;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.utils.ControlKeyboardUtils;
import com.share.jack.utils.YXApplication;
import com.share.jack.utils.YXConstant;
import com.share.jack.widget.MyEditText;
import com.share.jack.widget.SelectPicPopupWindow;
import com.share.jack.widget.SoftKeyBoardSatusView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/23 12:54
 * Copyright:1.0
 */

public class PublishActivity extends BaseActivity implements View.OnClickListener
        , AMapLocationListener, View.OnLayoutChangeListener, SoftKeyBoardSatusView.SoftkeyBoardListener {

    private static final String TAG = "PublishActivity";

    @Bind(R.id.ltb_rv_left)
    RelativeLayout mRvLeft;
    @Bind(R.id.ltb_tv_left)
    TextView mTvLeft;
    @Bind(R.id.ltb_tv_title)
    TextView mTvTitle;
    @Bind(R.id.ltb_tv_right)
    TextView mTvRight;
    @Bind(R.id.ap_ib_write_location)
    ImageButton mIbWriteLocation;
    @Bind(R.id.ap_rv_write_location)
    RelativeLayout mRvWriteLocation;
    @Bind(R.id.ap_rv_location)
    RelativeLayout mRvLocation;
    @Bind(R.id.ap_rv_top)
    RelativeLayout mRvGetMainPic;
    @Bind(R.id.ap_iv_refresh)
    ImageView mIvLocationRefresh;
    @Bind(R.id.ap_et_write_location)
    EditText mEtLocation;
    @Bind(R.id.ap_et_content)
    MyEditText mEtContent;
    @Bind(R.id.ap_et_title)
    EditText mEtTitle;
    @Bind(R.id.ap_tv_start_location)
    TextView mTvStartLocation;
    @Bind(R.id.ap_rv_word_set)
    RelativeLayout mRvWordSetting;
    @Bind(R.id.ap_lv_add_pic)
    LinearLayout mLvAddPic;
    @Bind(R.id.ap_iv_main_pic)
    ImageView mIvGetMainPic;
    @Bind(R.id.ap_btn_word_bold)
    Button mBtnWordBold;
    @Bind(R.id.ap_btn_word_normal)
    Button mBtnWordNormal;
    @Bind(R.id.ap_btn_insert_pic)
    Button mBtnInsertPic;
    @Bind(R.id.ap_sv_scroll)
    ScrollView mScroll;
    @Bind(R.id.ap_rv_content)
    RelativeLayout mRvContent;
    @Bind(R.id.ap_rv_main)
    RelativeLayout activityRootView;   //Activity最外层的Layout视图
    @Bind(R.id.ap_soft_status_view)
    SoftKeyBoardSatusView statusview;

    private int keyHeight = 0;        //软件盘弹起后所占高度阀值
    private final int WHAT_SCROLL = 0;
    private final int WHAT_BTN_VISABEL = WHAT_SCROLL + 1;
    private SelectPicPopupWindow mGetMainPicPop = null;
    private InputMethodManager imm;

    //调用摄像头拍照和调用图库的请求码——选择主页图片
    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;  // 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;   // 从相册中选择，主图
    private static final int PHOTO_REQUEST_GALLERY2 = 3;   // 从相册中选择，其他图片
    private String mPagerItemFromMainActivity = null;

    //声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient = null;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;
    private AnimationDrawable mAnimation = null;
    private List<String> mPicList = new ArrayList<>();
    private List<Integer> mPicIndexList = new ArrayList<>();
    private SharedPreferences sp = null;
    private SharedPreferences spPublish = null;
    private ProgressDialog pd = null;
    private AnimationDrawable mRefreshAnim;


    private Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case WHAT_SCROLL:
                    int move = (Integer) msg.obj;
                    mScroll.smoothScrollBy(0, move);
                    break;
                case WHAT_BTN_VISABEL:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YXApplication.getInstance().addActivity(this);
        setContentView(R.layout.activity_publish);
        sp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
        pd = new ProgressDialog(this);
        spPublish = getSharedPreferences(YXConstant.PUBLISH, MODE_PRIVATE);
        imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        ButterKnife.bind(this);
        //阀值设置为屏幕高度的1/3
        keyHeight = getScreenHeight() / 3;

        statusview.setSoftKeyBoardListener(this);
        mPagerItemFromMainActivity = getIntent().getStringExtra("pagerItemToPublish");
        initeGaodeLocation();
        // 获取ImageView上的动画背景
        mRefreshAnim = (AnimationDrawable) mIvLocationRefresh.getBackground();
        // 开始动画
        mRefreshAnim.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        mLocationClient.startLocation();
        mEtContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {//获得焦点
                    mBtnInsertPic.setEnabled(true);
                    mRvWordSetting.setVisibility(View.VISIBLE);
                } else {//失去焦点
                    mBtnInsertPic.setEnabled(false);
                    mRvWordSetting.setVisibility(View.GONE);
                }
            }
        });
        initeView();
    }

    private void initeView() {
        mRvLeft.setVisibility(View.VISIBLE);
        mTvRight.setVisibility(View.VISIBLE);
        mTvTitle.setText(getResources().getString(R.string.info_setting));
        mTvLeft.setText(getResources().getString(R.string.back));
        mTvRight.setText(getResources().getString(R.string.publish));
        mTvRight.setOnClickListener(this);
        mRvLeft.setOnClickListener(this);
        mIbWriteLocation.setOnClickListener(this);
        mRvGetMainPic.setOnClickListener(this);
        mBtnInsertPic.setOnClickListener(this);
        mBtnWordBold.setOnClickListener(this);
        mBtnWordNormal.setOnClickListener(this);
        mRvContent.setOnClickListener(this);
        mEtTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mRvWordSetting.setVisibility(View.GONE);
                } else {
                    mRvWordSetting.setVisibility(View.VISIBLE);
                }
            }
        });
        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged_start=" + start);
                Log.d(TAG, "beforeTextChanged_count=" + count);
                Log.d(TAG, "beforeTextChanged_after=" + after);
                if (count > 2) {
                    if (mPicList.size() > 0) {
                        Log.d(TAG, "before_size=" + mPicList.size());
                        mPicList.remove(mPicList.size() - 1);
                        Log.d(TAG, "before_after=" + mPicList.size());
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged_start=" + start);
                Log.d(TAG, "onTextChanged_before=" + before);
                Log.d(TAG, "onTextChanged_count=" + count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged_s=" + s.toString());
            }
        });
    }

    /**
     * 初始化高德定位
     */
    private void initeGaodeLocation() {
        // 初始化定位，
        mLocationClient = new AMapLocationClient(this.getApplicationContext());
        // 初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(true);
        // 设置定位模式为低功耗定位
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        // 设置定位回调监听
        mLocationClient.setLocationListener(this);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        // 设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
    }


    @Override
    public void onClick(View v) {
        String strTitle = mEtTitle.getText().toString();
        String strContent = mEtContent.getText().toString();
        String strLocation = mEtLocation.getText().toString();
        switch (v.getId()) {
            case R.id.ltb_rv_left:
                finish();
                break;
            case R.id.ltb_tv_right:
                hideKeyboard();
                createOtherPicToJson();
                asyncHttpPost(strTitle, strContent, strLocation);
                break;
            case R.id.ap_ib_write_location:
                mRvWriteLocation.setVisibility(View.VISIBLE);
                mRvLocation.setVisibility(View.GONE);
                break;
            case R.id.ap_rv_top:
                showGetMainPicPop();
                break;
            case R.id.ap_btn_word_bold:
                TextPaint tp = mEtContent.getPaint();
                tp.setFakeBoldText(true);
                showToast("字体即将加粗");
                break;
            case R.id.ap_btn_word_normal:
                tp = mEtContent.getPaint();
                tp.setFakeBoldText(false);
                showToast("字体即将变细");
                break;
            case R.id.ap_btn_insert_pic:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PHOTO_REQUEST_GALLERY2);
                break;
        }
    }

    private void asyncHttpPost(String title, String content, String location) {
        pd.setMessage("发布中，请稍后...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        String url = "http://115.28.101.140/youxing/Home/Recomment/publish";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        Log.d(TAG, "id=" + sp.getInt(YXConstant.USER_ID, 0));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Title", title);
        params.put("Address", location);
        params.put("City_Id", spPublish.getString(YXConstant.PUBLISH_CITY_ID, ""));
        params.put("Longitude", spPublish.getString(YXConstant.PUBLISH_LONGITUED, ""));
        params.put("Latitude", spPublish.getString(YXConstant.PUBLISH_LATITUDE, ""));
        params.put("Main_Image", spPublish.getString(YXConstant.PUBLISH_MAIN_PIC, ""));
        Log.d(TAG, "main_image=" + spPublish.getString(YXConstant.PUBLISH_MAIN_PIC, ""));
        params.put("Content", content);

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
                        uploadOtherPic(jsonObject.getJSONObject("response").getJSONObject("data").getString("recomment_id"));
                    } else {
                        showToast("发布文章失败" + jsonObject.getJSONObject("response").getString("message"));
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

    private void uploadOtherPic(String articleId) {
        pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("发布中...");
        String url = "http://115.28.101.140/youxing/Home/Recomment/uploadImgArray";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        Log.d(TAG, "id=" + sp.getInt(YXConstant.USER_ID, 0));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Recomment_Id", articleId);
        params.put("Other_Image", spPublish.getString(YXConstant.PUBLISH_OTHER_PIC, ""));

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
                        SharedPreferences spp = getSharedPreferences(YXConstant.PUBLISH, MODE_APPEND);
                        SharedPreferences.Editor editor = spp.edit();
                        editor.clear();
                        editor.commit();
                        startActivity(new Intent(PublishActivity.this, MainActivity.class));
                        Log.d(TAG, "publish success");
                        finish();
                    } else {
                        showToast("发布失败" + jsonObject.getJSONObject("response").getString("message"));
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

    //将list集合转化成json字符串
    private void createOtherPicToJson() {
        Gson gson = new Gson();
        String json = gson.toJson(mPicList);
        Log.d(TAG, "json=" + json);
        Log.d(TAG, "count" + mPicList.size());
        SharedPreferences spp = getSharedPreferences(YXConstant.PUBLISH, MODE_PRIVATE);
        SharedPreferences.Editor editor = spp.edit();
        editor.putString(YXConstant.PUBLISH_OTHER_PIC, json);
        editor.commit();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (PublishActivity.this.getCurrentFocus() != null && PublishActivity.this.getCurrentFocus().getWindowToken() != null) {
                mRvWordSetting.setVisibility(View.GONE);
                imm.hideSoftInputFromWindow(PublishActivity.this.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.onTouchEvent(event);
    }

    //选择主页图片
    private void showGetMainPicPop() {
        mGetMainPicPop = new SelectPicPopupWindow(this, new ItemSetHeadOnClickListener());
        //显示窗口位置
        mGetMainPicPop.showAtLocation(PublishActivity.this.findViewById(R.id.ap_rv_main),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }

    //自定义Pop中按钮的点击事件
    private class ItemSetHeadOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.item_popupwindows_camera:
                    Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);   //打开系统照相机
                    startActivityForResult(intentCamera, PHOTO_REQUEST_TAKEPHOTO);
                    mGetMainPicPop.dismiss();
                    break;
                case R.id.item_popupwindows_photo:
                    Intent intentGallery = new Intent(Intent.ACTION_GET_CONTENT);   //打开一个内容选择的界面
                    intentGallery.setType("image/*");     //设定要选择的对象是图片
                    startActivityForResult(intentGallery, PHOTO_REQUEST_GALLERY);
                    mGetMainPicPop.dismiss();
                    break;
                case R.id.item_popupwindows_cancel:
                    mGetMainPicPop.dismiss();
                    break;
                case R.id.ap_rv_content:
                    mEtContent.requestFocus();
                    ControlKeyboardUtils.showKeyBoard(PublishActivity.this, mEtContent);
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
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_REQUEST_TAKEPHOTO:
                    if (data == null) {        //说明用户在调用摄像头拍照之后点击了取消按钮
                        return;
                    } else {      //用户点击了拍照按钮，从data中取出数据
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            mLvAddPic.setVisibility(View.GONE);
                            mIvGetMainPic.setVisibility(View.VISIBLE);
                            Bitmap bitmap = bundle.getParcelable("data");     //保存数据
                            Bitmap bm = getimage(getContentResolver(), saveBitmap(bitmap));
                            SharedPreferences spp = getSharedPreferences(YXConstant.PUBLISH, MODE_APPEND);
                            SharedPreferences.Editor editor = spp.edit();
                            editor.putString(YXConstant.PUBLISH_MAIN_PIC, ImageUtils.bitmaptoString(bm));
                            editor.commit();
                            mIvGetMainPic.setImageBitmap(bm);
                        }
                    }
                    break;
                case PHOTO_REQUEST_GALLERY:
                    if (data == null) {
                        return;
                    } else {
                        //data参数将会包含一个uri——统一资源标识符
                        //该uri就是我们选择的图片所对应的uri
                        mLvAddPic.setVisibility(View.GONE);
                        mIvGetMainPic.setVisibility(View.VISIBLE);
                        Uri uri = data.getData();
                        Bitmap bitmap = getimage(getContentResolver(), uri);
                        SharedPreferences spp = getSharedPreferences(YXConstant.PUBLISH, MODE_APPEND);
                        SharedPreferences.Editor editor = spp.edit();
                        editor.putString(YXConstant.PUBLISH_MAIN_PIC, ImageUtils.bitmaptoString(bitmap));
                        editor.commit();
                        mIvGetMainPic.setImageBitmap(bitmap);
                    }
                    break;
                case PHOTO_REQUEST_GALLERY2:
                    if (data == null) {
                        return;
                    } else {
                        //data参数将会包含一个uri——统一资源标识符
                        //该uri就是我们选择的图片所对应的uri
                        Uri uri = data.getData();
                        Bitmap bitmap = getimage(getContentResolver(), uri);
                        mPicIndexList.add(mEtContent.getText().toString().length());
                        Log.d(TAG, "indexList" + mEtContent.getText().toString().length());
                        mPicList.add(ImageUtils.bitmaptoString(bitmap));
                        mEtContent.insertDrawable(mEtContent, bitmap, getScreenWidth(), "★");
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Bitmap getimage(ContentResolver cr, Uri uri) {
        try {
            Bitmap bitmap = null;
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            // options.inJustDecodeBounds=true,图片不加载到内存中
            newOpts.inJustDecodeBounds = true;

            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);

            BitmapFactory.decodeStream(cr.openInputStream(uri), null, newOpts);
            newOpts.inJustDecodeBounds = false;
            int imgWidth = dm.widthPixels;
            int imgHeight = dm.heightPixels;
            int heightRadio = (int) Math.ceil(newOpts.outHeight / (float) imgWidth);
            int widthRadio = (int) Math.ceil(newOpts.outWidth / (float) imgHeight);
            if (heightRadio > 1 && widthRadio > 1) {
                if (heightRadio > widthRadio) {
                    newOpts.inSampleSize = heightRadio;
                } else {
                    newOpts.inSampleSize = widthRadio;
                }
            }
            //真正解码图片
            newOpts.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri), null, newOpts);
            return ImageUtils.compressImage(bitmap);
        } catch (Exception e) {
            System.out.println("文件不存在");
            return null;
        }
    }

    //将bitmap保存到sd卡中，并返回一个file类型的uri
    private Uri saveBitmap(Bitmap bt) {
        File tmpDir = new File(Environment.getExternalStorageDirectory() + "/com.share.jack.swingtravel");
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
        File img = new File(tmpDir.getAbsolutePath() + getPhotoFileName());
        try {
            FileOutputStream fos = new FileOutputStream(img);
            bt.compress(Bitmap.CompressFormat.PNG, 85, fos);
            fos.flush();
            fos.close();
            return Uri.fromFile(img);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (null != aMapLocation) {
            if (0 == aMapLocation.getErrorCode()) {       //定位成功
                mRvLocation.setVisibility(View.GONE);
                mRvWriteLocation.setVisibility(View.VISIBLE);
                //定位成功回调信息，设置相关消息
                aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                aMapLocation.getLatitude();//获取纬度
                aMapLocation.getLongitude();//获取经度
                aMapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(aMapLocation.getTime());
                df.format(date);//定位时间
                aMapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                aMapLocation.getCountry();//国家信息
                aMapLocation.getProvince();//省信息
                aMapLocation.getCity();//城市信息
                aMapLocation.getDistrict();//城区信息
                aMapLocation.getRoad();//街道信息
                aMapLocation.getCityCode();//城市编码
                aMapLocation.getAdCode();//地区编码
                SharedPreferences spp = getSharedPreferences(YXConstant.PUBLISH, MODE_APPEND);
                SharedPreferences.Editor editor = spp.edit();
                editor.putString(YXConstant.PUBLISH_LONGITUED, String.valueOf(aMapLocation.getLongitude()));
                editor.putString(YXConstant.PUBLISH_CITY_ID, aMapLocation.getCityCode());
                editor.putString(YXConstant.PUBLISH_LATITUDE, String.valueOf(aMapLocation.getLatitude()));
                editor.commit();
                mLocationClient.stopLocation();
                mEtLocation.setText(aMapLocation.getAddress().toString());
                mAnimation.stop();
            } else {      //定位失败
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e(TAG, "location Error, ErrCode:" + aMapLocation.getErrorCode() + "" +
                        ", errInfo:" + aMapLocation.getErrorInfo());
                mRvLocation.setVisibility(View.VISIBLE);
                mRvWriteLocation.setVisibility(View.GONE);
                mTvStartLocation.setText("定位失败...要不自己写写?");
                mTvStartLocation.setTextColor(Color.RED);
                mRefreshAnim.stop();
                mIvLocationRefresh.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityRootView.addOnLayoutChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(PublishActivity.this, MainActivity.class)
                    .putExtra("pagerItemToMainActivity", mPagerItemFromMainActivity));
            this.finish();
            return true;
        }
        return false;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        //现在认为只要控件将Activity向上推的高度超过了1/3屏幕高，就认为软键盘弹起
        if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {
            mRvWordSetting.setVisibility(View.VISIBLE);
        } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {
            mRvWordSetting.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        cancelToast();
        super.onBackPressed();
    }

    //控制布局往上移软键盘不挡住输入框
    @Override
    public void keyBoardStatus(int w, int h, int oldw, int oldh) {

    }

    @Override
    public void keyBoardVisable(int move) {
        mRvWordSetting.getScrollY();
        Message message = new Message();
        message.what = WHAT_SCROLL;
        message.obj = move;
        mHandler.sendMessageDelayed(message, 500);
    }

    @Override
    public void keyBoardInvisable(int move) {
        mHandler.sendEmptyMessageDelayed(WHAT_BTN_VISABEL, 200);
    }
}
