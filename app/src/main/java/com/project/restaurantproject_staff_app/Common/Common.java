package com.project.restaurantproject_staff_app.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.project.restaurantproject_staff_app.Model.Order;
import com.project.restaurantproject_staff_app.Model.RestaurantOwner;
import com.project.restaurantproject_staff_app.R;

public class Common {

    public static String REMEMBER_FBID ="" ;
    public static final String API_KEY_TAG = "API_KEY_TAG";

    public static final String API_RESTAURANT_ENDPOINT ="" ; // Where the server is Running ( IP:Port )
    public static final String API_KEY="1234";

    public static final String NOTIF_TITLE = "Title" ;
    public static final String NOTIF_CONTENT = "Content";

    public static RestaurantOwner currentRestaurantOwner ;
    public static Order currentOrder;

    public static void ShowNotification(Context context, int notifId, String title, String body, Intent intent) {
        PendingIntent pendingIntent = null ;
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context,notifId,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "my_restaurant_staff_app";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "My Restaurant Notification",NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Restaurant Staff App");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);

        builder.setContentTitle(title).setContentText(body)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.app_icon));

        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification mNotification = builder.build();

        notificationManager.notify(notifId,mNotification);

    }

    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus) {
            case 0:
                return "Placed";
            case 1 :
                return "Shipping";
            case 2 :
                return " Shipped";
            case -1 :
                return "Cancelled";
            default:
                return "Cancelled";
        }
    }

    public static int convertStatusToIndex(int orderStatus) {
        if (orderStatus == -1){
            return 3;
        }else{
            return orderStatus;
        }
    }

    public static String getTopicChannel(int restaurantId) {
        return new StringBuilder("Restaurant_").append(restaurantId).toString();
    }

    public static int convertStringToStatus(String status) {
        if (status.equals("Placed"))
            return 0;
        if (status.equals("Shipping"))
            return 1;
        if (status.equals("Shipped"))
            return 2;
        if (status.equals("Cancelled"))
            return -1;
        return -1 ;
    }
}
