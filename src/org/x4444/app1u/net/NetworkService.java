
package org.x4444.app1u.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.x4444.app1u.App1uApp;
import org.x4444.app1u.C;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class NetworkService extends IntentService {

    public static final String locationUrlStr = "http://172.31.60.250:8080/dlp-proxy-server/rest/location/save";

    public static final Charset utf8 = Charset.forName("UTF-8");

    public NetworkService() {
        super("NetwotkServiceWorker");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("gps", "NetworkService th: " + Thread.currentThread());
        readAndSendLocations();
    }

    @Override
    public void onDestroy() {
        Log.i("gps", "NetworkService onDestroy");
        super.onDestroy();
    }

    protected void readAndSendLocations() {
        try {
            boolean more = true;
            int cnt = 0;
            while (more) {
                List<String> res = new ArrayList<String>();
                more = App1uApp.locationDao.getFirstNLocations(res, C.SEND_BATCH_SIZE);
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
                    if (dlpLocTs >= C.MIN_LOC_TS && dlpLocTs < C.MAX_LOC_TS) {
                        dlpLocList.put(dlpLoc);
                    }
                    if (firstId == 0L) {
                        firstId = dlpLocTs;
                    }
                }
                lastId = dlpLocTs;

                if (dlpLocList.length() > 0) {
                    locList.put("ll", dlpLocList);
                    locList.put("plNo", App1uApp.plateNo);
                    locList.put("ts", System.currentTimeMillis());
                    // send data
                    cnt++;
                    String locListJson = locList.toString();
                    Log.i("gps", "batch " + cnt + ": " + locListJson);

                    sendLocationList(locListJson);
                }
                Log.i("gps", "deleting batch " + cnt + "; firstId: " + firstId + ", lastId: "
                        + lastId);
                App1uApp.locationDao.delLocations(firstId, lastId);
                App1uApp.sendCnt++;
            }
        } catch (Exception e) {
            Log.e("gps", e.getMessage(), e);
            broadcastError(e.getMessage());
        }
    }

    public void sendLocationList(String json) throws IOException {
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
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
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    protected void broadcastError(String msg) {
        Intent localIntent = new Intent(C.BROADCAST_ERROR_ACTION)
        // Puts the status into the Intent
                .putExtra(C.BROADCAST_ERROR_MSG, msg);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
