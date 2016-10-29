package com.example.zpiao1.excited.views;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.data.EventContract.EventEntry;
import com.example.zpiao1.excited.logic.GetTravelTimeTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import static com.example.zpiao1.excited.R.id.map;

public class EventDetailActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = EventDetailActivity.class.getSimpleName();

    // Projection and project indices for the event associated with this Activity
    private static final String[] EVENT_PROJECTION = new String[]{
            EventEntry._ID,
            EventEntry.COLUMN_IMAGE_ID,
            EventEntry.COLUMN_TITLE,
            EventEntry.COLUMN_CATEGORY,
            EventEntry.COLUMN_DATE,
            EventEntry.COLUMN_START_TIME,
            EventEntry.COLUMN_END_TIME,
            EventEntry.COLUMN_VENUE
    };
    private static final int EVENT_INDEX_IMAGE_ID = 1;
    private static final int EVENT_INDEX_TITLE = 2;
    private static final int EVENT_INDEX_CATEGORY = 3;
    private static final int EVENT_INDEX_DATE = 4;
    private static final int EVENT_INDEX_START_TIME = 5;
    private static final int EVENT_INDEX_END_TIME = 6;
    private static final int EVENT_INDEX_VENUE = 7;

    // Projection and project indices for the counts of starred and removed items
    private static final String[] COUNT_PROJECTION = new String[]{
            "COUNT(*)",
    };
    private static final String COUNT_STARRED_SELECTION = EventEntry.COLUMN_IS_STARRED + "=?";
    private static final String[] COUNT_STARRED_SELECTION_ARGS = new String[]{Integer.toString(
            EventEntry.BOOLEAN_TRUE)};
    private static final String COUNT_REMOVED_SELECTION = EventEntry.COLUMN_IS_REMOVED + "=?";
    private static final String[] COUNT_REMOVED_SELECTION_ARGS = new String[]{Integer.toString(
            EventEntry.BOOLEAN_TRUE)};
    private static final int COUNT_INDEX = 0;

    private static int PERMISSIONS_REQUEST_FINE_LOCATION = 0;

    private MapView mMapView;
    private GoogleApiClient mGoogleApiClient = null;
    private Location mLastLocation = null;
    private GoogleMap mGoogleMap = null;
    private LatLng mCurrentLatLng;
    private LatLng mDestinationLatLng;

    private ImageView mDetailEventImage;
    private TextView mDetailTitle;
    private TextView mDetailCategory;
    private TextView mDetailDate;
    private TextView mDetailTime;
    private TextView mDetailVenue;

    private TextView mRemovedCountView;
    private TextView mStarredCountView;

    private Cursor mEventCursor;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                // Update the UI for the destination
                setDestinationLatLng();
                updateMapUI(mDestinationLatLng, "Destination");
                updateTravelTimeUI();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Setup Google API client
        buildGoogleApiClient();

        // Setup the Google Map View
        mMapView = (MapView) findViewById(map);
        mMapView.onCreate(savedInstanceState);

        mMapView.getMapAsync(this);

        // Initialize the view
        mDetailEventImage = (ImageView) findViewById(R.id.detail_event_image);
        mDetailTitle = (TextView) findViewById(R.id.detail_title);
        mDetailCategory = (TextView) findViewById(R.id.detail_category);
        mDetailDate = (TextView) findViewById(R.id.detail_date);
        mDetailTime = (TextView) findViewById(R.id.detail_time);
        mDetailVenue = (TextView) findViewById(R.id.detail_venue);

        // Get data from database
        Uri queryUri = getIntent().getData();
        Log.v(LOG_TAG, "queryUri: " + queryUri);
        mEventCursor = getContentResolver().query(queryUri, EVENT_PROJECTION, null, null, null);
        if (mEventCursor != null)
            mEventCursor.moveToFirst();
        else
            throw new RuntimeException("Query failed, mEventCursor is null");

        // Update the text from the database
        updateBasicUI();
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
        getMenuInflater().inflate(R.menu.event_detail, menu);

        View removedActionView = menu.findItem(R.id.action_removed).getActionView();
        ((ImageView) removedActionView.findViewById(R.id.action_icon)).setImageResource(
                R.drawable.ic_garbage);
        mRemovedCountView = (TextView) removedActionView.findViewById(R.id.count_text);

        View scheduleActionView = menu.findItem(R.id.action_schedule).getActionView();
        ((ImageView) scheduleActionView.findViewById(R.id.action_icon)).setImageResource(
                R.drawable.ic_star);
        mStarredCountView = (TextView) scheduleActionView.findViewById(R.id.count_text);

        updateActionIconCount(mStarredCountView, COUNT_STARRED_SELECTION,
                COUNT_STARRED_SELECTION_ARGS);
        updateActionIconCount(mRemovedCountView, COUNT_REMOVED_SELECTION,
                COUNT_REMOVED_SELECTION_ARGS);
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

    private void tryToRequestPermission() {
        ActivityCompat.requestPermissions(this, new
                        String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSIONS_REQUEST_FINE_LOCATION);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                setUserLatLng();
                updateMapUI(mCurrentLatLng, "Current Location");

            } else
                Toast.makeText(this, "No location detected", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
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

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mGoogleApiClient.connect();
        } else {
            Toast.makeText(this, "onStart(): Permission denied! Try to request the permission",
                    Toast.LENGTH_SHORT).show();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED)
                tryToRequestPermission();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull
            int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_FINE_LOCATION)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mGoogleApiClient.connect();
                Toast.makeText(this, "onRequestPermissionResult(): Permission granted!",
                        Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "onRequestPermissionResult(): Permission denied!",
                        Toast.LENGTH_SHORT).show();
    }

    private void updateMapUI(LatLng latLng, String title) {
        if (latLng == null) {
            Toast.makeText(this, "Unknown Latitude and Longitude", Toast.LENGTH_SHORT).show();
            return;
        }
        mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(title));

        // Move the camera only when both latlngs are ready
        if (mDestinationLatLng != null && mCurrentLatLng != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(mDestinationLatLng);
            builder.include(mCurrentLatLng);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 100);
            mGoogleMap.animateCamera(cameraUpdate);
        }
    }

    private void setUserLatLng() {
        mCurrentLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
    }

    private void setDestinationLatLng() {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        double latitude = 0, longitude = 0;
        String venue = mEventCursor.getString(EVENT_INDEX_VENUE);
        try {
            addresses = geocoder.getFromLocationName(venue, 5);
            latitude = addresses.get(0).getLatitude();
            longitude = addresses.get(0).getLongitude();
        } catch (IndexOutOfBoundsException e) {
            Log.e(LOG_TAG, "No latitude and longitude for address: " + venue + " is found.");
            Toast.makeText(this, "Unknown coordinate for " + venue, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failure in getting destination coordinates", e);
        } finally {
            mDestinationLatLng = new LatLng(latitude, longitude);
        }
    }

    private void updateTravelTimeUI() {
        new GetTravelTimeTask(this).execute(mCurrentLatLng, mDestinationLatLng);
    }

    private void updateBasicUI() {
        int imageId = mEventCursor.getInt(EVENT_INDEX_IMAGE_ID);
        String title = mEventCursor.getString(EVENT_INDEX_TITLE);
        int categoryId = mEventCursor.getInt(EVENT_INDEX_CATEGORY);
        String category = EventEntry.getCatetoryFromId(categoryId);
        String date = mEventCursor.getString(EVENT_INDEX_DATE);
        String startTime = mEventCursor.getString(EVENT_INDEX_START_TIME);
        String endTime = mEventCursor.getString(EVENT_INDEX_END_TIME);
        String venue = mEventCursor.getString(EVENT_INDEX_VENUE);

        String time = startTime + " - " + endTime;
        mDetailEventImage.setImageResource(imageId);
        mDetailTitle.setText(title);
        mDetailCategory.setText(category);
        mDetailDate.setText(date);
        mDetailTime.setText(time);
        mDetailVenue.setText(venue);
    }

    private void updateActionIconCount(TextView countView, String selection,
                                       String[] selectionArgs) {
        Cursor countCursor = getContentResolver().query(
                EventEntry.CONTENT_URI,
                COUNT_PROJECTION,
                selection,
                selectionArgs,
                null);

        if (countCursor != null) {
            countCursor.moveToFirst();
            int count = countCursor.getInt(COUNT_INDEX);
            countView.setText(Integer.toString(count));
            countCursor.close();
        } else
            throw new RuntimeException("updateActionIconCount: countCursor is null");
    }
}