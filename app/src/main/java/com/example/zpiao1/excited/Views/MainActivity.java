package com.example.zpiao1.excited.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.data.User;
import com.example.zpiao1.excited.server.HttpError;
import com.example.zpiao1.excited.server.HttpErrorUtils;
import com.example.zpiao1.excited.user.UserManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import java.io.IOException;

import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener,
        UserManager.UserActivityHandler {

    public static final int LOGIN_REQUEST = 0;
    private static final String TAG = MainActivity.class.getSimpleName();
    private CompositeDisposable mDisposable;
    private ImageView mHeaderImage;
    private TextView mHeaderUserName;
    private TextView mHeaderEmail;
    private NavigationView mNavigationView;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDisposable = new CompositeDisposable();
        // Logging App Activations
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());
        // For Google
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken(getString(R.string.web_client_id))
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        setUpNavigationUI();

        if (savedInstanceState == null) {
            MainFragment mainFragment = MainFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mainFragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_view_events: {
                break;
            }
            case R.id.nav_add_event: {
                break;
            }
            case R.id.nav_settings: {
                // Show the settings
                showSettingsFragment();
                break;
            }
            case R.id.nav_log_out: {
                UserManager.logOut(this, mGoogleApiClient, this);
                FragmentManager manager = getSupportFragmentManager();
                // Remove the SettingsFragment if it is displayed when user logs out
                if (manager.findFragmentById(R.id.fragment_container)
                        instanceof SettingsFragment) {
                    if (manager.getBackStackEntryCount() > 0)
                        manager.popBackStack();
                }
                break;
            }
            case R.id.nav_log_in: {
                startActivityForResult(new Intent(MainActivity.this, LoginActivity.class),
                        LOGIN_REQUEST);
                break;
            }
        }

        closeDrawer();
        return true;
    }

    void setUpNavigationUI() {
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setItemIconTintList(null);
        toggleNavMenu(UserManager.hasLoggedIn(this));

        View header = mNavigationView.getHeaderView(0);
        mHeaderImage = (ImageView) header.findViewById(R.id.header_image);
        mHeaderUserName = (TextView) header.findViewById(R.id.header_user_name);
        mHeaderEmail = (TextView) header.findViewById(R.id.header_email);

        mNavigationView.setCheckedItem(R.id.nav_view_events);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!UserManager.hasLoggedIn(MainActivity.this)) {
                    // User is not logged in
                    // Login the user
                    startActivityForResult(new Intent(MainActivity.this,
                                    LoginActivity.class),
                            LOGIN_REQUEST);
                } else {
                    showSettingsFragment();
                }
                closeDrawer();
            }
        });

        setUpHeaderUI();
    }


    private void setUpHeaderUI() {
        UserManager.getUser(this, new UserManager.UserHandler() {
            @Override
            public void handleUser(User user) {
                if (user == null)
                    return;
                Glide.with(MainActivity.this)
                        .load(user.getImageUrl())
                        .fitCenter()
                        .into(mHeaderImage);
                mHeaderUserName.setText(user.getDisplayName());
                mHeaderEmail.setVisibility(View.VISIBLE);
                mHeaderEmail.setText(user.email);
            }

            @Override
            public void handleError(Throwable throwable) {
                if (throwable instanceof HttpException) {
                    try {
                        HttpError error = HttpErrorUtils.convert(
                                (HttpException) throwable);
                        Log.e(TAG, "error: " + error.method);
                        Log.e(TAG, "error: " + error.err.name);
                        Log.e(TAG, "error: " + error.err.message);
                    } catch (IOException e) {
                        Log.d(TAG, "error in conversion: ", e);
                    }
                } else {
                    Log.e(TAG, "error", throwable);
                }
            }
        });
    }

    private void updateNavigationUI() {
        toggleNavMenu(UserManager.hasLoggedIn(this));
        setUpHeaderUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_REQUEST && resultCode == RESULT_OK) {
            updateNavigationUI();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed" + connectionResult.getErrorMessage());
    }

    private void toggleNavMenu(boolean hasLoggedIn) {
        Menu navMenu = mNavigationView.getMenu();
        navMenu.setGroupVisible(R.id.nav_group_logged_in, hasLoggedIn);
        navMenu.setGroupVisible(R.id.nav_group_logged_out, !hasLoggedIn);
    }

    @Override
    public void onSuccess(int code) {
        if (code == LOG_OUT_SUCCESSFUL) {
            updateNavigationUI();
            mNavigationView.setCheckedItem(R.id.nav_view_events);
        } else if (code == GOOGLE_SIGN_OUT_SUCCESSFUL) {
            Log.d(TAG, "Google Sign out successfully");
        }
    }

    @Override
    public void onFail(Throwable throwable, int code) {
        if (code == LOG_OUT_FAILED) {
            Toast.makeText(this, "Failed to log out", Toast.LENGTH_SHORT).show();
        } else if (code == GOOGLE_SIGN_OUT_FAILED) {
            Toast.makeText(this, "Failed to sign out Google Account", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSettingsFragment() {
        mNavigationView.setCheckedItem(R.id.nav_settings);
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentById(R.id.fragment_container)
                instanceof SettingsFragment) {
            // Prevent the same fragment to be added twice
            return;
        }
        manager.beginTransaction()
                .replace(R.id.fragment_container,
                        SettingsFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    private void closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }
}