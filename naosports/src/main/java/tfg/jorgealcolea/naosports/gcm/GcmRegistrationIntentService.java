package tfg.jorgealcolea.naosports.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import tfg.jorgealcolea.naosports.MyGcmManager;
import tfg.jorgealcolea.naosports.R;

/**
 * Created by george on 23/08/16.
 */
public class GcmRegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    public GcmRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        registerGCM(intent.getBooleanExtra("register", true));
    }

    /**
     * register == true -> register in GCM
     * register == false -> unRegister from GCM
     */
    private void registerGCM(boolean register){
        try {

            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            if (register){
                Log.d("GcmRegistrationService", "GCM Registration Token: " + token);
                if(registerInServer(true, token)){
                    MyGcmManager.getInstance().setRegisteredInGCM(true);
                }
            } else {
                instanceID.deleteInstanceID();
                Log.d("GcmRegistrationService", "GCM Token for deletion: "  + token);
                if(registerInServer(false, token)){
                    MyGcmManager.getInstance().setRegisteredInGCM(false);
                }
            }

        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private boolean registerInServer(boolean register, String token) {
        // Add custom implementation, as needed.
        // TODO
        return true;
    }
}
