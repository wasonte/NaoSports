package tfg.jorgealcolea.naosports.gcm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import tfg.jorgealcolea.naosports.MyGcmManager;

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
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        // Process different notifications if needed
        //sendNotification(message);

        // TODO Actualizar marcadores
        Intent updateScore = new Intent(MyGcmManager.GCM_NOTIFICATION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateScore);
        // TODO 2 sincronizar dispositivos para modo versus
    }
}
