package com.trackpad.aldini.phonetrackpad;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.database.Cursor;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity { //implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private AutoCompleteTextView mIP;
    private EditText mPort;
    private View mProgressView;
    private View mLoginFormView;
    private Socket testSocket = null;
    public static final String err = "error";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        try {
            testSocket.setSoTimeout(2000);
        } catch (Exception e) {}
        mIP = (AutoCompleteTextView) findViewById(R.id.ip);

        mPort = (EditText) findViewById(R.id.port);
        mPort.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.connect);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }
    
    


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mIP.setError(null);
        mPort.setError(null);

        // Store values at the time of the login attempt.
        String ip = mIP.getText().toString();
        String port = mPort.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(port) && !isPortValid(port)) {
            mPort.setError(getString(R.string.error_invalid_password));
            focusView = mPort;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(ip)) {
            mIP.setError(getString(R.string.error_field_required));
            focusView = mIP;
            cancel = true;
        } else if (!isIPValid(ip)) {
            mIP.setError(getString(R.string.error_invalid_email));
            focusView = mIP;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);
            //mAuthTask = new UserLoginTask(ip, port);
            //mAuthTask.execute((Void) null);
            showProgress(true);
            NetworkOperations n = new NetworkOperations();
            n.execute(ip, port);

            String res = "";


            try {
                res = n.get();
            } catch (Exception e) {
                cancel = true;
            }

            showProgress(false);

            if (res == "") {
                Toast.makeText(this, "Could not connect", Toast.LENGTH_SHORT).show();
                cancel = true;
            } else if (res == "e") {
                Toast.makeText(this, "Could not connect", Toast.LENGTH_SHORT).show();
                cancel = true;
            } else if (res == "connect") {
                Toast.makeText(this, "Could not connect", Toast.LENGTH_SHORT).show();
                cancel = true;
            } else if (res == "time") {
                Toast.makeText(this, "Could not connect", Toast.LENGTH_SHORT).show();
                cancel = true;
            } else if (res == "false") {
                Toast.makeText(this, "Could not connect", Toast.LENGTH_SHORT).show();
                cancel = true;
            } else {
                try {
                    testSocket.close();
                } catch (Exception e) { }

                Intent intent = new Intent(getBaseContext(), Trackpad.class);
                intent.putExtra("IP", ip);
                intent.putExtra("PORT", port);
                startActivity(intent);
            }

        }
    }

    private boolean isIPValid(String ip) {

        String[] ip_part = ip.split("\\.");
        if(ip_part.length != 4)
            return false;
        for(int i = 0; i < 4; ++i) {
            try {
                if ((Integer.parseInt(ip_part[i]) > 255) || (Integer.parseInt(ip_part[i]) < 0))
                    return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private boolean isPortValid(String port) {
        try {
            if (Integer.parseInt(port) > 65535 || Integer.parseInt(port) < 0)
                return false;
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    private class NetworkOperations extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... args) {

            try {
                testSocket = new Socket();
                testSocket.connect(new InetSocketAddress(args[0], Integer.parseInt(args[1])), 1000);
            } catch (ConnectException ce) {
                Log.e(err, "ConnectionException thrown... \n" + Log.getStackTraceString(ce));
                return "connect";
            } catch (SocketTimeoutException e) {
                Log.e(err, "Timeout thrown... \n" + Log.getStackTraceString(e));
                return "time";
            } catch (Exception e) {
                Log.e(err, "Exception thrown... \n" + Log.getStackTraceString(e));
                return "e";
            }
            if (testSocket != null) {
                return "true";
            } else {
                return "false";
            }
        }
    }
}

