package com.PauloHDSousa.SpotifyWithLyricsInside;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.PauloHDSousa.Broadcast.CurrentNetworkChangeReceiver;
import com.PauloHDSousa.Models.Playlist;
import com.PauloHDSousa.Utils.Playlists;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
public class HomeActivity extends AppCompatActivity {


    private static Activity context;

    private static final int REQUEST_CODE = 2337;
    private static final String REDIRECT_URI = "com.PauloHDSousa.SpotifyWithLyricsInside://callback";
    SpotifyAppRemote mSpotifyAppRemote;
    LinearLayout llPlaylists;
    ImageButton ibDrivePlaylist, ibFitnessPlaylist,ibWakePlaylist,ibDefaultPlaylist,ibRate,ibShare;
    Button btnOuvirComLetra;
    LinearLayout horizontalParent;
    ProgressBar pbLoadingPlaylist;
    ScrollView scrollView;


    Playlists playlists = new Playlists();

    private CurrentNetworkChangeReceiver mNetworkReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        setContentView(R.layout.activity_home);

        //Register Receiver
        mNetworkReceiver = new CurrentNetworkChangeReceiver();
        registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
    }

    void unregisterReceiver(){
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static void redirect(boolean isConnected){

        Class redirectActivity = InternetLostActivity.class;

        if(isConnected)
            redirectActivity = HomeActivity.class;

        context.startActivity(new Intent(context, redirectActivity));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!isPackageInstalled("com.spotify.music", this.getPackageManager())){
            Intent myIntent = new Intent(HomeActivity.this, SpotifyConnectionActivity.class);
            startActivity(myIntent);
        }

        llPlaylists = findViewById(R.id.llPlaylists);

        ibDrivePlaylist  = (ImageButton) findViewById(R.id.ibDrivePlaylist);
        ibFitnessPlaylist =(ImageButton) findViewById(R.id.ibFitnessPlaylist);
        ibWakePlaylist =(ImageButton) findViewById(R.id.ibWakePlaylist);
        ibDefaultPlaylist =(ImageButton) findViewById(R.id.ibDefaultPlaylist);
        scrollView = (ScrollView)findViewById(R.id.svPlaylists);

        ibRate = (ImageButton)findViewById(R.id.ibRate);
        ibShare= (ImageButton)findViewById(R.id.ibShare);

        pbLoadingPlaylist = (ProgressBar)  findViewById(R.id.pbLoadingPlaylist);


        ibDefaultPlaylist.setBackgroundColor(getResources().getColor(R.color.selected));

        btnOuvirComLetra = (Button) findViewById(R.id.btnOuvirComLetra);

        btnOuvirComLetra.setOnClickListener(v ->{
            Intent myIntent = new Intent(this, MainActivity.class);
            startActivity(myIntent);
            return;
        });

        ibShare.setOnClickListener(v -> {

            final String appPackageName = getPackageName();
            String app_name = getResources().getString(R.string.app_name);
            String share = getResources().getString(R.string.share);
            String share_message = getResources().getString(R.string.share_message);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, app_name);
            intent.putExtra(Intent.EXTRA_TEXT, share_message + " https://play.google.com/store/apps/details?id=" + appPackageName);
            startActivity(Intent.createChooser(intent, share));
        });

        ibRate.setOnClickListener(v -> {

            final String appPackageName = getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        });

        ibDrivePlaylist.setOnClickListener(v -> {
            setSelectedButton((ImageButton)v);
            loadPlaylistSuggestion(Playlists.PlaylistType.AUTOMOTIVE);
        });

        ibFitnessPlaylist.setOnClickListener(v -> {
            setSelectedButton((ImageButton)v);
            loadPlaylistSuggestion(Playlists.PlaylistType.FITNESS);
        });

        ibWakePlaylist.setOnClickListener(v -> {
            setSelectedButton((ImageButton)v);
            loadPlaylistSuggestion(Playlists.PlaylistType.WAKE);
        });

        ibDefaultPlaylist.setOnClickListener(v -> {
            setSelectedButton((ImageButton)v);
            loadPlaylistSuggestion(Playlists.PlaylistType.DEFAULT);
        });

        ConnectionParams connectionParams =
                new ConnectionParams.Builder("9e5381fc5bb34ce2a0e68dadd7662977")
                        .setRedirectUri(REDIRECT_URI)
                        .build();


        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {

            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                connected();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Intent myIntent = new Intent(HomeActivity.this, SpotifyConnectionActivity.class);
                startActivity(myIntent);
                return;
            }
        });

    }

    void setSelectedButton(ImageButton button){

        ibDrivePlaylist.setBackgroundColor(Color.TRANSPARENT);
        ibFitnessPlaylist.setBackgroundColor(Color.TRANSPARENT);
        ibWakePlaylist.setBackgroundColor(Color.TRANSPARENT);
        ibDefaultPlaylist.setBackgroundColor(Color.TRANSPARENT);

        button.setBackgroundColor(getResources().getColor(R.color.selected));
    }

    private void connected() {
        if(llPlaylists.getChildCount() == 0)
            loadPlaylistSuggestion(ContentApi.ContentType.DEFAULT);
    }

    void loadPlaylistSuggestion(String playList){

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, 0);
                scrollView.pageScroll(View.FOCUS_UP);
                scrollView.smoothScrollTo(0,0);
            }
        });

        pbLoadingPlaylist.setVisibility(View.VISIBLE);

        if(llPlaylists.getChildCount() > 0)
            llPlaylists.removeAllViews();


        List<Playlist> recommendedItems = playlists.getPlayList(playList);

        for(Playlist playItem : recommendedItems) {
            AddItemToView(playItem);
        }

        pbLoadingPlaylist.setVisibility(View.GONE);
    }


    void AddItemToView(Playlist item){

        if(horizontalParent == null || horizontalParent.getChildCount() == 2) {
            horizontalParent = new LinearLayout(this);
            horizontalParent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            horizontalParent.setOrientation(LinearLayout.HORIZONTAL);
        }

        LinearLayout parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams parentParams  = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        parentParams.setMargins(15, 10, 30, 0);

        parent.setLayoutParams(parentParams);

        ImageButton ibPlaylist = new ImageButton(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(312, 312);
        params.setMargins(0, 10, 0,0);

        ibPlaylist.setLayoutParams(params);
        ibPlaylist.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ibPlaylist.setBackground(null);

        Picasso.get().load(item.ImageURL).placeholder(R.drawable.playlist).into(ibPlaylist);

        ibPlaylist.setOnClickListener(view -> {
            mSpotifyAppRemote.getPlayerApi().play(item.URL);
        });
        //Add image button to item
        parent.addView(ibPlaylist);
        //Add item to a Horizontal Layout
        horizontalParent.addView(parent);

        if(horizontalParent.getChildCount() == 2)
            llPlaylists.addView(horizontalParent);
    }

    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}