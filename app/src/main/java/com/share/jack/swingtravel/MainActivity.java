package com.share.jack.swingtravel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.util.EMLog;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.share.jack.bean.LocationBean;
import com.share.jack.controller.HXSDKHelper;
import com.share.jack.demo.Constant;
import com.share.jack.demoutils.ImageUtils;
import com.share.jack.fragment.MeetFragment;
import com.share.jack.fragment.MessegeFragment;
import com.share.jack.fragment.PersonalFragment;
import com.share.jack.fragment.ShopFragment;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.model.DemoHXSDKHelper;
import com.share.jack.utils.YXApplication;
import com.share.jack.utils.YXConstant;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/20 19:18
 * Copyright:1.0
 */

public class MainActivity extends SubMainActivity implements View.OnClickListener
        , AMapLocationListener, EMEventListener {

    private static final String TAG = "MainActivity";
    private static final int GET_WEATHER = 10;

    @Bind(R.id.am_rv_shop)
    RelativeLayout mRvShop;
    @Bind(R.id.am_iv_shop)
    ImageView mIvShop;
    @Bind(R.id.am_tv_shop)
    TextView mTvShop;
    @Bind(R.id.am_rv_meet)
    RelativeLayout mRvMeet;
    @Bind(R.id.am_iv_meet)
    ImageView mIvMeet;
    @Bind(R.id.am_tv_meet)
    TextView mTvMeet;
    @Bind(R.id.am_rv_publish)
    RelativeLayout mRvPublish;
    @Bind(R.id.am_rv_messege)
    RelativeLayout mRvMessege;
    @Bind(R.id.am_iv_messege)
    ImageView mIvMessege;
    @Bind(R.id.am_tv_messege)
    TextView mTvMessege;
    @Bind(R.id.am_rv_personal)
    RelativeLayout mRvPersonal;
    @Bind(R.id.am_iv_personal)
    ImageView mIvPersonal;
    @Bind(R.id.am_tv_personal)
    TextView mTvPersonal;
    @Bind(R.id.am_unread_msg_number)
    TextView unreadLabel;

    private ShopFragment mShopFragment;
    private MeetFragment mMeetFragment;
    private MessegeFragment mMessegeFragment;
    private PersonalFragment mPersonalFragment;

    private int mFragmentItem = 0;      //表示当前显示的是哪个界面
    private long clickTime = 0; //记录第一次点击返回键的时间
    public static MainActivity mainActivityInstance = null;
    private NewMessageBroadcastReceiver msgReceiver;

    private android.app.AlertDialog.Builder conflictBuilder;
    private android.app.AlertDialog.Builder accountRemovedBuilder;
    private boolean isConflictDialogShow;
    private boolean isAccountRemovedDialogShow;

    // 账号在别处登录
    public static boolean isConflict = false;
    // 账号被移除
    public static boolean isCurrentAccountRemoved = false;
    private SharedPreferences sp = null;


    //声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient = null;

    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;

    private String mCityName = "";

    /**
     * 检查当前用户是否被删除
     */
    public boolean getCurrentAccountRemoved() {
        return isCurrentAccountRemoved;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_WEATHER:
                    String result = YXConstant.decodeUnicode((String) msg.obj);
                    Log.d(TAG, "result=" + result);
                    getJsonDatas(result);
                    break;
                case 11:
                    String head = (String) msg.obj;
                    SharedPreferences infoSp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
                    SharedPreferences.Editor editor = infoSp.edit();
                    Log.d(TAG, "head=" + head);
                    editor.putString(YXConstant.USER_HEAD, head);
                    editor.commit();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YXApplication.getInstance().addActivity(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        sp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
        mainActivityInstance = this;
        initeGaodeLocation();
        mLocationClient.startLocation();
        initeView();          //初始化控件
        setSelect(mFragmentItem);
        if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
            YXApplication.getInstance().setmUserId(String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
            YXApplication.getInstance().setmUserToken(sp.getString(YXConstant.USER_TOKEN, ""));
            if (savedInstanceState != null && savedInstanceState.getBoolean(Constant.ACCOUNT_REMOVED, false)) {
                // 防止被移除后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
                // 三个fragment里加的判断同理
                DemoHXSDKHelper.getInstance().logout(true, null);
                finish();
                startActivity(new Intent(this, LoginActivity.class));
            } else if (savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false)) {
                // 防止被T后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
                // 三个fragment里加的判断同理
                finish();
                startActivity(new Intent(this, LoginActivity.class));
            }
            if (getIntent().getBooleanExtra("conflict", false) && !isConflictDialogShow) {
                showConflictDialog();
            } else if (getIntent().getBooleanExtra(Constant.ACCOUNT_REMOVED, false) && !isAccountRemovedDialogShow) {
                showAccountRemovedDialog();
            }
            MobclickAgent.updateOnlineConfig(this);
            // 注册一个接收消息的BroadcastReceiver
            msgReceiver = new NewMessageBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
            intentFilter.setPriority(3);   //设置广播的优先级
            registerReceiver(msgReceiver, intentFilter);
            EMChat.getInstance().setAppInited();
        }
    }

    private void initeView() {
        mRvShop.setOnClickListener(this);
        mRvMeet.setOnClickListener(this);
        mRvPublish.setOnClickListener(this);
        mRvMessege.setOnClickListener(this);
        mRvPersonal.setOnClickListener(this);
        String strPagerItem = getIntent().getStringExtra("pagerItemToMainActivity");
        if (strPagerItem != null) {
            mFragmentItem = Integer.valueOf(strPagerItem);
        }
    }

    /**
     * 显示帐号在别处登录dialog
     */
    public void showConflictDialog() {
        isConflictDialogShow = true;
        DemoHXSDKHelper.getInstance().logout(false, null);
        String st = getResources().getString(R.string.Logoff_notification);
        if (!isFinishing()) {
            // clear up global variables
            try {
                if (conflictBuilder == null) {
                    conflictBuilder = new android.app.AlertDialog.Builder(this);
                }
                conflictBuilder.setTitle(st);
                conflictBuilder.setMessage(R.string.connect_conflict);
                conflictBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        conflictBuilder = null;
                        finish();
                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                        startMain.addCategory(Intent.CATEGORY_HOME);
                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(startMain);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        YXApplication.getInstance().finishAllActivity();
                    }
                });
                conflictBuilder.setCancelable(false);
                conflictBuilder.create().show();
                isConflict = true;
            } catch (Exception e) {
                EMLog.e("出去嗨---->", "---------color conflictBuilder error" + e.getMessage());
            }
        }
    }

    /**
     * 帐号被移除的dialog
     */
    public void showAccountRemovedDialog() {
        isAccountRemovedDialogShow = true;
        DemoHXSDKHelper.getInstance().logout(true, null);
        String st5 = getResources().getString(R.string.Remove_the_notification);
        if (!isFinishing()) {
            // clear up global variables
            try {
                if (accountRemovedBuilder == null)
                    accountRemovedBuilder = new android.app.AlertDialog.Builder(this);
                accountRemovedBuilder.setTitle(st5);
                accountRemovedBuilder.setMessage(R.string.em_user_remove);
                accountRemovedBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        accountRemovedBuilder = null;
                        finish();
                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                        startMain.addCategory(Intent.CATEGORY_HOME);
                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(startMain);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        YXApplication.getInstance().finishAllActivity();

                    }
                });
                accountRemovedBuilder.setCancelable(false);
                accountRemovedBuilder.create().show();
                isCurrentAccountRemoved = true;
            } catch (Exception e) {
                EMLog.e(TAG, "---------color userRemovedBuilder error" + e.getMessage());
            }
        }
    }

    Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            Message msg = mHandler.obtainMessage();
            BufferedReader reader = null;
            String result = null;
            StringBuffer sbf = new StringBuffer();
            String httpUrl = null;
            try {
                httpUrl = "http://apis.baidu.com/apistore/weatherservice/cityname"
                        + "?cityname=" + URLEncoder.encode(mCityName, "UTF-8");
                Log.d(TAG, "httpUrl=" + httpUrl);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                URL url = new URL(httpUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                // 填入apikey到HTTP header
                connection.setRequestProperty("apikey", YXConstant.BAIDU_APP_KEY);
                connection.connect();
                InputStream is = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String strRead = null;
                while ((strRead = reader.readLine()) != null) {
                    sbf.append(strRead);
                    sbf.append("\r\n");
                }
                reader.close();
                result = sbf.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            msg.what = GET_WEATHER;
            msg.obj = result;
            mHandler.sendMessage(msg);
        }
    };

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
        resetImage();
        switch (v.getId()) {
            case R.id.am_rv_shop:
                mFragmentItem = 0;
                setSelect(mFragmentItem);
                break;
            case R.id.am_rv_meet:
                mFragmentItem = 1;
                setSelect(mFragmentItem);
                break;
            case R.id.am_rv_publish:
                if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
                    startActivity(new Intent(this, PublishActivity.class)
                            .putExtra("pagerItemToPublish", String.valueOf(mFragmentItem)));
                } else {
                    startActivity(new Intent(this, LoginActivity.class).putExtra("isLoging", "jack"));
                }
                break;
            case R.id.am_rv_messege:
                mFragmentItem = 2;
                setSelect(mFragmentItem);
                break;
            case R.id.am_rv_personal:
                mFragmentItem = 3;
                setSelect(mFragmentItem);
                break;
        }
    }

    public void setSelect(int select) {
        // 设置内容区域
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();    //创建一个事务
        hideFragment(transaction);     //我们先把所有的Fragment隐藏了，然后下面再开始处理具体要显示的Fragment
        // 设置图片和文字的亮色
        switch (select) {
            case 0:
                if (mShopFragment == null) {
                    mShopFragment = new ShopFragment();
                    transaction.add(R.id.am_fl_layout, mShopFragment);    //将Fragment添加到Activity中
                } else {
                    transaction.show(mShopFragment);          //将当前fragment显示到界面
                }
                mIvShop.setImageResource(R.mipmap.am_iv_shop_pressed);
                mTvShop.setTextColor(getResources().getColor(R.color.btnLoginGreenNormal));
                break;
            case 1:
                if (mMeetFragment == null) {
                    mMeetFragment = new MeetFragment();
                    transaction.add(R.id.am_fl_layout, mMeetFragment);
                } else {
                    transaction.show(mMeetFragment);
                }
                mIvMeet.setImageResource(R.mipmap.am_iv_meet_pressed);
                mTvMeet.setTextColor(getResources().getColor(R.color.btnLoginGreenNormal));
                break;
            case 2:
                if (mMessegeFragment == null) {
                    mMessegeFragment = new MessegeFragment();
                    transaction.add(R.id.am_fl_layout, mMessegeFragment);
                } else {
                    transaction.show(mMessegeFragment);
                }
                mIvMessege.setImageResource(R.mipmap.am_iv_messege_pressed);
                mTvMessege.setTextColor(getResources().getColor(R.color.btnLoginGreenNormal));
                break;
            case 3:
                if (mPersonalFragment == null) {
                    mPersonalFragment = new PersonalFragment();
                    transaction.add(R.id.am_fl_layout, mPersonalFragment);
                } else {
                    transaction.show(mPersonalFragment);
                }
                mIvPersonal.setImageResource(R.mipmap.am_iv_personal_pressed);
                mTvPersonal.setTextColor(getResources().getColor(R.color.btnLoginGreenNormal));
                break;
        }
        /**
         * 在这里使用commit()这个方法会出错，因为onSaveInstanceState方法是在该Activity即将被销毁前调用，
         * 来保存Activity数据的，如果在保存玩状态后再给它添加Fragment就会出错。所以改用commitAllowingStateLoss()
         * 这个方法
         */
//        transaction.commit();
        transaction.commitAllowingStateLoss();    //提交事务，千万不能忘，否则布局中会没有东西
    }

    /**
     * 隐藏所有的fragment
     *
     * @param transaction
     */
    private void hideFragment(FragmentTransaction transaction) {
        if (mShopFragment != null) {
            transaction.hide(mShopFragment);
        }
        if (mMeetFragment != null) {
            transaction.hide(mMeetFragment);
        }
        if (mMessegeFragment != null) {
            transaction.hide(mMessegeFragment);
        }
        if (mPersonalFragment != null) {
            transaction.hide(mPersonalFragment);
        }
    }

    // 将图片及文字变为暗色
    private void resetImage() {
        mIvShop.setImageResource(R.mipmap.am_iv_shop_normal);
        mIvMeet.setImageResource(R.mipmap.am_iv_meet_normal);
        mIvMessege.setImageResource(R.mipmap.am_iv_messege_normal);
        mIvPersonal.setImageResource(R.mipmap.am_iv_personal_normal);
        mTvShop.setTextColor(Color.BLACK);
        mTvMeet.setTextColor(Color.BLACK);
        mTvMessege.setTextColor(Color.BLACK);
        mTvPersonal.setTextColor(Color.BLACK);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSelect(mFragmentItem);
        Log.d(TAG, "token=" + sp.getString(YXConstant.USER_TOKEN, "false"));
        if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
            if (!isConflict && !isCurrentAccountRemoved) {
                MainActivity.mainActivityInstance.updateUnreadLabel();
                EMChatManager.getInstance().activityResumed();
            }
            updateUnreadLabel();
            EMChatManager.getInstance().activityResumed();
            DemoHXSDKHelper sdkHelper = (DemoHXSDKHelper) DemoHXSDKHelper.getInstance();
            sdkHelper.pushActivity(this);

            // register the event listener when enter the foreground
            EMChatManager.getInstance().registerEventListener(this,
                    new EMNotifierEvent.Event[]{EMNotifierEvent.Event.EventNewMessage, EMNotifierEvent.Event.EventOfflineMessage, EMNotifierEvent.Event.EventConversationListChanged});
        }
    }

    @Override
    protected void onStop() {
        if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
            EMChatManager.getInstance().unregisterEventListener(this);
            DemoHXSDKHelper sdkHelper = (DemoHXSDKHelper) DemoHXSDKHelper.getInstance();
            sdkHelper.popActivity(this);
        }
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isConflict", isConflict);
        outState.putBoolean(Constant.ACCOUNT_REMOVED, isCurrentAccountRemoved);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - clickTime) > 2000) {       //如果两次时间间隔大于两秒则不做处理，记下最后一次按下back键的时间
                YXConstant.showToast(this, "再按一次后退键退出程序");
                clickTime = System.currentTimeMillis();
            } else {
                if (YXApplication.getInstance().mList.size() != 0) {
                    upLoadLocation();
                }
                finish();
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

    private void upLoadLocation() {
        String url = "http://115.28.101.140/youxing/Home/Meet/uploadUserLocation";
        Gson gson = new Gson();
        String json = gson.toJson(YXApplication.getInstance().mList);
        RequestParams params = new RequestParams();
        params.put("User_ID", YXApplication.getInstance().getmUserId());
        params.put("Token", YXApplication.getInstance().getmUserToken());
        params.put("User_Point", json);
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        Log.d(TAG, "success");
                    } else {
                        Log.d(TAG, "failure");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    /**
     * 刷新未读消息数
     */
    public void updateUnreadLabel() {
        int count = getUnreadMsgCountTotal();
        if (count > 0) {
            unreadLabel.setText(String.valueOf(count));
            unreadLabel.setVisibility(View.VISIBLE);
        } else {
            unreadLabel.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        YXConstant.cancelToast();
        super.onBackPressed();
    }

    /**
     * 获取未读消息数
     *
     * @return
     */
    public int getUnreadMsgCountTotal() {
        int unreadMsgCountTotal = 0;
        unreadMsgCountTotal = EMChatManager.getInstance().getUnreadMsgsCount();
        return unreadMsgCountTotal;
    }

    @Override
    public void onEvent(EMNotifierEvent event) {
        switch (event.getEvent()) {
            case EventNewMessage: // 普通消息
            {
                EMMessage message = (EMMessage) event.getData();
                // 提示新消息
                HXSDKHelper.getInstance().getNotifier().onNewMsg(message);
                refreshUI();
                break;
            }
            case EventOfflineMessage: {
                refreshUI();
                break;
            }
            case EventConversationListChanged: {
                refreshUI();
                break;
            }
        }
    }

    private void refreshUI() {
        runOnUiThread(new Runnable() {
            public void run() {
                // 刷新bottom bar消息未读数
                updateUnreadLabel();
                if (mFragmentItem == 2) {
                    // 当前页面如果为聊天历史页面，刷新此页面
                    if (mMessegeFragment != null) {
                        mMessegeFragment.refresh();
                    }
                }
            }
        });
    }

    /**
     * 新消息广播接收者
     */
    private class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 主页面收到消息后，主要为了提示未读，实际消息内容需要到chat页面查看

            String from = intent.getStringExtra("from");
            // 消息id
            String msgId = intent.getStringExtra("msgid");
            EMMessage message = EMChatManager.getInstance().getMessage(msgId);
            // 2014-10-22 修复在某些机器上，在聊天页面对方发消息过来时不立即显示内容的bug
            if (ChatActivity.activityInstance != null) {
                if (message.getChatType() == EMMessage.ChatType.GroupChat) {
                    if (message.getTo().equals(
                            ChatActivity.activityInstance.getToChatUsername()))
                        return;
                } else {
                    if (from.equals(ChatActivity.activityInstance
                            .getToChatUsername()))
                        return;
                }
            }
            // 注销广播接收者，否则在ChatActivity中会收到这个广播
            abortBroadcast();
            notifyNewMessage(message);
            // 刷新bottom bar消息未读数
            updateUnreadLabel();
            if (mFragmentItem == 0) {
                // 当前页面如果为聊天历史页面，刷新此页面
                if (mMessegeFragment != null) {
                    mMessegeFragment.refresh();
                }
            }
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        Log.d(TAG, "success to location");
        if (null != aMapLocation) {
            if (0 == aMapLocation.getErrorCode()) {       //定位成功
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
                mCityName = aMapLocation.getCity();
                mCityName = mCityName.replace("市", "");
                Log.d(TAG, "城市=" + aMapLocation.getCity());

                SharedPreferences.Editor editor = sp.edit();
                Log.d(TAG, "cityCode=" + aMapLocation.getCityCode());
                editor.putString(YXConstant.USER_CITY_ID, aMapLocation.getCityCode());
                editor.commit();
                mLocationClient.stopLocation();
                new Thread(networkTask).start();
            } else {      //定位失败
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.d(TAG, "location Error, ErrCode:" + aMapLocation.getErrorCode() + "" +
                        ", errInfo:" + aMapLocation.getErrorInfo());
            }
        }
    }

    //数据解析
    private void getJsonDatas(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result).getJSONObject("retData");
            SharedPreferences weatherSp = getSharedPreferences(YXConstant.WEATHER_INFO, MODE_PRIVATE);
            SharedPreferences.Editor editor = weatherSp.edit();
            Log.d(TAG, "city=" + jsonObject.getString("city") + "\t" + "temp=" + jsonObject.getString("temp"));
            editor.putString(YXConstant.WEATHER_CITY, jsonObject.getString("city"));
            editor.putString(YXConstant.WEATHER, jsonObject.getString("weather"));
            editor.putString(YXConstant.WEATHER_DEGREE, jsonObject.getString("temp"));
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
            if (conflictBuilder != null) {
                conflictBuilder.create().dismiss();
                conflictBuilder = null;
            }
            if (msgReceiver != null) {
                unregisterReceiver(msgReceiver);
            }
        }
        mLocationClient.onDestroy();
    }

    //onTouchEvent的回调接口
    public interface MyTouchListener {
        public void onTouchEvent(MotionEvent event);
    }

    //保存MyTouchListener接口的列表
    private ArrayList<MyTouchListener> myTouchListeners = new ArrayList<MyTouchListener>();

    /**
     * 提供给Fragment通过getActivity()方法来注册自己的触摸事件的方法
     *
     * @param listener
     */
    public void registerMyTouchListener(MyTouchListener listener) {
        myTouchListeners.add(listener);
    }

    /**
     * 提供给Fragment通过getActivity()方法来取消注册自己的触摸事件的方法
     *
     * @param listener
     */
    public void unRegisterMyTouchListener(MyTouchListener listener) {
        myTouchListeners.remove(listener);
    }

    /**
     * 分发触摸事件给所有注册了MyTouchListener的接口
     *
     * @param ev
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (MyTouchListener listener : myTouchListeners) {
            listener.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }
}
