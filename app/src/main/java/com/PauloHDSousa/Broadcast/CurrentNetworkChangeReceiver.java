package com.PauloHDSousa.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.PauloHDSousa.Services.NetworkUtil;

import static com.PauloHDSousa.SpotifyWithLyricsInside.HomeActivity.redirect;

public class CurrentNetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean internetLostContext = context.getClass().getSimpleName().equals("InternetLostActivity");
        int status = NetworkUtil.getConnectivityStatusString(context);

        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED && !internetLostContext) {
                redirect(false);
            } else if (status != NetworkUtil.NETWORK_STATUS_NOT_CONNECTED && internetLostContext) {
                redirect(true);
            }
        }
    }
}