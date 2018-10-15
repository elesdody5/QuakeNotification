package com.example.android.quakenotification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

/**
 * Created by mohamed on 10/4/2018.
 */

public class NotificationUtilites {
     private final static  String NOTIFICATION_CHANNEL_ID="2";
    private final static  String NOTIFICATION_CHANNEL_NAME="earthauake";
    private final static  int NOTIFICATION_ID=100;
    private final static  int INTENT_CODE=15;


    static void showNotification(Earthquake earthquake, Context context)
    {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,NOTIFICATION_CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(mChannel);

        }
        Intent intent =new Intent(context,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,INTENT_CODE,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(context,String.valueOf(NOTIFICATION_ID))
                .setLargeIcon(getIconBitmap(R.mipmap.notification_layer,context))
                .setSmallIcon(R.mipmap.notification_layer)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setContentTitle(earthquake.getTitle())
                .setContentText(earthquake.getBody())
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(earthquake.getBody()))
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();
      mNotificationManager.notify(NOTIFICATION_ID,notification);

    }
    private static Bitmap getIconBitmap(int imageSrc,Context context)
    {
        Resources resources =context.getResources();
        Bitmap image= BitmapFactory.decodeResource(resources,imageSrc);
        return image;

    }
}
