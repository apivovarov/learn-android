
package org.x4444.app1u;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkService {

    Activity a;

    Context ctx;

    String locationUrlStr = "http://172.31.60.250:8080/dlp-proxy-server/rest/location/save";

    Charset utf8 = Charset.forName("UTF-8");

    public NetworkService(Activity a, Context ctx) {
        this.a = a;
        this.ctx = ctx;
    }

    public boolean sendLocationList(String json) {
        ConnectivityManager connMgr = (ConnectivityManager)a
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            HttpURLConnection urlConnection = null;
            try {
                URL locationUrl = new URL(locationUrlStr);
                urlConnection = (HttpURLConnection)locationUrl.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.addRequestProperty("Content-Type", "application/json");

                byte[] jsonBytes = json.getBytes(utf8);
                Log.i("gps", "body length: " + jsonBytes.length);
                urlConnection.setFixedLengthStreamingMode(jsonBytes.length);
                // Log.i("gps", "connecting");
                // urlConnection.connect();
                Log.i("gps", "getting output stream");
                OutputStream out = urlConnection.getOutputStream();
                Log.i("gps", "out: " + out);
                out.write(jsonBytes);

                int responseCode = urlConnection.getResponseCode();
                Log.i("gps", "resp code: " + responseCode);

                byte[] respMsg = new byte[100];
                int respLen = urlConnection.getInputStream().read(respMsg);
                String resp = new String(respMsg, 0, respLen);
                Log.i("gps", "resp msg: " + resp);

            } catch (Exception e) {
                Log.e("gps", "network error: " + e.getMessage(), e);
                return false;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return true;
        } else {
            Log.w("gsp", "network not connected");
            return false;
        }

    }

}
