package com.PauloHDSousa.Services;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {

    //Keys
    public  static String  ScrollSpeedKey = "scrollspeed";

    private static AppPreferences appprefs;
    private SharedPreferences prefs;

    public static AppPreferences getInstance(Context context)
    {
        if(appprefs == null)
            appprefs = new AppPreferences(context);
        return appprefs;
    }

    private AppPreferences(Context context)
    {
        prefs = context.getSharedPreferences("com.PauloHDSousa.settings", Context.MODE_PRIVATE);
    }

    public void storeInt(String key, int num)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, num);
        editor.apply();
    }

    public int getInt(String key)
    {
        return prefs.getInt(key,2);
    }
}