package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.MealEntry;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;

public class RecommendationEngine {

    public static DashboardInsight buildDashboardInsight(User user, int consumed, int goal) {
        return buildDashboardInsight(user, consumed, goal, null, null);
    }

    public static DashboardInsight buildDashboardInsight(
            User user,
            int consumed,
            int goal,
            TimeZone timeZone,
            String timezoneLabelOverride
    ) {
        boolean english = isEnglish();
        TimeZone effectiveTimeZone = timeZone != null ? timeZone : resolveUserTimeZone(user);
        DashboardInsight insight = new DashboardInsight();
        insight.mealPeriodTitle = getMealPeriodTitle(effectiveTimeZone);
        insight.timezoneLabel = timezoneLabelOverride != null
                ? timezoneLabelOverride
                : buildTimezoneLabel(effectiveTimeZone);
        insight.percent = goal > 0 ? Math.min((consumed * 100) / goal, 999) : 0;
        insight.limitMessage = consumed > goal
                ? (english
                    ? "You exceeded your daily calorie limit. Make a lighter choice this evening."
                    : "Günlük kalori limitini aştınız. Bu akşam daha hafif bir seçim yapın.")
                : null;
        insight.healthyAlternative = consumed > goal
                ? (english
                    ? "Alternative: choose yogurt, a seasonal salad, or grilled vegetables."
                    : "Alternatif: yoğurt, mevsim salatası veya ızgara sebze tercih edin.")
                : getMealSuggestion(user, effectiveTimeZone);
        insight.diseaseWarning = getDiseaseWarning(user);
        insight.lateMealWarning = isLateHour(effectiveTimeZone)
                ? (english
                    ? "A heavy meal late at night can strain digestion. Pick a lighter option."
                    : "Geç saatte ağır yemek sindirimi zorlayabilir. Hafif bir öğün seçin.")
                : null;
        insight.recommendationChips = getSmartSuggestions(user, effectiveTimeZone, goal - consumed);
        return insight;
    }

    public static String getMealSuggestion(User user) {
        return getMealSuggestion(user, resolveUserTimeZone(user));
    }

    public static String getMealSuggestion(User user, TimeZone timeZone) {
        boolean english = isEnglish();
        TimeZone tz = timeZone != null ? timeZone : TimeZone.getDefault();
        int hour = Calendar.getInstance(tz).get(Calendar.HOUR_OF_DAY);
        if (hour < 5 || hour >= 22) {
            return english
                    ? "It is late. Prefer herbal tea, light yogurt, or avoid eating."
                    : "Geç saat. Bitki çayı, sade yoğurt tercih edin ya da yemekten kaçının.";
        }
        if (hour < 11) {
            if (english) {
                return user != null && user.isHasDiabetes()
                        ? "Breakfast suggestion: choose whole wheat bread, boiled eggs, and unsweetened tea instead of simit."
                        : "Breakfast suggestion: build a balanced breakfast with eggs, cheese, and whole wheat options.";
            }
            return user != null && user.isHasDiabetes()
                    ? "Sabah önerisi: simit yerine tam buğday ekmeği, haşlanmış yumurta ve şekersiz çay."
                    : "Sabah önerisi: yumurta, peynir ve tam buğday ile dengeli kahvaltı.";
        }
        if (hour < 16) {
            if (english) {
                return user != null && user.isHasHypertension()
                        ? "Lunch suggestion: go with soup, grilled chicken, and a low-salt salad."
                        : "Lunch suggestion: make a balanced plate with soup, bulgur pilaf, and yogurt.";
            }
            return user != null && user.isHasHypertension()
                    ? "Öğle önerisi: çorba, ızgara tavuk ve az tuzlu salata."
                    : "Öğle önerisi: çorba, bulgur pilavı ve yoğurt ile dengeli tabak.";
        }
        return english
                ? "Dinner suggestion: finish the day with a light salad, soup, or grilled protein."
                : "Akşam önerisi: hafif salata, çorba veya ızgara protein ile günü kapatın.";
    }

    public static List<String> getSmartSuggestions(User user) {
        return getSmartSuggestions(user, resolveUserTimeZone(user), Integer.MAX_VALUE);
    }

    public static List<String> getSmartSuggestions(User user, TimeZone timeZone, int remainingCalories) {
        List<String> chips = new ArrayList<>();
        boolean english = isEnglish();
        TimeZone tz = timeZone != null ? timeZone : TimeZone.getDefault();
        int hour = Calendar.getInstance(tz).get(Calendar.HOUR_OF_DAY);
        if (remainingCalories > 0 && remainingCalories <= 300) {
            chips.add(english ? "Light soup" : "Hafif çorba");
            chips.add(english ? "Seasonal salad" : "Mevsim salatası");
            chips.add(english ? "Yogurt" : "Yoğurt");
        } else if (remainingCalories <= 0) {
            chips.add(english ? "Water and herbal tea" : "Su ve bitki çayı");
            chips.add(english ? "Skip snacks" : "Ara öğünü atla");
            chips.add(english ? "Light vegetables" : "Hafif sebze");
        } else if (hour < 5 || hour >= 22) {
            // Gece geç saatler
            chips.add(english ? "Herbal tea" : "Bitki çayı");
            chips.add(english ? "Light yogurt" : "Sade yoğurt");
            chips.add(english ? "Skip heavy meals" : "Ağır yemekten kaçın");
        } else if (hour < 11) {
            chips.add(english ? "Whole wheat toast" : "Tam buğday tost");
            chips.add(english ? "Boiled eggs" : "Haşlanmış yumurta");
            chips.add(english ? "Unsweetened tea" : "Şekersiz çay");
        } else if (hour < 16) {
            chips.add(english ? "Lentil soup" : "Mercimek çorbası");
            chips.add(english ? "Grilled chicken" : "Izgara tavuk");
            chips.add(english ? "Ayran" : "Ayran");
        } else {
            chips.add(english ? "Seasonal salad" : "Mevsim salatası");
            chips.add(english ? "Grilled fish" : "Izgara balık");
            chips.add(english ? "Yogurt" : "Yoğurt");
        }

        if (user != null && user.isHasDiabetes()) {
            chips.remove(english ? "Unsweetened tea" : "Şekersiz çay");
            chips.add(english ? "Snack: apple and cinnamon" : "Ara öğün: elma ve tarçın");
        }
        if (user != null && user.isHasHypertension()) {
            chips.add(english ? "Low-salt option" : "Az tuzlu seçim");
        }
        if (user != null && user.isHasCholesterol()) {
            chips.add(english ? "Choose grilled over fried" : "Kızartma yerine ızgara");
        }
        return chips;
    }

    public static String getDiseaseWarning(User user) {
        if (user == null) return null;
        boolean english = isEnglish();
        if (user.isHasDiabetes()) {
            return english
                    ? "Diabetes note: limit sugary drinks and dessert portions."
                    : "Diyabet notu: şekerli içecekler ve tatlılarda porsiyonu kısıtlayın.";
        }
        if (user.isHasHypertension()) {
            return english
                    ? "Blood pressure note: be careful with packaged foods high in salt."
                    : "Tansiyon notu: fazla tuz içeren paketli gıdalara dikkat edin.";
        }
        if (user.isHasCholesterol()) {
            return english
                    ? "Cholesterol note: prefer grilled foods over fried and processed meats."
                    : "Kolesterol notu: kızartma ve işlenmiş et yerine ızgara seçin.";
        }
        if (user.getOtherDiseases() != null && !user.getOtherDiseases().trim().isEmpty()) {
            return (english ? "Health note: " : "Sağlık notu: ") + user.getOtherDiseases().trim();
        }
        return null;
    }

    public static String buildLastMealAlternative(User user, MealEntry lastMeal, boolean limitExceeded) {
        if (lastMeal == null) return null;
        boolean english = isEnglish();
        String foodName = lastMeal.getFoodName() == null
                ? ""
                : lastMeal.getFoodName().toLowerCase(Locale.ROOT);

        String base;
        if (containsAny(foodName, "simit", "pogaca", "poğaça", "acma", "açma")) {
            base = english
                    ? "Last meal alternative: choose whole wheat toast, boiled eggs, or cheese with greens."
                    : "Son öğün alternatifi: tam buğday tost, haşlanmış yumurta veya yeşillikli peynir seçin.";
        } else if (containsAny(foodName, "tost", "sandvic", "sandviç")) {
            base = english
                    ? "Last meal alternative: make the sandwich lighter with whole wheat bread, less cheese, and salad."
                    : "Son öğün alternatifi: tam buğday ekmek, daha az peynir ve salata ile daha hafif seçim yapın.";
        } else if (containsAny(foodName, "baklava", "lokum", "sutlac", "sütlaç", "helva", "kek", "cikolata", "çikolata")) {
            base = english
                    ? "Last meal alternative: pick fruit, yogurt, or a smaller dessert portion."
                    : "Son öğün alternatifi: meyve, yoğurt veya daha küçük tatlı porsiyonu tercih edin.";
        } else if (containsAny(foodName, "cips", "kizartma", "kızartma", "tava", "hamburger", "pizza")) {
            base = english
                    ? "Last meal alternative: choose grilled protein, soup, or a seasonal salad."
                    : "Son öğün alternatifi: ızgara protein, çorba veya mevsim salatası seçin.";
        } else {
            return null;
        }

        if (user != null && user.isHasDiabetes()
                && containsAny(foodName, "baklava", "lokum", "sutlac", "sütlaç", "helva", "kek", "cikolata", "çikolata")) {
            return base + " " + (english
                    ? "Diabetes note: avoid sugary drinks with it."
                    : "Diyabet notu: yanında şekerli içecekten kaçının.");
        }

        if (limitExceeded) {
            return base + " " + (english
                    ? "You are over target today, so keep the next meal lighter."
                    : "Bugün hedefi aştığınız için sonraki öğünü daha hafif tutun.");
        }
        return base;
    }

    public static String buildAiReport(List<Integer> calories, int goal) {
        return buildAiReport(calories, goal, null);
    }

    public static String buildAiReport(List<Integer> calories, int goal, List<MealEntry> mealEntries) {
        boolean english = isEnglish();
        if (calories.isEmpty()) {
            return english
                    ? "Not enough data yet. As you log meals over the week, the analysis will appear here."
                    : "Yeterli veri yok. Bir hafta boyunca öğün ekledikçe analiz burada görünecek.";
        }

        int total = 0;
        int highCarbDays = 0;
        for (int consumed : calories) {
            total += consumed;
            if (consumed > goal * 0.85f) highCarbDays++;
        }
        int avg = Math.round(total / (float) calories.size());
        MacroReport macroReport = buildMacroReport(mealEntries);

        if (avg > goal) {
            return english
                    ? "This week your average intake went above your calorie goal. Lighten your evening meals."
                    : "Bu hafta ortalama kalori hedefinizin üstüne çıktı. Özellikle akşam öğünlerini hafifletin.";
        }
        if (macroReport.hasMacroData && macroReport.carbRatio >= 0.50f) {
            return english
                    ? "Your carbohydrate intake increased this week. Add more protein, soup, and salad balance to your meals."
                    : "Bu hafta karbonhidrat tüketiminiz arttı. Öğünlerde protein, çorba ve salata dengesini artırın.";
        }
        if (macroReport.totalCalories > 0 && macroReport.eveningCalorieRatio >= 0.45f) {
            return english
                    ? "Your evening calorie intake is high. Try lighter dinner portions and avoid late snacks."
                    : "Akşam saatlerinde kalori alımınız yüksek. Daha hafif akşam porsiyonları ve geç atıştırmalıktan kaçınmayı deneyin.";
        }
        if (!macroReport.hasMacroData && highCarbDays >= 3) {
            return english
                    ? "Several days were close to the calorie limit. Add more soup, salad, and protein balance."
                    : "Haftada birkaç gün kalori limitine yaklaşılmış. Çorba, salata ve protein dengesini artırın.";
        }
        return english
                ? "You are staying close to target this week. Keep your hydration and snack balance steady."
                : "Bu hafta hedefe yakın gidiyorsunuz. Su tüketimi ve ara öğün dengesini koruyun.";
    }

    private static MacroReport buildMacroReport(List<MealEntry> mealEntries) {
        MacroReport report = new MacroReport();
        if (mealEntries == null || mealEntries.isEmpty()) {
            return report;
        }

        int totalMacros = 0;
        int totalCarbs = 0;
        int totalCalories = 0;
        int eveningCalories = 0;

        for (MealEntry entry : mealEntries) {
            int protein = Math.max(0, entry.getProteinGrams());
            int carb = Math.max(0, entry.getCarbGrams());
            int fat = Math.max(0, entry.getFatGrams());
            int macroSum = protein + carb + fat;
            if (macroSum > 0) {
                report.hasMacroData = true;
                totalMacros += macroSum;
                totalCarbs += carb;
            }

            int calories = Math.max(0, entry.getTotalCalories());
            totalCalories += calories;
            if ("DINNER".equals(entry.getMealType()) || isEveningEntry(entry.getCreatedAt())) {
                eveningCalories += calories;
            }
        }

        report.totalCalories = totalCalories;
        report.carbRatio = totalMacros == 0 ? 0f : totalCarbs / (float) totalMacros;
        report.eveningCalorieRatio = totalCalories == 0 ? 0f : eveningCalories / (float) totalCalories;
        return report;
    }

    private static boolean isEveningEntry(long createdAt) {
        if (createdAt <= 0) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(createdAt);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour >= 18 || hour < 5;
    }

    private static class MacroReport {
        boolean hasMacroData;
        int totalCalories;
        float carbRatio;
        float eveningCalorieRatio;
    }

    private static String getMealPeriodTitle(TimeZone timeZone) {
        boolean english = isEnglish();
        TimeZone tz = timeZone != null ? timeZone : TimeZone.getDefault();
        int hour = Calendar.getInstance(tz).get(Calendar.HOUR_OF_DAY);
        // 00:00-04:59 → Geç saatler, 05:00-10:59 → Kahvaltı, 11:00-15:59 → Öğle, 16:00-21:59 → Akşam, 22:00+ → Geç
        if (hour < 5 || hour >= 22) return english ? "Late hours" : "Geç saatler";
        if (hour < 11) return english ? "Breakfast time" : "Kahvaltı zamanı";
        if (hour < 16) return english ? "Lunch time" : "Öğle zamanı";
        return english ? "Dinner time" : "Akşam zamanı";
    }

    private static boolean isLateHour(TimeZone timeZone) {
        TimeZone tz = timeZone != null ? timeZone : TimeZone.getDefault();
        int hour = Calendar.getInstance(tz).get(Calendar.HOUR_OF_DAY);
        return hour >= 22 || hour < 5;
    }

    private static TimeZone resolveUserTimeZone(User user) {
        String timezoneId = user != null && user.getTimezoneId() != null && !user.getTimezoneId().isEmpty()
                ? user.getTimezoneId()
                : TimeZone.getDefault().getID();
        return TimeZone.getTimeZone(timezoneId);
    }

    private static String buildTimezoneLabel(TimeZone timezone) {
        int offsetHours = timezone.getOffset(System.currentTimeMillis()) / (60 * 60 * 1000);
        return String.format(Locale.getDefault(), "%s UTC%+d", timezone.getID(), offsetHours);
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    public static class DashboardInsight {
        public String mealPeriodTitle;
        public String timezoneLabel;
        public int percent;
        public String limitMessage;
        public String healthyAlternative;
        public String diseaseWarning;
        public String lateMealWarning;
        public List<String> recommendationChips;
    }

    private static boolean isEnglish() {
        return Locale.getDefault().getLanguage().equals("en");
    }
}
