package com.example.zpiao1.excited.views;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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

import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.server.HttpError;
import com.example.zpiao1.excited.server.HttpErrorUtils;
import com.example.zpiao1.excited.user.UserManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import java.io.IOException;

import io.reactivex.disposables.CompositeDisposable;

public class LoginActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        UserManager.UserActivityHandler {

    private static final String TAG = LoginActivity.class.getSimpleName();


    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private ProgressDialog mProgressDialog;

    private GoogleApiClient mGoogleApiClient;

    private CompositeDisposable mDisposable;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UserManager.handleActivityResult(this, requestCode, resultCode, data, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDisposable = new CompositeDisposable();
        // For Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        // For Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken(getString(R.string.web_client_id))
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        setContentView(R.layout.activity_login);
        setupActionBar();
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                return id == R.id.login || id == EditorInfo.IME_NULL;
            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        setUpEmailLogIn();
        setupGoogleSignIn();
        setUpFacebookLogIn();
    }


    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, connectionResult.getErrorMessage());
    }

    private void setUpEmailLogIn() {
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleProgressUi(true);
                UserManager.emailLogIn(mEmailView.getText().toString(),
                        mPasswordView.getText().toString(),
                        LoginActivity.this,
                        LoginActivity.this);
            }
        });
    }

    void setupGoogleSignIn() {
        SignInButton signInButton = (SignInButton) findViewById(R.id.google_sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                UserManager.googleSignIn(mGoogleApiClient, LoginActivity.this);
            }
        });
    }

    void setUpFacebookLogIn() {
        LoginButton facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);

        facebookLoginButton.registerCallback(UserManager.getCallbackManager(),
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        toggleProgressUi(true);
                        UserManager.facebookLogIn(
                                loginResult, LoginActivity.this, LoginActivity.this);
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "FacebookCallback Login cancelled");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.e(TAG, "FacebookLoginCallback", error);
                    }
                });
    }

    private void toggleProgressUi(boolean show) {
        if (show) {
            mProgressDialog = ProgressDialog.show(this, null, getString(R.string.progress_loading));
        } else {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
    }

    @Override
    public void onSuccess(int code) {
        if (code == LOG_IN_SUCCESSFUL) {
            Toast.makeText(this, "Login Successfully!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
            toggleProgressUi(false);
        } else if (code == GOOGLE_SIGN_IN_SUCCESSFUL) {
            Log.d(TAG, "Google Sign In Successfully!");
            // Start to log in to server
            toggleProgressUi(true);
        }
    }

    @Override
    public void onFail(Throwable throwable, int code) {
        Log.e(TAG, "onFail", throwable);
        if (code == LOG_IN_FAILED) {
            toggleProgressUi(false);
        } else if (code == GOOGLE_SIGN_IN_FAILED) {
            Log.d(TAG, "Google Sign in Fail");
        } else if (code == EMAIL_LOG_IN_FAILED) {
            if (throwable instanceof HttpException) {
                try {
                    HttpError httpError = HttpErrorUtils.convert((HttpException) throwable);
                    Toast.makeText(LoginActivity.this,
                            "Please check your email and Password",
                            Toast.LENGTH_SHORT)
                            .show();
                    Log.e(TAG, "loginViaEmail \n" + httpError.method);
                    Log.d(TAG, "loginViaEmail \n" + httpError.err.name);
                    Log.d(TAG, "loginViaEmail \n" + httpError.err.message);
                } catch (IOException e) {
                    Log.e(TAG, "Conversion Error", e);
                }
            } else {
                Log.e(TAG, "loginViaEmail", throwable);
            }
        }
    }
}

