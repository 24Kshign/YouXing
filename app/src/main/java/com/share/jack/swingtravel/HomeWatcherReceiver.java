package com.share.jack.swingtravel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.utils.YXApplication;
import com.share.jack.utils.YXConstant;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeWatcherReceiver extends BroadcastReceiver {

    private static final String TAG = "HomeWatcherReceiver";

    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
    private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "onReceive: action: " + action);
        if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            Log.d(TAG, "reason=" + reason);
            if (YXApplication.getInstance().mList.size() == 0) {
                return;
            }
            if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                // 短按Home键
                Log.d(TAG, "短按Home键");
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
                        Log.d(TAG, context.getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                    }
                });


            } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                // 长按Home键 或者 activity切换键
                Log.d(TAG, "长按Home键 或者 activity切换键");
            } else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
                // 锁屏
                Log.d(TAG, "锁屏");
            } else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
                // 长按Home键
                Log.d(TAG, "长按Home键");
            }
        }
    }
}
