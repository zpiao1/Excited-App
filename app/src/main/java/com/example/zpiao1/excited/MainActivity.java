package com.example.zpiao1.excited;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final float Y_CHANGE_THRESHOLD = 300f;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int TRANSPARENT_COLOR = 0x00000000;

    private IconAdapter mIconAdapter;
    private EventImagePagerAdapter mPagerAdapter;
    private ImageView[] mDotsIndicator;
    private ArrayList<EventImage> mEventImages;
    private int mGarbageIconDefaultHeight;
    private int mStarIconDefaultHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        loadGridView();
        createFakeData();
        loadImageViewPager();

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
        categoryIcons.add(new CategoryIcon("Movie", R.drawable.ic_film_reel));
        categoryIcons.add(new CategoryIcon("Art", R.drawable.ic_masks));
        categoryIcons.add(new CategoryIcon("Sports", R.drawable.ic_archery));
        categoryIcons.add(new CategoryIcon("Nightlife", R.drawable.ic_wine_glass));
        categoryIcons.add(new CategoryIcon("Kids", R.drawable.ic_boy));
        categoryIcons.add(new CategoryIcon("Expo", R.drawable.ic_exhibition));
        mIconAdapter = new IconAdapter(MainActivity.this, categoryIcons);

        gridView.setAdapter(mIconAdapter);
    }

    private void loadImageViewPager() {
        // Add the events to the view pager
        ViewPager viewPager = (ViewPager) findViewById(R.id.event_view_pager);

        mPagerAdapter = new EventImagePagerAdapter(getSupportFragmentManager(), mEventImages);
        viewPager.setAdapter(mPagerAdapter);

        // Create the dot indicators
        LinearLayout pagerIndicator = (LinearLayout) findViewById(R.id.pager_indicator);
        // TODO might need to add background color
        pagerIndicator.removeAllViewsInLayout();
        mDotsIndicator = new ImageView[mEventImages.size()];

        for (int i = 0; i < mEventImages.size(); ++i) {
            mDotsIndicator[i] = new ImageView(MainActivity.this);
            if (i == 0)
                mDotsIndicator[i].setImageResource(R.drawable.selected_item_dot);
            else
                mDotsIndicator[i].setImageResource(R.drawable.unselected_item_dot);
            LinearLayout.LayoutParams params = new
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            params.setMargins(8, 0, 8, 0);
            pagerIndicator.addView(mDotsIndicator[i], params);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < mEventImages.size(); ++i)
                    if (i == position)
                        mDotsIndicator[i].setImageResource(R.drawable.selected_item_dot);
                    else
                        mDotsIndicator[i].setImageResource(R.drawable.unselected_item_dot);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void createFakeData() {
        mEventImages = new ArrayList<>();
        mEventImages.add(new EventImage(R.mipmap.boat_people, "Sep 15", "Boat People"));
        mEventImages.add(new EventImage(R.mipmap.free_baby_event_cradle_of_love, "Sep 18",
                "Free Baby Event Cradle Of Love"));
        mEventImages.add(new EventImage(R.mipmap.free_yoga_and_tea_session_with_dr_trish_corley,
                "Sep 15", "Free Yoga & Tea Session with Dr. Trish Corley"));
    }

    public void removeEventImage(int position) {
        EventImage eventImage = mEventImages.get(position);
        eventImage.setRemoved(true);
        Toast.makeText(this, String.format("%s is removed.", eventImage.getTitle()),
                Toast.LENGTH_SHORT).show();
        mEventImages.remove(position);
        loadImageViewPager();
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
            color = getResources().getColor(R.color.lightRed);
            defaultHeight = mGarbageIconDefaultHeight;
            changeIconGraduallyHelper(iconImage, color, defaultHeight, scale);
        } else if (dy < -10) {
            iconImage = (ImageView) findViewById(R.id.star_image);
            color = getResources().getColor(R.color.lightGreen);
            defaultHeight = mStarIconDefaultHeight;
            changeIconGraduallyHelper(iconImage, color, defaultHeight, scale);
        }
    }

    private void changeIconGraduallyHelper(ImageView iconImage, int color, int defaultHeight, float scale) {
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

        iconImage.setBackgroundColor(TRANSPARENT_COLOR);
    }
}
