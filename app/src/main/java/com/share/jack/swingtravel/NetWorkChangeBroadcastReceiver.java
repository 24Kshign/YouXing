package com.share.jack.swingtravel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.share.jack.utils.YXConstant;

public class NetWorkChangeBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "NetWorkChangeBroadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        //此处是主要代码，
        //如果是在开启wifi连接和有网络状态下
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (NetworkInfo.State.CONNECTED == info.getState()) {
                //连接状态
                Log.d(TAG, "有网络连接");
            } else {
                //未连接状态
                YXConstant.showToast(context, "当前网络不可用，请检查网络设置");
                Log.e(TAG, "无网络连接");
            }
        }
    }
}
