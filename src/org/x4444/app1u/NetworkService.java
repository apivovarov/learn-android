
package org.x4444.app1u;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkService {

    final ConnectivityManager connMgr;

    String locationUrlStr = "http://172.31.60.250:8080/dlp-proxy-server/rest/location/save";

    Charset utf8 = Charset.forName("UTF-8");

    public NetworkService(ConnectivityManager connMgr) {
        this.connMgr = connMgr;
    }

    public void sendLocationList(String json) throws IOException {
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            throw new IOException("networkInfo is not connected");
        }
        HttpURLConnection urlConnection = null;
        try {
            URL locationUrl = new URL(locationUrlStr);
            urlConnection = (HttpURLConnection)locationUrl.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setConnectTimeout(3000);
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
            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("bad response code: " + responseCode);
            }
            byte[] respMsg = new byte[100];
            int respLen = urlConnection.getInputStream().read(respMsg);
            String resp = new String(respMsg, 0, respLen);
            Log.i("gps", "resp msg: " + resp);

        } catch (IOException e) {
            Log.e("gps", "network error: " + e.getMessage(), e);
            throw e;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
