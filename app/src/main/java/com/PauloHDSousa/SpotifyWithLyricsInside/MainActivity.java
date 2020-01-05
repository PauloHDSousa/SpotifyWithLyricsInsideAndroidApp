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
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
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
import com.PauloHDSousa.Utils.ResizeAnimation;
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
    FloatingActionButton fbSlowScroll, fbSpeedScroll, fbStopPlayScroll;
    WebView webView;
    Handler mHandler = new Handler();
    boolean autoScrollOn = true;
    int scrollSpeed = 2;
    int scrollUpdateInMS = 400;
    boolean isShuffleOn;
    boolean isSongPaused;
    SeekBar sbMusic;
    Handler seekHandler = new Handler();
    String currentLibrary = "";
    LinearLayout linearLayoutMusicContnet, layoutHTMLContent;
    RelativeLayout relativeLayoutHTMLContent;

    //Auto-Scroll
    Runnable mScrollDown = new Runnable() {
        public void run() {
            if (autoScrollOn && !isSongPaused) {
                webView.scrollBy(0, scrollSpeed);
                mHandler.postDelayed(this, scrollUpdateInMS);
            }
        }
    };


    @Override
    protected void onStart() {
        super.onStart();

        ivAlbum = (ImageView) findViewById(R.id.ivAlbum);
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

                    final Track track = playerState.track;

                    if (track != null && lastTrack != track) {

                        currentLibrary = track.uri;

                        seekHandler.removeCallbacks(run);

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

        //SharedPrefs
        AppPreferences appPreferences = AppPreferences.getInstance(this);
        scrollSpeed = appPreferences.getInt(AppPreferences.ScrollSpeedKey);
        ProgressBar pbLoadingHTML = findViewById(R.id.pbLoadingHTML);

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
        layoutHTMLContent= (LinearLayout) findViewById(R.id.layoutHTMLContent);

        ibNext = (ImageButton) findViewById(R.id.ibNext);
        ibStop = (ImageButton) findViewById(R.id.ibStop);
        ibPrevious = (ImageButton) findViewById(R.id.ibPrevious);
        ibPlay = (ImageButton) findViewById(R.id.ibPlay);
        ibShuffle = (ImageButton) findViewById(R.id.ibShuffle);
        sbMusic = (SeekBar) findViewById(R.id.sbMusic);
        ibSpotify = (ImageButton) findViewById(R.id.ibSpotify);
        ibClosePlayer = (ImageButton) findViewById(R.id.ibClosePlayer);


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
        fbStopPlayScroll = (FloatingActionButton) findViewById(R.id.fbStopPlayScroll);

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

        fbStopPlayScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isSongPaused)
                    return;

                autoScrollOn = !autoScrollOn;
                int backgroundImage = R.drawable.play;

                if (autoScrollOn) {
                    backgroundImage = R.drawable.pause;
                    mScrollDown.run();
                }
                fbStopPlayScroll.setImageResource(backgroundImage);
            }
        });



        //Music Control Buttons Actions
        ibClosePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayoutMusicContnet.animate()
                    .translationY(linearLayoutMusicContnet.getHeight() - 50 )
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                });

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
