//import android.Manifest;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.os.Looper;
//import android.util.Log;
//
//import androidx.core.app.ActivityCompat;
//
//import com.example.bouncermodule.GPSPoint;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationResult;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.LocationSettingsRequest;
//
///**
// * Uses Google Play API for obtaining device locations
// * Created by alejandro.tkachuk
// * alejandro@calculistik.com
// * www.calculistik.com Mobile Development
// */
//
//public class Wherebouts extends Context {
//
//    private static final Wherebouts instance = new Wherebouts();
//
//    private static final String TAG = Wherebouts.class.getSimpleName();
//
//    private FusedLocationProviderClient mFusedLocationClient;
//    private LocationCallback locationCallback;
//    private LocationRequest locationRequest;
//    private LocationSettingsRequest locationSettingsRequest;
//
//    private Workable<GPSPoint> workable;
//
//    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
//    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
//
//    private Wherebouts() {
//        this.locationRequest = new LocationRequest();
//        this.locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
//        this.locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
//        this.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//        builder.addLocationRequest(this.locationRequest);
//        this.locationSettingsRequest = builder.build();
//
//        this.locationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                super.onLocationResult(locationResult); // why? This is ridiculous, Android!
//                Location currentLocation = locationResult.getLastLocation();
//
//                GPSPoint gpsPoint = new GPSPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
//                Log.i(TAG, "Location Callback results: " + gpsPoint);
//                if (null != workable)
//                    workable.work(gpsPoint);
//            }
//        };
//
//        this.mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MainApplication.getAppContext());
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return TODO;
//        }
//        this.mFusedLocationClient.requestLocationUpdates(this.locationRequest, this.locationCallback, Looper.myLooper());
//    }
//
//    public static Wherebouts instance() {
//        return instance;
//    }
//
//    public void onChange(Workable<GPSPoint> workable) {
//        this.workable = workable;
//    }
//
//    public LocationSettingsRequest getLocationSettingsRequest() {
//        return this.locationSettingsRequest;
//    }
//
//    public void stop() {
//        Log.i(TAG, "stop() Stopping location tracking");
//        this.mFusedLocationClient.removeLocationUpdates(this.locationCallback);
//    }
//
//}