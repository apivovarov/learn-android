
package org.x4444.app1u.loc;

import org.x4444.app1u.R;
import org.x4444.app1u.App1uApp;
import org.x4444.app1u.C;

import android.app.Notification;
import android.app.NotificationManager;
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

    Notification.Builder notifBuilder;

    static final int NOTIF_ID = 1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int freq = intent.getIntExtra(C.GPS_FREQ, 5000);
        Log.i("gps", "LocationService onStartCommand, freq: " + freq);

        if (App1uApp.gpsLocListener != null) {
            removeLocationListener();
            try {
                // sleep needed to switch from 3 min to 5 sec gps listener
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }
        addLocationListener(freq);

        notifBuilder.setContentTitle("GPS update freq: " + freq / 1000 + " sec");
        getNotifMngr().notify(NOTIF_ID, notifBuilder.getNotification());

        App1uApp.gpsFreq = freq;

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i("gps", "LocationService onCreate: " + this);
        super.onCreate();

        notifBuilder = new Notification.Builder(App1uApp.context)
                .setContentTitle("GPS update freq: -").setContentText("")
                .setSmallIcon(R.drawable.pin_map_gps);

        startForeground(NOTIF_ID, notifBuilder.getNotification());
        Log.i("gps", "started service in foreground");
    }

    @Override
    public void onDestroy() {
        removeLocationListener();
        Log.i("gps", "LocationService onDestroy: " + this);
        super.onDestroy();
    }

    private void addLocationListener(int freq) {
        try {
            LocationManager lm = getLocationManager();
            App1uApp.gpsLocListener = new GpsLocationListener();
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, freq, 0f,
                    App1uApp.gpsLocListener);
            Log.i("gps", "LocationListener added, freq: " + freq);
        } catch (RuntimeException e) {
            Log.e("gps", e.getMessage(), e);
        }
    }

    protected void removeLocationListener() {
        try {
            if (App1uApp.gpsLocListener != null) {
                getLocationManager().removeUpdates(App1uApp.gpsLocListener);
                App1uApp.gpsLocListener = null;
                Log.i("gps", "LocationListener removed");
            }
        } catch (RuntimeException e) {
            Log.e("gps", e.getMessage(), e);
        }
    }

    protected void updateLocation(Location location) {
        long locTs = location.getTime();
        if (locTs >= C.MIN_LOC_TS && locTs < C.MAX_LOC_TS) {
            App1uApp.locationDao.saveLocation(location);
            App1uApp.gpsCnt++;
            App1uApp.lastLocation = location;
        } else {
            Log.i("gps", "skipped location with time: " + locTs);
        }
    }

    public class GpsLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            try {
                if (location != null) {
                    logLocation("update", location);
                    updateLocation(location);

                    notifBuilder.setContentText(location.getLatitude() + ","
                            + location.getLongitude() + "@" + location.getTime());
                    getNotifMngr().notify(NOTIF_ID, notifBuilder.getNotification());
                }
            } catch (Exception e) {
                Log.e("gps", e.getMessage(), e);
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

    protected NotificationManager getNotifMngr() {
        NotificationManager notifMngr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return notifMngr;
    }

    protected void logLocation(String eventDesc, Location loc) {
        Log.i("gps", eventDesc + ". latlon: " + loc.getLatitude() + "," + loc.getLongitude()
                + " alt: " + loc.getAltitude() + " time: " + loc.getTime());
    }
}
