package com.PauloHDSousa.SpotifyWithLyricsInside;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.PauloHDSousa.Broadcast.CurrentNetworkChangeReceiver;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import androidx.appcompat.app.AppCompatActivity;

public class SpotifyConnectionActivity extends AppCompatActivity {

    Button btnRetry,btnInstall;
    private CurrentNetworkChangeReceiver mNetworkReceiver;
    private static final String REDIRECT_URI = "com.PauloHDSousa.SpotifyWithLyricsInside://callback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_spotify);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //Register Internet Receiver
        mNetworkReceiver = new CurrentNetworkChangeReceiver();
        registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        btnRetry = findViewById(R.id.btnRetry);
        btnInstall = findViewById(R.id.btnInstall);
        btnInstall.setOnClickListener(v ->{
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music"));
            startActivity(intent);
        });

        btnRetry.setOnClickListener(v -> {

            ConnectionParams connectionParams = new ConnectionParams.Builder("9e5381fc5bb34ce2a0e68dadd7662977")
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build();

            SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {

                @Override
                public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                    Intent myIntent = new Intent(SpotifyConnectionActivity.this, HomeActivity.class);
                    startActivity(myIntent);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    String without_connection = getResources().getString(R.string.spotify_error);
                    Toast.makeText(SpotifyConnectionActivity.this, without_connection, Toast.LENGTH_SHORT).show();
                }
            });


        });

    }
    @Override
    protected void onStart() {
        super.onStart();
        if (!isPackageInstalled("com.spotify.music", this.getPackageManager())) {
            btnInstall.setVisibility(View.VISIBLE);
            btnRetry.setVisibility(View.GONE);
        }
        else
        {
            btnInstall.setVisibility(View.GONE);
            btnRetry.setVisibility(View.VISIBLE);
        }
    }

    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
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
}
