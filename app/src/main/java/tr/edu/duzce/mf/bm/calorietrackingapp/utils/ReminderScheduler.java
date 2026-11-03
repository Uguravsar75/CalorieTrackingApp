package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Tüm WorkManager zamanlama işlemlerini merkezileştirir.
 *
 * <p>Kahvaltı ve su hatırlatmaları için kullanıcının seçtiği saate tam olarak
 * OneTimeWorkRequest ile zamanlanır. Worker çalıştıktan sonra kendisi ertesi güne
 * yeniden planlanır (kendi-kendini yenileyen zincir).
 *
 * <p>Limit aşımı ve hedefe yaklaşma bildirimleri anlık tetiklendiği için
 * bu Worker'lar için periyodik (2 saatlik) bir iş yeterlidir.
 */
public class ReminderScheduler {

    private static final String TAG_MEAL   = "meal_reminder";
    private static final String TAG_WATER  = "water_reminder";
    private static final String TAG_ALERTS = "calorie_alerts";

    /**
     * Uygulama başlangıcında (SplashActivity) tüm zamanlamaları kurar.
     * Eğer zaten planlanmış bir iş varsa dokunmaz (KEEP politikası).
     */
    public static void scheduleAll(Context ctx) {
        int mealH = NotificationHelper.getMealReminderHour(ctx);
        int mealM = NotificationHelper.getMealReminderMinute(ctx);
        scheduleMealReminder(ctx, mealH, mealM, ExistingWorkPolicy.KEEP);

        int waterH = NotificationHelper.getWaterReminderHour(ctx);
        int waterM = NotificationHelper.getWaterReminderMinute(ctx);
        scheduleWaterReminder(ctx, waterH, waterM, ExistingWorkPolicy.KEEP);

        scheduleAlertWorker(ctx);
    }

    /**
     * Saat değiştirildiğinde (SettingsActivity) mevcut işi iptal edip yeni saate göre planlar.
     */
    public static void rescheduleMealReminder(Context ctx, int hour, int minute) {
        scheduleMealReminder(ctx, hour, minute, ExistingWorkPolicy.REPLACE);
    }

    public static void rescheduleWaterReminder(Context ctx, int hour, int minute) {
        scheduleWaterReminder(ctx, hour, minute, ExistingWorkPolicy.REPLACE);
    }

    /**
     * Worker tamamlandığında kendini ertesi güne zamanlar (REPLACE — yeni zaman).
     * Bu metot MealReminderWorker ve WaterReminderWorker tarafından çağrılır.
     */
    public static void scheduleMealReminder(Context ctx, int hour, int minute) {
        scheduleMealReminder(ctx, hour, minute, ExistingWorkPolicy.REPLACE);
    }

    public static void scheduleWaterReminder(Context ctx, int hour, int minute) {
        scheduleWaterReminder(ctx, hour, minute, ExistingWorkPolicy.REPLACE);
    }

    // ------------------------------------------------------------------ //

    private static void scheduleMealReminder(Context ctx, int hour, int minute,
                                             ExistingWorkPolicy policy) {
        long delayMs = calcDelayMs(hour, minute);
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(MealReminderWorker.class)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .addTag(TAG_MEAL)
                .build();
        WorkManager.getInstance(ctx).enqueueUniqueWork(TAG_MEAL, policy, request);
    }

    private static void scheduleWaterReminder(Context ctx, int hour, int minute,
                                              ExistingWorkPolicy policy) {
        long delayMs = calcDelayMs(hour, minute);
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(WaterReminderWorker.class)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .addTag(TAG_WATER)
                .build();
        WorkManager.getInstance(ctx).enqueueUniqueWork(TAG_WATER, policy, request);
    }

    /**
     * Limit aşımı ve hedefe yaklaşma bildirimleri için 2 saatlik periyodik worker.
     * Bu bildirimler anlık tetiklendiği için tam saat hassasiyeti gerekmez.
     */
    private static void scheduleAlertWorker(Context ctx) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                ReminderWorker.class, 2, TimeUnit.HOURS)
                .build();
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                TAG_ALERTS,
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    /**
     * Hedef saat bugün geçmişse yarına, geçmemişse bugüne zamanlar.
     *
     * @param hour   24-saat formatında saat (0-23)
     * @param minute dakika (0-59)
     * @return milisaniye cinsinden gecikme
     */
    private static long calcDelayMs(int hour, int minute) {
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, hour);
        target.set(Calendar.MINUTE, minute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        long now = System.currentTimeMillis();
        long targetMs = target.getTimeInMillis();

        // Saat geçtiyse yarın
        if (targetMs <= now) {
            targetMs += TimeUnit.DAYS.toMillis(1);
        }

        return targetMs - now;
    }
}
