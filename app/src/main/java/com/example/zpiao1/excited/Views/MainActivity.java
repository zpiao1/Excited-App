package com.example.zpiao1.excited.views;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.adapters.EventImagePagerAdapter;
import com.example.zpiao1.excited.adapters.IconAdapter;
import com.example.zpiao1.excited.data.EventContract.EventEntry;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    // The indices for each column in EVENT_COLUMNS
    public static final int COL_ROW_ID = 0;
    public static final int COL_IMAGE_ID = 1;
    public static final int COL_DATE = 2;
    public static final int COL_TITLE = 3;
    private static final float Y_CHANGE_THRESHOLD = 300f;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int TRANSPARENT_COLOR = 0x00000000;
    private static final int LOADER_ID = 1;
    // The columns to be projected on
    private static final String[] EVENT_COLUMNS = new String[]{
            EventEntry._ID,
            EventEntry.COLUMN_IMAGE_ID,
            EventEntry.COLUMN_DATE,
            EventEntry.COLUMN_TITLE
    };
    private IconAdapter mIconAdapter;
    private EventImagePagerAdapter mEventImagePagerAdapter;
    private ViewPager mViewPager;

    private ImageView[] mDotsIndicator;
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

        // Add the events to the view pager
        mViewPager = (ViewPager) findViewById(R.id.event_view_pager);
        loadGridView();
        createFakeData();
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

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
        // Create the dot indicators
        LinearLayout pagerIndicator = (LinearLayout) findViewById(R.id.pager_indicator);
        // TODO might need to add background color
        pagerIndicator.removeAllViewsInLayout();
        mDotsIndicator = new ImageView[mEventImagePagerAdapter.getCount()];

        for (int i = 0; i < mEventImagePagerAdapter.getCount(); ++i) {
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

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < mEventImagePagerAdapter.getCount(); ++i)
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
        // Clear the table first
        getContentResolver().delete(EventEntry.CONTENT_URI, null, null);
        // bulkInsert into the database through ContentProvider
        ArrayList<ContentValues> valuesList = new ArrayList<>();

        ContentValues boatPeopleValues = EventEntry.buildContentValues(
                R.mipmap.boat_people,
                "Sep 15",
                "Boat People",
                "7.00pm",
                "9.00pm",
                EventEntry.CATEGORY_ART,
                "8 College Ave W, Singapore 138608");
        ContentValues freeBabyValues = EventEntry.buildContentValues(
                R.mipmap.free_baby_event_cradle_of_love,
                "Sep 18",
                "Free Baby Event Cradle Of Love",
                "1:30pm",
                "6 pm",
                EventEntry.CATEGORY_KIDS,
                "Pickering Street, Singapore 048659");
        ContentValues freeYogaValues = EventEntry.buildContentValues(
                R.mipmap.free_yoga_and_tea_session_with_dr_trish_corley,
                "Sep 15",
                "Free Yoga & Tea Session with Dr. Trish Corley",
                "7:45PM",
                "9.15PM",
                EventEntry.CATEGORY_SPORTS,
                "yoga in common, 10 Petain Rd, Singapore 208089");

        valuesList.add(boatPeopleValues);
        valuesList.add(freeBabyValues);
        valuesList.add(freeYogaValues);

        getContentResolver().bulkInsert(EventEntry.CONTENT_URI,
                valuesList.toArray(new ContentValues[valuesList.size()]));
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

        iconImage.setBackgroundColor(TRANSPARENT_COLOR);
    }

    public void onImageRemoved(Uri uri) {
        // Uri contains the id of the row where is_removed is set to true (1)
        ContentValues values = new ContentValues();
        values.put(EventEntry.COLUMN_IS_REMOVED, EventEntry.BOOLEAN_TRUE);
        // The selection and selection args is given in the ContentProvider since we just update one
        // row
        getContentResolver().update(uri, values, null, null);

        // Show a toast that the image is removed successfully
        String[] projection = new String[]{EventEntry.COLUMN_TITLE};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null)
            throw new RuntimeException("Cursor is null");
        cursor.moveToFirst();
        String title = cursor.getString(0);
        Toast.makeText(this, title + " is removed", Toast.LENGTH_SHORT).show();
        cursor.close();
    }

    public void onImageStarred(Uri uri) {
        // Uri contains the id of the row where is_starred is set to true (1)
        ContentValues values = new ContentValues();
        values.put(EventEntry.COLUMN_IS_STARRED, EventEntry.BOOLEAN_TRUE);
        getContentResolver().update(uri, values, null, null);

        // Show a toast that the image is starred
        String[] projection = new String[]{EventEntry.COLUMN_TITLE};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null)
            throw new RuntimeException("Cursor is null");
        cursor.moveToFirst();
        Log.v(LOG_TAG, "onImageStarred: cursor size: " + cursor.getCount());
        String title = cursor.getString(0);
        Toast.makeText(this, title + " is starred", Toast.LENGTH_SHORT).show();
        cursor.close();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Query the _id, image_id, date and title for events that are not removed
        // SELECT _id, image_id, date, title
        // FROM events
        // WHERE is_removed = 0
        // ORDER BY _id ASC;
        String selection = EventEntry.COLUMN_IS_REMOVED + " = ?";
        String[] selectionArgs = new String[]{Integer.toString(EventEntry.BOOLEAN_FALSE)};
        CursorLoader cursorLoader = new CursorLoader(this,
                EventEntry.CONTENT_URI,
                EVENT_COLUMNS,
                selection,
                selectionArgs,
                null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished: Check the cursor");
        Log.v(LOG_TAG, "_id, image_id, date, title: ");
        while (data.moveToNext()) {
            long id = data.getLong(COL_ROW_ID);
            int imageId = data.getInt(COL_IMAGE_ID);
            String date = data.getString(COL_DATE);
            String title = data.getString(COL_TITLE);
            Log.v(LOG_TAG, Long.toString(id) + ", " + Integer.toString(imageId) + ", " + date + ", " + title);
        }
        if (mEventImagePagerAdapter == null) {
            mEventImagePagerAdapter = new EventImagePagerAdapter(getSupportFragmentManager(), data);
            mViewPager.setAdapter(mEventImagePagerAdapter);
        } else
            mEventImagePagerAdapter.swapCursor(data);
        loadImageViewPager();
    }
//
//    private void requery() {
//        String selection = EventEntry.COLUMN_IS_REMOVED + " = ?";
//        String[] selectionArgs = new String[]{Integer.toString(EventEntry.BOOLEAN_FALSE)};
//        Cursor cursor = getContentResolver().query(
//                EventEntry.CONTENT_URI,
//                EVENT_COLUMNS,
//                selection,
//                selectionArgs,
//                null);
//        if (mEventImagePagerAdapter != null)
//            mEventImagePagerAdapter.swapCursor(cursor);
//    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mEventImagePagerAdapter.swapCursor(null);
    }
}
