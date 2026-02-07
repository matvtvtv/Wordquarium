package com.example.wordquarium.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {
    private static final String PREFS = "AppPrefs";
    private static final String KEY_NOTIF_ENABLED = "notif_enabled";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Восстанавливаем, если уведомления включены в настройках
            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            boolean enabled = prefs.getBoolean(KEY_NOTIF_ENABLED, true);
            if (enabled) {
                NotificationScheduler.scheduleDailyNoon(context);
            }
        }
    }
}
