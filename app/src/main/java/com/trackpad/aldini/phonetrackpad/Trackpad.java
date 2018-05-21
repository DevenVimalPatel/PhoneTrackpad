package com.trackpad.aldini.phonetrackpad;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;

public class Trackpad extends AppCompatActivity {
    public static final String err = "error";
    private TextView mTextMessage;
    private Socket toServer = null;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trackpad);

        if(!isConnected()){
            Log.e(err, "Device is not connected.");
            //handleerror
            finish();
        }
        if(!connectToServ()){
            Log.e(err, "Connection to server failed.");
            //handleerror
            finish();
        }

        final Button button = findViewById(R.id.button);
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    private boolean connectToServ(){
        try {
            toServer = new Socket("127.0.0.1", 8000);
        } catch (IOException e) {
            Log.e(err, "IOException thrown when connecting.");
            //handleerror
            finish();
        }
        return toServer != null;
    }

    private boolean isConnected(){
        ConnectivityManager connection =
                (ConnectivityManager) this.getSystemService(getBaseContext().CONNECTIVITY_SERVICE);
        NetworkInfo info = null;
        try {
            info = connection.getActiveNetworkInfo();
        } catch(NullPointerException npe) {
            Log.e(err, "Nullpointerexception thrown by NetworkInfo");
            //handleerror
            finish();
        }
        if(info != null){
            if(info.getType() != ConnectivityManager.TYPE_WIFI){
                Toast.makeText(this, info.getTypeName(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (info.getType() != ConnectivityManager.TYPE_MOBILE)
            {
                Toast.makeText(this, info.getTypeName(), Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

}
