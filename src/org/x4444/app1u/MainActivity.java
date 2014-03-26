
package org.x4444.app1u;

import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

        locMngr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.01f, locListener);
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
}
