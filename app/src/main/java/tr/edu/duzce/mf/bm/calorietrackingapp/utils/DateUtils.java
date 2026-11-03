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

    // SimpleDateFormat thread-safe değil → ThreadLocal ile güvenli hale getirildi
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT_HOLDER =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()));

    private static SimpleDateFormat getDateFormat() {
        return DATE_FORMAT_HOLDER.get();
    }

    /** Bugünün tarihini "yyyy-MM-dd" formatında döner. */
    public static String getToday() {
        return getDateFormat().format(new Date());
    }

    /** N gün öncesinin tarihini "yyyy-MM-dd" formatında döner. */
    public static String getDaysAgo(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -days);
        return getDateFormat().format(cal.getTime());
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
            Date date = getDateFormat().parse(dateStr);
            if (date == null) return dateStr;
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            return displayFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }
}
