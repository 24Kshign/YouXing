
package com.share.jack.model;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.EMChatRoomChangeListener;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Type;
import com.easemob.util.EMLog;
import com.easemob.util.EasyUtils;
import com.share.jack.controller.HXSDKHelper;
import com.share.jack.demo.Constant;
import com.share.jack.demoutils.CommonUtils;
import com.share.jack.domain.RobotUser;
import com.share.jack.domain.User;
import com.share.jack.swingtravel.ChatActivity;
import com.share.jack.swingtravel.LoginActivity;
import com.share.jack.swingtravel.R;
import com.share.jack.utils.YXConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Demo UI HX SDK helper class which subclass HXSDKHelper
 *
 * @author easemob
 */
public class DemoHXSDKHelper extends HXSDKHelper {

    private static final String TAG = "DemoHXSDKHelper";

    /**
     * EMEventListener
     */
    protected EMEventListener eventListener = null;

    /**
     * contact list in cache
     */
    private Map<String, User> contactList;

    /**
     * robot list in cache
     */
    private Map<String, RobotUser> robotList;

    private UserProfileManager userProManager;

    /**
     * 用来记录foreground Activity
     */
    private List<Activity> activityList = new ArrayList<Activity>();

    public void pushActivity(Activity activity) {
        if (!activityList.contains(activity)) {
            activityList.add(0, activity);
        }
    }

    public void popActivity(Activity activity) {
        activityList.remove(activity);
    }

    @Override
    public synchronized boolean onInit(Context context) {
        if (super.onInit(context)) {
            getUserProfileManager().onInit(context);

            //if your app is supposed to user Google Push, please set project number
            String projectNumber = "562451699741";
            EMChatManager.getInstance().setGCMProjectNumber(projectNumber);
            return true;
        }

        return false;
    }

    @Override
    protected void initHXOptions() {
        super.initHXOptions();

        // you can also get EMChatOptions to set related SDK options
        EMChatOptions options = EMChatManager.getInstance().getChatOptions();
        options.allowChatroomOwnerLeave(getModel().isChatroomOwnerLeaveAllowed());
    }

    @Override
    protected void initListener() {
        super.initListener();
        IntentFilter callFilter = new IntentFilter(EMChatManager.getInstance().getIncomingCallBroadcastAction());
        //注册消息事件监听
        initEventListener();
    }

    /**
     * 全局事件监听
     * 因为可能会有UI页面先处理到这个消息，所以一般如果UI页面已经处理，这里就不需要再次处理
     * activityList.size() <= 0 意味着所有页面都已经在后台运行，或者已经离开Activity Stack
     */
    protected void initEventListener() {
        eventListener = new EMEventListener() {
            private BroadcastReceiver broadCastReceiver = null;

            @Override
            public void onEvent(EMNotifierEvent event) {
                EMMessage message = null;
                if (event.getData() instanceof EMMessage) {
                    message = (EMMessage) event.getData();
                    EMLog.d(TAG, "receive the event : " + event.getEvent() + ",id : " + message.getMsgId());
                }

                switch (event.getEvent()) {
                    case EventNewMessage:
                        //应用在后台，不需要刷新UI,通知栏提示新消息
                        if (activityList.size() <= 0) {
                            HXSDKHelper.getInstance().getNotifier().onNewMsg(message);
                        }
                        break;
                    case EventOfflineMessage:
                        if (activityList.size() <= 0) {
                            EMLog.d(TAG, "received offline messages");
                            List<EMMessage> messages = (List<EMMessage>) event.getData();
                            HXSDKHelper.getInstance().getNotifier().onNewMesg(messages);
                        }
                        break;
                    // below is just giving a example to show a cmd toast, the app should not follow this
                    // so be careful of this
                    case EventNewCMDMessage: {

                        EMLog.d(TAG, "收到透传消息");
                        //获取消息body
                        CmdMessageBody cmdMsgBody = (CmdMessageBody) message.getBody();
                        final String action = cmdMsgBody.action;//获取自定义action

                        //获取扩展属性 此处省略
                        //message.getStringAttribute("");
                        EMLog.d(TAG, String.format("透传消息：action:%s,message:%s", action, message.toString()));
                        final String str = appContext.getString(R.string.receive_the_passthrough);

                        final String CMD_TOAST_BROADCAST = "easemob.demo.cmd.toast";
                        IntentFilter cmdFilter = new IntentFilter(CMD_TOAST_BROADCAST);

                        if (broadCastReceiver == null) {
                            broadCastReceiver = new BroadcastReceiver() {

                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    Toast.makeText(appContext, intent.getStringExtra("cmd_value"), Toast.LENGTH_SHORT).show();
                                }
                            };

                            //注册广播接收者
                            appContext.registerReceiver(broadCastReceiver, cmdFilter);
                        }

                        Intent broadcastIntent = new Intent(CMD_TOAST_BROADCAST);
                        broadcastIntent.putExtra("cmd_value", str + action);
                        appContext.sendBroadcast(broadcastIntent, null);

                        break;
                    }
                    case EventDeliveryAck:
                        message.setDelivered(true);
                        break;
                    case EventReadAck:
                        message.setAcked(true);
                        break;
                    // add other events in case you are interested in
                    default:
                        break;
                }

            }
        };

        EMChatManager.getInstance().registerEventListener(eventListener);
    }

    /**
     * 自定义通知栏提示内容
     *
     * @return
     */
    @Override
    protected HXNotifier.HXNotificationInfoProvider getNotificationListener() {
        //可以覆盖默认的设置
        return new HXNotifier.HXNotificationInfoProvider() {
            @Override
            public String getTitle(EMMessage message) {
                //修改标题,这里使用默认
                return null;
            }

            @Override
            public int getSmallIcon(EMMessage message) {
                //设置小图标，这里为默认
                return 0;
            }

            @Override
            public String getDisplayedText(EMMessage message) {
                // 设置状态栏的消息提示，可以根据message的类型做相应提示
                String ticker = CommonUtils.getMessageDigest(message, appContext);
                if (message.getType() == Type.TXT) {
                    ticker = ticker.replaceAll("\\[.{2,3}\\]", "[表情]");
                }
                Map<String, RobotUser> robotMap = ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getRobotList();
                if (robotMap != null && robotMap.containsKey(message.getFrom())) {
                    String nick = robotMap.get(message.getFrom()).getNick();
                    if (!TextUtils.isEmpty(nick)) {
                        return nick + ": " + ticker;
                    } else {
                        return message.getFrom() + ": " + ticker;
                    }
                } else {
                    return message.getFrom() + ": " + ticker;
                }
            }

            @Override
            public String getLatestText(EMMessage message, int fromUsersNum, int messageNum) {
                return fromUsersNum + "个基友，发来了" + messageNum + "条消息";
            }

            @Override
            public Intent getLaunchIntent(EMMessage message) {
                //设置点击通知栏跳转事件
                Intent intent = new Intent(appContext, ChatActivity.class);
                //有电话时优先跳转到通话页面
                ChatType chatType = message.getChatType();
                if (chatType == ChatType.Chat) { // 单聊信息
                    intent.putExtra("userId", message.getFrom());
                    intent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
                }
                return intent;
            }
        };
    }


    //连接冲突——同时又两台设备登陆在线时会自动退出
    @Override
    protected void onConnectionConflict() {
        Log.d(TAG, "ConnectionConflict------>");
        Intent intent = new Intent(appContext, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("conflict", true);
        SharedPreferences sp = getAppContext().getSharedPreferences(YXConstant.USER, getAppContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(YXConstant.USER_TOKEN, "false");
        editor.commit();
        Looper.prepare();
        YXConstant.showToast(getAppContext(),"账号在别处登陆，请重新登陆");
        appContext.startActivity(intent);
    }

    @Override
    protected void onCurrentAccountRemoved() {
        Log.d(TAG, "CurrentAccountRemoved------>");
        Intent intent = new Intent(appContext, LoginActivity.class);
        SharedPreferences sp = getAppContext().getSharedPreferences(YXConstant.USER, getAppContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(YXConstant.USER_TOKEN, "false");
        editor.commit();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.ACCOUNT_REMOVED, true);
        YXConstant.showToast(getAppContext(), "账号被移除");
        appContext.startActivity(intent);
    }


    @Override
    protected HXSDKModel createModel() {
        return new DemoHXSDKModel(appContext);
    }

    @Override
    public HXNotifier createNotifier() {
        return new HXNotifier() {
            public synchronized void onNewMsg(final EMMessage message) {
                if (EMChatManager.getInstance().isSlientMessage(message)) {
                    return;
                }
                String chatUsename = null;
                List<String> notNotifyIds = null;
                // 获取设置的不提示新消息的用户或者群组ids
                if (message.getChatType() == ChatType.Chat) {
                    chatUsename = message.getFrom();
                    notNotifyIds = ((DemoHXSDKModel) hxModel).getDisabledGroups();
                } else {
                    chatUsename = message.getTo();
                    notNotifyIds = ((DemoHXSDKModel) hxModel).getDisabledIds();
                }
                if (notNotifyIds == null || !notNotifyIds.contains(chatUsename)) {
                    // 判断app是否在后台
                    if (!EasyUtils.isAppRunningForeground(appContext)) {
                        EMLog.d(TAG, "app is running in backgroud");
                        sendNotification(message, false);
                    } else {
                        sendNotification(message, true);

                    }
                    viberateAndPlayTone(message);
                }
            }
        };
    }

    /**
     * get demo HX SDK Model
     */
    public DemoHXSDKModel getModel() {
        return (DemoHXSDKModel) hxModel;
    }

    /**
     * 获取内存中好友user list
     *
     * @return
     */
    public Map<String, User> getContactList() {
        if (getHXId() != null && contactList == null) {
            contactList = ((DemoHXSDKModel) getModel()).getContactList();
        }
        return contactList;
    }

    public Map<String, RobotUser> getRobotList() {
        if (getHXId() != null && robotList == null) {
            robotList = ((DemoHXSDKModel) getModel()).getRobotList();
        }
        return robotList;
    }


    public void setRobotList(Map<String, RobotUser> robotList) {
        this.robotList = robotList;
    }

    /**
     * 设置好友user list到内存中
     *
     * @param contactList
     */
    public void setContactList(Map<String, User> contactList) {
        this.contactList = contactList;
    }

    /**
     * 保存单个user
     */
    public void saveContact(User user) {
        contactList.put(user.getUsername(), user);
        ((DemoHXSDKModel) getModel()).saveContact(user);
    }

    @Override
    public void logout(final boolean unbindDeviceToken, final EMCallBack callback) {
        endCall();
        super.logout(unbindDeviceToken, new EMCallBack() {

            @Override
            public void onSuccess() {
                setContactList(null);
                setRobotList(null);
                getUserProfileManager().reset();
                getModel().closeDB();
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(int code, String message) {
                if (callback != null) {
                    callback.onError(code, message);
                }
            }

            @Override
            public void onProgress(int progress, String status) {
                if (callback != null) {
                    callback.onProgress(progress, status);
                }
            }

        });
    }

    void endCall() {
        try {
            EMChatManager.getInstance().endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * update User cach And db
     */
    public void updateContactList(List<User> contactInfoList) {
        for (User u : contactInfoList) {
            contactList.put(u.getUsername(), u);
        }
        ArrayList<User> mList = new ArrayList<User>();
        mList.addAll(contactList.values());
        ((DemoHXSDKModel) getModel()).saveContactList(mList);
    }

    public UserProfileManager getUserProfileManager() {
        if (userProManager == null) {
            userProManager = new UserProfileManager();
        }
        return userProManager;
    }

}
