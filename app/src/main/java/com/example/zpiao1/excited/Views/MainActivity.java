package com.example.zpiao1.excited.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.data.User;
import com.example.zpiao1.excited.logic.Observer;
import com.example.zpiao1.excited.logic.UserManager;
import com.example.zpiao1.excited.server.HttpError;
import com.example.zpiao1.excited.server.HttpErrorUtils;
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
        UserManager.UserActivityHandler,
        Observer<User> {

    public static final int LOGIN_REQUEST = 0;
    public static final int SUBSCRIBE_ID = 0;
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
        // initialize the user subject
        UserManager.init(getApplicationContext());

        setContentView(R.layout.activity_main);

        mDisposable = new CompositeDisposable();
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
        UserManager.subscribeForUser(SUBSCRIBE_ID, this);

        if (savedInstanceState == null) {
            MainFragment mainFragment = MainFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mainFragment)
                    .commit();
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
                mNavigationView.setCheckedItem(R.id.nav_view_events);
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
                showViewEventsFragment();
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

        View header = mNavigationView.getHeaderView(0);
        mHeaderImage = (ImageView) header.findViewById(R.id.header_image);
        mHeaderUserName = (TextView) header.findViewById(R.id.header_user_name);
        mHeaderEmail = (TextView) header.findViewById(R.id.header_email);

        mNavigationView.setCheckedItem(R.id.nav_view_events);
        header.setOnClickListener(view -> {
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
        });

        updateNavigationUI();
    }

    private void updateNavigationUI() {
        toggleNavMenu(UserManager.hasLoggedIn(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
        UserManager.unsubscribeForUser(SUBSCRIBE_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_REQUEST && resultCode == RESULT_OK) {
            UserManager.fetchUser(this);
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
        navMenu.findItem(R.id.nav_log_in).setVisible(!hasLoggedIn);
        navMenu.findItem(R.id.nav_settings).setVisible(hasLoggedIn);
        navMenu.findItem(R.id.nav_log_out).setVisible(hasLoggedIn);
    }

    @Override
    public void onSuccess(int code) {
        if (code == LOG_OUT_SUCCESSFUL) {
            Toast.makeText(this, "You are logged out", Toast.LENGTH_SHORT).show();
            updateNavigationUI();
            resetHeader();
            showViewEventsFragment();
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
                        SettingsFragment.newInstance(mGoogleApiClient))
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .addToBackStack(null)
                .commit();
    }

    private void showViewEventsFragment() {
        mNavigationView.setCheckedItem(R.id.nav_view_events);
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragment_container);
        if (fragment != null && fragment instanceof MainFragment) {
            return;
        }
        if (manager.getBackStackEntryCount() > 0) {
            manager.popBackStack();
        }
        fragment = manager.findFragmentById(R.id.fragment_container);
        if (fragment != null && fragment instanceof MainFragment) {
            return;
        }
        manager.beginTransaction()
                .replace(R.id.fragment_container,
                        MainFragment.newInstance())
                .commit();
    }

    private void closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void resetHeader() {
        mHeaderImage.setImageResource(R.drawable.ic_user_not_logged_in);
        mHeaderUserName.setText(getString(R.string.header_log_in));
        mHeaderEmail.setVisibility(View.GONE);
    }

    @Override
    public void onNext(User user) {
        Log.d(TAG, "onNext: user " + (user != null));
        if (user == null) {
            throw new NullPointerException("User is null!");
        } else if (user.status == User.STATUS_INIT || user.status == User.STATUS_LOGGED_OUT) {
            resetHeader();
        } else if (user.status == User.STATUS_PASSWORD_CHANGED) {
            Toast.makeText(this, "Password is changed successfully.\nPlease Sign in again.",
                    Toast.LENGTH_SHORT).show();
            UserManager.logOut(this, mGoogleApiClient, this);
        } else if (user.status == User.STATUS_LOGGED_IN) {
            if (user.getImageUrl() == null)
                mHeaderImage.setImageResource(R.drawable.ic_user_not_logged_in);
            else {
                Log.d(TAG, "imageUri: " + user.getImageUrl());
                SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_pref_name_server), MODE_PRIVATE);
                String token = prefs.getString(getString(R.string.pref_token_key), null);
                if (token != null) {
                    LazyHeaders headers = new LazyHeaders.Builder()
                            .addHeader("x-access-token", token)
                            .build();
                    GlideUrl url = new GlideUrl(user.getImageUrl(), headers);
                    Glide.with(MainActivity.this)
                            .load(url)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .fitCenter()
                            .into(mHeaderImage);
                }
            }
            mHeaderUserName.setText(user.getDisplayName());
            mHeaderEmail.setVisibility(View.VISIBLE);
            mHeaderEmail.setText(user.email);
        }

    }

    @Override
    public void onError(Throwable throwable) {
        if (throwable instanceof HttpException) {
            try {
                HttpError error = HttpErrorUtils.convert((HttpException) throwable);
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
}