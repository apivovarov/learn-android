
package org.x4444.app1u;

import org.x4444.app1u.db.PlateDbHelper;
import org.x4444.app1u.db.LocationDao;
import org.x4444.app1u.loc.LocationService.GpsLocationListener;

import android.app.Application;
import android.content.Context;
import android.location.Location;

public class App1uApp extends Application {

    public static Context context;

    public static int gpsFreq;

    public static int gpsCnt;

    public static int sendCnt;

    public static String plateNo;

    public static Location lastLocation;

    public static GpsLocationListener gpsLocListener;

    public static PlateDbHelper dbHelper;

    public static LocationDao locationDao;

    /*
     * (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        App1uApp.context = getApplicationContext();

        dbHelper = new PlateDbHelper(context);
        locationDao = new LocationDao();
    }

    public static Context getAppContext() {
        return App1uApp.context;
    }

}
