package com.easeic.elcontrol;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class BackgroundService extends Service {

    private NotificationManager mNM;
    Bundle b;
    Intent notificationIntent;
    private final IBinder mBinder = new LocalBinder();
    private String newtext;
    PowerManager.WakeLock wakeLock = null;
    WifiManager.WifiLock wifiLock = null;

    public class LocalBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        showNotification();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            PowerManager pm = (PowerManager) this
                    .getSystemService(Context.POWER_SERVICE);

            // acquire a WakeLock to keep the CPU running
            if (wakeLock == null)
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "MyWakeLock");
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }

            Log.i("ServiceAlarmBroadcastReceiver", "WakeLock acquired!");


            WifiManager wm = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            if (wifiLock == null)
                wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "MyWifiLock");
            if (!wifiLock.isHeld()) {
                wifiLock.acquire();
            }
        }
        catch (Exception e)
        {

        }
        return START_STICKY;
    }
    public void onDestroy() {

        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
                Log.i("ServiceAlarmBroadcastReceiver", "WakeLock released!");
            }
            wifiLock = null;
        }

        // release the WifiLock
        if (wifiLock != null) {
            if (wifiLock.isHeld()) {
                wifiLock.release();
                Log.i("ServiceAlarmBroadcastReceiver", "WiFi Lock released!");
            }
            wifiLock = null;
        }

        mNM.cancel(R.string.local_service_started);
        stopSelf();
    }

    private void showNotification() {
        CharSequence text = getText(R.string.local_service_started);

    //    Notification notification = new Notification(R.mipmap.ic_launcher, text, System.currentTimeMillis());
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,intent, 0);
     //   notification.setlatestEventInfo(this, "ELControl",newtext, contentIntent);
    //    notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
    //    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Notification.Builder builder = new Notification.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("ELControl")
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);
        Notification notification=builder.build();

        mNM.notify(R.string.local_service_started, notification);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
