
package org.x4444.app1u;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.x4444.app1u.db.LocationDao;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GpsDbNetActivity extends Activity {

    static int SEND_BATCH_SIZE = 40;

    static MyLocationListener locListener;

    static LocationDao dao;

    static NetworkService netService;

    String plateNo;

    static int gpsCnt;

    static int sendCnt;

    protected void cleanAll() {
        stopListenGps();
        sendCnt = 0;
        gpsCnt = 0;
        netService = null;
        dao = null;
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
                gpsDbNetActivity.showToast(e.getMessage(), Toast.LENGTH_LONG);
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
            gpsDbNetActivity.showToast("GPS status: " + status, Toast.LENGTH_LONG);
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

    protected void listenGps(int intervalMs) {
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

            LocationManager locMngr = getLocationManager();
            locMngr.requestLocationUpdates(LocationManager.GPS_PROVIDER, intervalMs, 0f,
                    locListener);
            updateGspUpdateStatusText((intervalMs / 1000) + " sec");
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

    protected void stopListenGps() {
        if (locListener != null) {
            LocationManager locMngr = getLocationManager();
            locMngr.removeUpdates(locListener);
            locListener = null;
            updateGspUpdateStatusText("stopped");
            showShortToast("gps listener removed");
            Log.i("gps", "gps listener removed");
        }
    }

    protected void updateGspUpdateStatusText(String status) {
        TextView textGpsStatus = (TextView)findViewById(R.id.textGpsUpdatesStatus);
        textGpsStatus.setText(status);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("gps", "onCreate");

        setContentView(R.layout.gpsdbnet);

        if (dao == null) {
            dao = LocationDao.getInstance();
            dao.init(getApplicationContext());
        }

        if (netService == null) {
            ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            netService = new NetworkService(connManager);
        }

        if (locListener != null) {
            locListener.setActivity(this);
        }
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        plateNo = sharedPref.getString("plateno", "6YIT551");
        EditText editPlateNo = (EditText)findViewById(R.id.editPlateNo);
        editPlateNo.setText(plateNo);

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

    public void showShortToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    public void showToast(String msg, int duration) {
        Toast.makeText(getApplicationContext(), msg, duration).show();
    }

    public void button4Click(View view) {
        SendLocationListTask sendTask = new SendLocationListTask();
        sendTask.execute();
    }

    public void buttonListenGps5(View view) {
        stopListenGps();
        listenGps(5000);
    }

    public void buttonListenGps180(View view) {
        stopListenGps();
        listenGps(180000);
    }

    public void button6Click(View view) {
        stopListenGps();
    }

    public void buttonSelectCountClick(View view) {
        try {
            int cnt = dao.getCount();
            updateTextSelectCount(cnt, null);
            showShortToast("count: " + cnt);
        } catch (RuntimeException e) {
            showShortToast("err: " + e.getMessage());
        }
    }

    public void buttonSavePlateNo(View view) {
        savePlateNo();
    }

    protected void savePlateNo() {
        EditText editPlateNo = (EditText)findViewById(R.id.editPlateNo);
        String plateNo0 = editPlateNo.getText().toString();

        if (plateNo0 == null || (plateNo0 = plateNo0.trim()).isEmpty()) {
            showShortToast("plateNo is empty");
            return;
        }

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("plateno", plateNo0);
        boolean res = editor.commit();
        showShortToast("commited: " + res);
        if (res) {
            plateNo = plateNo0;
            hideSoftInput(editPlateNo.getWindowToken());
            LinearLayout topPanel = (LinearLayout)findViewById(R.id.topPanel);
            topPanel.requestFocus();
        }
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

                if (dlpLocList.length() > 0) {
                    locList.put("ll", dlpLocList);
                    locList.put("plNo", plateNo);
                    locList.put("ts", System.currentTimeMillis());
                    // send data
                    cnt++;
                    String locListJson = locList.toString();
                    Log.i("gps", "batch " + cnt + ": " + locListJson);

                    netService.sendLocationList(locListJson);
                }
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
    }

    @Override
    protected void onRestoreInstanceState(Bundle b) {
        super.onRestoreInstanceState(b);
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
        TextView textSelectCount = (TextView)findViewById(R.id.textSelectCount);
        textSelectCount.setText(cntStr == null ? String.valueOf(cnt) : cntStr);
    }

    protected void toggleSoftInput() {
        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    protected void hideSoftInput(IBinder windowToken) {
        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    protected LocationManager getLocationManager() {
        LocationManager locMngr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        return locMngr;
    }
}
