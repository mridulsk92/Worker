package com.example.mridul_xpetize.worker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkReceiver extends BroadcastReceiver {
    public NetworkReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null != intent) {
            NetworkInfo.State wifiState = null;
            NetworkInfo.State mobileState = null;
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            if (wifiState != null && mobileState != null
                    && NetworkInfo.State.CONNECTED != wifiState
                    && NetworkInfo.State.CONNECTED == mobileState) {
                // phone network connect success
                Intent i = new Intent(context, PostService.class);
                context.startService(i);
            } else if (wifiState != null && mobileState != null
                    && NetworkInfo.State.CONNECTED != wifiState
                    && NetworkInfo.State.CONNECTED != mobileState) {
                // no network

            } else if (wifiState != null && NetworkInfo.State.CONNECTED == wifiState) {
                // wifi connect success
                Intent i = new Intent(context, PostService.class);
                context.startService(i);
            }
        }
    }
}
