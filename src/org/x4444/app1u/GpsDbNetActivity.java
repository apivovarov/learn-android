
package org.x4444.app1u;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.x4444.app1u.db.LocationDao;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("ShowToast")
public class GpsDbNetActivity extends Activity {

    ContentResolver contentResolver;

    LocationManager locMngr;

    MyLocationListener locListener;

    TextView textLatLon;

    NoGpsDialogFragment dia;

    Toast gpsEnabledToast;

    Toast gpsDisabledToast;

    Toast gpsListenerAddedToast;

    Toast gpsListenerRemovedToast;

    LocationDao dao;

    NetworkService netService;

    String plateNo = "6YIT551";

    TextView textGpsCnt;

    TextView textSendCnt;

    TextView textSelectCnt;

    int gpsCnt;

    int sendCnt;

    boolean gpsListen;

    static class MyLocationListener implements LocationListener {

        GpsDbNetActivity gpsDbNetActivity;

        TextView textLatLon;

        public MyLocationListener(GpsDbNetActivity gpsDbNetActivity) {
            this.gpsDbNetActivity = gpsDbNetActivity;
        }

        public void init(TextView textLatLon) {
            this.textLatLon = textLatLon;
        }

        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                gpsDbNetActivity.logLocation("onLocationChanged", loc);
                gpsDbNetActivity.updateTextLatLon(textLatLon, loc);
                gpsDbNetActivity.saveLastKnownLocation();
                gpsDbNetActivity.gpsCnt++;
                gpsDbNetActivity.textGpsCnt.setText(String.valueOf(gpsDbNetActivity.gpsCnt));
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            gpsDbNetActivity.gpsDisabledToast.show();
            Log.i("gps", "gps provider disabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            gpsDbNetActivity.gpsEnabledToast.show();
            Log.i("gps", "gps provider enabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i("gps", "gps status: " + status);
            gpsDbNetActivity.makeToast("GPS status: " + status, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("gps", "onPause");
        // locMngr.removeUpdates(locListener);
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
        List<String> provs = locMngr.getProviders(true);
        Log.i("gps", "provs: " + provs);
        boolean isLocationProviderEnabled = Settings.Secure.isLocationProviderEnabled(
                contentResolver, LocationManager.GPS_PROVIDER);
        Log.i("gps", "isLocationProviderEnabled: " + isLocationProviderEnabled);

        if (!gpsListen) {
            locMngr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, locListener);
            gpsListen = true;
            gpsListenerAddedToast.show();
            Log.i("gps", "gps listener added");
        }
        Location loc = locMngr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc != null) {
            updateTextLatLon(textLatLon, loc);
        }

        if (!isLocationProviderEnabled) {
            dia.show(getFragmentManager(), "nogps");
        }
    }

    protected void stopListenGps() {
        if (gpsListen) {
            locMngr.removeUpdates(locListener);
            gpsListen = false;
            gpsListenerRemovedToast.show();
            Log.i("gps", "gps listener removed");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("gps", "onCreate");

        dia = new NoGpsDialogFragment();
        contentResolver = getBaseContext().getContentResolver();

        locMngr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        setContentView(R.layout.gpsdbnet);

        locListener = new MyLocationListener(this);
        TextView textLatLon = (TextView)findViewById(R.id.textLatLon);
        locListener.init(textLatLon);

        textGpsCnt = (TextView)findViewById(R.id.textGpsCnt);
        textSendCnt = (TextView)findViewById(R.id.textSendCnt);
        textSelectCnt = (TextView)findViewById(R.id.textSelectCount);

        gpsEnabledToast = makeShortToast("GPS Enabled");
        gpsDisabledToast = makeShortToast("GPS Disabled");
        gpsListenerAddedToast = makeShortToast("gps listener added");
        gpsListenerRemovedToast = makeShortToast("gps listener removed");

        Context context = getApplicationContext();
        dao = LocationDao.getInstance();
        dao.init(context);

        netService = new NetworkService(this, context);
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

    protected void updateTextLatLon(TextView textLatLon, Location loc) {
        if (textLatLon != null) {
            String alt = "" + loc.getAltitude();
            if (alt.length() > 10) {
                alt = alt.substring(0, 10);
            }
            textLatLon.setText(loc.getLatitude() + "," + loc.getLongitude() + " alt: " + alt);
        }
    }

    protected void logLocation(String eventDesc, Location loc) {
        Log.i("gps", eventDesc + ". latlon: " + loc.getLatitude() + "," + loc.getLongitude()
                + " alt: " + loc.getAltitude());
    }

    public Toast makeShortToast(String msg) {
        return makeToast(msg, Toast.LENGTH_SHORT);
    }

    public Toast makeToast(String msg, int duration) {
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, msg, duration);
        return toast;
    }

    public void button3Click(View view) {
        saveLastKnownLocation();
    }

    protected void saveLastKnownLocation() {
        Log.i("gps", "dao: " + dao);

        Location loc = locMngr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc != null) {
            Log.i("gps", "loc saving");
            dao.saveLocation(loc);
            Log.i("gps", "loc saved");
        }
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
        Log.i("gps", "dao: " + dao);
        int cnt = dao.getCount();
        textSelectCnt.setText(String.valueOf(cnt));
    }

    protected String readAndSendLocations() {
        try {
            Log.i("gps", "dao: " + dao);

            boolean more = true;
            int cnt = 0;
            while (more) {
                List<String> res = new ArrayList<String>();
                more = dao.getFirstNLocations(res, 10);
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
                for (String s : res) {
                    dlpLoc = new JSONObject(s);
                    dlpLocList.put(dlpLoc);
                    if (firstId == 0L) {
                        firstId = (Long)dlpLoc.get("ts");
                    }
                }
                lastId = (Long)dlpLoc.get("ts");

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
                textSendCnt.setText(String.valueOf(sendCnt));
            } else {
                makeShortToast("sent result: " + result).show();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            stopListenGps();
        }
        return super.onKeyDown(keyCode, event);
    }
}
