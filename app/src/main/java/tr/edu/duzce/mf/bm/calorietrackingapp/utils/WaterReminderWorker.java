package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Su içme hatırlatma bildirimi gönderir.
 * Kullanıcının seçtiği saatte, günde yalnızca bir kez tetiklenir.
 * Çalışma sonunda ertesi güne otomatik olarak yeniden zamanlanır.
 */
public class WaterReminderWorker extends Worker {

    public WaterReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();

        // Aynı gün içinde ikinci kez gönderilmesin
        SharedPreferences prefs = ctx.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE);
        String today = DateUtils.getToday();
        String lastDate = prefs.getString("last_water_notif_date", "");

        if (NotificationHelper.isWaterReminderEnabled(ctx) && !today.equals(lastDate)) {
            NotificationHelper.sendWaterReminderNotification(ctx);
            prefs.edit().putString("last_water_notif_date", today).apply();
        }

        // Ertesi güne yeniden zamanla
        int hour   = NotificationHelper.getWaterReminderHour(ctx);
        int minute = NotificationHelper.getWaterReminderMinute(ctx);
        ReminderScheduler.scheduleWaterReminder(ctx, hour, minute);
        return Result.success();
    }
}
