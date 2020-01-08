package com.PauloHDSousa.SpotifyWithLyricsInside;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.PauloHDSousa.Broadcast.CurrentNetworkChangeReceiver;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.ListItem;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {


    private static Activity context;

    private static final int REQUEST_CODE = 2337;
    private static final String REDIRECT_URI = "http://com.PauloHDSousa.SpotifyWithLyricsInside://callback";
    SpotifyAppRemote mSpotifyAppRemote;
    LinearLayout llPlaylists;
    ImageButton ibDrivePlaylist, ibFitnessPlaylist, ibSleepPlaylist, ibWakePlaylist,ibDefaultPlaylist,ibFirePlaylist, ibRate;
    Button btnOuvirComLetra;
    LinearLayout horizontalParent;
    ProgressBar pbLoadingPlaylist;

    private CurrentNetworkChangeReceiver mNetworkReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        setContentView(R.layout.activity_home);

        //Register Receiver
        mNetworkReceiver = new CurrentNetworkChangeReceiver();
        registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

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


        llPlaylists = findViewById(R.id.llPlaylists);
        ibDrivePlaylist  = (ImageButton) findViewById(R.id.ibDrivePlaylist);
        ibFitnessPlaylist =(ImageButton) findViewById(R.id.ibFitnessPlaylist);
        ibSleepPlaylist=(ImageButton) findViewById(R.id.ibSleepPlaylist);
        ibWakePlaylist =(ImageButton) findViewById(R.id.ibWakePlaylist);
        ibDefaultPlaylist =(ImageButton) findViewById(R.id.ibDefaultPlaylist);
        ibFirePlaylist =(ImageButton) findViewById(R.id.ibFirePlaylist);
        ibRate = (ImageButton)findViewById(R.id.ibRate);

        pbLoadingPlaylist = (ProgressBar)  findViewById(R.id.pbLoadingPlaylist);


        ibDefaultPlaylist.setBackgroundColor(getResources().getColor(R.color.selected));

        btnOuvirComLetra = (Button) findViewById(R.id.btnOuvirComLetra);

        btnOuvirComLetra.setOnClickListener(v ->{
            Intent myIntent = new Intent(this, MainActivity.class);
            startActivity(myIntent);
            return;
        });

        ibRate.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.android.example"));
            startActivity(intent);

            
            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        });

        ibDrivePlaylist.setOnClickListener(v -> {
            setSelectedButton((ImageButton)v);
            loadPlaylistSuggestion(ContentApi.ContentType.NAVIGATION);
        });

        ibFirePlaylist.setOnClickListener(v -> {
            setSelectedButton((ImageButton)v);
            loadPlaylistSuggestion(ContentApi.ContentType.AUTOMOTIVE);
        });

        ibFitnessPlaylist.setOnClickListener(v -> {
            setSelectedButton((ImageButton)v);
            loadPlaylistSuggestion(ContentApi.ContentType.FITNESS);
        });

        ibSleepPlaylist.setOnClickListener(v -> {
            setSelectedButton((ImageButton)v);
            loadPlaylistSuggestion(ContentApi.ContentType.SLEEP);
        });

        ibWakePlaylist.setOnClickListener(v -> {
            setSelectedButton((ImageButton)v);
            loadPlaylistSuggestion(ContentApi.ContentType.WAKE);
        });

        ibDefaultPlaylist.setOnClickListener(v -> {
            setSelectedButton((ImageButton)v);
            loadPlaylistSuggestion(ContentApi.ContentType.DEFAULT);
        });

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

    void setSelectedButton(ImageButton button){

        ibDrivePlaylist.setBackgroundColor(Color.TRANSPARENT);
        ibFitnessPlaylist.setBackgroundColor(Color.TRANSPARENT);
        ibSleepPlaylist.setBackgroundColor(Color.TRANSPARENT);
        ibWakePlaylist.setBackgroundColor(Color.TRANSPARENT);
        ibDefaultPlaylist.setBackgroundColor(Color.TRANSPARENT);
        ibFirePlaylist.setBackgroundColor(Color.TRANSPARENT);

        button.setBackgroundColor(getResources().getColor(R.color.selected));
    }

    private void connected() {
        loadPlaylistSuggestion(ContentApi.ContentType.DEFAULT);
    }

    void loadPlaylistSuggestion(String playList){

        pbLoadingPlaylist.setVisibility(View.VISIBLE);

        if(llPlaylists.getChildCount() > 0)
            llPlaylists.removeAllViews();

        mSpotifyAppRemote.getContentApi().getRecommendedContentItems(playList).setResultCallback(recommended ->{
            ListItem[] recommendedItems = recommended.items;

            for(ListItem playItem : recommendedItems){
                AddItemToView(playItem);
            }

            pbLoadingPlaylist.setVisibility(View.GONE);
        });
    }


    void AddItemToView(ListItem item){

        if(horizontalParent == null || horizontalParent.getChildCount() == 2) {
            horizontalParent = new LinearLayout(this);
            horizontalParent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            horizontalParent.setOrientation(LinearLayout.HORIZONTAL);
        }

        LinearLayout parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams parentParams  = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        parentParams.setMargins(15, 10, 15, 0);

        parent.setLayoutParams(parentParams);

        ImageButton ibPlaylist = new ImageButton(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(312, 312);
        params.setMargins(0, 10, 0,0);

        ibPlaylist.setLayoutParams(params);
        ibPlaylist.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ibPlaylist.setBackground(null);

        if(!item.imageUri.raw.equals("android.resource://com.spotify.music/drawable/mediaservice_browse")) {
            CallResult<Bitmap> imageBitmap = mSpotifyAppRemote.getImagesApi().getImage(item.imageUri);
            imageBitmap.setResultCallback(bitmap -> ibPlaylist.setImageBitmap(bitmap));
        }else{
            ibPlaylist.setBackgroundResource(R.drawable.playlist);
        }

        ibPlaylist.setOnClickListener(view -> {
            mSpotifyAppRemote.getPlayerApi().play(item.uri);
        });

        //Add image button to item
        parent.addView(ibPlaylist);

        TextView textView = new TextView(this);
        RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        tvParams.setMargins(20,0,0,0);
        textView.setText(item.title);
        textView.setLayoutParams(tvParams);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setLines(1);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);

        //Add text to item
        parent.addView(textView);

        //Add item to a Horizontal Layout
        horizontalParent.addView(parent);

        if(horizontalParent.getChildCount() == 2)
            llPlaylists.addView(horizontalParent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
}