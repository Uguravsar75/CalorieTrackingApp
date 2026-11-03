package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.activities.MainActivity;

public class NotificationHelper {

    private static final String CHANNEL_ID = "calorie_tracker_channel";
    private static final int NOTIF_ID_REMINDER = 1001;
    private static final int NOTIF_ID_LIMIT = 1002;
    private static final int NOTIF_ID_WATER = 1003;
    private static final int NOTIF_ID_GOAL = 1004;
    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_MEAL_ENABLED = "meal_enabled";
    private static final String KEY_WATER_ENABLED = "water_enabled";
    private static final String KEY_LIMIT_ENABLED = "limit_enabled";
    private static final String KEY_GOAL_ENABLED = "goal_enabled";
    private static final String KEY_LAST_MEAL_DATE = "last_meal_notif_date";
    private static final String KEY_LAST_LIMIT_DATE = "last_limit_notif_date";
    private static final String KEY_LAST_GOAL_DATE = "last_goal_notif_date";
    // Hatirlatma saatleri: dakika cinsinden (saat * 60 + dakika)
    private static final String KEY_MEAL_HOUR    = "meal_reminder_hour";
    private static final String KEY_MEAL_MINUTE  = "meal_reminder_minute";
    private static final String KEY_WATER_HOUR   = "water_reminder_hour";
    private static final String KEY_WATER_MINUTE = "water_reminder_minute";
    // Varsayilan saatler
    public static final int DEFAULT_MEAL_HOUR    = 8;
    public static final int DEFAULT_MEAL_MINUTE  = 0;
    public static final int DEFAULT_WATER_HOUR   = 14;
    public static final int DEFAULT_WATER_MINUTE = 0;

    public static void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(context.getString(R.string.notif_channel_description));
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    public static boolean hasNotificationPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static Intent buildNotificationSettingsIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:" + context.getPackageName()));
        }
        return intent;
    }

    public static void sendMealReminderNotification(Context context) {
        if (!isMealReminderEnabled(context) || !shouldSendOnceDaily(context, KEY_LAST_MEAL_DATE)) {
            return;
        }
        notify(context, NOTIF_ID_REMINDER,
                context.getString(R.string.app_name),
                context.getString(R.string.notif_meal_reminder));
    }

    public static void sendLimitExceededNotification(Context context, int exceededBy) {
        if (!isLimitAlertEnabled(context) || !shouldSendOnceDaily(context, KEY_LAST_LIMIT_DATE)) {
            return;
        }
        notify(context, NOTIF_ID_LIMIT,
                context.getString(R.string.app_name),
                context.getString(R.string.notif_limit_exceeded, exceededBy));
    }

    public static void sendWaterReminderNotification(Context context) {
        if (!isWaterReminderEnabled(context)) {
            return;
        }
        notify(context, NOTIF_ID_WATER,
                context.getString(R.string.app_name),
                context.getString(R.string.notif_water_reminder));
    }

    public static void sendGoalCloseNotification(Context context, int remaining) {
        if (!isGoalCloseAlertEnabled(context) || !shouldSendOnceDaily(context, KEY_LAST_GOAL_DATE)) {
            return;
        }
        notify(context, NOTIF_ID_GOAL,
                context.getString(R.string.app_name),
                context.getString(R.string.notif_goal_close, remaining));
    }

    public static boolean isMealReminderEnabled(Context context) {
        return prefs(context).getBoolean(KEY_MEAL_ENABLED, true);
    }

    public static boolean isWaterReminderEnabled(Context context) {
        return prefs(context).getBoolean(KEY_WATER_ENABLED, true);
    }

    public static boolean isLimitAlertEnabled(Context context) {
        return prefs(context).getBoolean(KEY_LIMIT_ENABLED, true);
    }

    public static boolean isGoalCloseAlertEnabled(Context context) {
        return prefs(context).getBoolean(KEY_GOAL_ENABLED, true);
    }

    public static void setMealReminderEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_MEAL_ENABLED, enabled).apply();
    }

    public static void setWaterReminderEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_WATER_ENABLED, enabled).apply();
    }

    public static void setLimitAlertEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_LIMIT_ENABLED, enabled).apply();
    }

    public static void setGoalCloseAlertEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_GOAL_ENABLED, enabled).apply();
    }

    // ------------------------------------------------------------------ //
    //  Hatirlatma saati ayarlari                                           //
    // ------------------------------------------------------------------ //

    public static int getMealReminderHour(Context context) {
        return prefs(context).getInt(KEY_MEAL_HOUR, DEFAULT_MEAL_HOUR);
    }

    public static int getMealReminderMinute(Context context) {
        return prefs(context).getInt(KEY_MEAL_MINUTE, DEFAULT_MEAL_MINUTE);
    }

    public static void setMealReminderTime(Context context, int hour, int minute) {
        prefs(context).edit()
                .putInt(KEY_MEAL_HOUR, hour)
                .putInt(KEY_MEAL_MINUTE, minute)
                .apply();
    }

    public static int getWaterReminderHour(Context context) {
        return prefs(context).getInt(KEY_WATER_HOUR, DEFAULT_WATER_HOUR);
    }

    public static int getWaterReminderMinute(Context context) {
        return prefs(context).getInt(KEY_WATER_MINUTE, DEFAULT_WATER_MINUTE);
    }

    public static void setWaterReminderTime(Context context, int hour, int minute) {
        prefs(context).edit()
                .putInt(KEY_WATER_HOUR, hour)
                .putInt(KEY_WATER_MINUTE, minute)
                .apply();
    }

    /**
     * Kullanicinin sectigi saat su an icinde mi kontrol eder.
     * Worker her 2-6 saatte bir calistigi icin +/- 1 saat tolerans kullanilir.
     */
    public static boolean isWithinReminderWindow(int targetHour, int targetMinute) {
        java.util.Calendar now = java.util.Calendar.getInstance();
        int nowH = now.get(java.util.Calendar.HOUR_OF_DAY);
        int nowM = now.get(java.util.Calendar.MINUTE);
        int nowTotal    = nowH * 60 + nowM;
        int targetTotal = targetHour * 60 + targetMinute;
        // Hedef saatten 0-59 dakika icerisindeyse gonder
        return nowTotal >= targetTotal && nowTotal < targetTotal + 60;
    }

    private static PendingIntent buildMainPendingIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void notify(Context context, int notificationId, String title, String text) {
        if (!hasNotificationPermission(context)) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(buildMainPendingIntent(context))
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    private static boolean shouldSendOnceDaily(Context context, String key) {
        if (!hasNotificationPermission(context)) {
            return false;
        }
        String today = DateUtils.getToday();
        SharedPreferences preferences = prefs(context);
        if (today.equals(preferences.getString(key, ""))) {
            return false;
        }
        preferences.edit().putString(key, today).apply();
        return true;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
