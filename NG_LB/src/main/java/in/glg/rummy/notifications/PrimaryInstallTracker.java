package in.glg.rummy.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.webengage.sdk.android.WebEngage;

public class PrimaryInstallTracker extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("PrimaryTracker","onReceive");
        WebEngage.get().analytics().installed(intent);
    }
}
