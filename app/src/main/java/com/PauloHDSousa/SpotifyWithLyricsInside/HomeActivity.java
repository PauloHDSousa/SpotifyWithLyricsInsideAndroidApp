package com.PauloHDSousa.SpotifyWithLyricsInside;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.PauloHDSousa.Utils.Internet;
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



    private static final int REQUEST_CODE = 2337;
    private static final String REDIRECT_URI = "http://com.PauloHDSousa.SpotifyWithLyricsInside://callback";
    SpotifyAppRemote mSpotifyAppRemote;
    LinearLayout llPlaylists;
    ImageButton ibDrivePlaylist, ibFitnessPlaylist;
    Button btnOuvirComLetra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();

        llPlaylists = findViewById(R.id.llPlaylists);
        ibDrivePlaylist  = (ImageButton) findViewById(R.id.ibDrivePlaylist);
        ibFitnessPlaylist =(ImageButton) findViewById(R.id.ibFitnessPlaylist);
        btnOuvirComLetra = (Button) findViewById(R.id.btnOuvirComLetra);

        btnOuvirComLetra.setOnClickListener(v ->{
            Intent myIntent = new Intent(this, MainActivity.class);
            startActivity(myIntent);
            return;
        });

        ibDrivePlaylist.setOnClickListener(v -> {
            loadPlaylistSuggestion(ContentApi.ContentType.AUTOMOTIVE);
        });


        ibFitnessPlaylist.setOnClickListener(v -> {
            loadPlaylistSuggestion(ContentApi.ContentType.FITNESS);
        });

        Internet internet = new Internet(this);


        //If there is no internet, move to Another Activity
        if(!internet.isNetworkConnected()){
            Intent myIntent = new Intent(this, InternetLostActivity.class);
            startActivity(myIntent);
            return;
        }

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


    private void connected() {
        loadPlaylistSuggestion(ContentApi.ContentType.FITNESS);
    }

    void loadPlaylistSuggestion(String playList){

        if(llPlaylists.getChildCount() > 0)
            llPlaylists.removeAllViews();


        mSpotifyAppRemote.getContentApi().getRecommendedContentItems(playList).setResultCallback(recommended ->{
            ListItem[] items = recommended.items;

            for(ListItem item : items){

                LinearLayout parent = new LinearLayout(this);

                parent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                parent.setOrientation(LinearLayout.VERTICAL);



                ImageButton ibPlaylist = new ImageButton(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(248, 248);
                params.setMargins(0, 10, 0,0);
                ibPlaylist.setLayoutParams(params);
                ibPlaylist.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ibPlaylist.setBackground(null);

                CallResult<Bitmap> imageBitmap = mSpotifyAppRemote.getImagesApi().getImage(item.imageUri);
                imageBitmap.setResultCallback(bitmap -> ibPlaylist.setImageBitmap(bitmap));

                ibPlaylist.setOnClickListener(i -> {
                    mSpotifyAppRemote.getPlayerApi().play(item.uri);
                });

                parent.addView(ibPlaylist);


                TextView textView = new TextView(this);
                textView.setText(item.title);
                textView.setPadding(20,0,0,0);
                parent.addView(textView);


                llPlaylists.addView(parent);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
}