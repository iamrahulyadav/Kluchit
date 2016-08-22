package com.cybussolutions.kluchit.PushNotification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.cybussolutions.kluchit.Activities.Login_activity;
import com.cybussolutions.kluchit.R;
import com.google.android.gms.gcm.GcmListenerService;

import java.util.Date;


public class GCMPushReceiverService extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        sendNotification(message);
    }
    private void sendNotification(String message) {

            Date now = new Date();
            long uniqueId = now.getTime();
            Intent intent = new Intent(this, Login_activity.class);
            intent.putExtra("message", message);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            int requestCode = 0;//Your request code

            intent.setAction("new_notification" + uniqueId);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
            //Setup notification
            //Sound
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            //Build notification
            NotificationCompat.Builder noBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.applogo)
                    .setContentTitle("New Even Created")
                    .setContentText(message)
                    .setSound(sound)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify((int) uniqueId, noBuilder.build()); //0 = ID of notification

    }
}
