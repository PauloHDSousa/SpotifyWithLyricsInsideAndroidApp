package com.PauloHDSousa.Utils;

import android.content.Context;
import android.net.ConnectivityManager;

public class Internet {
    Context context;

    public Internet(Context _context){
        this.context = _context;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
