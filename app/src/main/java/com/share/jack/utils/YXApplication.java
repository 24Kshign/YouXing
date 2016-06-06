package com.share.jack.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChat;
import com.share.jack.bean.LocationBean;
import com.share.jack.model.DemoHXSDKHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/21 19:38
 * Copyright:1.0
 */
public class YXApplication extends Application {

    private static final String TAG = "YXApplication";

    public static Context applicationContext;
    private static YXApplication instance;
    private List<Activity> activities = new ArrayList<Activity>();

    //当前用户nickname,为了苹果推送不是userid而是昵称
    public static DemoHXSDKHelper hxSDKHelper = new DemoHXSDKHelper();

    private String mUserId;
    private String mUserToken;
    public List<LocationBean> mList = new ArrayList<>();


    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        instance = this;
        hxSDKHelper.onInit(applicationContext);
    }

    public static YXApplication getInstance() {
        if (null == instance) {
            instance = new YXApplication();
        }
        return instance;
    }

    public String getmUserId() {
        return mUserId;
    }

    public void setmUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getmUserToken() {
        return mUserToken;
    }

    public void setmUserToken(String mUserToken) {
        this.mUserToken = mUserToken;
    }

    /**
     * 退出登录,清空数据
     */
    public void logout(final boolean isGCM, final EMCallBack emCallBack) {
        // 先调用sdk logout，在清理app中自己的数据
        hxSDKHelper.logout(isGCM, emCallBack);
    }

    public YXApplication() {
    }

    /**
     * 获取当前登陆用户名
     *
     * @return
     */
    public String getUserName() {
        return hxSDKHelper.getHXId();
    }

    /**
     * 设置用户名
     *
     * @param username
     */
    public void setUserName(String username) {
        hxSDKHelper.setHXId(username);
    }

    /**
     * 设置密码 下面的实例代码 只是demo，实际的应用中需要加password 加密后存入 preference 环信sdk
     * 内部的自动登录需要的密码，已经加密存储了
     *
     * @param pwd
     */
    public void setPassword(String pwd) {
        hxSDKHelper.setPassword(pwd);
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public void finishAllActivity() {
        for (Activity a : activities) {
            a.finish();
        }
        System.exit(0);
    }
}
