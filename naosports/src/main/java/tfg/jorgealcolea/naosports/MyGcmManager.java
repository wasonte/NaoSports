package tfg.jorgealcolea.naosports;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import tfg.jorgealcolea.naosports.gcm.GcmRegistrationIntentService;

/**
 * Created by george on 23/08/16.
 */
public class MyGcmManager {

    public static final String GCM_UPDATE_SCORE = "GcmUpdateScore";
    public static final String GCM_START = "GcmStart";
    public static final String GCM_CHALLENGE = "GcmChallenge";

    // Singleton
    private static MyGcmManager gcmInstance = new MyGcmManager();

    private boolean playServicesAvailable;
    private boolean registeredInGCM;

    private MyGcmManager(){}

    public static MyGcmManager getInstance(){
        return gcmInstance;
    }

    public boolean getRegisteredInGCM(){
        return registeredInGCM;
    }

    public void setRegisteredInGCM(boolean registeredInGCM){
        this.registeredInGCM = registeredInGCM;
    }

    public boolean getPlayServicesAvailable(){ return playServicesAvailable; }

    public void registerGCM(Context ctx, boolean register){
        if (checkPlayServices(ctx)) {
            playServicesAvailable = true;
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(ctx, GcmRegistrationIntentService.class);
            intent.putExtra("register", register);
            ctx.startService(intent);
        } else {
            playServicesAvailable = false;
        }
    }


    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices(Context ctx) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(ctx);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                AlertDialog dialog =  (AlertDialog)apiAvailability.getErrorDialog((Activity)ctx, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
                // Change the title and message if app < API 14
                //dialog.setTitle(ctx.getResources().getString(R.string.str_login_google_services_title));
                //dialog.setMessage(ctx.getResources().getString(R.string.str_login_google_services_text));
                dialog.show();
            }
            return false;
        }
        return true;
    }
}
