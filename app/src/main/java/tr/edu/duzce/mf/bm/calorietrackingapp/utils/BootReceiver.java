package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Cihaz yeniden başladığında WorkManager görevlerini yeniden planlar.
 * WorkManager OneTimeWorkRequest'leri reboot sonrası iptal olduğu için
 * BOOT_COMPLETED alındığında tüm hatırlatmalar scheduleAll() ile yeniden kurulur.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            ReminderScheduler.scheduleAll(context);
        }
    }
}
