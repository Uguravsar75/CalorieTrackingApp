package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Tarih işlemleri için yardımcı sınıf.
 * Tüm tarihler "yyyy-MM-dd" formatında saklanır.
 */
public class DateUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("dd MMMM yyyy", new Locale("tr"));

    /** Bugünün tarihini "yyyy-MM-dd" formatında döner. */
    public static String getToday() {
        return DATE_FORMAT.format(new Date());
    }

    /** N gün öncesinin tarihini "yyyy-MM-dd" formatında döner. */
    public static String getDaysAgo(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -days);
        return DATE_FORMAT.format(cal.getTime());
    }

    /**
     * Günün saatine göre öğün tipini önerir.
     * 06:00 – 10:59 → BREAKFAST
     * 11:00 – 14:59 → LUNCH
     * 15:00 – 20:59 → DINNER
     * Diğer saatler → SNACK
     */
    public static String getSuggestedMealType() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 6 && hour < 11) return "BREAKFAST";
        if (hour >= 11 && hour < 15) return "LUNCH";
        if (hour >= 15 && hour < 21) return "DINNER";
        return "SNACK";
    }

    /** "yyyy-MM-dd" formatındaki tarihi kullanıcı dostu formata çevirir. */
    public static String toDisplayFormat(String dateStr) {
        try {
            Date date = DATE_FORMAT.parse(dateStr);
            return date != null ? DISPLAY_FORMAT.format(date) : dateStr;
        } catch (Exception e) {
            return dateStr;
        }
    }
}
