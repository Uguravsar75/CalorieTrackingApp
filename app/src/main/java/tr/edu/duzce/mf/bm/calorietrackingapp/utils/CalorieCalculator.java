package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;

/**
 * Harris-Benedict formülü kullanarak günlük bazal metabolizma hızı (BMR)
 * ve önerilen günlük kalori hedefini hesaplar.
 * Aktivite seviyesi: orta (x1.55) varsayılmıştır.
 */
public class CalorieCalculator {

    /**
     * Verilen kullanıcı profiline göre günlük kalori hedefi hesaplar.
     * @param user Kullanıcı entity'si
     * @return Günlük önerilen kalori (kcal)
     */
    public static int calculateDailyGoal(User user) {
        float bmr = calculateBMR(user.getGender(), user.getWeight(), user.getHeight(), user.getAge());
        return Math.round(bmr * getActivityFactor(user.getActivityLevel()));
    }

    /**
     * Harris-Benedict denklemi ile BMR hesaplar.
     * Erkek:  88.362 + (13.397 × kilo) + (4.799 × boy) − (5.677 × yaş)
     * Kadın: 447.593 + (9.247 × kilo) + (3.098 × boy) − (4.330 × yaş)
     */
    public static float calculateBMR(String gender, float weightKg, float heightCm, int age) {
        if ("MALE".equals(gender)) {
            return 88.362f + (13.397f * weightKg) + (4.799f * heightCm) - (5.677f * age);
        } else {
            return 447.593f + (9.247f * weightKg) + (3.098f * heightCm) - (4.330f * age);
        }
    }

    /**
     * Gram ve 100g kalorisine göre toplam kaloriyi hesaplar.
     */
    public static int calculateCalories(float amountGrams, int caloriesPer100g) {
        return Math.round((amountGrams / 100f) * caloriesPer100g);
    }

    public static float getActivityFactor(String activityLevel) {
        if ("LOW".equals(activityLevel)) return 1.3f;
        if ("HIGH".equals(activityLevel)) return 1.72f;
        if ("VERY_HIGH".equals(activityLevel)) return 1.9f;
        return 1.55f;
    }
}
