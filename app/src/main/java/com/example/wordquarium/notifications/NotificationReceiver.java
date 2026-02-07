package com.example.wordquarium.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.wordquarium.R;
import com.example.wordquarium.ui.MainActivity;

import java.io.InputStream;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "wordly_daily_channel";
    public static final int NOTIF_ID = 1001;
    private static final String PREFS = "AppPrefs";
    private static final String KEY_NOTIF_IMAGE_URI = "notif_image_uri";

    @Override
    public void onReceive(Context context, Intent intent) {
        createChannelIfNeeded(context);

        // Intent для открытия приложения при тапе
        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.dgv) // замени на свой small icon
                .setContentTitle("Пора поиграть")
                .setContentText("Испытайте силы в слово дня")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Попробуем загрузить пользовательскую картинку
        Bitmap bigBitmap = loadBitmapFromPrefs(context);
        if (bigBitmap != null) {
            NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle()
                    .bigPicture(bigBitmap)
                    .setSummaryText("Испытайте силы в слово дня");
            builder.setStyle(style);
        } else {
            // fallback: big text или large icon из drawable
            Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.dgv);
            builder.setLargeIcon(largeIcon);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {

                NotificationManagerCompat.from(context).notify(NOTIF_ID, builder.build());

            } else {
                // разрешения нет — можно запросить или просто не показывать уведомление
                Log.w("NOTIF", "POST_NOTIFICATIONS permission not granted");
            }
        } else {
            // до Android 13 разрешение не требуется
            NotificationManagerCompat.from(context).notify(NOTIF_ID, builder.build());
        }

    }

    private void createChannelIfNeeded(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Ежедневные уведомления";
            String description = "Уведомления напомнят поиграть в 12:00";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private Bitmap loadBitmapFromPrefs(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String uriString = prefs.getString(KEY_NOTIF_IMAGE_URI, null);
        if (uriString == null) return null;

        try {
            Uri uri = Uri.parse(uriString);
            ContentResolver resolver = ctx.getContentResolver();
            InputStream is = resolver.openInputStream(uri);
            if (is == null) return null;
            Bitmap bmp = BitmapFactory.decodeStream(is);
            is.close();
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
