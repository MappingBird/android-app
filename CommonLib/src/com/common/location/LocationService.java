package com.common.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.hlrt.common.DeBug;
import com.hlrt.common.Utils;

public class LocationService implements LocationListener {

	private static final String TAG = "LocationService";
    boolean isGPSEnabled = false;

    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;
    
    boolean mStartLocationService = false;

    final static long MIN_TIME_INTERVAL = 10 * 1000L;

    Location location;
    private Context mContext;

    private LocationServiceListener mLocationServiceListener = null;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 2000; // 1 minute

    protected LocationManager locationManager;

    private CountDownTimer timer = new CountDownTimer(5 * 1000, 5000) {

        public void onTick(long millisUntilFinished) {

        }

        public void onFinish() {
            stopUsingGPS();
        }
    };

    public LocationService(Context context) {
    	mContext = context;
    }


    public void start() {
    	if(mStartLocationService)
    		return;
        if (Utils.isNetworkAvailable(mContext)) {
        	mStartLocationService = true;
            try {
                timer.start();
                DeBug.i(TAG, "LocationService start");
                locationManager = (LocationManager) mContext
                        .getSystemService(Context.LOCATION_SERVICE);

                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    DeBug.d(TAG, "Network Enabled");
                    if (locationManager != null) {
                        Location tempLocation = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (tempLocation != null
                                && isBetterLocation(tempLocation,
                                        location))
                            location = tempLocation;
                    }
                }
                if (isGPSEnabled) {

                    locationManager.requestSingleUpdate(
                            LocationManager.GPS_PROVIDER, this, null);
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    DeBug.d(TAG, "GPS Enabled");
                    if (locationManager != null) {
                        Location tempLocation = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (tempLocation != null
                                && isBetterLocation(tempLocation,
                                        location))
                            location = tempLocation;
                    }
                }
                DeBug.d(TAG, "Location = "+ location);
                if(mLocationServiceListener != null)
                	mLocationServiceListener.onLocationChanged(location);
                timer.cancel();
            } catch (Exception e) {
            	mStartLocationService = false;
//                onTaskError(e.getMessage());
                e.printStackTrace();
            }
        } else {
//            onOfflineResponse(requestData);
        }
    }

    public void stopUsingGPS() {
    	if(DeBug.DEBUG)
    		DeBug.e(TAG, "stopUsingGPS");
    	mStartLocationService = false;
        if (locationManager != null) {
            locationManager.removeUpdates(LocationService.this);
        }
    }

    public boolean canGetLocation() {
        locationManager = (LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGPSEnabled || isNetworkEnabled;
    }

    @Override
    public void onLocationChanged(Location location) {
    	timer.cancel();
    	DeBug.i(TAG, "onLocationChanged : "+location.toString());
        if (location != null
                && isBetterLocation(location, this.location)) {
            this.location = location;
            DeBug.i(TAG, "onLocationChanged : loation changed");
            if(mLocationServiceListener != null)
            	mLocationServiceListener.onLocationChanged(location);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public static boolean isBetterLocation(Location location,
            Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > MIN_TIME_INTERVAL;
        boolean isSignificantlyOlder = timeDelta < -MIN_TIME_INTERVAL;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location,
        // use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must
            // be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
                .getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and
        // accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate
                && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private static boolean isSameProvider(String provider1, String provider2) {
    	  if (provider1 == null) {
    	   return provider2 == null;
    	  }
    	  return provider1.equals(provider2);
    }

    public void setLocationServiceListener(LocationServiceListener listener) {
    	mLocationServiceListener = listener;
    }

    public interface LocationServiceListener {
    	public void onLocationChanged(Location location);
    }
}