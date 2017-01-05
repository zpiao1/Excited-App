package com.example.zpiao1.excited.views;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.zpiao1.excited.BuildConfig;
import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.data.Event;
import com.example.zpiao1.excited.server.IEventRequest;
import com.example.zpiao1.excited.server.IUserRequest;
import com.example.zpiao1.excited.server.LikesOrDislikesResponse;
import com.example.zpiao1.excited.server.ServerUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.Duration;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.joda.time.DateTime;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class EventDetailActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = EventDetailActivity.class.getSimpleName();

    private static int PERMISSIONS_REQUEST_FINE_LOCATION = 0;

    private MapView mMapView;
    private GoogleApiClient mGoogleApiClient = null;
    private GoogleMap mGoogleMap = null;
    private LatLng mCurLatLng;
    private LatLng mDestLatLng;
    private GeoApiContext mContext;

    private ImageView mDetailEventImage;
    private TextView mDetailCategory;
    private TextView mDetailDate;
    private TextView mDetailVenue;
    private TextView mDetailDrivingTime;
    private TextView mDetailTransitTime;
    private CollapsingToolbarLayout mCollapsingToolbar;

    private TextView mDislikesView;
    private TextView mLikesView;

    private CompositeDisposable mDisposable;

    private boolean mMapLoaded = false;
    private boolean mEventGotten = false;
    private boolean mGoogleApiConnected = false;

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

        mContext = new GeoApiContext()
                .setApiKey(getString(R.string.google_maps_service_api_key));

        // Setup Google API client
        buildGoogleApiClient();

        // Initialize the views
        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mDetailEventImage = (ImageView) findViewById(R.id.detail_event_image);
        mDetailCategory = (TextView) findViewById(R.id.detail_category);
        mDetailDate = (TextView) findViewById(R.id.detail_date);
        mDetailVenue = (TextView) findViewById(R.id.detail_venue);
        mDetailDrivingTime = (TextView) findViewById(R.id.detail_driving_time);
        mDetailTransitTime = (TextView) findViewById(R.id.detail_transit_time);
        mMapView = (MapView) findViewById(R.id.map);


        // Setup the Google Map View
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        mDisposable = new CompositeDisposable();
        getEvent();

        getEstimatedTime();
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
        ActionItemBadge.update(this,
                menu.findItem(R.id.action_likes),
                new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_star)
                        .sizeDp(24)
                        .color(Color.WHITE),
                ActionItemBadge.BadgeStyles.RED.getStyle(),
                Integer.MIN_VALUE,  // hide the badge
                new ActionItemBadge.ActionItemBadgeListener() {
                    @Override
                    public boolean onOptionsItemSelected(MenuItem menu) {
                        Log.d(TAG, "likes selected");
                        getLikes();
                        return true;
                    }
                });
        ActionItemBadge.update(this,
                menu.findItem(R.id.action_dislikes),
                new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_delete)
                        .color(Color.WHITE)
                        .sizeDp(24),
                ActionItemBadge.BadgeStyles.RED.getStyle(),
                Integer.MIN_VALUE,  // hide the badge
                new ActionItemBadge.ActionItemBadgeListener() {
                    @Override
                    public boolean onOptionsItemSelected(MenuItem menu) {
                        Log.d(TAG, "dislikes selected");
                        return true;
                    }
                });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.d(TAG, "action_settings selected");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMapLoaded = true;
                tryToGetEstimatedTime();
                tryToSetupMapUi();
            }
        });
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
        checkPermissionAndConnect();
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
        mDisposable.clear();
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

    private void checkPermissionAndConnect() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mGoogleApiClient.connect();
        } else {
            Log.v(TAG, "onStart(): Permission denied");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_FINE_LOCATION);
        }
    }

    @Override
    public synchronized void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices
                    .FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            if (lastLocation != null) {
                mCurLatLng = new
                        LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                Log.d(TAG, "mCurLatLng: " + mCurLatLng.lat + mCurLatLng.lng);
                mGoogleApiConnected = true;
                tryToGetEstimatedTime();
                tryToSetupMapUi();
            } else {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(this,
                            "No location detected. Use fake location instead",
                            Toast.LENGTH_LONG).show();
                    mCurLatLng = new LatLng(1.350436, 103.685065);
                }
                mGoogleApiConnected = true;
                tryToGetEstimatedTime();
                tryToSetupMapUi();
            }

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_FINE_LOCATION)
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mGoogleApiClient.connect();
                Toast.makeText(this, "onRequestPermissionResult(): Permission granted!",
                        Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "onRequestPermissionResult(): Permission denied!",
                        Toast.LENGTH_SHORT).show();
    }

    private void getEvent() {
        String id = getIntent().getStringExtra("_id");
        IEventRequest request = ServerUtils.getRetrofit()
                .create(IEventRequest.class);
        ServerUtils.addToDisposable(mDisposable,
                request.getEvent(id),
                new Consumer<Event>() {
                    @Override
                    public synchronized void accept(Event event) throws Exception {
                        // Set up the UI
                        mCollapsingToolbar.setTitle(event.title);
                        mDetailCategory.setText(event.category);
                        mDetailDate.setText(event.date);
                        mDetailVenue.setText(event.venue);
                        Glide.with(EventDetailActivity.this)
                                .load(event.pictureUrl)
                                .centerCrop()
                                .into(mDetailEventImage);
                        if (event.lat != null && event.lng != null)
                            mDestLatLng = new LatLng(event.lat, event.lng);
                        else
                            mDestLatLng = null;
                        mEventGotten = true;
                        tryToGetEstimatedTime();
                        tryToSetupMapUi();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "getEvent()", throwable);
                    }
                });
    }

    private void tryToGetEstimatedTime() {
        Log.d(TAG, "tryToGetEstimatedTime: " +
                ((mEventGotten && mGoogleApiConnected && mMapLoaded) ? "Can" : "Cannot"));
        if (mEventGotten && mGoogleApiConnected && mMapLoaded)
            getEstimatedTime();
    }

    private void getEstimatedTime() {
        if (mDestLatLng != null) {
            requestEstimatedTime(TravelMode.DRIVING);
            requestEstimatedTime(TravelMode.TRANSIT);
        }
    }

    private void requestEstimatedTime(final TravelMode mode) {
        DistanceMatrixApi.newRequest(mContext)
                .origins(mCurLatLng)
                .destinations(mDestLatLng)
                .mode(mode)
                .departureTime(DateTime.now())
                .setCallback(new PendingResult.Callback<DistanceMatrix>() {
                    @Override
                    public synchronized void onResult(DistanceMatrix result) {
                        DistanceMatrixRow[] rows = result.rows;
                        final DistanceMatrixElement element = rows[0].elements[0];
                        final Duration duration = element.duration;
                        final Duration durationInTraffic = element.durationInTraffic;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mode == TravelMode.DRIVING) {
                                    if (durationInTraffic != null)
                                        mDetailDrivingTime.setText(durationInTraffic.humanReadable);
                                    else if (duration != null)
                                        mDetailDrivingTime.setText(duration.humanReadable);
                                } else if (mode == TravelMode.TRANSIT) {
                                    if (duration != null)
                                        mDetailTransitTime.setText(duration.humanReadable);
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e(TAG, "requestEstimatedTime", e);
                    }
                });
    }

    private void tryToSetupMapUi() {
        if (mGoogleApiConnected && mMapLoaded && mEventGotten) {
            setupMapUi();
        }
    }

    private void setupMapUi() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        com.google.android.gms.maps.model.LatLng destLatLng, curLatLng =
                new com.google.android.gms.maps.model.LatLng(mCurLatLng.lat, mCurLatLng.lng);
        if (mDestLatLng != null) {
            destLatLng =
                    new com.google.android.gms.maps.model.LatLng(mDestLatLng.lat, mDestLatLng.lng);
            builder.include(destLatLng);
            mGoogleMap.addMarker(new MarkerOptions()
                    .title("Destination")
                    .position(destLatLng));
        }

        builder.include(curLatLng);
        mGoogleMap.addMarker(new MarkerOptions()
                .title("Current Location")
                .position(curLatLng));

        CameraUpdate cameraUpdate =
                CameraUpdateFactory.newLatLngBounds(builder.build(), 100);
        mGoogleMap.animateCamera(cameraUpdate);
    }


    private void updateActionIconCount(TextView countView, String selection,
                                       String[] selectionArgs) {
    }

    private void getLikes() {
        Log.d(TAG, "getLikes");
        SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_pref_name_server),
                Context.MODE_PRIVATE);
        // Look up the token and id
        String token = prefs.getString(getString(R.string.pref_token_key), null);
        String id = prefs.getString(getString(R.string.pref_id_key), null);
        if (token == null || id == null) {
            Toast.makeText(this, "You must login first!", Toast.LENGTH_SHORT).show();
        } else {
            IUserRequest request = ServerUtils.getRetrofit()
                    .create(IUserRequest.class);
            ServerUtils.addToDisposable(mDisposable,
                    request.getLikes(id, token),
                    new Consumer<LikesOrDislikesResponse>() {
                        @Override
                        public void accept(LikesOrDislikesResponse likesOrDislikesResponse) throws Exception {
                            String response = TextUtils.join(" ", likesOrDislikesResponse.events);
                            Log.d(TAG, response);
                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e(TAG, "getLikes", throwable);
                        }
                    });
        }
    }
}