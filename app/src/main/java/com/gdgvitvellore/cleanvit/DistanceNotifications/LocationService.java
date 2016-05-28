package com.gdgvitvellore.cleanvit.DistanceNotifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.gdgvitvellore.cleanvit.MainActivity;
import com.google.android.gms.maps.model.LatLng;

import exam.vsrk.cleanvit.R;

public class LocationService extends BroadcastReceiver {

    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private Context con;


    final Firebase ref = new Firebase("https://radiant-inferno-7381.firebaseio.com/");

    GeoFire geoFire = new GeoFire(new Firebase("https://radiant-inferno-7381.firebaseio.com/markers/"));
    GeoQuery geoQuery = geoFire.queryAtLocation(INITIAL_CENTER, 1);


    private static GeoLocation INITIAL_CENTER = new GeoLocation(26.204675, 78.191340);
    LatLng latLngCenter = new LatLng(INITIAL_CENTER.latitude, INITIAL_CENTER.longitude);

    @Override
    public void onReceive(Context context, Intent intent) {
        Firebase.setAndroidContext(context);
        con=context;
        Bundle extras=intent.getExtras();
        Location location=(Location)extras.get("KEY_STATE_CHANGED");
        updateLocation(location);
    }

    private void updateLocation(Location location) {

        final double USER_LATITUDE = location.getLatitude();
        final double USER_LONGITUDE = location.getLongitude();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                double DIRT_LATITUDE = location.latitude;
                double DIRT_LONGITUDE = location.longitude;
                DistanceCalculator calculator = new DistanceCalculator();
                //        calculator.CalculationByDistance((Double)

                double dist = calculator.CalculationByDistance(USER_LATITUDE, DIRT_LATITUDE, USER_LONGITUDE, DIRT_LONGITUDE);
                Log.v("DISTANCE", String.valueOf(dist));
                if (dist > 0.007) {
                    Log.v("NOTIFY_BUILDER", "Notifications building");
                    int icon = R.drawable.ic_launcher;
                    long when = System.currentTimeMillis();
                    NotificationManager nm = (NotificationManager) con.getSystemService(Context.NOTIFICATION_SERVICE);
                    Intent intent = new Intent(con, MainActivity.class);
                    PendingIntent pending = PendingIntent.getActivity(con, 0, intent, 0);
                    Notification notification;

                    notification = new Notification.Builder(con)
                            .setContentTitle("Clean VIT")
                            .setContentText(
                                    "There is a dirt just 5m near your current location Explore out in Clean VIT").setSmallIcon(R.drawable.ic_launcher)
                            .setContentIntent(pending).setWhen(when).setAutoCancel(true)
                            .build();

                    notification.flags |= Notification.FLAG_AUTO_CANCEL;
                    notification.defaults |= Notification.DEFAULT_SOUND;
                    nm.notify(0, notification);

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(FirebaseError error) {

            }
        });


    }

   /* private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(final Location location) {
            Log.e(TAG, "onLocationChanged: " + location);

            // Toast.makeText(getApplicationContext(),String.valueOf(location.getLatitude()),Toast.LENGTH_SHORT).show();
  /*          geoFire.getLocation("-464831238",new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    if (location != null) {
//                        Toast.makeText(LocationService.this,String.valueOf(location.latitude),Toast.LENGTH_SHORT).show();

                        System.out.println(String.format("The location for key %s is [%f,%f]", key, location.latitude, location.longitude));


                    } else {
                        System.out.println(String.format("There is no location for key %s in GeoFire", key));
                    }
                }


                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    System.err.println("There was an error getting the GeoFire location: " + firebaseError);
                }
            });
            */

//        }
/*
        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);


    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        Firebase.setAndroidContext(this);
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

   /* @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public class LocalBinder extends Binder {
        public LocationService getServerInstance() {
            return LocationService.this;
        }
    }*/

}