
package org.x4444.app1u;

import org.x4444.app1u.db.LocationDao;
import org.x4444.app1u.loc.LocationService;
import org.x4444.app1u.net.NetworkService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GpsDbNetActivity extends Activity {

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

    protected void updateGspUpdateStatusText(String status) {
        TextView textGpsStatus = (TextView)findViewById(R.id.textGpsUpdatesStatus);
        textGpsStatus.setText(status);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("gps", "onCreate");

        setContentView(R.layout.gpsdbnet);

        updateTextGpsCnt();
        updateTextSentCnt();
        updateTextLatLon(App1uApp.lastLocation);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        App1uApp.plateNo = sharedPref.getString(C.PLATE_NO, "6YIT551");
        EditText editPlateNo = (EditText)findViewById(R.id.editPlateNo);
        editPlateNo.setText(App1uApp.plateNo);

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
        if (loc != null) {
            TextView textLatLon = (TextView)findViewById(R.id.textLatLon);
            if (textLatLon != null) {
                String alt = "" + loc.getAltitude();
                if (alt.length() > 10) {
                    alt = alt.substring(0, 10);
                }
                textLatLon.setText(loc.getLatitude() + "," + loc.getLongitude() + " alt: " + alt);
            }
        }
    }

    public void showShortToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    public void showToast(String msg, int duration) {
        Toast.makeText(getApplicationContext(), msg, duration).show();
    }

    public void button4Click(View view) {
        Intent networkServIntent = new Intent(this, NetworkService.class);
        startService(networkServIntent);
    }

    public void buttonListenGps5(View view) {
        startLocationService(5000);
    }

    public void buttonListenGps180(View view) {
        startLocationService(180000);
    }

    public void button6Click(View view) {
        stopLocationService();
    }

    public void buttonRefreshCountClick(View view) {
        updateTextGpsCnt();
        updateTextSentCnt();
        updateTextLatLon(App1uApp.lastLocation);
    }

    public void buttonSelectCountClick(View view) {
        try {
            int cnt = LocationDao.getInstance().getCount();
            updateTextSelectCount(cnt, null);
            showShortToast("count: " + cnt);
        } catch (RuntimeException e) {
            showShortToast("err: " + e.getMessage());
        }
    }

    public void buttonSavePlateNo(View view) {
        savePlateNo();
    }

    protected void startLocationService(int gpsFreq) {
        Intent locationServIntent = new Intent(this, LocationService.class);
        locationServIntent.putExtra(C.GPS_FREQ, gpsFreq);
        startService(locationServIntent);
        Log.i("gps", "called service start");
        updateGspUpdateStatusText(gpsFreq / 1000 + " sec");
    }

    protected void stopLocationService() {
        Intent locationServIntent = new Intent(this, LocationService.class);
        stopService(locationServIntent);
        updateGspUpdateStatusText("-");
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
        editor.putString(C.PLATE_NO, plateNo0);
        boolean res = editor.commit();
        showShortToast("commited: " + res);
        if (res) {
            App1uApp.plateNo = plateNo0;
            hideSoftInput(editPlateNo.getWindowToken());
            LinearLayout topPanel = (LinearLayout)findViewById(R.id.topPanel);
            topPanel.requestFocus();
        }
    }

    // @Override
    // public boolean onKeyDown(int keyCode, KeyEvent event) {
    // if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
    // }
    // return super.onKeyDown(keyCode, event);
    // }

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
        textGpsCnt.setText(String.valueOf(App1uApp.gpsCnt));
    }

    protected void updateTextSentCnt() {
        TextView textSendCnt = (TextView)findViewById(R.id.textSendCnt);
        textSendCnt.setText(String.valueOf(App1uApp.sendCnt));
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
