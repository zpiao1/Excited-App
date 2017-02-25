package com.example.zpiao1.excited.views;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.data.Event;
import com.example.zpiao1.excited.server.IEventRequest;
import com.example.zpiao1.excited.server.ServerUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
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
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;
import org.joda.time.DateTime;

public class EventDetailFragment extends Fragment {

  private static final String TAG = EventDetailFragment.class.getSimpleName();
  private static int PERMISSIONS_REQUEST_FINE_LOCATION = 0;

  private String mId;

  private GoogleApiClient mClient;
  private MapView mMapView;

  private ReplaySubject<Object> mPermissionSubject;
  private ReplaySubject<View> mRootViewSubject;
  private ReplaySubject<Menu> mMenuSubject;

  public static EventDetailFragment newInstance(String id) {
    EventDetailFragment fragment = new EventDetailFragment();
    fragment.mId = id;
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    mClient = new GoogleApiClient.Builder(getContext())
        .addApi(LocationServices.API)
        .build();

    mRootViewSubject = ReplaySubject.create();
    mPermissionSubject = ReplaySubject.create();
    mMenuSubject = ReplaySubject.create();
    Observable<Object> clientConnectedObservable = getClientConnectedObservable(mClient);
    ConnectableObservable<Event> eventObservable = getEventObservable(mId);

    ConnectableObservable<Object> currentLatLngObservable = getCurrentLatLngObservable(
        clientConnectedObservable,
        mPermissionSubject,
        mClient);
    ConnectableObservable<Object> destinationLatLngObservable =
        getDestinationLatLngSubject(eventObservable);

    Observable<GoogleMap> mapObservable = getGoogleMapObservable(mRootViewSubject);

    Observable<Object> mapLoadedObservable = mapObservable.flatMap(this::getMapLoadedObservable);

    getMapViewSetupBundleObservable(currentLatLngObservable,
        destinationLatLngObservable,
        mapObservable,
        mapLoadedObservable)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::updateMapViewUI);

    GeoApiContext context = new GeoApiContext().setApiKey(getString(R.string
        .google_maps_service_api_key));
    Observable<String> transitEstimatedTimeObservable = getEstimatedTimeObservable(
        currentLatLngObservable,
        destinationLatLngObservable,
        context,
        TravelMode.TRANSIT);
    Observable<String> drivingEstimatedTimeObservable = getEstimatedTimeObservable(
        currentLatLngObservable,
        destinationLatLngObservable,
        context,
        TravelMode.DRIVING);

    getBasicUISetupObservable(eventObservable, mRootViewSubject)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::updateBasicUI);

    getEstimatedTimeUISetupObservable(transitEstimatedTimeObservable, mRootViewSubject)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> updateEstimatedTimeUI(pair, R.id.detail_transit_time));

    getEstimatedTimeUISetupObservable(drivingEstimatedTimeObservable, mRootViewSubject)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> updateEstimatedTimeUI(pair, R.id.detail_driving_time));

    eventObservable.connect();
    currentLatLngObservable.connect();
    destinationLatLngObservable.connect();

    mClient.connect();
    if (hasLocationPermission(getContext())) {
      mPermissionSubject.onNext(new Object());
      mPermissionSubject.onComplete();
    } else {
      requestLocationPermission(getActivity());
    }
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_event_detail, container, false);

    Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
    MainActivity mainActivity = (MainActivity) getActivity();
    mainActivity.setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
    Observable.<View>create(source -> fab.setOnClickListener(view -> {
      source.onNext(view);
      source.onComplete();
    }))
        .subscribe(view -> Snackbar.make(view, "Replace with your own action",
            Snackbar.LENGTH_SHORT)
            .setAction("Action", null)
            .show());

    DrawerLayout drawer = (DrawerLayout) mainActivity.findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        mainActivity,
        drawer,
        toolbar,
        R.string.navigation_drawer_open,
        R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    mMapView = (MapView) rootView.findViewById(R.id.map);
    mMapView.onCreate(savedInstanceState);

    mRootViewSubject.onNext(rootView);
    mRootViewSubject.onComplete();

    return rootView;
  }

  private Observable<Object> getClientConnectedObservable(GoogleApiClient client) {
    return Observable.create(source -> {
      client.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
          Log.d(TAG, "onConnected");
          source.onNext(new Object());
          source.onComplete();
        }

        @Override
        public void onConnectionSuspended(int i) {
          Log.d(TAG, "onConnectionSuspended");
          client.connect();
        }
      });
      client.registerConnectionFailedListener(result -> source.onError(new Exception
          ("onConnectionFailed: " + result.toString())));
    });
  }

  private ConnectableObservable<Event> getEventObservable(String id) {
    IEventRequest request = ServerUtils.getRetrofit()
        .create(IEventRequest.class);
    return request.getEvent(id)
        .subscribeOn(Schedulers.io())
        .replay();
  }

  private ConnectableObservable<Object> getCurrentLatLngObservable(
      Observable<Object> googleApiClientObservable,
      Observable<Object> locationPermissionObservable,
      GoogleApiClient client) {
    return Observable.zip(googleApiClientObservable,
        locationPermissionObservable,
        (observable1, observable2) -> {
          if (ContextCompat.checkSelfPermission(getContext(),
              Manifest.permission.ACCESS_FINE_LOCATION) ==
              PackageManager.PERMISSION_GRANTED) {
            Location location = LocationServices
                .FusedLocationApi
                .getLastLocation(client);
            if (location != null) {
              Log.d(TAG, "current: latitude: " + location.getLatitude());
              Log.d(TAG, "current: longitude: " + location.getLongitude());
              return new LatLng(location.getLatitude(),
                  location.getLongitude());
            } else {
              Log.d(TAG, "no current location");
              return new Object();
            }
          } else {
            return new Object();
          }
        })
        .replay();
  }

  private ConnectableObservable<Object> getDestinationLatLngSubject(
      Observable<Event> eventObservable) {
    return eventObservable.map(event -> {
      if (event.lat != null && event.lng != null) {
        Log.d(TAG, "destination: latitude: " + event.lat);
        Log.d(TAG, "destination: longitude: " + event.lng);
        return new LatLng(event.lat, event.lng);
      } else {
        return new Object();
      }
    })
        .replay();
  }

  private Observable<Pair<Event, View>> getBasicUISetupObservable(
      Observable<Event> eventObservable,
      Observable<View> viewObservable) {
    return Observable.zip(eventObservable, viewObservable, Pair::new);
  }

  private Observable<GoogleMap> getGoogleMapObservable(Observable<View> viewObservable) {
    return viewObservable.map(view -> (MapView) view.findViewById(R.id.map))
        .flatMap(mapView -> Observable.create(source -> mapView
            .getMapAsync(googleMap -> {
              source.onNext(googleMap);
              source.onComplete();
            })));
  }

  private Observable<Object> getMapLoadedObservable(GoogleMap googleMap) {
    return Observable.create(source -> googleMap.setOnMapLoadedCallback(() -> {
      source.onNext(new Object());
      source.onComplete();
    }));
  }

  private Observable<String> getEstimatedTimeObservable(
      Observable<Object> currentLatLngObservable,
      Observable<Object> destinationLatLngObservable,
      GeoApiContext context,
      TravelMode mode) {
    return Observable.zip(currentLatLngObservable, destinationLatLngObservable, Pair::new)
        .flatMap(pair -> {
          if (!(pair.first instanceof LatLng && pair.second instanceof LatLng)) {
            return Observable.just("Not available");
          } else {
            LatLng origin = (LatLng) pair.first;
            LatLng destination = (LatLng) pair.second;
            return Observable.create(source -> DistanceMatrixApi.newRequest(context)
                .origins(origin)
                .destinations(destination)
                .mode(mode)
                .departureTime(DateTime.now().plus(2 * 60 * 1000L)) // 2 minutes later
                .setCallback(new PendingResult.Callback<DistanceMatrix>() {
                  @Override
                  public void onResult(DistanceMatrix result) {
                    DistanceMatrixRow[] rows = result.rows;
                    DistanceMatrixElement element = rows[0].elements[0];
                    Duration duration = element.duration;
                    Duration durationInTraffic = element.durationInTraffic;
                    if (mode == TravelMode.DRIVING && durationInTraffic != null) {
                      source.onNext(durationInTraffic.humanReadable);
                    } else if (duration != null) {
                      source.onNext(duration.humanReadable);
                    } else {
                      source.onNext("Not Available");
                    }
                    source.onComplete();
                  }

                  @Override
                  public void onFailure(Throwable e) {
                    source.onError(e);
                  }
                }));
          }
        });
  }

  private Observable<Pair<String, View>> getEstimatedTimeUISetupObservable(
      Observable<String> estimatedTimeObservable,
      Observable<View> viewObservable) {
    return Observable.zip(estimatedTimeObservable, viewObservable, Pair::new);
  }

  private Observable<Object[]> getMapViewSetupBundleObservable(
      Observable<Object> currentLatLngObservable,
      Observable<Object> destinationLatLngObservable,
      Observable<GoogleMap> googleMapObservable,
      Observable<Object> mapLoadedObservable) {
    return Observable.zip(currentLatLngObservable,
        destinationLatLngObservable,
        googleMapObservable,
        mapLoadedObservable,
        (currentLatLng, destinationLatLng, googleMap, object) -> new Object[]
            {currentLatLng, destinationLatLng, googleMap});
  }

  private boolean hasLocationPermission(Context context) {
    return ContextCompat.checkSelfPermission(context,
        Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED;
  }

  private void requestLocationPermission(Activity activity) {
    ActivityCompat.requestPermissions(activity,
        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
        PERMISSIONS_REQUEST_FINE_LOCATION);
  }

  private void updateBasicUI(Pair<Event, View> pair) {
    Event event = pair.first;
    View view = pair.second;
    ((CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar))
        .setTitle(event.title);
    ((TextView) view.findViewById(R.id.detail_category))
        .setText(event.category);
    ((TextView) view.findViewById(R.id.detail_date))
        .setText(event.date);
    ((TextView) view.findViewById(R.id.detail_venue))
        .setText(event.venue);
    Glide.with(getActivity())
        .load(event.pictureUrl)
        .centerCrop()
        .into((ImageView) view.findViewById(R.id.detail_event_image));
  }

  private void updateMapViewUI(Object[] array) {
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    com.google.android.gms.maps.model.LatLng destinationLatLng = null,
        currentLatLng = null;
    GoogleMap googleMap = (GoogleMap) array[2];

    if (array[1] instanceof LatLng) {
      LatLng destLatLng = (LatLng) array[1];
      destinationLatLng = new com.google.android.gms.maps.model.LatLng(
          destLatLng.lat, destLatLng.lng);
      builder.include(destinationLatLng);
      googleMap.addMarker(new MarkerOptions()
          .title("Destination")
          .position(destinationLatLng));
    }

    if (array[0] instanceof LatLng) {
      LatLng curLatLng = (LatLng) array[0];
      currentLatLng = new com.google.android.gms.maps.model.LatLng(
          curLatLng.lat, curLatLng.lng);
      builder.include(currentLatLng);
      googleMap.addMarker(new MarkerOptions()
          .title("Current Location")
          .position(currentLatLng));
    }

    if (destinationLatLng != null || currentLatLng != null) {
      CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder
          .build(), 100);
      googleMap.animateCamera(cameraUpdate);
    }
  }

  private void updateEstimatedTimeUI(Pair<String, View> pair, int textViewId) {
    String estimatedTime = pair.first;
    View rootView = pair.second;
    ((TextView) rootView.findViewById(textViewId))
        .setText(estimatedTime);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (requestCode == PERMISSIONS_REQUEST_FINE_LOCATION) {
      if (grantResults.length > 0 &&
          grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        mPermissionSubject.onNext(new Object());
        mPermissionSubject.onComplete();
      } else {
        mPermissionSubject.onError(new Exception("Permission Denied"));
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    mMapView.onResume();
  }

  @Override
  public void onStart() {
    super.onStart();
    mMapView.onStart();
    mClient.connect();
  }

  @Override
  public void onStop() {
    super.onStop();
    mMapView.onStop();
    if (mClient.isConnected()) {
      mClient.disconnect();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mMapView.onDestroy();
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

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.event_detail, menu);

    View interestedView = menu.findItem(R.id.action_interested).getActionView();
    ImageView interestedIcon = (ImageView) interestedView.findViewById(R.id.menu_badge_icon);
    interestedIcon.setImageResource(R.drawable.ic_star);
    interestedIcon.setMaxWidth(24);
    interestedIcon.setMaxHeight(24);
    interestedView.findViewById(R.id.menu_badge).setVisibility(View.GONE);

    View uninterestedView = menu.findItem(R.id.action_uninterested).getActionView();
    ImageView uninterestedIcon = (ImageView) uninterestedView.findViewById(
        R.id.menu_badge_icon);
    uninterestedIcon.setImageResource(R.drawable.ic_garbage);
    uninterestedIcon.setMaxHeight(24);
    uninterestedIcon.setMaxHeight(24);
    uninterestedView.findViewById(R.id.menu_badge).setVisibility(View.GONE);

    mMenuSubject.onNext(menu);
    mMenuSubject.onComplete();
  }
}
