package com.share.jack.swingtravel;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.util.EasyUtils;
import com.share.jack.demoutils.CommonUtils;
import com.share.jack.utils.YXApplication;
import com.share.jack.utils.YXConstant;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/2/18 14:31
 * Copyright:1.0
 */
//MainActivity的父类
public class SubMainActivity extends FragmentActivity {

    private static final int notifiId = 11;
    protected NotificationManager notificationManager;
    private static HomeWatcherReceiver mHomeKeyReceiver = null;
    private SharedPreferences sp = null;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        sp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        YXApplication.getInstance().addActivity(this);
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
            // onresume时，取消notification显示
            EMChatManager.getInstance().activityResumed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
            unregisterHomeKeyReceiver(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * 当应用在前台时，如果当前消息不是属于当前会话，在状态栏提示一下 如果不需要，注释掉即可
     *
     * @param message
     */
    protected void notifyNewMessage(EMMessage message) {
        // 如果是设置了不提醒只显示数目的群组(这个是app里保存这个数据的，demo里不做判断)
        // 以及设置了setShowNotificationInbackgroup:false(设为false后，后台时sdk也发送广播)
        if (!EasyUtils.isAppRunningForeground(this)) {
            return;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(getApplicationInfo().icon)
                .setWhen(System.currentTimeMillis()).setAutoCancel(true);

        String ticker = CommonUtils.getMessageDigest(message, this);
        if (message.getType() == EMMessage.Type.TXT)
            ticker = ticker.replaceAll("\\[.{2,3}\\]", "[表情]");
        // 设置状态栏提示
        mBuilder.setTicker(message.getFrom() + ": " + ticker);

        // 必须设置pendingintent，否则在2.3的机器上会有bug
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notifiId,
                intent, PendingIntent.FLAG_ONE_SHOT);
        mBuilder.setContentIntent(pendingIntent);

        Notification notification = mBuilder.build();
        notificationManager.notify(notifiId, notification);
        notificationManager.cancel(notifiId);
    }


}
