
package org.x4444.app1u.broadcast;

import org.x4444.app1u.C;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ErrorReceiver extends BroadcastReceiver {

    // Prevents instantiation
    // private ErrorReceiver() {
    // }

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra(C.BROADCAST_ERROR_MSG);
        Log.i("gps", "ErrorReceiver.onReceive msg: " + msg);
        if (msg != null) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }
}
