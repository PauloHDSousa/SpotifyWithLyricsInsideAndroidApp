package com.PauloHDSousa.SpotifyWithLyricsInside;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.PauloHDSousa.Broadcast.CurrentNetworkChangeReceiver;
import com.PauloHDSousa.Utils.Internet;

import androidx.appcompat.app.AppCompatActivity;

public class InternetLostActivity  extends AppCompatActivity {

    Button btnRetry;
    private CurrentNetworkChangeReceiver mNetworkReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_internet_lost);

        //Register Receiver
        mNetworkReceiver = new CurrentNetworkChangeReceiver();
        registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        Internet internet = new Internet(this);

        btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(v -> {
            if(internet.isNetworkConnected()){
                Intent myIntent = new Intent(this, MainActivity.class);
                startActivity(myIntent);
                return;
            }
            else{
                Toast.makeText(this,"Você ainda não possuí conexão com a internet", Toast.LENGTH_SHORT).show();
            }
        });

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
