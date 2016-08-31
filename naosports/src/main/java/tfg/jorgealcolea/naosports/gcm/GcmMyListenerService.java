package tfg.jorgealcolea.naosports.gcm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import tfg.jorgealcolea.naosports.MyGcmManager;
import tfg.jorgealcolea.naosports.RobotSession;

/**
 * Created by george on 23/08/16.
 */
public class GcmMyListenerService extends GcmListenerService {

    private static final String TAG = "GcmMyListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String action = data.getString("action");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "action: " + action);

        if (action.equals("start")){
            Intent intent = new Intent(MyGcmManager.GCM_START);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else if (action.equals("score")){
            Intent intent = new Intent(MyGcmManager.GCM_UPDATE_SCORE);
            if (RobotSession.getInstance().getMode().equals("solo")){
                intent.putExtra("playerScore", data.getString("playerScore"));
            } else {
                // Versus mode
                intent.putExtra("playerScore", data.getString("playerScore"));
                intent.putExtra("rivalScore", data.getString("rivalScore"));
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else if (action.equals("challenge")){
            Intent intent = new Intent(MyGcmManager.GCM_CHALLENGE);
            intent.putExtra("message", data.getString("message"));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }


    }
}
