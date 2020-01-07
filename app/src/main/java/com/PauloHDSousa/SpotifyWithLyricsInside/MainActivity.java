package com.PauloHDSousa.SpotifyWithLyricsInside;

 import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
 import android.animation.AnimatorSet;
 import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
 import android.util.DisplayMetrics;
 import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
 import android.view.ViewTreeObserver;
 import android.view.Window;
import android.view.WindowManager;
 import android.view.animation.AccelerateDecelerateInterpolator;
 import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.PauloHDSousa.Services.AppPreferences;
import com.PauloHDSousa.Services.Services;
 import com.PauloHDSousa.Utils.Internet;
 import com.PauloHDSousa.Utils.ResizeAnimation;
 import com.google.android.gms.ads.AdRequest;
 import com.google.android.gms.ads.AdView;
 import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.internal.ContentApiImpl;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.ListItems;
import com.spotify.protocol.types.Track;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 2337;
    private static final String REDIRECT_URI = "http://com.PauloHDSousa.SpotifyWithLyricsInside://callback";
    SpotifyAppRemote mSpotifyAppRemote;
    ImageView ivAlbum;
    TextView tvCurrentSong;
    Track lastTrack;
    ImageButton ibNext, ibStop, ibPrevious, ibPlay, ibShuffle, ibSpotify, ibClosePlayer;
    FloatingActionButton fbSlowScroll, fbSpeedScroll;
    WebView webView;
    Handler mHandler = new Handler();
    boolean autoScrollOn = true;
    int scrollSpeed = 2;
    int scrollUpdateInMS = 400;
    boolean isSongPaused, isMusicContentHide,isShuffleOn;
    boolean isFirstLoad = true;
    SeekBar sbMusic;
    Handler seekHandler = new Handler();
    String currentLibrary = "";
    LinearLayout linearLayoutMusicContnet;
    RelativeLayout relativeWebView;
    ProgressBar pbLoadingHTML;
    //Auto-Scroll
    Runnable mScrollDown = new Runnable() {
        public void run() {
            if (autoScrollOn && !isSongPaused)
                webView.scrollBy(0, scrollSpeed);

            mHandler.postDelayed(this, scrollUpdateInMS);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Internet internet = new Internet(this);

        //If there is no internet, move to Another Activity
        if(!internet.isNetworkConnected()){
            Intent myIntent = new Intent(MainActivity.this, InternetLostActivity.class);
            startActivity(myIntent);
            return;
        }

        ivAlbum = (ImageView) findViewById(R.id.ivAlbum);
        tvCurrentSong = (TextView) findViewById(R.id.tvCurrentSong);
        pbLoadingHTML = findViewById(R.id.pbLoadingHTML);

        ConnectionParams connectionParams =
                new ConnectionParams.Builder("9e5381fc5bb34ce2a0e68dadd7662977")
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();


        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {

            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                connected();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("MainActivity", throwable.getMessage(), throwable);
            }
        });

    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };

    public void seekUpdation() {
        sbMusic.setProgress(sbMusic.getProgress() + 1000);
        seekHandler.postDelayed(run, 1000);
    }

    private void connected() {

        // mSpotifyAppRemote.getContentApi().getRecommendedContentItems(ContentApi.ContentType.FITNESS).setResultCallback(recommended ->{
        //     ListItem[] items = recommended.items;
        // });


        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {

                    isSongPaused = playerState.isPaused;

                    if(isFirstLoad) {
                        //Handle the STOP / PLAY button
                        if (isSongPaused) {
                            ibPlay.setVisibility(View.VISIBLE);
                            ibStop.setVisibility(View.GONE);
                        } else {
                            ibStop.setVisibility(View.VISIBLE);
                            ibPlay.setVisibility(View.GONE);
                        }

                        //Handle the Shuffle button
                        isShuffleOn = playerState.playbackOptions.isShuffling;
                        if (isShuffleOn) {
                            onShuffleOn();
                        } else {
                            onShuffleOff();
                        }

                        isFirstLoad = false;
                    }

                    final Track track = playerState.track;

                    if (track != null && lastTrack == null || !lastTrack.name.equals(track.name)) {

                        //Some lyrics takes longer to render HTML, it prevents to show the wrong lyric on a song
                        webView.setAlpha(0f);

                        //Loading bar
                        pbLoadingHTML.setVisibility(View.VISIBLE);

                        //Current Library to open on Spotify link
                        currentLibrary = track.uri;

                        //Stops the music bar
                        seekHandler.removeCallbacks(run);

                        //Saves the last track to not call again
                        lastTrack = track;

                        sbMusic.setMax((int) track.duration);
                        int playbackPosition = (int) playerState.playbackPosition;
                        sbMusic.setProgress(playbackPosition);

                        if (!isSongPaused)
                            run.run();

                        String artist = track.artist.name;
                        String music = track.name;

                        //Loading the current Album IMAGE
                        CallResult<Bitmap> imageBitmap = mSpotifyAppRemote.getImagesApi().getImage(track.imageUri);
                        imageBitmap.setResultCallback(bitmap -> ivAlbum.setImageBitmap(bitmap));

                        tvCurrentSong.setText(artist + " - " + music);

                        new Services(this).execute(artist, music);
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

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //SharedPrefs
        AppPreferences appPreferences = AppPreferences.getInstance(this);
        scrollSpeed = appPreferences.getInt(AppPreferences.ScrollSpeedKey);


        //Music HTML
        webView = (WebView) findViewById(R.id.wbLyrics);
        webView.setVerticalScrollBarEnabled(false);
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                pbLoadingHTML.setProgress(progress);
                if (progress == 100) {
                    pbLoadingHTML.setVisibility(View.GONE);

                } else {
                    pbLoadingHTML.setVisibility(View.VISIBLE);
                }
            }
        });

        //Music Controls
        linearLayoutMusicContnet = (LinearLayout) findViewById(R.id.layoutMusicContent);
        relativeWebView = (RelativeLayout) findViewById(R.id.rlWebView);

        ibNext = (ImageButton) findViewById(R.id.ibNext);
        ibStop = (ImageButton) findViewById(R.id.ibStop);
        ibPrevious = (ImageButton) findViewById(R.id.ibPrevious);
        ibPlay = (ImageButton) findViewById(R.id.ibPlay);
        ibShuffle = (ImageButton) findViewById(R.id.ibShuffle);
        sbMusic = (SeekBar) findViewById(R.id.sbMusic);
        ibSpotify = (ImageButton) findViewById(R.id.ibSpotify);
        ibClosePlayer = (ImageButton) findViewById(R.id.ibClosePlayer);


        ViewTreeObserver viewTreeObserver = linearLayoutMusicContnet.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int viewHeight = linearLayoutMusicContnet.getHeight();

                    if(viewHeight != 0) {
                        DisplayMetrics metrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(metrics);
                        int height = metrics.heightPixels;

                        height = height - viewHeight;

                        ViewGroup.LayoutParams params = webView.getLayoutParams();
                        params.height = height;

                        webView.setLayoutParams(params);

                        linearLayoutMusicContnet.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }

        //Music Bar
        sbMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekHandler.removeCallbacks(run);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekHandler.removeCallbacks(run);

                long currentPosition = seekBar.getProgress();

                mSpotifyAppRemote.getPlayerApi().seekTo(currentPosition);

                if (!isSongPaused)
                    run.run();
            }
        });

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
                if (scrollSpeed > 0) {
                    scrollSpeed--;
                    appPreferences.storeInt(AppPreferences.ScrollSpeedKey, scrollSpeed);
                }
            }
        });


        //Music Control Buttons Actions
        ibClosePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMusicContentHide){
                    linearLayoutMusicContnet.animate()
                            .translationY(0)
                            .setDuration(500)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);

                                    int viewHeight = linearLayoutMusicContnet.getHeight();

                                    if (viewHeight != 0) {
                                        DisplayMetrics metrics = new DisplayMetrics();
                                        getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                        int height = metrics.heightPixels;

                                        height = height - viewHeight;

                                        ViewGroup.LayoutParams params = webView.getLayoutParams();
                                        params.height = height;

                                        webView.setLayoutParams(params);
                                    }
                                }
                            });



                    ibClosePlayer.setImageResource(R.drawable.down);
                    isMusicContentHide = false;
                }
                else {
                    linearLayoutMusicContnet.animate()
                            .translationY(linearLayoutMusicContnet.getHeight() - ibClosePlayer.getHeight())
                            .setDuration(500)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                }
                            });

                    int viewHeight = linearLayoutMusicContnet.getHeight();

                    if (viewHeight != 0) {
                        DisplayMetrics metrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(metrics);
                        int height = metrics.heightPixels;

                        height = height + viewHeight;

                        ViewGroup.LayoutParams params = webView.getLayoutParams();
                        params.height = height;

                        webView.setLayoutParams(params);
                    }

                    ibClosePlayer.setImageResource(R.drawable.up);
                    isMusicContentHide = true;
                }
            }
        });

        ibNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().skipNext();
                webView.scrollTo(0, 0);
            }
        });

        ibStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().pause();
                ibStop.setVisibility(View.GONE);
                ibPlay.setVisibility(View.VISIBLE);
                autoScrollOn = false;

                seekHandler.removeCallbacks(run);
            }
        });

        ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().resume();
                ibPlay.setVisibility(View.GONE);
                ibStop.setVisibility(View.VISIBLE);
                autoScrollOn = true;
                mScrollDown.run();
            }
        });

        ibPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().skipPrevious();
                webView.scrollTo(0, 0);
            }
        });

        ibShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().toggleShuffle();

                isShuffleOn = !isShuffleOn;

                if (isShuffleOn)
                    onShuffleOn();
                else
                    onShuffleOff();
            }
        });

        ibSpotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri = currentLibrary;
                Intent launcher = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(launcher);
            }
        });
    }

    void onShuffleOn() {
        ibShuffle.setImageResource(R.drawable.shuffleon);
    }

    void onShuffleOff() {
        ibShuffle.setImageResource(R.drawable.shuffle);
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

                webView.setAlpha(0f);
                webView.animate().alpha(1f).setDuration(500);
            }
        });

        mScrollDown.run();

        webView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                WebView webView = (WebView) view;
                float contentHeight = webView.getContentHeight() * webView.getScaleY();
                float total = contentHeight * getResources().getDisplayMetrics().density - view.getHeight();

                if (scrollY >= total - 1) {
                    autoScrollOn = false;
                } else {
                    autoScrollOn = true;
                }
            }
        });
    }
}
