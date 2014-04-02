
package org.x4444.app1u.loc;

import org.x4444.app1u.App1uApp;
import org.x4444.app1u.C;
import org.x4444.app1u.db.LocationDao;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {

    MyLocationListener myLocationListener;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("gps", "LocationService onStartCommand: " + this);
        int res = super.onStartCommand(intent, flags, startId);
        int freq = intent.getIntExtra(C.GPS_FREQ, 5000);
        removeLocationListener();
        addLocationListener(freq);
        App1uApp.freqStatus = freq + " sec";
        Log.i("gps", "LocationService started, freq: " + freq);
        return res;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("gps", "LocationService onCreate: " + this);
        App1uApp.service1 = this;
    }

    @Override
    public void onDestroy() {
        removeLocationListener();
        App1uApp.freqStatus = "stopped";
        Log.i("gps", "LocationService onDestroy: " + this);
        super.onDestroy();
    }

    private void addLocationListener(final int freq) {
        LocationManager lm = getLocationManager();
        myLocationListener = new MyLocationListener();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, freq, 0f, myLocationListener);
    }

    protected void removeLocationListener() {
        if (myLocationListener != null) {
            getLocationManager().removeUpdates(myLocationListener);
            myLocationListener = null;
            Log.i("gps", "LocationListener removed");
        }
    }

    public static void updateLocation(Location location) {
        long locTs = location.getTime();
        if (locTs >= C.MIN_LOC_TS && locTs < C.MAX_LOC_TS) {
            LocationDao dao = LocationDao.getInstance();
            dao.saveLocation(location);
            App1uApp.gpsCnt++;
            App1uApp.lastLocation = location;
        } else {
            Log.i("gps", "skipped location with time: " + locTs);
        }
    }

    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                logLocation("update", location);
                updateLocation(location);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i("gps", "gps disabled");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i("gps", "gps enabled");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i("gps", "gps status: " + status);
        }
    }

    protected LocationManager getLocationManager() {
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        return lm;
    }

    public static void logLocation(String eventDesc, Location loc) {
        Log.i("gps", eventDesc + ". latlon: " + loc.getLatitude() + "," + loc.getLongitude()
                + " alt: " + loc.getAltitude() + " time: " + loc.getTime());
    }
}
