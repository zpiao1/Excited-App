package com.example.zpiao1.excited.views;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.adapters.EventImagePagerAdapter;
import com.example.zpiao1.excited.adapters.IconAdapter;
import com.example.zpiao1.excited.data.CategoryIcon;
import com.example.zpiao1.excited.data.SimpleEvent;
import com.example.zpiao1.excited.server.IEventRequest;
import com.example.zpiao1.excited.server.ServerUtils;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final float Y_CHANGE_THRESHOLD = 300f;

    private static final String[] CATEGORIES =
            {"movie", "art", "sports", "nightlife", "kids", "expo"};

    private IconAdapter mIconAdapter;
    private EventImagePagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    private int mGarbageIconDefaultHeight;
    private int mStarIconDefaultHeight;


    private boolean[] mCategoryCheckedStates;

    private CompositeDisposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // Logging App Activations
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Add the events to the view pager
        mViewPager = (ViewPager) findViewById(R.id.event_view_pager);
        loadGridView();

        mDisposable = new CompositeDisposable();
        fetchEvents();

        mGarbageIconDefaultHeight = findViewById(R.id.garbage_image).getLayoutParams().height;
        mStarIconDefaultHeight = findViewById(R.id.star_image).getLayoutParams().height;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_login) {
            // Start the LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadGridView() {
        GridView gridView = (GridView) findViewById(R.id.icon_container);
        ArrayList<CategoryIcon> categoryIcons = new ArrayList<>();
        categoryIcons.add(new CategoryIcon("Movie", R.drawable.ic_film_reel,
                R.drawable.ic_film_reel_grey));
        categoryIcons.add(new CategoryIcon("Art", R.drawable.ic_masks,
                R.drawable.ic_masks_grey));
        categoryIcons.add(new CategoryIcon("Sports", R.drawable.ic_archery,
                R.drawable.ic_archery_grey));
        categoryIcons.add(new CategoryIcon("Nightlife", R.drawable.ic_wine_glass,
                R.drawable.ic_wine_glass_grey));
        categoryIcons.add(new CategoryIcon("Kids", R.drawable.ic_boy,
                R.drawable.ic_boy_grey));
        categoryIcons.add(new CategoryIcon("Expo", R.drawable.ic_exhibition,
                R.drawable.ic_exhibition_grey));
        mIconAdapter = new IconAdapter(MainActivity.this, categoryIcons);

        gridView.setAdapter(mIconAdapter);

        // Initialize all the categories to be checked
        mCategoryCheckedStates = new boolean[categoryIcons.size()];
        for (int i = 0; i < mCategoryCheckedStates.length; ++i)
            mCategoryCheckedStates[i] = true;
    }

    public void changeIconGradually(float startTouchY, float currentTouchY) {
        float dy = currentTouchY - startTouchY;
        // Swiping down, should change the garbage icon
        ImageView iconImage;
        int color;
        int defaultHeight;
        float scale = Math.abs(dy) / Y_CHANGE_THRESHOLD;
        if (scale > 1)
            scale = 1;
        if (dy > 10) {
            iconImage = (ImageView) findViewById(R.id.garbage_image);
            color = ContextCompat.getColor(this, R.color.lightRed);
            defaultHeight = mGarbageIconDefaultHeight;
            changeIconGraduallyHelper(iconImage, color, defaultHeight, scale);
        } else if (dy < -10) {
            iconImage = (ImageView) findViewById(R.id.star_image);
            color = ContextCompat.getColor(this, R.color.lightGreen);
            defaultHeight = mStarIconDefaultHeight;
            changeIconGraduallyHelper(iconImage, color, defaultHeight, scale);
        }
    }

    private void changeIconGraduallyHelper(ImageView iconImage, int color, int defaultHeight, float
            scale) {
        // Change the size of the ImageView
        ViewGroup.LayoutParams params = iconImage.getLayoutParams();
        params.height = (int) (defaultHeight * (1 + scale));
        iconImage.setLayoutParams(params);

        // Change the color of the ImageView
        int alpha = (int) (0xFF * 0.5f * scale);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        iconImage.setBackgroundColor(Color.argb(alpha, red, green, blue));
    }

    public void resetIcons() {
        ImageView garbageImage = (ImageView) findViewById(R.id.garbage_image);
        ImageView startImage = (ImageView) findViewById(R.id.star_image);
        resetIconsHelper(garbageImage, mGarbageIconDefaultHeight);
        resetIconsHelper(startImage, mStarIconDefaultHeight);
    }

    private void resetIconsHelper(ImageView iconImage, int defaultHeight) {
        ViewGroup.LayoutParams params = iconImage.getLayoutParams();
        params.height = defaultHeight;
        iconImage.setLayoutParams(params);

        iconImage.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
    }

    public void onImageRemoved(Uri uri) {
    }

    public void onImageStarred(Uri uri) {
    }


    public void onCategoryCheckedChanged(int position, boolean isChecked) {
        if (mCategoryCheckedStates == null)
            throw new RuntimeException("mCategoryCheckedStates is null");
        // If there is change in selection of categories
        if (mCategoryCheckedStates[position] != isChecked) {
            mCategoryCheckedStates[position] = isChecked;
            fetchEvents();
        }
    }

    private void fetchEvents() {
        IEventRequest request = ServerUtils.getRetrofit()
                .create(IEventRequest.class);
        ServerUtils.addToDisposable(mDisposable,
                request.getEvents(buildEventSelection()),
                new Consumer<List<SimpleEvent>>() {
                    @Override
                    public void accept(List<SimpleEvent> events) throws Exception {
                        mPagerAdapter = new EventImagePagerAdapter(getSupportFragmentManager(),
                                events);
                        mViewPager.setAdapter(mPagerAdapter);
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "fetchEvents", throwable);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    private String buildEventSelection() {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < mCategoryCheckedStates.length; ++i)
            if (mCategoryCheckedStates[i])
                selected.add(CATEGORIES[i]);
        return selected.isEmpty() ? "none" : TextUtils.join("|", selected);
    }
}
