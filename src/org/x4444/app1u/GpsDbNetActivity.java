
package org.x4444.app1u;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.x4444.app1u.db.LocationDao;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class GpsDbNetActivity extends Activity {

    static int SEND_BATCH_SIZE = 40;

    static Context appContext;

    static LocationManager locMngr;

    static MyLocationListener locListener;

    static LocationDao dao;

    static ConnectivityManager connManager;

    static NetworkService netService;

    static final String plateNo = "6YIT551";

    static int gpsCnt;

    static int sendCnt;

    static void cleanAll() {
        stopListenGps();
        sendCnt = 0;
        netService = null;
        dao = null;
        connManager = null;
        locMngr = null;
        appContext = null;
    }

    static class MyLocationListener implements LocationListener {

        GpsDbNetActivity gpsDbNetActivity;

        public MyLocationListener() {
        }

        public void setActivity(GpsDbNetActivity gpsDbNetActivity) {
            this.gpsDbNetActivity = gpsDbNetActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            try {
                if (loc != null) {
                    logLocation("onLocationChanged", loc);
                    dao.saveLocation(loc);
                    gpsCnt++;
                    Log.i("gps", "loc saved");
                    if (gpsDbNetActivity != null) {
                        gpsDbNetActivity.updateTextLatLon(loc);
                        gpsDbNetActivity.updateTextGpsCnt();
                    }
                }
            } catch (RuntimeException e) {
                Log.e("gps", e.getMessage(), e);
                showToast(e.getMessage(), Toast.LENGTH_LONG);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i("gps", "gps provider disabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i("gps", "gps provider enabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i("gps", "gps status: " + status);
            showToast("GPS status: " + status, Toast.LENGTH_LONG);
        }
    }

    public GpsDbNetActivity() {
        Log.i("gps", "constructor");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("gps", "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("gps", "onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("gps", "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("gps", "onStop");
    }

    protected void listenGps() {
        // List<String> provs = locMngr.getProviders(true);
        // Log.i("gps", "provs: " + provs);
        // ContentResolver contentResolver =
        // getBaseContext().getContentResolver();
        // boolean isLocationProviderEnabled =
        // Settings.Secure.isLocationProviderEnabled(
        // contentResolver, LocationManager.GPS_PROVIDER);
        // Log.i("gps", "isLocationProviderEnabled: " +
        // isLocationProviderEnabled);

        if (locListener == null) {

            locListener = new MyLocationListener();
            locListener.setActivity(this);
            Log.i("gps", "locListener: " + locListener);
            // TextView textLatLon = (TextView)findViewById(R.id.textLatLon);
            // Log.i("gps", "textLatLon: " + textLatLon);

            locMngr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, locListener);
            showShortToast("gps listener added");
            Log.i("gps", "gps listener added");
        }
        // Location loc =
        // locMngr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        // if (loc != null) {
        // updateTextLatLon(textLatLon, loc);
        // }

        // if (!isLocationProviderEnabled) {
        // NoGpsDialogFragment dia = new NoGpsDialogFragment();
        // dia.show(getFragmentManager(), "nogps");
        // }
    }

    static void stopListenGps() {
        if (locListener != null) {
            locMngr.removeUpdates(locListener);
            locListener = null;
            gpsCnt = 0;
            showShortToast("gps listener removed");
            Log.i("gps", "gps listener removed");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("gps", "onCreate");

        setContentView(R.layout.gpsdbnet);

        if (appContext == null) {
            Log.i("gps", "app context is null");
            appContext = getApplicationContext();
        } else {
            Log.i("gps", "app context is not null");
        }

        if (locMngr == null) {
            locMngr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        }

        if (connManager == null) {
            connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        if (dao == null) {
            dao = LocationDao.getInstance();
            dao.init(appContext);
        }

        if (netService == null) {
            netService = new NetworkService(connManager);
        }

        if (locListener != null) {
            locListener.setActivity(this);
        }
        Log.i("gps", "onCreate done");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                openSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gpsdbnet, menu);
        return true;
    }

    protected void openSettings() {

    }

    protected void updateTextLatLon(Location loc) {
        TextView textLatLon = (TextView)findViewById(R.id.textLatLon);
        if (textLatLon != null) {
            String alt = "" + loc.getAltitude();
            if (alt.length() > 10) {
                alt = alt.substring(0, 10);
            }
            textLatLon.setText(loc.getLatitude() + "," + loc.getLongitude() + " alt: " + alt);
        }
    }

    public static void logLocation(String eventDesc, Location loc) {
        Log.i("gps", eventDesc + ". latlon: " + loc.getLatitude() + "," + loc.getLongitude()
                + " alt: " + loc.getAltitude() + " time: " + loc.getTime());
    }

    public static void showShortToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    public static void showToast(String msg, int duration) {
        Toast.makeText(appContext, msg, duration).show();
    }

    public void button4Click(View view) {
        SendLocationListTask sendTask = new SendLocationListTask();
        sendTask.execute();
    }

    public void button5Click(View view) {
        listenGps();
    }

    public void button6Click(View view) {
        stopListenGps();
    }

    public void buttonSelectCountClick(View view) {
        int cnt = dao.getCount();
        updateTextSelectCount(cnt, null);
    }

    protected String readAndSendLocations() {
        try {
            boolean more = true;
            int cnt = 0;
            while (more) {
                List<String> res = new ArrayList<String>();
                more = dao.getFirstNLocations(res, SEND_BATCH_SIZE);
                more = false;

                if (res.size() == 0) {
                    Log.i("gps", "no data were read from DB");
                    break;
                }

                JSONObject locList = new JSONObject();
                JSONArray dlpLocList = new JSONArray();
                long firstId = 0L;
                long lastId = 0L;

                JSONObject dlpLoc = null;
                long dlpLocTs = 0L;
                for (String s : res) {
                    dlpLoc = new JSONObject(s);
                    dlpLocTs = dlpLoc.getLong("ts");
                    // valid time 01/01/2014 - 01/01/2050
                    if (dlpLocTs >= 1388534400000L && dlpLocTs < 2524608000000L) {
                        dlpLocList.put(dlpLoc);
                    }
                    if (firstId == 0L) {
                        firstId = dlpLocTs;
                    }
                }
                lastId = dlpLocTs;

                locList.put("ll", dlpLocList);
                locList.put("plNo", plateNo);
                locList.put("ts", System.currentTimeMillis());
                // send data
                cnt++;
                String locListJson = locList.toString();
                Log.i("gps", "batch " + cnt + ": " + locListJson);

                netService.sendLocationList(locListJson);
                Log.i("gps", "deleting batch " + cnt + "; firstId: " + firstId + ", lastId: "
                        + lastId);
                dao.delLocations(firstId, lastId);
                sendCnt++;
            }
            return null;
        } catch (Exception e) {
            Log.e("gps", e.getMessage(), e);
            return e.getMessage();
        }
    }

    private class SendLocationListTask extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            String res = readAndSendLocations();
            return res;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                updateTextSentCnt();
            } else {
                showToast("err: " + result, Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            cleanAll();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        TextView textLatLon = (TextView)findViewById(R.id.textLatLon);
        String latLon = textLatLon.getText().toString();
        outState.putString("latlon", latLon);

        TextView textSelectCount = (TextView)findViewById(R.id.textSelectCount);
        outState.putString("selectCount", textSelectCount.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String latLon = savedInstanceState.getString("latlon");
        TextView textLatLon = (TextView)findViewById(R.id.textLatLon);
        textLatLon.setText(latLon);

        updateTextGpsCnt();
        updateTextSentCnt();

        String selectCount = savedInstanceState.getString("selectCount");
        updateTextSelectCount(0, selectCount);
    }

    protected void updateTextGpsCnt() {
        TextView textGpsCnt = (TextView)findViewById(R.id.textGpsCnt);
        textGpsCnt.setText(String.valueOf(gpsCnt));
    }

    protected void updateTextSentCnt() {
        TextView textSendCnt = (TextView)findViewById(R.id.textSendCnt);
        textSendCnt.setText(String.valueOf(sendCnt));
    }

    protected void updateTextSelectCount(int cnt, String cntStr) {
        TextView textSelectCnt = (TextView)findViewById(R.id.textSelectCount);
        textSelectCnt.setText(cntStr == null ? String.valueOf(cnt) : cntStr);
    }
}
