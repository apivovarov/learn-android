
package org.x4444.app1u;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("ShowToast")
public class MainActivity extends Activity {

    ContentResolver contentResolver;

    LocationManager locMngr;

    MyLocationListener locListener;

    EditText editLat1;

    EditText editLon1;

    EditText editAlt1;

    EditText editLat2;

    EditText editLon2;

    EditText editAlt2;

    TextView textLatLon;

    EditText editDist;

    NoGpsDialogFragment dia;

    Toast gpsEnabledToast;

    Toast gpsDisabledToast;

    LocationDao dao;

    NetworkService netService;

    String plateNo = "6YIT551";

    static class MyLocationListener implements LocationListener {

        MainActivity mainActivity;

        TextView textLatLon;

        public MyLocationListener(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        public void init(TextView textLatLon) {
            this.textLatLon = textLatLon;
        }

        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                mainActivity.logLocation("onLocationChanged", loc);
                mainActivity.updateTextLatLon(textLatLon, loc);
                mainActivity.saveLastKnownLocation();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            mainActivity.gpsDisabledToast.show();
            Log.i("gps", "gps provider disabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            mainActivity.gpsEnabledToast.show();
            Log.i("gps", "gps provider enabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i("gps", "gps status: " + status);
            mainActivity.makeToast("GPS status: " + status, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("gps", "onPause");
        locMngr.removeUpdates(locListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("gps", "onResume");
        List<String> provs = locMngr.getProviders(true);
        Log.i("gps", "provs: " + provs);
        boolean isLocationProviderEnabled = Settings.Secure.isLocationProviderEnabled(
                contentResolver, LocationManager.GPS_PROVIDER);
        Log.i("gps", "isLocationProviderEnabled: " + isLocationProviderEnabled);

        locMngr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, locListener);
        Location loc = locMngr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc != null) {
            updateTextLatLon(textLatLon, loc);
        }

        if (!isLocationProviderEnabled) {
            dia.show(getFragmentManager(), "nogps");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("gps", "onCreate");

        dia = new NoGpsDialogFragment();
        contentResolver = getBaseContext().getContentResolver();

        locMngr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        setContentView(R.layout.main);

        locListener = new MyLocationListener(this);
        TextView textLatLon = (TextView)findViewById(R.id.textLatLon);
        locListener.init(textLatLon);

        editLat1 = (EditText)findViewById(R.id.editLat1);
        editLon1 = (EditText)findViewById(R.id.editLon1);
        editAlt1 = (EditText)findViewById(R.id.editAlt1);

        editLat2 = (EditText)findViewById(R.id.editLat2);
        editLon2 = (EditText)findViewById(R.id.editLon2);
        editAlt2 = (EditText)findViewById(R.id.editAlt2);

        editDist = (EditText)findViewById(R.id.editDist);

        gpsEnabledToast = makeShortToast("GPS Enabled");
        gpsDisabledToast = makeShortToast("GPS Disabled");

        Context context = getApplicationContext();
        dao = LocationDao.getInstance();
        dao.init(context);

        netService = new NetworkService(this, context);
    }

    public void button1Click(View view) {
        pinLocation(editLat1, editLon1, editAlt1, "A");
    }

    public void button2Click(View view) {
        pinLocation(editLat2, editLon2, editAlt2, "B");
    }

    protected void pinLocation(EditText editLat, EditText editLon, EditText editAlt, String pontName) {
        Location loc = locMngr.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (loc != null) {
            editLat.setText("" + loc.getLatitude());
            editLon.setText("" + loc.getLongitude());
            editAlt.setText("" + loc.getAltitude());

            logLocation("getLastKnownLocation " + pontName, loc);

            updateDistance();
        }
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
        getMenuInflater().inflate(R.menu.main, menu);
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

    protected void updateDistance() {
        if (editLat1.length() > 0 && editLon1.length() > 0 && editLat2.length() > 0
                && editLon2.length() > 0) {
            try {
                double lat1D = Double.parseDouble(editLat1.getText().toString());
                double lon1D = Double.parseDouble(editLon1.getText().toString());
                double lat2D = Double.parseDouble(editLat2.getText().toString());
                double lon2D = Double.parseDouble(editLon2.getText().toString());

                double d = GeoUtils.getDistance(lat1D, lon1D, lat2D, lon2D);

                editDist.setText("" + d);

                Log.i("gps", "dist: " + d);
            } catch (RuntimeException e) {
                Log.e("gps", e.getMessage());
            }
        }
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

    protected boolean readAndSendLocations() {
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
            try {

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

            } catch (JSONException e) {
                throw new RuntimeException(e.getMessage());
            }
            // send data
            cnt++;
            String locListJson = locList.toString();
            Log.i("gps", "batch " + cnt + ": " + locListJson);

            boolean sendRes = netService.sendLocationList(locListJson);
            Log.i("gps", "sent res: " + sendRes);
            if (sendRes) {
                Log.i("gps", "deleting batch " + cnt + "; firstId: " + firstId + ", lastId: "
                        + lastId);
                dao.delLocations(firstId, lastId);
            } else {
                return false;
            }
        }
        return true;
    }

    private class SendLocationListTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            boolean res = readAndSendLocations();
            return res;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Boolean result) {

            // makeShortToast("sent result: " + result).show();
        }
    }

}
