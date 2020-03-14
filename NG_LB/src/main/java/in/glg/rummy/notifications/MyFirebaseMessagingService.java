package in.glg.rummy.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.webengage.sdk.android.WebEngage;

import java.util.Map;

import in.glg.rummy.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("MyFireService", "onNewToken = " + s);
        WebEngage.get().setRegistrationID(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e("MyFireService", "onMessageReceived");
        Map<String, String> data = remoteMessage.getData();
        if (data != null) {
            if (data.containsKey("source") && "webengage".equals(data.get("source"))) {
                WebEngage.get().receive(data);
            }
        }
//
       /* String Str = remoteMessage.getNotification().getBody();
        String[] tmp;
        tmp = Str.split(" ");
        String temp1 = tmp[0];
        String temp2 = tmp[1];
        String id = tmp[2];
        notify = temp1 + " " + temp2;
        requstId = id;*/

//        showNotification(remoteMessage.getNotification().getBody());
//        sendNotification();
    }

    /*private final static String TAG = "FCM Message";
    String notify, requstId, Notification;
    */
    PendingIntent pendingIntent;


    public void sendNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_dialog_alert);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tajrummy.com/"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
        builder.setContentTitle("Notifications Title");
        builder.setContentText("Your notification content here.");
        builder.setSubText("Tap to view the website.");

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Will display the notification in the notification bar
        notificationManager.notify(1, builder.build());
    }
}