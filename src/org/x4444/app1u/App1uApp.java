
package org.x4444.app1u;

import org.x4444.app1u.db.LocationDao;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.location.Location;

public class App1uApp extends Application {

    private static Context context;

    public static Service service1;

    public static String freqStatus = "none";

    public static int gpsCnt;

    public static int sendCnt;

    public static String plateNo;

    public static Location lastLocation;

    /*
     * (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        App1uApp.context = getApplicationContext();
        LocationDao.getInstance().init(context);
    }

    public static Context getAppContext() {
        return App1uApp.context;
    }

}
