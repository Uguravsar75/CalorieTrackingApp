package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;

/**
 * Kahvaltı / öğün hatırlatma bildirimi gönderir.
 * Kullanıcının seçtiği saate göre ReminderScheduler tarafından tam olarak planlanır.
 * Çalışma sonunda ertesi güne otomatik olarak yeniden zamanlanır.
 */
public class MealReminderWorker extends Worker {

    public MealReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        AppRepository repository = new AppRepository(ctx);

        // Kullanıcı giriş yapmış mı?
        if (repository.getCurrentUserEmail() == null) {
            rescheduleForTomorrow(ctx);
            return Result.success();
        }

        int mealCount = repository.getMealCountForDate(DateUtils.getToday());

        // O gün hiç öğün girilmediyse bildirim gönder
        if (mealCount == 0) {
            NotificationHelper.sendMealReminderNotification(ctx);
        }

        // Ertesi güne yeniden zamanla
        rescheduleForTomorrow(ctx);
        return Result.success();
    }

    private void rescheduleForTomorrow(Context ctx) {
        int hour   = NotificationHelper.getMealReminderHour(ctx);
        int minute = NotificationHelper.getMealReminderMinute(ctx);
        ReminderScheduler.scheduleMealReminder(ctx, hour, minute);
    }
}
