package com.share.jack.swingtravel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.share.jack.controller.HXSDKHelper;
import com.share.jack.db.UserDao;
import com.share.jack.demo.Constant;
import com.share.jack.domain.User;
import com.share.jack.model.DemoHXSDKHelper;
import com.share.jack.utils.YXConstant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/20 19:54
 * Copyright:1.0
 */
public class BaseActivity extends Activity {

    private static Toast mToast;
    private InputMethodManager inputMethodManager;
    public static BaseActivity mActivity = null;
    private static HomeWatcherReceiver mHomeKeyReceiver = null;
    private SharedPreferences sp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        sp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
        mActivity = this;
    }

    //根据输入框中的内容是否为空来判断按钮是否显示
    public void buttonShowOrNot(EditText editText, Button button) {
        String strContent = editText.getText().toString();
        if (!strContent.isEmpty()) {
            showView(button);
        } else {
            hideView(button);
        }
    }

    //隐藏控件
    public void hideView(View v) {
        v.setVisibility(v.INVISIBLE);
    }

    //显示控件
    public void showView(View v) {
        v.setVisibility(v.VISIBLE);
    }

    //弹出提示框
    public void showToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(this, text, 2000);
        } else {
            mToast.setText(text);
            mToast.setDuration(2000);
        }
        mToast.show();
    }

    public static void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    //弹出提示框
    public void showToast(int resId) {
        String text = getString(resId);
        if (mToast == null) {
            mToast = Toast.makeText(this, text, 2000);
        } else {
            mToast.setText(text);
            mToast.setDuration(2000);
        }
        mToast.show();
    }

    //让按钮不可点击
    public void disableButton(Button button) {
        button.setEnabled(false);
    }

    //让按钮不可点击
    public void enableButton(Button button) {
        button.setEnabled(true);
    }

    /**
     * 根据EditText文本内容是否为空判断是否disable按钮
     * 如果有一个EditText文本内容为空，则disable按钮
     */
    public void enableButtonOrNot(final Button button, final EditText... editText) {
        int length = editText.length; // 表示传入的EditText的个数
        final boolean[] flag = new boolean[length]; // 表示各个edittext文本内容是否为空，false表示空，true表示非空
        for (int i = 0; i < length; i++) {
            final int finalI = i;
            editText[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (editText[finalI].getText().toString().length() == 0) {
                        flag[finalI] = false;
                    } else {
                        flag[finalI] = true;
                    }
                    if (allNotEmpty(flag)) {
                        enableButton(button);
                    } else {
                        disableButton(button);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    /**
     * 判断是不是所有的标记表示的都是非空
     * 如果有一个表示的为空，则返回false
     * 否则，返回true
     */
    private boolean allNotEmpty(boolean[] flag) {
        for (int i = 0; i < flag.length; i++) {
            if (false == flag[i])
                return false;
        }
        return true;
    }

    /**
     * 触屏事件
     * 为隐藏软键盘做判断
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            if (null != getCurrentFocus() && null != getCurrentFocus().getWindowToken()) {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 隐藏软键盘
     */
    protected void hideKeyboard() {
        if (null != getCurrentFocus() && null != getCurrentFocus().getWindowToken()) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    //判断键盘是否弹出
    public boolean isKeyboard() {
        if (getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
            return true;
        }
        return false;
    }

    //获取屏幕宽度
    public int getScreenWidth() {
        return this.getWindowManager().getDefaultDisplay().getWidth();
    }

    //获取屏幕高度
    public int getScreenHeight() {
        return this.getWindowManager().getDefaultDisplay().getHeight();
    }

    // MD5加密，32位
    public static String MD5(String str) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        char[] charArray = str.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    //初始化好友列表
    public static void initializeContacts() {
        Map<String, User> userlist = new HashMap<String, User>();
        // 添加user"申请与通知"
        User newFriends = new User();
        newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
        String strChat = mActivity.getString(R.string.Application_and_notify);
        newFriends.setNick(strChat);
        userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
        // 存入内存
        ((DemoHXSDKHelper) HXSDKHelper.getInstance()).setContactList(userlist);
        // 存入db
        UserDao dao = new UserDao(mActivity);
        List<User> users = new ArrayList<User>(userlist.values());
        dao.saveContactList(users);
    }

    // 使用系统当前日期加以调整作为照片的名称
    public static String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    //判断手机格式是否正确,false代表格式正确
    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[0-9])|(17[7-8])|(18[0-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    private static void registerHomeKeyReceiver(Context context) {
        mHomeKeyReceiver = new HomeWatcherReceiver();
        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.registerReceiver(mHomeKeyReceiver, homeFilter);
    }

    private static void unregisterHomeKeyReceiver(Context context) {
        if (null != mHomeKeyReceiver) {
            context.unregisterReceiver(mHomeKeyReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
            registerHomeKeyReceiver(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
            unregisterHomeKeyReceiver(this);
        }
    }
}
