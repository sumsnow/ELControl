package com.easeic.elcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by sam on 2016/9/23.
 */
public class ServiceAlarmBroadcastReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        PowerManager.WakeLock wakeLock = null;
        WifiManager.WifiLock wifiLock = null;
        try {
            PowerManager pm = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);

            // acquire a WakeLock to keep the CPU running
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyWakeLock");
            if(!wakeLock.isHeld()){
                wakeLock.acquire();
            }

            Log.i("ServiceAlarmBroadcastReceiver", "WakeLock acquired!");


            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL , "MyWifiLock");
            if(!wifiLock.isHeld()){
                wifiLock.acquire();
            }

            Log.i("ServiceAlarmBroadcastReceiver", "WifiLock acquired!");
           // context.startService(new Intent(context, ThePollerService.class));
        } finally {
            // release the WakeLock to allow CPU to sleep
            if (wakeLock != null) {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                    Log.i("ServiceAlarmBroadcastReceiver", "WakeLock released!");
                }
            }

            // release the WifiLock
            if (wifiLock != null) {
                if (wifiLock.isHeld()) {
                    wifiLock.release();
                    Log.i("ServiceAlarmBroadcastReceiver", "WiFi Lock released!");
                }
            }
        }
    }
}