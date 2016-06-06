package com.share.jack.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.EMCallBack;
import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.EMValueCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactListener;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMConversation;
import com.easemob.util.EMLog;
import com.easemob.util.HanziToPinyin;
import com.easemob.util.NetUtils;
import com.loopj.android.http.RequestParams;
import com.share.jack.adapter.ContactListAdapter;
import com.share.jack.bean.PersonalBean;
import com.share.jack.bean.UsersBean;
import com.share.jack.controller.HXSDKHelper;
import com.share.jack.db.InviteMessgeDao;
import com.share.jack.db.UserDao;
import com.share.jack.demo.Constant;
import com.share.jack.domain.InviteMessage;
import com.share.jack.domain.User;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.model.DemoHXSDKHelper;
import com.share.jack.swingtravel.ChatActivity;
import com.share.jack.swingtravel.CommentListActivity;
import com.share.jack.swingtravel.InteractionListActivity;
import com.share.jack.swingtravel.LoginActivity;
import com.share.jack.swingtravel.MainActivity;
import com.share.jack.swingtravel.R;
import com.share.jack.swingtravel.RegisterActivity;
import com.share.jack.utils.ControlKeyboardUtils;
import com.share.jack.utils.YXApplication;
import com.share.jack.utils.YXConstant;
import com.share.jack.widget.MyDampScrollView;
import com.share.jack.widget.MyNoScrollListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/23 12:23
 * Copyright:1.0
 */
public class MessegeFragment extends Fragment implements View.OnClickListener, EMEventListener {

    private final static String TAG = "MessegeFragment";
    private View mView;

    private RelativeLayout mRvLeft;
    private TextView mTvTitle;
    private RelativeLayout mRvInterAction;
    private RelativeLayout mRvComment;
    private RelativeLayout mRvNotLogin;
    private MyDampScrollView mSvMain;
    private Button mBtnGoLogin;
    private Button mBtnGoRegister;

    public static MessegeFragment mActivity = null;
    private MyNoScrollListView mListView;
    public ContactListAdapter mAdapter;
    public RelativeLayout mErrorItem;

    public TextView errorText;
    private List<EMConversation> conversationList = new ArrayList<>();

    private InviteMessgeDao inviteMessgeDao;
    private UserDao userDao;
    private boolean hidden;

    private boolean handled;


    public MyConnectionListener connectionListener = null;
    private SharedPreferences sp = null;

    private List<UsersBean> mUserInfoList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_message, container, false);
        sp = getActivity().getSharedPreferences(YXConstant.USER, getActivity().MODE_PRIVATE);
        initeView();
        Log.d(TAG, "token=" + sp.getString(YXConstant.USER_TOKEN, "false"));
        if (sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {    //未登陆过
            mRvNotLogin.setVisibility(View.VISIBLE);
            mSvMain.setVisibility(View.GONE);
            mBtnGoLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), LoginActivity.class).putExtra("isLoging", "jack"));
                }
            });
            mBtnGoRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), RegisterActivity.class));
                }
            });
        } else {
            mRvNotLogin.setVisibility(View.GONE);
            mSvMain.setVisibility(View.VISIBLE);
            inviteMessgeDao = new InviteMessgeDao(getActivity());
            userDao = new UserDao(getActivity());
            init();
            //异步获取当前用户的昵称和头像
            ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getUserProfileManager().asyncGetCurrentUserInfo();
            MessegeFragment.mActivity = this;
            conversationList.addAll(loadConversationsWithRecentChat());

            for (int i = 0; i < conversationList.size(); i++) {
                getUserInfo(conversationList.get(i).getUserName());
            }
            mAdapter = new ContactListAdapter(getActivity(), 1, conversationList, mUserInfoList);
            initeListener();
        }
        return mView;
    }

    private void getUserInfo(String username) {
        String url = "http://115.28.101.140/youxing/Home/User/getUserInfoByPhone";
        RequestParams params = new RequestParams();
        params.put("User_Phone", username);
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        UsersBean userBean = new UsersBean();
                        userBean.userHead = jsonObject.getJSONObject("response")
                                .getJSONObject("data").getString("Head");
                        userBean.userNick = jsonObject.getJSONObject("response")
                                .getJSONObject("data").getString("Nickname");
                        userBean.userId = jsonObject.getJSONObject("response")
                                .getJSONObject("data").getString("Uid");
                        userBean.userSex = jsonObject.getJSONObject("response")
                                .getJSONObject("data").getString("Sex");
                        userBean.userLocation = jsonObject.getJSONObject("response")
                                .getJSONObject("data").getString("Live");
                        mUserInfoList.add(userBean);
                    } else {
                        Log.d(TAG, jsonObject.getJSONObject("response").getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                YXConstant.showToast(getActivity(), getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    private void init() {
        // setContactListener监听联系人的变化等
        EMContactManager.getInstance().setContactListener(new MyContactListener());
        // 注册一个监听连接状态的listener
        connectionListener = new MyConnectionListener();
        EMChatManager.getInstance().addConnectionListener(connectionListener);
    }

    private void initeView() {
        mRvLeft = (RelativeLayout) mView.findViewById(R.id.ltb_rv_left);
        mTvTitle = (TextView) mView.findViewById(R.id.ltb_tv_title);
        mRvInterAction = (RelativeLayout) mView.findViewById(R.id.fm_rv_interaction);
        mRvComment = (RelativeLayout) mView.findViewById(R.id.fm_rv_comment);
        mListView = (MyNoScrollListView) mView.findViewById(R.id.fm_listview);
        mRvNotLogin = (RelativeLayout) mView.findViewById(R.id.fm_rv_not_login);
        mSvMain = (MyDampScrollView) mView.findViewById(R.id.fm_sv_main);
        mBtnGoLogin = (Button) mView.findViewById(R.id.lnl_btn_login);
        mBtnGoRegister = (Button) mView.findViewById(R.id.lnl_btn_register);

        mErrorItem = (RelativeLayout) mView.findViewById(R.id.test_rl_error_item);
        errorText = (TextView) mErrorItem.findViewById(R.id.tv_connect_errormsg);
        mRvLeft.setVisibility(View.GONE);
        mTvTitle.setText(getResources().getString(R.string.messege));
        mRvInterAction.setOnClickListener(this);
        mRvComment.setOnClickListener(this);
    }

    private void initeListener() {
        // 设置adapter
        if (mAdapter == null) {
            mListView.setAdapter(null);
        } else {
            mListView.setAdapter(mAdapter);
            setListHeightBasedOnChildren(mListView);
            final String st2 = getResources().getString(R.string.Cant_chat_with_yourself);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    EMConversation conversation = mAdapter.getItem(position);
                    String username = conversation.getUserName();
                    if (username.equals(YXApplication.getInstance().getUserName())) {
                        YXConstant.showToast(getActivity(), st2);
                    } else {
                        // 进入聊天页面
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        Bundle bundle = new Bundle();
                        PersonalBean personalBean = new PersonalBean();
                        personalBean.setUserHead(mUserInfoList.get(position).userHead);
                        personalBean.setUserNick(mUserInfoList.get(position).userNick);
                        personalBean.setUserId(mUserInfoList.get(position).userId);
                        personalBean.setUserSex(mUserInfoList.get(position).userSex);
                        personalBean.setUserLocation(mUserInfoList.get(position).userLocation);
                        bundle.putParcelable(YXConstant.PAR_USER_KEY, personalBean);
                        intent.putExtras(bundle);
                        intent.putExtra("userId", username);
                        startActivity(intent);
                    }
                }
            });
            // 注册上下文菜单
            registerForContextMenu(mListView);
            mListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // 隐藏软键盘
                    ControlKeyboardUtils.hideKeyboard(getActivity());
                    return false;
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fm_rv_interaction:
                startActivity(new Intent(getActivity(), InteractionListActivity.class));
                break;
            case R.id.fm_rv_comment:
                startActivity(new Intent(getActivity(), CommentListActivity.class));
                break;
        }
    }


    static void asyncFetchGroupsFromServer() {
        HXSDKHelper.getInstance().asyncFetchGroupsFromServer(new EMCallBack() {
            @Override
            public void onSuccess() {
                HXSDKHelper.getInstance().noitifyGroupSyncListeners(true);
                if (HXSDKHelper.getInstance().isContactsSyncedWithServer()) {
                    HXSDKHelper.getInstance().notifyForRecevingEvents();
                }
            }

            @Override
            public void onError(int code, String message) {
                HXSDKHelper.getInstance().noitifyGroupSyncListeners(false);
            }

            @Override
            public void onProgress(int progress, String status) {
            }

        });
    }

    static void asyncFetchContactsFromServer() {
        HXSDKHelper.getInstance().asyncFetchContactsFromServer(new EMValueCallBack<List<String>>() {

            @Override
            public void onSuccess(List<String> usernames) {
                Context context = HXSDKHelper.getInstance().getAppContext();

                System.out.println("----------------" + usernames.toString());
                EMLog.d("roster", "contacts size: " + usernames.size());
                Map<String, User> userlist = new HashMap<String, User>();
                for (String username : usernames) {
                    User user = new User();
                    user.setUsername(username);
                    setUserHearder(username, user);
                    userlist.put(username, user);
                }
                // 添加user"申请与通知"
                User newFriends = new User();
                newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
                String strChat = context.getString(R.string.Application_and_notify);
                newFriends.setNick(strChat);
                userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
                // 存入内存
                ((DemoHXSDKHelper) HXSDKHelper.getInstance()).setContactList(userlist);
                // 存入db
                UserDao dao = new UserDao(context);
                List<User> users = new ArrayList<User>(userlist.values());
                dao.saveContactList(users);

                HXSDKHelper.getInstance().notifyContactsSyncListener(true);

                if (HXSDKHelper.getInstance().isGroupsSyncedWithServer()) {
                    HXSDKHelper.getInstance().notifyForRecevingEvents();
                }
                ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getUserProfileManager().asyncFetchContactInfosFromServer(usernames, new EMValueCallBack<List<User>>() {
                    @Override
                    public void onSuccess(List<User> uList) {
                        ((DemoHXSDKHelper) HXSDKHelper.getInstance()).updateContactList(uList);
                        ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getUserProfileManager().notifyContactInfosSyncListener(true);
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                HXSDKHelper.getInstance().notifyContactsSyncListener(false);
            }

        });
    }

    static void asyncFetchBlackListFromServer() {
        HXSDKHelper.getInstance().asyncFetchBlackListFromServer(new EMValueCallBack<List<String>>() {
            @Override
            public void onSuccess(List<String> value) {
                EMContactManager.getInstance().saveBlackList(value);
                HXSDKHelper.getInstance().notifyBlackListSyncListener(true);
            }

            @Override
            public void onError(int error, String errorMsg) {
                HXSDKHelper.getInstance().notifyBlackListSyncListener(false);
            }

        });
    }


    /**
     * 设置hearder属性，方便通讯中对联系人按header分类显示，以及通过右侧ABCD...字母栏快速定位联系人
     *
     * @param username
     * @param user
     */
    private static void setUserHearder(String username, User user) {
        String headerName = null;
        if (!TextUtils.isEmpty(user.getNick())) {
            headerName = user.getNick();
        } else {
            headerName = user.getUsername();
        }
        if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
            user.setHeader("");
        } else if (Character.isDigit(headerName.charAt(0))) {
            user.setHeader("#");
        } else {
            user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0, 1)
                    .toUpperCase());
            char header = user.getHeader().toLowerCase().charAt(0);
            if (header < 'a' || header > 'z') {
                user.setHeader("#");
            }
        }
    }

    /***
     * 好友变化listener
     */
    public class MyContactListener implements EMContactListener {
        @Override
        public void onContactAdded(List<String> usernameList) {
            // 保存增加的联系人
            Map<String, User> localUsers = ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getContactList();
            Map<String, User> toAddUsers = new HashMap<String, User>();
            for (String username : usernameList) {
                User user = setUserHead(username);
                // 添加好友时可能会回调added方法两次
                if (!localUsers.containsKey(username)) {
                    userDao.saveContact(user);
                }
                toAddUsers.put(username, user);
            }
            localUsers.putAll(toAddUsers);
        }

        @Override
        public void onContactDeleted(final List<String> usernameList) {
            // 被删除
            Map<String, User> localUsers = ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getContactList();
            for (String username : usernameList) {
                localUsers.remove(username);
                userDao.deleteContact(username);
                inviteMessgeDao.deleteMessage(username);
            }
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    // 如果正在与此用户的聊天页面
                    String st10 = getResources().getString(R.string.have_you_removed);
                    if (ChatActivity.activityInstance != null
                            && usernameList.contains(ChatActivity.activityInstance.getToChatUsername())) {
                        YXConstant.showToast(getActivity(), ChatActivity.activityInstance.getToChatUsername() + st10);
                        ChatActivity.activityInstance.finish();
                    }
                    MainActivity.mainActivityInstance.updateUnreadLabel();
                }
            });
        }

        @Override
        public void onContactInvited(String username, String reason) {
            // 接到邀请的消息，如果不处理(同意或拒绝)，掉线后，服务器会自动再发过来，所以客户端不需要重复提醒
            List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();
            for (InviteMessage inviteMessage : msgs) {
                if (inviteMessage.getGroupId() == null && inviteMessage.getFrom().equals(username)) {
                    inviteMessgeDao.deleteMessage(username);
                }
            }
            // 自己封装的javabean
            InviteMessage msg = new InviteMessage();
            msg.setFrom(username);
            msg.setTime(System.currentTimeMillis());
            msg.setReason(reason);
            Log.d(TAG, username + "请求加你为好友,reason: " + reason);
            // 设置相应status
            msg.setStatus(InviteMessage.InviteMesageStatus.BEINVITEED);
            notifyNewIviteMessage(msg);

        }

        @Override
        public void onContactAgreed(String username) {
            List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();
            for (InviteMessage inviteMessage : msgs) {
                if (inviteMessage.getFrom().equals(username)) {
                    return;
                }
            }
            // 自己封装的javabean
            InviteMessage msg = new InviteMessage();
            msg.setFrom(username);
            msg.setTime(System.currentTimeMillis());
            Log.d(TAG, username + "同意了你的好友请求");
            msg.setStatus(InviteMessage.InviteMesageStatus.BEAGREED);
            notifyNewIviteMessage(msg);

        }

        @Override
        public void onContactRefused(String username) {
            // 参考同意，被邀请实现此功能,demo未实现
            Log.d(username, username + "拒绝了你的好友请求");
        }

    }

    /**
     * 连接监听listener
     */
    public class MyConnectionListener implements EMConnectionListener {
        @Override
        public void onConnected() {
            boolean groupSynced = HXSDKHelper.getInstance().isGroupsSyncedWithServer();
            boolean contactSynced = HXSDKHelper.getInstance().isContactsSyncedWithServer();
            // in case group and contact were already synced, we supposed to notify sdk we are ready to receive the events
            if (groupSynced && contactSynced) {
                new Thread() {
                    @Override
                    public void run() {
                        HXSDKHelper.getInstance().notifyForRecevingEvents();
                    }
                }.start();
            } else {
                if (!groupSynced) {
                    asyncFetchGroupsFromServer();
                }
                if (!contactSynced) {
                    asyncFetchContactsFromServer();
                }
                if (!HXSDKHelper.getInstance().isBlackListSyncedWithServer()) {
                    asyncFetchBlackListFromServer();
                }
            }
        }

        @Override
        public void onDisconnected(final int error) {
            final String st1 = getResources().getString(R.string.can_not_connect_chat_server_connection);
            final String st2 = getResources().getString(R.string.the_current_network);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (error == EMError.USER_REMOVED) {
                        // 显示帐号已经被移除
                        MainActivity.mainActivityInstance.showAccountRemovedDialog();
                    } else if (error == EMError.CONNECTION_CONFLICT) {
                        // 显示帐号在其他设备登陆dialog
                        MainActivity.mainActivityInstance.showConflictDialog();
                    } else {
                        mErrorItem.setVisibility(View.VISIBLE);
                        if (NetUtils.hasNetwork(getActivity()))
                            errorText.setText(st1);
                        else
                            errorText.setText(st2);
                    }
                }
            });
        }
    }

    /**
     * 保存提示新消息
     *
     * @param msg
     */
    private void notifyNewIviteMessage(InviteMessage msg) {
        saveInviteMsg(msg);
        // 提示有新消息
        HXSDKHelper.getInstance().getNotifier().viberateAndPlayTone(null);
        // 刷新bottom bar消息未读数
        MainActivity.mainActivityInstance.updateUnreadLabel();
    }

    /**
     * 保存邀请等msg
     *
     * @param msg
     */
    private void saveInviteMsg(InviteMessage msg) {
        // 保存msg
        inviteMessgeDao.saveMessage(msg);
        // 未读数加1
        User user = ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getContactList().get(Constant.NEW_FRIENDS_USERNAME);
        if (user.getUnreadMsgCount() == 0)
            user.setUnreadMsgCount(user.getUnreadMsgCount() + 1);
    }

    /**
     * set head
     *
     * @param username
     * @return
     */
    User setUserHead(String username) {
        User user = new User();
        user.setUsername(username);
        String headerName = null;
        if (!TextUtils.isEmpty(user.getNick())) {
            headerName = user.getNick();
        } else {
            headerName = user.getUsername();
        }
        if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
            user.setHeader("");
        } else if (Character.isDigit(headerName.charAt(0))) {
            user.setHeader("#");
        } else {
            user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0, 1)
                    .toUpperCase());
            char header = user.getHeader().toLowerCase().charAt(0);
            if (header < 'a' || header > 'z') {
                user.setHeader("#");
            }
        }
        return user;
    }

    @Override
    public void onStart() {
        if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
            refresh();
            EMChatManager.getInstance().addConnectionListener(connectionListener);
            Log.d("MessegeFragment----->", "onStart()");
        }
        super.onStart();
    }

    @Override
    public void onCreateContextMenu(android.view.ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.delete_message, menu);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        handled = false;
        boolean deleteMessage = false;
        if (item.getItemId() == R.id.delete_message) {
            handled = true;
            deleteMessage = true;
        } else if (item.getItemId() == R.id.delete_conversation) {
            deleteMessage = false;
            handled = true;
        } else if (item.getItemId() == R.id.add_to_interaction) {
            String url = "http://115.28.101.140/youxing/Home/User/addFriendsList";
            RequestParams params = new RequestParams();
            params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
            params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
            params.put("Friend_Id", mUserInfoList.get(((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position).userId);
            RequestUtils.ClientPost(url, params, new NetCallBack() {
                @Override
                public void onMySuccess(byte[] response) {
                    try {
                        JSONObject jsonObject = new JSONObject(new String(response));
                        if (jsonObject.getString("result").equals("success")) {
                            YXConstant.showToast(getActivity(), "加入成功");
                        } else {
                            Log.d(TAG, jsonObject.getJSONObject("response").getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onMyFailure(byte[] response, Throwable throwable) {
                    Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                    YXConstant.showToast(getActivity(), getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                }
            });
            handled = true;
            return handled ? true : super.onContextItemSelected(item);
        }
        EMConversation tobeDeleteCons = mAdapter.getItem(((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position);
        // 删除此会话
        EMChatManager.getInstance().deleteConversation(tobeDeleteCons.getUserName(), tobeDeleteCons.isGroup(), deleteMessage);
        InviteMessgeDao inviteMessgeDao = new InviteMessgeDao(getActivity());
        inviteMessgeDao.deleteMessage(tobeDeleteCons.getUserName());
        mAdapter.remove(tobeDeleteCons);
        mAdapter.notifyDataSetChanged();
        return handled ? true : super.onContextItemSelected(item);
    }

    public void setListHeightBasedOnChildren(ListView listView) {
        ListAdapter adapter = listView.getAdapter();
        if (adapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        conversationList.clear();
        conversationList.addAll(loadConversationsWithRecentChat());
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    /**
     * 获取所有会话
     *
     * @return
     */
    private List<EMConversation> loadConversationsWithRecentChat() {
        // 获取所有会话，包括陌生人
        Hashtable<String, EMConversation> conversations = EMChatManager.getInstance().getAllConversations();
        // 过滤掉messages size为0的conversation
        /**
         * 如果在排序过程中有新消息收到，lastMsgTime会发生变化
         * 影响排序过程，Collection.sort会产生异常
         * 保证Conversation在Sort过程中最后一条消息的时间不变
         * 避免并发问题
         */
        List<Pair<Long, EMConversation>> sortList = new ArrayList<Pair<Long, EMConversation>>();
        synchronized (conversations) {
            for (EMConversation conversation : conversations.values()) {
                if (conversation.getAllMessages().size() != 0) {
                    //if(conversation.getType() != EMConversationType.ChatRoom){
                    sortList.add(new Pair<Long, EMConversation>(conversation.getLastMessage().getMsgTime(), conversation));
                    //}
                }
            }
        }
        try {
            // Internal is TimSort algorithm, has bug
            sortConversationByLastChatTime(sortList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<EMConversation> list = new ArrayList<EMConversation>();
        for (Pair<Long, EMConversation> sortItem : sortList) {
            list.add(sortItem.second);
        }
        return list;
    }

    /**
     * 根据最后一条消息的时间排序
     */
    private void sortConversationByLastChatTime(List<Pair<Long, EMConversation>> conversationList) {
        Collections.sort(conversationList, new Comparator<Pair<Long, EMConversation>>() {
            @Override
            public int compare(final Pair<Long, EMConversation> con1, final Pair<Long, EMConversation> con2) {
                if (con1.first == con2.first) {
                    return 0;
                } else if (con2.first > con1.first) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {   //未登陆过
            refresh();
            if (connectionListener != null) {
                EMChatManager.getInstance().removeConnectionListener(connectionListener);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
            if (!hidden && !((MainActivity) getActivity()).isConflict) {
                refresh();
            }
            if (NetUtils.hasNetwork(getActivity())) {
                mErrorItem.setVisibility(View.GONE);
            } else {
                mErrorItem.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
            this.hidden = hidden;
            if (!hidden) {
                refresh();
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
            if (((MainActivity) getActivity()).isConflict) {
                outState.putBoolean("isConflict", true);
            } else if (((MainActivity) getActivity()).getCurrentAccountRemoved()) {
                outState.putBoolean(Constant.ACCOUNT_REMOVED, true);
            }
        }
    }

    @Override
    public void onEvent(EMNotifierEvent emNotifierEvent) {

    }
}
