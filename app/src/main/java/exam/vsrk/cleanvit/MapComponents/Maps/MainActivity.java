package exam.vsrk.cleanvit.MapComponents.Maps;

/**
 * Created by VSRK on 12/31/2015.
 */

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;

import exam.vsrk.cleanvit.MapComponents.Maps.DistanceNotifications.LocationService;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.snapshot.DoubleNode;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exam.vsrk.cleanvit.MapComponents.Maps.FireBaseUI.AllSpotsActivity;
import exam.vsrk.cleanvit.R;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CONTACTS;
import static exam.vsrk.cleanvit.MapComponents.Maps.AppController.mAuthData;
import static exam.vsrk.cleanvit.MapComponents.Maps.AppController.mFirebaseRef;


public class MainActivity extends AppCompatActivity implements GeoQueryEventListener,
        GoogleMap.OnCameraChangeListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener, SlidingUpPanelLayout.PanelSlideListener, GoogleMap.OnMapClickListener, View.OnClickListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {


    private static final int REQUEST_ACCESS_COARSE_LOCATION = 0;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final LatLng NEBOUND = new LatLng(12.976415, 79.165004);
    private static final LatLng SWBOUND = new LatLng(12.968218, 79.155284);
    private static final LatLngBounds MAPBOUNDARY = new LatLngBounds(SWBOUND, NEBOUND);
    public static int POSITION_REDIRECT_CODE = 9;
    private LatLng lastCenter = new LatLng(12.971883, 79.159145);


    private static GeoLocation INITIAL_CENTER = new GeoLocation(12.971883, 79.159145);
    private static final int INITIAL_ZOOM_LEVEL = 19;
    private static final String GEO_FIRE_REF = "https://radiant-inferno-7381.firebaseio.com/markers/";

    private boolean mRequestingLocationUpdates;
    private String tempAddSpotDes = null;
    private String tempMarkerId = null;

    private GoogleMap map;
    private Circle searchCircle;
    private GeoFire geoFire;
    private GeoQuery geoQuery;

    private FloatingActionButton myLocationFab, markerFab;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private FrameLayout mainLayout;
    private LinearLayout panelAddMarker, panelViewMarker;

    private EditText addSpotDescription;
    private Button addSpotConfirmButton;

    Location mLastLocation;

    private Map<String, Spot> markers;
    boolean mBounded;
    LocationService mServer;


    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup map and camera position
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        this.map = mapFragment.getMap();

        Firebase.setAndroidContext(this);
        //setActionBar((Toolbar)findViewById(R.id.toolbar));

        // setup GeoFire
        this.geoFire = new GeoFire(new Firebase(GEO_FIRE_REF));
        LatLng latLngCenter = new LatLng(INITIAL_CENTER.latitude, INITIAL_CENTER.longitude);
        this.searchCircle = this.map.addCircle(new CircleOptions().center(latLngCenter).radius(1000));
        this.searchCircle.setFillColor(Color.argb(66, 181, 63, 65));
        this.searchCircle.setStrokeColor(Color.argb(155, 181, 63, 65));
        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngCenter, INITIAL_ZOOM_LEVEL));
        this.map.setOnCameraChangeListener(this);
        this.map.setOnMarkerDragListener(this);
        this.map.setOnMapLongClickListener(this);
        this.map.setOnMapClickListener(this);
        this.map.setInfoWindowAdapter(this);
        this.map.setOnInfoWindowClickListener(this);
        // radius in km
        this.geoQuery = this.geoFire.queryAtLocation(INITIAL_CENTER, 1);
        this.geoQuery.addGeoQueryEventListener(this);
        // setup markers
        this.markers = new HashMap<String, Spot>();
        mainLayout = (FrameLayout) findViewById(R.id.main_layout);
        panelAddMarker = (LinearLayout) findViewById(R.id.panel_add_marker);
        panelViewMarker = (LinearLayout) findViewById(R.id.panel_view_marker);

        addSpotDescription = (EditText) findViewById(R.id.spot_add_description);
        addSpotConfirmButton = (Button) findViewById(R.id.spot_add_confirm_button);
        addSpotConfirmButton.setOnClickListener(this);

        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setPanelSlideListener(this);
        myLocationFab = (FloatingActionButton) findViewById(R.id.my_location_fab);
        myLocationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

                    startLocationUpdates();

                }
            }
        });

        markerFab = (FloatingActionButton) findViewById(R.id.mark_spot_fab);
        markerFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage("Tap for long a point on map which you wish to mark as dirty spot");
            }
        });


        buildGoogleApiClient();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // remove all event listeners to stop updating in the background
        if (geoQuery != null)
            this.geoQuery.removeAllListeners();
        for (Spot spot : this.markers.values()) {
            spot.getMarker().remove();
        }
        this.markers.clear();
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // add an event listener to start updating locations again

        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onresume", "outif");
        if (mGoogleApiClient != null)
            if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
                Log.d("onresume", "inif");
                startLocationUpdates();

            }
        if (mLastLocation != null) {
            Log.d("onresume", "locnotnull");
            geoQuery.removeAllListeners();
            geoQuery = geoFire.queryAtLocation(geoQuery.getCenter(), geoQuery.getRadius());
            geoQuery.addGeoQueryEventListener(this);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();

        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        try {


            if (mGoogleApiClient != null) {

                LocationServices.FusedLocationApi.removeLocationUpdates(
                        mGoogleApiClient, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onKeyEntered(final String key, GeoLocation location) {
        // Add a new marker to the map
        Log.d("keyEntered", key);
        final Marker marker = this.map.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)));
        marker.setDraggable(true);
        mFirebaseRef.child("markers/" + key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String owner = (String) dataSnapshot.child("owner").getValue();
                final String status = (String) dataSnapshot.child("status").getValue();
                final String description = (String) dataSnapshot.child("description").getValue();
                final String place = (String) dataSnapshot.child("place").getValue();
                String cleanedBy = null;
                try {
                    if (status.equals(Spot.SPOT_CLEANED)) {
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        cleanedBy = (String) dataSnapshot.child("cleanedBy").getValue();
                    }
                    if (owner.equals(mAuthData.getUid())) {
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                    }
                } catch (NullPointerException exc) {
                    exc.printStackTrace();
                }
                final String finalCleanedBy = cleanedBy;
                mFirebaseRef.child("users/" + owner).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            String displayName = (String) dataSnapshot.child("displayName").getValue();
                            Log.d("displayName", displayName);
                            marker.setTitle(displayName);
                            Spot s = new Spot(key, marker, owner, displayName, null);
                            s.setDescription(description);
                            s.setStatus(status);
                            s.setCleanedBy(finalCleanedBy);
                            s.setPlace(place);
                            markers.put(key, s);
                        } catch (NullPointerException exc) {
                            exc.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.d("FbError->users/owner", firebaseError.getMessage());
                        marker.setTitle("Unknown");
                        Spot s = new Spot(key, marker, mAuthData.getUid(), "Unknown", null);
                        markers.put(key, s);
                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("FbError->masters/key", firebaseError.getMessage());
                marker.setTitle("Owner not found");
                Spot s = new Spot(key, marker, mAuthData.getUid(), "Not Found", null);
                markers.put(key, s);

            }
        });

    }

    @Override
    public void onKeyExited(String key) {
        // Remove any old marker
        Log.d("keyExited", key);
        Spot spot = this.markers.get(key);
        if (spot != null) {
            spot.getMarker().remove();
            this.markers.remove(key);
        }
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        // Move the marker
        Log.d("marker_moved", "key:" + key);
        Spot spot = this.markers.get(key);
        if (spot != null) {
            this.animateMarkerTo(spot.getMarker(), location.latitude, location.longitude);
        }
    }

    @Override
    public void onGeoQueryReady() {
    }

    @Override
    public void onGeoQueryError(FirebaseError error) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("There was an unexpected error querying GeoFire: " + error.getMessage())
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Animation handler for old APIs without animation support
    private void animateMarkerTo(final Marker marker, final double lat, final double lng) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long DURATION_MS = 3000;
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final LatLng startPosition = marker.getPosition();
        handler.post(new Runnable() {
            @Override
            public void run() {
                float elapsed = SystemClock.uptimeMillis() - start;
                float t = elapsed / DURATION_MS;
                float v = interpolator.getInterpolation(t);

                double currentLat = (lat - startPosition.latitude) * v + startPosition.latitude;
                double currentLng = (lng - startPosition.longitude) * v + startPosition.longitude;
                marker.setPosition(new LatLng(currentLat, currentLng));

                // if animation is not finished yet, repeat
                if (t < 1) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private double zoomLevelToRadius(double zoomLevel) {
        // Approximation to fit circle into view
        return 16384000 / Math.pow(2, zoomLevel);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        // Update the search criteria for this geoQuery and the circle on the map
        LatLng center = cameraPosition.target;
        LatLngBounds visibleBounds = map.getProjection().getVisibleRegion().latLngBounds;
        if (!MAPBOUNDARY.contains(visibleBounds.northeast) || !MAPBOUNDARY.contains(visibleBounds.southwest)) {
            map.moveCamera(CameraUpdateFactory.newLatLng(lastCenter));
        } else
            lastCenter = center;
        double radius = zoomLevelToRadius(cameraPosition.zoom);
        this.searchCircle.setCenter(lastCenter);
        this.searchCircle.setRadius(radius);
        this.geoQuery.setCenter(new GeoLocation(lastCenter.latitude, lastCenter.longitude));
        // radius in km
        this.geoQuery.setRadius(radius / 1000);


    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mRequestingLocationUpdates = true;
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    public void updateUI() {
        if (mLastLocation != null) {
            double lat = mLastLocation.getLatitude();
            double lng = mLastLocation.getLongitude();

            LatLng latLngCenter = new LatLng(lat, lng);
            this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngCenter, INITIAL_ZOOM_LEVEL));
            this.searchCircle.setCenter(latLngCenter);
            // radius in km
            this.geoQuery.setCenter(new GeoLocation(lat, lng));

            Log.d("Location", " DDD lat: " + lat + ",  longitude: " + lng);

        } else {

            Toast.makeText(this, "Location Not found", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("onconnected", "onconnected");
        if(mayRequestLocation()) {
            if (mLastLocation == null) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                updateUI();
            }

            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.d("onconnecsusus", "onconnectedsus");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.d("onlocation changed", "onlocationchanged");
        mLastLocation = location;

        updateUI();
    }

    public void addSpot(final LatLng location, final String marker) {

        Log.d("addSp", marker);
        /*geoFire.setLocation(String.valueOf(location.hashCode()), new GeoLocation(location.latitude, location.longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, FirebaseError error) {
                Log.d("addSpotCompleted",key);
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                    markers.get(marker).getMarker().remove();
                    markers.remove(marker);
                } else {
                    System.out.println("Location saved on server successfully!");
                    Map<String, Map<String, String>> object = new HashMap<String, Map<String, String>>();
                    Map<String, Object> map = new HashMap<String, Object>();
                    if(mAuthData.getUid()==null){
                        Log.d("mAuthUid","null");
                    }
                    map.put("owner", mAuthData.getUid());
                    //map.put("description", tempAddSpotDes);
                    //object.put(String.valueOf(location.hashCode()),map);
                    mFirebaseRef.child("markers/" + location.hashCode()).updateChildren(map, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            if (firebaseError != null) {
                                Log.d("fireBaseError", firebaseError.getMessage());
                                showMessage("Failed");
                            } else {
                                showMessage("Added");
                            }
                        }
                    });
                }
            }
        });
        */

        geoFire.setLocation(String.valueOf(location.hashCode()), new GeoLocation(location.latitude, location.longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, FirebaseError error) {
                Log.d("completed", key);

            }
        });
        System.out.println("Location saved on server successfully!");
        Map<String, Object> map = new HashMap<String, Object>();
        if (mAuthData.getUid() == null) {
            Log.d("mAuthUid", "null");
        }
        map.put("owner", mAuthData.getUid());
        map.put("description", tempAddSpotDes);
        map.put("status", "dirty");
        String place = null;

        try {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            if (addressList != null) {
                if (addressList.size() != 0) {
                    Address address = addressList.get(0);
                    place = address.getAddressLine(0) + ", " + address.getAddressLine(1) + ", " + address.getAddressLine(2);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (place == null) {
                map.put("place", "Not Provided");
            } else {
                map.put("place", place);
            }


            //object.put(String.valueOf(location.hashCode()),map);
            mFirebaseRef.child("markers/" + location.hashCode()).updateChildren(map, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        Log.d("fireBaseError", firebaseError.getMessage());


                        showMessage("Failed");
                    } else {
                        showMessage("Added");
                    }
                }
            });
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.d("onconnectedfail", "onconnectedfail");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* If a user is currently authenticated, display a logout menu */
        if (AppController.mAuthData != null) {
            getMenuInflater().inflate(R.menu.menu, menu);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.removed_spots) {
            Intent intent = new Intent(MainActivity.this, AllSpotsActivity.class);
            startActivityForResult(intent, POSITION_REDIRECT_CODE);
        }
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Unauthenticate from Firebase and from providers where necessary.
     */
    private void logout() {
        if (AppController.mAuthData != null) {
            /* logout of Firebase */
            mFirebaseRef.unauth();

            /* Update authenticated user and navigate to login screen*/
            mAuthData = null;
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == POSITION_REDIRECT_CODE) {
                Log.d("pop", "pop");
                try {
                    String lat = data.getStringExtra("lat");
                    String lon = data.getStringExtra("lon");
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(lat), Double.parseDouble(lon)), 19));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(final Marker marker) {
        for (final String s : markers.keySet()) {
            final String s1 = s;
            final Spot spot = markers.get(s);
            if (marker.equals(spot.getMarker())) {
                geoFire.setLocation(s, new GeoLocation(marker.getPosition().latitude, marker.getPosition().longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, FirebaseError error) {
                        if (error != null) {
                            showMessage("Failed to update");
                            marker.setPosition(spot.getMarker().getPosition());
                            spot.setMarker(marker);
                            markers.put(s1, spot);

                        } else {
                            showMessage("Location Updated");
                            Map<String, Map<String, String>> object = new HashMap<String, Map<String, String>>();
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("owner", mAuthData.getUid());
                            //object.put(String.valueOf(location.hashCode()),map);
                            mFirebaseRef.child("markers/" + s).updateChildren(map, new Firebase.CompletionListener() {
                                @Override
                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    if (firebaseError != null) {
                                        Log.d("fireBaseError", firebaseError.getMessage());
                                        showMessage("Failed");
                                    } else {
                                        showMessage("Added");
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    public void showMessage(String s) {
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.d("mapLongClick", latLng.toString());
        Marker marker = this.map.addMarker(new MarkerOptions().position(latLng));
        marker.setDraggable(true);
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        marker.setTitle((String) mAuthData.getProviderData().get("email"));
        this.markers.put(String.valueOf(latLng.hashCode()), new Spot(String.valueOf(latLng.hashCode()), marker, mAuthData.getUid(), (String) mAuthData.getProviderData().get("email"), null));
        //addSpot(latLng, String.valueOf(latLng.hashCode()));
        tempMarkerId = String.valueOf(latLng.hashCode());
        panelViewMarker.setVisibility(LinearLayout.GONE);
        panelAddMarker.setVisibility(LinearLayout.VISIBLE);

        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);


    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {

    }

    @Override
    public void onPanelCollapsed(View panel) {
        if (panelAddMarker.getVisibility() == LinearLayout.VISIBLE) {
            if (tempAddSpotDes == null) {
                this.markers.get(tempMarkerId).getMarker().remove();
                this.markers.remove(tempMarkerId);
                tempMarkerId = null;
            } else {
                addSpot(markers.get(tempMarkerId).getMarker().getPosition(), tempMarkerId);
                tempAddSpotDes = null;
                tempMarkerId = null;
            }
        }

        map.getUiSettings().setAllGesturesEnabled(true);
    }

    @Override
    public void onPanelExpanded(View panel) {
        map.getUiSettings().setAllGesturesEnabled(false);
    }

    @Override
    public void onPanelAnchored(View panel) {

        map.getUiSettings().setAllGesturesEnabled(false);
    }

    @Override
    public void onPanelHidden(View panel) {
        map.getUiSettings().setAllGesturesEnabled(true);
    }


    @Override
    public void onMapClick(LatLng latLng) {
        if (slidingUpPanelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED) || slidingUpPanelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.ANCHORED)) {
            Log.d("panel", "isExpanded");
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    @Override
    public void onBackPressed() {
        if (slidingUpPanelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            Log.d("panel", "isExpanded");
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.spot_add_confirm_button:
                if (TextUtils.isEmpty(addSpotDescription.getText())) {
                    addSpotDescription.setError("Enter Description");
                } else {
                    tempAddSpotDes = addSpotDescription.getText().toString();
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
                break;

        }
    }

    @Override
    public View getInfoWindow(Marker marker) {

        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = getLayoutInflater().inflate(R.layout.map_marker_view, null);
        ((TextView) view.findViewById(R.id.tv_lat)).setText(String.valueOf(marker.getPosition().latitude));
        ((TextView) view.findViewById(R.id.tv_lng)).setText(String.valueOf(marker.getPosition().longitude));
        for (Spot s : markers.values()) {
            Marker mk = s.getMarker();
            if (mk.equals(marker)) {
                Log.d("markerEqual", s.getOwnerName());
                ((TextView) view.findViewById(R.id.host)).setText(s.getOwnerName());
                break;
            }
        }
        return view;
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

        panelViewMarker.setVisibility(LinearLayout.VISIBLE);
        panelAddMarker.setVisibility(LinearLayout.GONE);

        for (final Spot s : markers.values()) {
            final Spot s1 = s;
            Marker mk = s.getMarker();
            if (mk.equals(marker)) {
                Log.d("markerEqualInfoWindow", s.getOwnerName());
                ((TextView) findViewById(R.id.marker_latitude)).setText(String.valueOf(marker.getPosition().latitude));
                ((TextView) findViewById(R.id.marker_longitude)).setText(String.valueOf(marker.getPosition().longitude));
                ((TextView) findViewById(R.id.marker_spot_address)).setText(s.getPlace());
                ((TextView) findViewById(R.id.marker_host)).setText(s.getOwnerName());
                ((TextView) findViewById(R.id.marker_description)).setText(s.getDescription());
                if (s.getStatus().equals(Spot.SPOT_CLEANED)) {
                    ((TextView) findViewById(R.id.cleanedByLabel)).setVisibility(TextView.VISIBLE);
                    ((TextView) findViewById(R.id.marker_cleaned_by)).setVisibility(TextView.VISIBLE);
                    ((TextView) findViewById(R.id.marker_cleaned_by)).setText(s1.getCleanedBy());
                    ((Button) findViewById(R.id.spot_cleaned_button)).setVisibility(Button.GONE);
                } else {
                    ((TextView) findViewById(R.id.cleanedByLabel)).setVisibility(TextView.GONE);
                    ((TextView) findViewById(R.id.marker_cleaned_by)).setVisibility(TextView.GONE);
                }
                Log.d("dekhteh", s.getOwnerId() + "*" + mAuthData.getUid());
                if (s.getOwnerId().equals(mAuthData.getUid())) {
                    ((Button) findViewById(R.id.spot_remove_button)).setVisibility(Button.VISIBLE);
                } else {
                    ((Button) findViewById(R.id.spot_remove_button)).setVisibility(Button.GONE);
                }
                ((Button) findViewById(R.id.spot_cleaned_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v("CLEAN_PROGRESS", "Started Cleaning");
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

                        Map<String, Object> updateMap = new HashMap<String, Object>();
                        updateMap.put("status", Spot.SPOT_CLEANED);
                        updateMap.put("cleanedBy", mAuthData.getProviderData().get("email"));
                        mFirebaseRef.child("markers/" + s1.getSpotId()).updateChildren(updateMap, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError != null) {
                                    Log.d("fireBaseError", firebaseError.getMessage());
                                    showMessage("Failed");
                                } else {
                                    showMessage("Marked as clean");
                                }
                            }
                        });
                    }
                });
                ((Button) findViewById(R.id.spot_remove_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        mFirebaseRef.child("markers/" + s1.getSpotId()).removeValue(new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError != null) {
                                    Log.d("fireBaseError", firebaseError.getMessage());
                                    showMessage("Failed");
                                } else {
                                    showMessage("Marker Removed");
                                    marker.remove();
                                    markers.remove(s1);

                                }
                            }
                        });
                    }
                });
                break;
            }
        }
    }

    private boolean mayRequestLocation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)) {
            Snackbar.make(mainLayout, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
                        }
                    });
        } else {
            requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
        }

        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                        Snackbar.make(mainLayout, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                                .setAction(android.R.string.ok, new View.OnClickListener() {
                                    @Override
                                    @TargetApi(Build.VERSION_CODES.M)
                                    public void onClick(View v) {
                                        requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
                                    }
                                });
                    } else {
                        requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
                    }
                }
            }
            if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {

            }
        }
    }
}