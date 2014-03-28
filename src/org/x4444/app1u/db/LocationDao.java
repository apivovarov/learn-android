
package org.x4444.app1u.db;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

public class LocationDao {

    public static final String TABLE_NAME = "location";

    public static final String COLUMN_NAME_KEY = "k";

    public static final String COLUMN_NAME_VALUE = "v";

    PlateDbHelper dbHelper;

    // SQLiteDatabase db;

    static LocationDao instance = new LocationDao();

    public static LocationDao getInstance() {
        return instance;
    }

    private LocationDao() {

    }

    public void init(Context context) {
        dbHelper = new PlateDbHelper(context);
        Log.d("gps", "init done");
    }

    public void saveLocation(Location loc) {
        JSONObject o = getDlpLocationJson(loc);

        String json = o.toString();
        Log.d("gps", "json: " + json);
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_KEY, loc.getTime());
        values.put(COLUMN_NAME_VALUE, json);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            long rowId = db.insert(TABLE_NAME, null, values);
            Log.i("gps", "rowId: " + rowId);
        } finally {
            db.close();
            Log.d("gps", "db closed");
        }
    }

    JSONObject getDlpLocationJson(Location loc) {
        // DlpLocation l = new DlpLocation();
        // l.prv = "gps";
        // l.ts = loc.getTime();
        // l.lat = loc.getLatitude();
        // l.lon = loc.getLongitude();
        // l.alt = loc.getAltitude();
        // l.acc = loc.getAccuracy();
        // l.brng = loc.getBearing();
        // l.spd = loc.getSpeed();

        JSONObject o = new JSONObject();
        try {
            o.put("prv", "gps");
            o.put("ts", loc.getTime());
            o.put("lat", loc.getLatitude());
            o.put("lon", loc.getLongitude());
            o.put("alt", loc.getAltitude());
            o.put("acc", loc.getAccuracy());
            o.put("brng", loc.getBearing());
            o.put("spd", loc.getSpeed());
            return o;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
