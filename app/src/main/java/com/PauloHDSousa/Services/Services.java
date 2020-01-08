package com.PauloHDSousa.Services;

import android.os.AsyncTask;

import com.PauloHDSousa.SpotifyWithLyricsInside.MainActivity;
import com.PauloHDSousa.SpotifyWithLyricsInside.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.apache.commons.lang3.StringUtils;
import java.io.IOException;
import java.util.Arrays;


public class Services extends AsyncTask<String, Void, String> {

    String SourceLETRASMUSICA = "https://www.letras.mus.br/";
    String QueryLETRASMUSICA = ".p402_premium";

    String SourceOUVIRMUSICA  = "https://www.ouvirmusica.com.br/";
    String QueryOUVIRMUSICA   = ".cnt";

    String SourceGenius = "https://genius.com/";
    String QueryGENIUS = ".lyrics";

    private MainActivity mainActivity;

    public Services(MainActivity _mainActivity) {
        this.mainActivity = _mainActivity;
    }

    @Override
    protected String doInBackground(String... params) {
        String artist = params[0];
        String music = params[1];

        String HTML = "";

        //Get from LETRASMUSICA
        HTML = getHTMLfromSource(artist,music, SourceLETRASMUSICA, QueryLETRASMUSICA, false);
        if(!HTML.isEmpty())
            return  HTML;

        //Get from OUVIRMUSICA
        HTML = getHTMLfromSource(artist, music, SourceOUVIRMUSICA, QueryOUVIRMUSICA, false);
        if(!HTML.isEmpty())
            return  HTML;

        //Get from GENIUS
        HTML = getHTMLfromSource(artist, music, SourceGenius, QueryGENIUS,true);
        if(!HTML.isEmpty()) {
            //Removing the ADS
            HTML =  HTML.replaceAll("(.*)?<a.*?>", "").replaceAll("</a>", "");
            return HTML;
        }

        return  HTML;
    }


    String getHTMLfromSource(String artist, String music, String source, String query, boolean isGenius){

        String HTML = "";

        Document document = getMusicHTML(artist, music , source, isGenius);

        if(document != null){
            HTML = document.body().select(query).get(0).html();
        }

        return  HTML;
    }

    Document getMusicHTML(String artist, String music, String source, boolean isGENIUS){

        //Splits the Artists name (Thank' you OneRepublic)
        String[] artistName = artist.split("(?=\\p{Upper})");

        artist = Arrays.toString(artistName);

        artist = ToCleanUrl(artist);
        if(artist.startsWith("-"))
            artist = artist.substring(1,artist.length());

        music = ToCleanUrl(music);

        String url = "";
        if(isGENIUS) {
            url = source + artist + "-" + music +"-lyrics";
        }
        else{
            url = source + artist + "/" + music;
        }

        Document document = null;

        try {
            document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:49.0) Gecko/20100101 Firefox/49.0")
                    .ignoreContentType(true)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            return  document;
        }
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