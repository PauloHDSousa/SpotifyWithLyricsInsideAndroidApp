package com.PauloHDSousa.SpotifyWithLyricsInside;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.PauloHDSousa.Services.AppPreferences;
import com.PauloHDSousa.Services.Services;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.Track;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "http://com.PauloHDSousa.SpotifyWithLyricsInside://callback";
    SpotifyAppRemote mSpotifyAppRemote;
    ImageView ivAlbum;
    TextView tvCurrentSong;
    Track lastTrack;
    ImageButton ibNext, ibStop, ibPrevious, ibPlay, ibShuffle;
    FloatingActionButton fbSlowScroll,fbSpeedScroll;
    WebView webView;
    Handler mHandler = new Handler();
    boolean autoScrollOn = true;
    int scrollSpeed = 2;
    int scrollUpdateInMS = 200;
    boolean isShuffleOn;

    @Override
    protected void onStart() {
        super.onStart();

        ivAlbum = (ImageView)findViewById(R.id.ivAlbum);
        tvCurrentSong = (TextView) findViewById(R.id.tvCurrentSong);


        ConnectionParams connectionParams =
                new ConnectionParams.Builder("9e5381fc5bb34ce2a0e68dadd7662977")
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();


        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                         mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });

    }

    private void connected() {
        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {

                    //Handle the STOP / PLAY button
                    if(playerState.isPaused){
                        ibPlay.setVisibility(View.VISIBLE);
                        ibStop.setVisibility(View.GONE);
                    }
                    else{
                        ibStop.setVisibility(View.VISIBLE);
                        ibPlay.setVisibility(View.GONE);
                    }

                    //Handle the Shuffle button
                    isShuffleOn = playerState.playbackOptions.isShuffling;
                    if(isShuffleOn){

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ibShuffle.getBackground().setColorFilter(Color.argb(0, 50, 10, 5), PorterDuff.Mode.SRC_IN);
                        } else {
                            Drawable wrapDrawable = DrawableCompat.wrap(ibShuffle.getBackground());
                            DrawableCompat.setTint(wrapDrawable, Color.argb(0, 50, 10, 5));
                            ibShuffle.setBackgroundDrawable(DrawableCompat.unwrap(wrapDrawable));
                        }
                        //ibShuffle.setColorFilter(R.color.browser_actions_bg_grey);
                    }
                    else{

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ibShuffle.getBackground().setColorFilter(Color.argb(0, 50, 10, 5), PorterDuff.Mode.SRC_IN);
                        } else {
                            Drawable wrapDrawable = DrawableCompat.wrap(ibShuffle.getBackground());
                            DrawableCompat.setTint(wrapDrawable, Color.argb(0, 50, 10, 5));
                            ibShuffle.setBackgroundDrawable(DrawableCompat.unwrap(wrapDrawable));
                        }
                        //ibShuffle.setColorFilter(R.color.browser_actions_bg_grey);
                    }

                    final Track track = playerState.track;
                    if (track != null && lastTrack != track) {
                        lastTrack = track;
                        String artist = track.artist.name;
                        String music  = track.name;

                        //Loading the current Album IMAGE
                        CallResult<Bitmap> imageBitmap = mSpotifyAppRemote.getImagesApi().getImage(track.imageUri);
                        imageBitmap.setResultCallback(bitmap ->    ivAlbum.setImageBitmap(bitmap));

                        tvCurrentSong.setText(artist + " - " + music);

                        new Services(this).execute(artist,music);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //SharedPrefs
        AppPreferences appPreferences = AppPreferences.getInstance(this);
        scrollSpeed = appPreferences.getInt(AppPreferences.ScrollSpeedKey);

        //Music HTML
        webView = (WebView)findViewById(R.id.wbLyrics);
        webView.setVerticalScrollBarEnabled(false);

        //Music Controls
        ibNext =  (ImageButton)findViewById(R.id.ibNext);
        ibStop = (ImageButton) findViewById(R.id.ibStop);
        ibPrevious = (ImageButton)findViewById(R.id.ibPrevious);
        ibPlay = (ImageButton)findViewById(R.id.ibPlay);
        ibShuffle = (ImageButton)findViewById(R.id.ibShuffle);

        //Scroll Speed Controls
        fbSpeedScroll = (FloatingActionButton) findViewById(R.id.fbspeedScroll);
        fbSlowScroll = (FloatingActionButton) findViewById(R.id.fbslowScroll);

        fbSpeedScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollSpeed++;
                appPreferences.storeInt(AppPreferences.ScrollSpeedKey, scrollSpeed);
            }
        });

        fbSlowScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(scrollSpeed > 1) {
                    scrollSpeed--;
                    appPreferences.storeInt(AppPreferences.ScrollSpeedKey, scrollSpeed);
                }
            }
        });

        //Music Control Buttons Actions
        ibNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().skipNext();
            }
        });

        ibStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().pause();
                ibStop.setVisibility(View.GONE);
                ibPlay.setVisibility(View.VISIBLE);
            }
        });

        ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().resume();
                ibPlay.setVisibility(View.GONE);
                ibStop.setVisibility(View.VISIBLE);
            }
        });

        ibPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().skipPrevious();
            }
        });

        ibShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().toggleShuffle();

                isShuffleOn = !isShuffleOn;

                if(isShuffleOn)
                    onShuffleOn();
                else
                    onShuffleOff();
            }
        });
    }
    void onShuffleOn(){
        ibShuffle.getBackground().setColorFilter(Color.rgb(0,0,0) , PorterDuff.Mode.SRC_IN);
        //ibShuffle.setImageResource(R.drawable.ic_launcher_background);
    }

    void onShuffleOff(){
        ibShuffle.getBackground().setColorFilter(Color.rgb(0,0,0) , PorterDuff.Mode.SRC_IN);
        //ibShuffle.setImageResource(R.drawable.shuffle);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

    }

    public void processValue(String HTMLContent) {
        webView.loadDataWithBaseURL(null, HTMLContent, "text/html", "UTF-8", null);

        webView.setWebViewClient(new WebViewClient() {

            //Enables Auto-Scroll and Scroll to TOP after loading the Lyrics HTML
            public void onPageFinished(WebView view, String url) {
                autoScrollOn = true;
                webView.scrollBy(0, 0);
            }
        });


        Runnable mScrollDown = new Runnable()
        {
            public void run()
            {
                if(!autoScrollOn)
                    return;

                webView.scrollBy(0, scrollSpeed);
                mHandler.postDelayed(this, scrollUpdateInMS);
            }
        };

        mScrollDown.run();

        webView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                WebView webView = (WebView) view;
                float contentHeight = webView.getContentHeight() * webView.getScaleY();
                float total = contentHeight * getResources().getDisplayMetrics().density - view.getHeight();

                if (scrollY >= total - 1) {
                    Log.d("SCROLL", "Reached bottom");
                    autoScrollOn = false;
                }
            }
        });
    }
}
