package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * DailyStatsManager: Kullanıcının günlük manuel girişlerini (su, adım, egzersiz)
 * SharedPreferences üzerinde tarih bazlı olarak saklar.
 * Her gün otomatik olarak sıfırlanır — eski tarihler okunsa 0 döner.
 */
public class DailyStatsManager {

    private static final String PREFS_NAME = "daily_stats_prefs";
    private static final String KEY_WATER   = "water_";
    private static final String KEY_STEPS   = "steps_";
    private static final String KEY_EXERCISE = "exercise_";

    private final SharedPreferences prefs;

    public DailyStatsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // --- Su (bardak) ---

    public int getWaterGlasses(String date) {
        return prefs.getInt(KEY_WATER + date, 0);
    }

    public void setWaterGlasses(String date, int glasses) {
        prefs.edit().putInt(KEY_WATER + date, Math.max(0, Math.min(20, glasses))).apply();
    }

    // --- Adım sayısı ---

    public int getSteps(String date) {
        return prefs.getInt(KEY_STEPS + date, 0);
    }

    public void setSteps(String date, int steps) {
        prefs.edit().putInt(KEY_STEPS + date, Math.max(0, steps)).apply();
    }

    // --- Egzersiz (kcal) ---

    public int getExerciseKcal(String date) {
        return prefs.getInt(KEY_EXERCISE + date, 0);
    }

    public void setExerciseKcal(String date, int kcal) {
        prefs.edit().putInt(KEY_EXERCISE + date, Math.max(0, kcal)).apply();
    }
}
