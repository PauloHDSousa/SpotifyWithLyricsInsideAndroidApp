package com.PauloHDSousa.Services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.webkit.WebView;

import com.PauloHDSousa.SpotifyWithLyricsInside.MainActivity;
import com.PauloHDSousa.SpotifyWithLyricsInside.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;


public class Services extends AsyncTask<String, Void, String> {

    private MainActivity mainActivity;

    public Services(MainActivity _mainActivity) {
        this.mainActivity = _mainActivity;
    }

    @Override
    protected String doInBackground(String... params) {
        String artist = ToCleanUrl(params[0]);
        String music = ToCleanUrl(params[1]);

        String url = "https://www.letras.mus.br/" + artist + "/" + music;
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String value = document.body().select(".p402_premium").get(0).html();

        return value;
    }


    @Override
    protected void onPostExecute(String result) {
        mainActivity.processValue(result);
    }


    public static String RemoveSpecialCharacters(String text)
    {
        String r = StringUtils.stripAccents(text);
        r = r.replace(" ", "-");
        r = r.replaceAll("[^\\.A-Za-z0-9-]", "");
        return r;
    }

    public static String ToCleanUrl(String text)
    {
        String cleanText = RemoveSpecialCharacters(text.toLowerCase());
        return cleanText.replaceAll(" ", "-").replaceAll("--", "-");
    }
}