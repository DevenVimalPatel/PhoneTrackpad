package com.trackpad.aldini.phonetrackpad;

import android.annotation.SuppressLint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class Trackpad extends AppCompatActivity {
    public static final String err = "error";
    private TextView mTextMessage;
    private Socket toServer = null;
    private PrintWriter myPrint = null;
    private VelocityTracker myVelo = null;
    private float pastX = 0, pastY = 0, currX = 0, currY = 0;

    private class NetworkOperations extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... args) {
            String ip = getIntent().getStringExtra("IP");
            int port = Integer.parseInt(getIntent().getStringExtra("PORT"));

            try {
                toServer = new Socket(ip, port);
            } catch (ConnectException ce) {
                Log.e(err, "ConnectionException thrown... \n" + Log.getStackTraceString(ce));
                return "connect";
            } catch (Exception e) {
                Log.e(err, "Exception thrown... \n" + Log.getStackTraceString(e));
                return "e";
            }
            if (toServer != null) {
                return "true";
            } else {
                return "false";
            }
        }
    }

    private class NetworkOut extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... args) {
            myPrint.println(args[0]);
            return "done";

        }
    }

    private class NetworkDone extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... args) {
            myPrint.close();
            try {
                toServer.close();
            } catch (Exception e) {}
            return "done";

        }
    }

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

    private void printStuff(String tobeprint) {
        NetworkOut nout = new NetworkOut();
        nout.execute(tobeprint);
        try {
            nout.get();
        } catch (Exception e) { }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trackpad);

        if (!isConnected()) {
            Log.e(err, "Device is not connected.");
            Toast.makeText(this, "No Connection", Toast.LENGTH_SHORT).show();
            finish();
        }

        NetworkOperations n = new NetworkOperations();
        n.execute("");

        String res = "";

        try {
            res = n.get();
        } catch (CancellationException e) {
            Log.e(err, "One of Three Exceptions thrown");
            finish();
        } catch (ExecutionException e) {
            Log.e(err, "One of Three Exceptions thrown");
            finish();
        } catch (InterruptedException e) {
            Log.e(err, "One of Three Exceptions thrown");
            finish();
        }

        if (res == "") {
            Log.e(err, "n.get gives empty string");
            Toast.makeText(this, "Could not connect", Toast.LENGTH_SHORT).show();
            finish();
        } else if (res == "e") {
            Log.e(err, "e thrown");
            Toast.makeText(this, "Could not connect", Toast.LENGTH_SHORT).show();
            finish();
        } else if (res == "connect") {
            Log.e(err, "connect thrown");
            Toast.makeText(this, "Could not connect", Toast.LENGTH_SHORT).show();
            finish();
        } else if (res == "false") {
            Log.e(err, "toServer is Null");
            Toast.makeText(this, "Could not connect", Toast.LENGTH_SHORT).show();
            finish();
        }

        try {
            myPrint = new PrintWriter(toServer.getOutputStream(), true);
        } catch (IOException e) {
            Log.e(err, "can't even make the writer");
            finish();
        }


        final Button button = findViewById(R.id.button);
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        printStuff("1");
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        printStuff("2");
                        break;
                    }

                }
                return true;
            }
        });

        final Button button2 = findViewById(R.id.button2);
        button2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        pastX = event.getX();
                        pastY = event.getY();
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        currX = event.getX();
                        currY = event.getY();
                        String out = ""+(currX-pastX)+","+(currY-pastY);
                        printStuff(out);
                        Log.d("track", out);
                        pastX = currX;
                        pastY = currY;
                        break;
                    }

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        break;

                }
                return true;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        printStuff("quit");

        NetworkDone ndone = new NetworkDone();
        ndone.execute("");
        try {
            ndone.get();
        } catch (Exception e) { }

        finish();
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
