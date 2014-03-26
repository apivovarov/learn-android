
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

    static class MyLocationListener implements LocationListener {

        TextView textLatLon;

        public void init(TextView textLatLon) {
            this.textLatLon = textLatLon;
        }

        @Override
        public void onLocationChanged(Location loc) {
            logLocation("onLocationChanged", loc);
            updateTextLatLon(textLatLon, loc);
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
        locMngr.removeUpdates(locListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locMngr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.01f, locListener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver contentResolver = getBaseContext().getContentResolver();
        boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(contentResolver,
                LocationManager.GPS_PROVIDER);
        Log.i("gps", "" + gpsStatus);

        locMngr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        List<String> provs = locMngr.getProviders(true);
        Log.i("gps", "provs: " + provs);

        setContentView(R.layout.main);

        locListener = new MyLocationListener();
        TextView textLatLon = (TextView)findViewById(R.id.textLatLon);
        locListener.init(textLatLon);
        locMngr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.01f, locListener);

        editLat1 = (EditText)findViewById(R.id.editLat1);
        editLon1 = (EditText)findViewById(R.id.editLon1);
        editAlt1 = (EditText)findViewById(R.id.editAlt1);

        editLat2 = (EditText)findViewById(R.id.editLat2);
        editLon2 = (EditText)findViewById(R.id.editLon2);
        editAlt2 = (EditText)findViewById(R.id.editAlt2);

        editDist = (EditText)findViewById(R.id.editDist);

        Location loc = locMngr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        updateTextLatLon(textLatLon, loc);
    }

    /** Called when the user clicks the Send button */
    public void button1Click(View view) {
        // Intent intent = new Intent(this, DisplayMessageActivity.class);

        Location loc = locMngr.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        editLat1.setText("" + loc.getLatitude());
        editLon1.setText("" + loc.getLongitude());
        editAlt1.setText("" + loc.getAltitude());

        logLocation("getLastKnownLocation A", loc);
        // String msg = editText1.getText().toString();

        // intent.putExtra("mymsg", msg);
        // startActivity(intent);

        updateDistance();
    }

    public void button2Click(View view) {
        Location loc = locMngr.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        editLat2.setText("" + loc.getLatitude());
        editLon2.setText("" + loc.getLongitude());
        editAlt2.setText("" + loc.getAltitude());

        logLocation("getLastKnownLocation B", loc);

        updateDistance();
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

    static void updateTextLatLon(TextView textLatLon, Location loc) {
        if (textLatLon != null) {
            String alt = "" + loc.getAltitude();
            if (alt.length() > 10) {
                alt = alt.substring(0, 10);
            }
            textLatLon.setText(loc.getLatitude() + "," + loc.getLongitude() + " alt: " + alt);
        }
    }

    static void logLocation(String eventDesc, Location loc) {
        Log.i("gps", eventDesc + ". latlon: " + loc.getLatitude() + "," + loc.getLongitude()
                + " alt: " + loc.getAltitude());
    }

    void updateDistance() {
        if (editLat1.length() > 0 && editLon1.length() > 0 && editLat2.length() > 0
                && editLon2.length() > 0) {
            double lat1D = Double.parseDouble(editLat1.getText().toString());
            double lon1D = Double.parseDouble(editLon1.getText().toString());
            double lat2D = Double.parseDouble(editLat2.getText().toString());
            double lon2D = Double.parseDouble(editLon2.getText().toString());

            double d = GeoUtils.getDistance(lat1D, lon1D, lat2D, lon2D);

            editDist.setText("" + d);

            Log.i("gps", "dist: " + d);
        }
    }
}
