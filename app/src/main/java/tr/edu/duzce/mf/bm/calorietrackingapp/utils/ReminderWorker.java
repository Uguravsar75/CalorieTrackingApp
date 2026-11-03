package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;

/**
 * Limit aşımı ve hedefe yaklaşma bildirimlerini yönetir.
 * 2 saatlik periyodik WorkManager iş olarak çalışır.
 *
 * Kahvaltı ve su hatırlatmaları artık tam saatli OneTimeWorkRequest'ler ile
 * MealReminderWorker ve WaterReminderWorker tarafından yapılıyor.
 */
public class ReminderWorker extends Worker {

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        AppRepository repository = new AppRepository(ctx);

        if (repository.getCurrentUserEmail() == null) {
            return Result.success();
        }

        int consumed = repository.getTotalCaloriesForDate(DateUtils.getToday());
        int goal     = repository.getUser() != null ? repository.getUser().getDailyCalorieGoal() : 0;

        if (goal > 0) {
            int remaining = goal - consumed;
            if (consumed > goal) {
                // Limit aşıldıysa aşım bildirimi
                NotificationHelper.sendLimitExceededNotification(ctx, consumed - goal);
            } else if (remaining <= 300) {
                // Hedefe 300 kcal kaldıysa yaklaşma bildirimi
                NotificationHelper.sendGoalCloseNotification(ctx, remaining);
            }
        }

        return Result.success();
    }
}
