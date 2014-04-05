
package org.x4444.app1u;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.x4444.app1u.db.LocationDao;
import org.x4444.app1u.db.PlateDbHelper;
import org.x4444.app1u.loc.LocationService.GpsLocationListener;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

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

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public static SimpleDateFormat sdfHhmmss = new SimpleDateFormat("HH:mm:ss", Locale.US);

    static ShowErrorHandler showErrorHandler;

    /*
     * (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        App1uApp.context = getApplicationContext();

        dbHelper = new PlateDbHelper(context);
        locationDao = new LocationDao();

        sdf.setTimeZone(TimeZone.getDefault());
        sdfHhmmss.setTimeZone(TimeZone.getDefault());

        showErrorHandler = new ShowErrorHandler();
    }

    public static Context getAppContext() {
        return App1uApp.context;
    }

    public static void resetCounters() {
        gpsCnt = 0;
        sendCnt = 0;
    }

    public static class ShowErrorHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            if (b != null) {
                String errMsg = b.getString(C.ERROR_KEY);
                if (errMsg != null) {
                    Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /*
     * Method can be called from other threads
     */
    public static void showErrorMsg(String msg) {
        Message mymsg = new Message();
        mymsg.getData().putString(C.ERROR_KEY, msg);
        showErrorHandler.sendMessage(mymsg);
    }
}
