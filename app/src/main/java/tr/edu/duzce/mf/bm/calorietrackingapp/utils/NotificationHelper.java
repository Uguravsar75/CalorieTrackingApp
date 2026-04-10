package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.activities.MainActivity;

/**
 * NotificationHelper: Uygulama bildirimleri için yardımcı sınıf.
 * - Öğün girişi hatırlatıcısı
 * - Günlük limit dolduğunda uyarı
 * - Limit aşıldığında bildirim
 */
public class NotificationHelper {

    private static final String CHANNEL_ID = "calorie_tracker_channel";
    private static final int NOTIF_ID_REMINDER = 1001;
    private static final int NOTIF_ID_LIMIT = 1002;

    /**
     * Bildirim kanalını oluşturur. Uygulama başlarken çağrılmalıdır.
     */
    public static void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Kalori takip bildirimleri");
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * Öğün girişi yapılmadığında hatırlatma bildirimi gönderir.
     */
    public static void sendMealReminderNotification(Context context) {
        PendingIntent pendingIntent = buildMainPendingIntent(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("Bugün henüz öğün girmediniz. Kalorinizi takip etmeyi unutmayın!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(NOTIF_ID_REMINDER, builder.build());
    }

    /**
     * Günlük kalori limiti aşıldığında bildirim gönderir.
     */
    public static void sendLimitExceededNotification(Context context) {
        PendingIntent pendingIntent = buildMainPendingIntent(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.warning_limit_exceeded))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(NOTIF_ID_LIMIT, builder.build());
    }

    private static PendingIntent buildMainPendingIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
