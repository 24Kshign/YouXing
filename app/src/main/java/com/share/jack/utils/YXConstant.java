package com.share.jack.utils;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/20 19:45
 * Copyright:1.0
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.view.WindowManager;
import android.widget.Toast;

import com.amap.api.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 这个类存放一些第三方的信息
 */
public class YXConstant {

    //Mob短信验证的App_Key和App_Secret
    public static final String SMSAPP_KEY = "120467ba95f4c";
    public static final String SMSAPP_SECRET = "d88daf6d6606fc3fe969066937922e26";

    // 判断应用是否初次加载，读取SharedPreferences中的guide_activity字段
    public static final String SHAREDPREFERENCES_NAME = "my_pref";
    public static final String KEY_GUIDE_ACTIVITY = "guide_activity";

    //用户自己的信息
    public static final String USER = "user";   //整个用户的信息
    public static final String USER_ID = "user_id";    //用户id
    public static final String USER_NICKNAME = "user_nickname";   //用户昵称
    public static final String USER_PHONE = "user_phone";  //用户手机号
    public static final String USER_GENDER = "user_gender";   //用户性别
    public static final String USER_HEAD = "user_head";   //用户头像
    public static final String USER_LOCATION = "user_location";   //用户归属地
    public static final String USER_PWD = "user_pwd";   //用户密码
    public static final String USER_TOKEN = "user_token";   //用户token
    public static final String USER_CITY_ID = "user_city_id";    //用户所在城市id

    //用户发布的信息
    public static final String PUBLISH = "publish";    //整个发布的信息
    public static final String PUBLISH_MAIN_PIC = "main_pic";   //发布的文章主图
    public static final String PUBLISH_OTHER_PIC = "other_pic";    //发布的文章其他图片
    public static final String PUBLISH_LONGITUED = "longitued";    //发布的文章经度
    public static final String PUBLISH_LATITUDE = "latitude";    //发布的文章纬度
    public static final String PUBLISH_CITY_ID = "latitude";    //发布的城市id

    //定位城市的天气信息
    public static final String WEATHER_INFO = "weather_info";
    public static final String WEATHER_CITY = "weather_city";
    public static final String WEATHER = "weather";
    public static final String WEATHER_DEGREE = "weather_degree";

    //序列化
    public final static String PAR_KEY = "com.share.jack.swingtravel";
    public final static String PAR_USER_KEY = "com.share.jack.swingtravel.user";


    //百度Api story的APP_KEY
    public static final String BAIDU_APP_KEY = "9280bd3c3f6c6d7b136705e8936b558a";

    public static final LatLng BEIJING = new LatLng(39.90403, 116.407525);// 北京市经纬度
    public static final LatLng XIAN = new LatLng(39.804280, 116.507580);// 西安市经纬度
    public static final LatLng HANGZHOU = new LatLng(39.704280, 116.607580);// 市经纬度



    //自定义显示Toast
    private static Toast mToast;

    //弹出提示框
    public static void showToast(Context context, String text) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, 2000);
        } else {
            mToast.setText(text);
            mToast.setDuration(2000);
        }
        mToast.show();
    }

    //弹出提示框
    public static void showToast(Context context, int resId) {
        String text = context.getResources().getString(resId);
        if (mToast == null) {
            mToast = Toast.makeText(context, text, 2000);
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

    //对json数据乱码进行修正
    public static String decodeUnicode(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';

                    else if (aChar == 'n')

                        aChar = '\n';

                    else if (aChar == 'f')

                        aChar = '\f';

                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }

    //判断字符串s1是否包含s2
    public static boolean AisContainB(String s1, String s2) {
        return s1.contains(s2);
    }

    /**
     * 替换表情
     *
     * @param str
     * @param context
     * @return
     */
    public static SpannableString getSpannableString(String str, Context context) {
        SpannableString spannableString = new SpannableString(str);
        String s = "\\[(.+?)\\]";
        Pattern pattern = Pattern.compile(s, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(spannableString);
        while (matcher.find()) {
            String key = matcher.group();
            int id = Expression.getIdAsName(key);
            if (id != 0) {
                Drawable drawable = context.getResources().getDrawable(id);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                ImageSpan imageSpan = new ImageSpan(drawable);
                spannableString.setSpan(imageSpan, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableString;
    }

}
