package in.glg.rummy.notifications;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import com.google.firebase.iid.FirebaseInstanceIdService;
import com.webengage.sdk.android.WebEngage;

import in.glg.rummy.utils.PrefManager;
import in.glg.rummy.utils.RummyConstants;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService
{
    private static final String TAG = RummyConstants.FIREBASE;

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        PrefManager.saveString(getApplicationContext(), RummyConstants.FIREBASE_TOKEN, refreshedToken);

        WebEngage.get().setRegistrationID(refreshedToken);
    }


}