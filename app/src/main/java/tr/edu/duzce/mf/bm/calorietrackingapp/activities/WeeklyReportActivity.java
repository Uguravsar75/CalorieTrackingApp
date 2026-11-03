package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.os.Bundle;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import tr.edu.duzce.mf.bm.calorietrackingapp.utils.WeeklyChartView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.adapters.WeeklyReportAdapter;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.dao.MealEntryDao;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.MealEntry;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.DateUtils;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.RecommendationEngine;

public class WeeklyReportActivity extends AppCompatActivity {

    private WeeklyReportAdapter adapter;
    private AppRepository repository;
    private TextView tvWeeklyAverage, tvTopFood, tvAdherenceRate, tvAiReport, tvStreakValue, tvStreakFire;
    private TextView tvCalendarTitle, tvMonthlyAverage, tvMonthlyAdherence, tvMonthlyTopFood;
    private GridLayout gridCalendar;
    private ImageButton btnPrevMonth, btnNextMonth;
    private WeeklyChartView weeklyChart;
    private TabLayout tabLayout;
    private LinearLayout layoutWeekly, layoutMonthly;

    /** Gösterilen takvim ayını temsil eder. Başlangıçta mevcut ay. */
    private int displayedYear;
    private int displayedMonth; // 1-12

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_report);

        repository = new AppRepository(getApplicationContext());

        // Başlangıç değerleri: mevcut ay
        Calendar now = Calendar.getInstance();
        displayedYear = now.get(Calendar.YEAR);
        displayedMonth = now.get(Calendar.MONTH) + 1;

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvWeeklyReport);
        adapter = new WeeklyReportAdapter(this, new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        tvWeeklyAverage = findViewById(R.id.tvWeeklyAverage);
        tvTopFood = findViewById(R.id.tvTopFood);
        tvAdherenceRate = findViewById(R.id.tvAdherenceRate);
        tvAiReport = findViewById(R.id.tvAiReport);
        tvStreakValue = findViewById(R.id.tvStreakValue);
        tvStreakFire = findViewById(R.id.tvStreakFire);
        tvCalendarTitle = findViewById(R.id.tvCalendarTitle);
        tvMonthlyAverage = findViewById(R.id.tvMonthlyAverage);
        tvMonthlyAdherence = findViewById(R.id.tvMonthlyAdherence);
        tvMonthlyTopFood = findViewById(R.id.tvMonthlyTopFood);
        gridCalendar = findViewById(R.id.gridCalendar);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        weeklyChart  = findViewById(R.id.weeklyChart);
        tabLayout    = findViewById(R.id.tabLayout);
        layoutWeekly = findViewById(R.id.layoutWeekly);
        layoutMonthly = findViewById(R.id.layoutMonthly);

        btnPrevMonth.setOnClickListener(v -> navigateMonth(-1));
        btnNextMonth.setOnClickListener(v -> navigateMonth(+1));

        setupTabs();
        loadReport();
    }

    /** Haftalık / Aylık tab geçişini yönetir. */
    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_weekly));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_monthly));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutWeekly.setVisibility(View.VISIBLE);
                    layoutMonthly.setVisibility(View.GONE);
                } else {
                    layoutWeekly.setVisibility(View.GONE);
                    layoutMonthly.setVisibility(View.VISIBLE);
                    // Aylık taba ilk geçişte veya her geçişte takvimi yenile
                    loadCalendarSection();
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Haftalık kalori chart'ını günceller.
     * @param calorieValues 7 günlük tüketilen kalori (veri olmayanlar 0)
     * @param goal          günlük kalori hedefi
     */
    private void setupChart(List<Integer> calorieValues, int goal) {
        if (weeklyChart == null) return;

        // 7 günlük listeyi eksiksiz hale getir (eksik günleri 0 ile doldur)
        List<Integer> chartData = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            chartData.add(i < calorieValues.size() ? calorieValues.get(i) : 0);
        }

        // Gün etiketleri: bugünden 6 gün öncesine kadar
        List<String> labels = new ArrayList<>();
        String[] dayNames = getWeekdayLabelsShort();
        for (int i = 6; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -i);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0=Paz
            labels.add(dayNames[dayOfWeek]);
        }

        weeklyChart.setData(chartData, goal, labels);
    }

    private String[] getWeekdayLabelsShort() {
        return new String[]{
            getString(R.string.weekday_short_sun),
            getString(R.string.weekday_short_mon),
            getString(R.string.weekday_short_tue),
            getString(R.string.weekday_short_wed),
            getString(R.string.weekday_short_thu),
            getString(R.string.weekday_short_fri),
            getString(R.string.weekday_short_sat)
        };
    }

    /**
     * Takvim ayını değiştirir ve sadece takvim + aylık istatistikleri yeniler.
     * @param delta -1 önceki ay, +1 sonraki ay
     */
    private void navigateMonth(int delta) {
        displayedMonth += delta;
        if (displayedMonth < 1) {
            displayedMonth = 12;
            displayedYear--;
        } else if (displayedMonth > 12) {
            displayedMonth = 1;
            displayedYear++;
        }
        updateNextMonthButton();
        loadCalendarSection();
    }

    /**
     * Sonraki ay butonu: Mevcut gerçek ayı aşmaya izin verme.
     */
    private void updateNextMonthButton() {
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1;

        boolean isCurrentOrFuture = (displayedYear > currentYear)
                || (displayedYear == currentYear && displayedMonth >= currentMonth);
        btnNextMonth.setEnabled(!isCurrentOrFuture);
        btnNextMonth.setAlpha(isCurrentOrFuture ? 0.3f : 1.0f);
    }

    private void loadReport() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = repository.getUser();
            int goal = user != null ? user.getDailyCalorieGoal() : 2000;
            String today = DateUtils.getToday();
            String sevenDaysAgo = DateUtils.getDaysAgo(6);

            List<MealEntryDao.DailyCalorieSummary> summaries = repository.getDailyCaloriesBetween(sevenDaysAgo, today);
            List<MealEntry> mealEntries = repository.getMealEntriesBetweenDates(sevenDaysAgo, today);

            Map<String, Integer> calorieMap = new HashMap<>();
            for (MealEntryDao.DailyCalorieSummary summary : summaries) {
                calorieMap.put(summary.date, summary.totalCalories);
            }

            List<WeeklyReportAdapter.ReportItem> items = new ArrayList<>();
            List<Integer> calorieValues = new ArrayList<>();
            int adherenceDays = 0;
            int currentStreak = 0;
            int bestStreak = 0;

            List<String> datesAscending = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                datesAscending.add(DateUtils.getDaysAgo(i));
            }

            for (String date : datesAscending) {
                WeeklyReportAdapter.ReportItem item = new WeeklyReportAdapter.ReportItem();
                item.date = date;
                item.goal = goal;
                if (calorieMap.containsKey(date)) {
                    item.consumed = calorieMap.get(date);
                    item.hasData = true;
                    item.adherent = item.consumed <= goal;
                    calorieValues.add(item.consumed);
                    if (item.adherent) {
                        adherenceDays++;
                        currentStreak++;
                        bestStreak = Math.max(bestStreak, currentStreak);
                    } else {
                        currentStreak = 0;
                    }
                    item.streakCount = currentStreak;
                } else {
                    item.consumed = 0;
                    item.hasData = false;
                    item.adherent = false;
                    item.streakCount = 0;
                    currentStreak = 0;
                }
                items.add(item);
            }

            int weeklyAverage = calorieValues.isEmpty() ? 0
                    : Math.round(sum(calorieValues) / (float) calorieValues.size());
            int adherenceRate = Math.round((adherenceDays / 7f) * 100f);
            String aiReport = RecommendationEngine.buildAiReport(calorieValues, goal, mealEntries);
            String topFoodsText = buildTopFoodsText(mealEntries);
            final int finalBestStreak = bestStreak;
            String streakFire = buildFireStreak(bestStreak);

            runOnUiThread(() -> {
                adapter.updateData(items);
                tvWeeklyAverage.setText(getString(R.string.report_average_format, weeklyAverage));
                tvTopFood.setText(topFoodsText);
                tvAdherenceRate.setText(getString(R.string.report_adherence_rate_format, adherenceRate));
                tvAiReport.setText(aiReport);
                tvStreakValue.setText(getString(R.string.report_streak_value_format, finalBestStreak));
                tvStreakFire.setText(streakFire);
                setupChart(calorieValues, goal);
            });
        });
    }

    /**
     * Takvim ve aylık istatistikleri displayedYear/displayedMonth'a göre yükler.
     * Ay navigasyonu her kullandığında sadece bu metot çağrılır.
     */
    private void loadCalendarSection() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = repository.getUser();
            int goal = user != null ? user.getDailyCalorieGoal() : 2000;

            MonthRange monthRange = buildMonthRange(displayedYear, displayedMonth);
            List<MealEntryDao.DailyCalorieSummary> monthlySummaries =
                    repository.getDailyCaloriesBetween(monthRange.startDate, monthRange.endDate);
            List<MealEntry> monthlyMealEntries =
                    repository.getMealEntriesBetweenDates(monthRange.startDate, monthRange.endDate);

            Map<String, Integer> monthlyCalorieMap = new HashMap<>();
            for (MealEntryDao.DailyCalorieSummary summary : monthlySummaries) {
                monthlyCalorieMap.put(summary.date, summary.totalCalories);
            }
            Map<String, List<MealEntry>> monthlyMealMap = groupMealsByDate(monthlyMealEntries);

            MonthlyStats monthlyStats = buildMonthlyStats(monthRange, monthlyCalorieMap, monthlyMealEntries, goal);

            runOnUiThread(() -> {
                tvMonthlyAverage.setText(getString(R.string.report_average_format, monthlyStats.averageCalories));
                tvMonthlyAdherence.setText(getString(R.string.report_adherence_rate_format, monthlyStats.adherenceRate));
                tvMonthlyTopFood.setText(monthlyStats.topFoodsText);
                renderCalendar(monthRange, monthlyCalorieMap, monthlyMealMap, goal);
                updateNextMonthButton();
            });
        });
    }

    private MonthlyStats buildMonthlyStats(
            MonthRange monthRange,
            Map<String, Integer> caloriesByDate,
            List<MealEntry> mealEntries,
            int goal
    ) {
        MonthlyStats stats = new MonthlyStats();
        List<Integer> calorieValues = new ArrayList<>();
        int adherentDays = 0;

        for (int day = 1; day <= monthRange.elapsedDays; day++) {
            String date = String.format(Locale.US, "%04d-%02d-%02d",
                    monthRange.year, monthRange.month, day);
            if (caloriesByDate.containsKey(date)) {
                int consumed = caloriesByDate.get(date);
                calorieValues.add(consumed);
                if (consumed <= goal) {
                    adherentDays++;
                }
            }
        }

        stats.averageCalories = calorieValues.isEmpty()
                ? 0
                : Math.round(sum(calorieValues) / (float) calorieValues.size());
        stats.adherenceRate = monthRange.elapsedDays == 0
                ? 0
                : Math.round((adherentDays / (float) monthRange.elapsedDays) * 100f);
        stats.topFoodsText = buildTopFoodsText(mealEntries);
        return stats;
    }

    private void renderCalendar(
            MonthRange monthRange,
            Map<String, Integer> caloriesByDate,
            Map<String, List<MealEntry>> mealsByDate,
            int goal
    ) {
        gridCalendar.removeAllViews();
        tvCalendarTitle.setText(monthRange.title);

        String[] weekDays = getWeekdayLabels();
        for (String weekDay : weekDays) {
            TextView label = makeCalendarTextView();
            label.setText(weekDay);
            label.setTypeface(Typeface.DEFAULT_BOLD);
            label.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            gridCalendar.addView(label);
        }

        for (int i = 1; i < monthRange.firstDayOfWeek; i++) {
            TextView empty = makeCalendarTextView();
            empty.setVisibility(View.INVISIBLE);
            gridCalendar.addView(empty);
        }

        for (int day = 1; day <= monthRange.daysInMonth; day++) {
            String date = String.format(Locale.US, "%04d-%02d-%02d",
                    monthRange.year, monthRange.month, day);
            int consumed = caloriesByDate.containsKey(date) ? caloriesByDate.get(date) : 0;
            List<MealEntry> dayMeals = mealsByDate.containsKey(date)
                    ? mealsByDate.get(date)
                    : Collections.emptyList();

            TextView dayView = makeCalendarTextView();
            dayView.setText(String.valueOf(day));
            dayView.setTextColor(ContextCompat.getColor(this, R.color.white));
            dayView.setTypeface(Typeface.DEFAULT_BOLD);
            dayView.setBackground(makeCalendarBackground(resolveCalendarColor(consumed, goal, dayMeals.isEmpty())));
            dayView.setOnClickListener(v -> showDaySummary(date, consumed, goal, dayMeals));
            gridCalendar.addView(dayView);
        }
    }

    private TextView makeCalendarTextView() {
        TextView view = new TextView(this);
        int size = Math.round(42 * getResources().getDisplayMetrics().density);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = size;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(4, 4, 4, 4);
        view.setLayoutParams(params);
        view.setGravity(Gravity.CENTER);
        view.setTextSize(13f);
        return view;
    }

    private GradientDrawable makeCalendarBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(14 * getResources().getDisplayMetrics().density);
        drawable.setColor(color);
        return drawable;
    }

    private int resolveCalendarColor(int consumed, int goal, boolean noMeals) {
        if (noMeals) return ContextCompat.getColor(this, R.color.report_grey);
        if (consumed > goal) return ContextCompat.getColor(this, R.color.report_red);
        if (consumed > goal * 0.9f) return ContextCompat.getColor(this, R.color.report_yellow);
        return ContextCompat.getColor(this, R.color.report_green);
    }

    private void showDaySummary(String date, int consumed, int goal, List<MealEntry> meals) {
        StringBuilder message = new StringBuilder();
        message.append(DateUtils.toDisplayFormat(date)).append("\n\n");
        message.append(getString(R.string.report_calendar_consumed_format, consumed)).append("\n");
        message.append(getString(R.string.report_calendar_goal_format, goal)).append("\n\n");
        message.append(getString(R.string.report_calendar_meals_title)).append("\n");

        if (meals.isEmpty()) {
            message.append(getString(R.string.report_calendar_meals_empty));
        } else {
            for (MealEntry meal : meals) {
                message.append("- ")
                        .append(meal.getFoodName())
                        .append(" (")
                        .append(meal.getTotalCalories())
                        .append(" kcal)");
                int protein = Math.max(0, meal.getProteinGrams());
                int carb = Math.max(0, meal.getCarbGrams());
                int fat = Math.max(0, meal.getFatGrams());
                if (protein > 0 || carb > 0 || fat > 0) {
                    message.append(" - ")
                            .append(getString(R.string.meal_macro_summary_format, protein, carb, fat));
                }
                if (meal.isAiEstimated()) {
                    message.append(" - ").append(getString(R.string.meal_ai_estimated));
                }
                message.append("\n");
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.report_calendar_day_summary)
                .setMessage(message.toString().trim())
                .setPositiveButton(R.string.btn_ok, null)
                .show();
    }

    private Map<String, List<MealEntry>> groupMealsByDate(List<MealEntry> mealEntries) {
        Map<String, List<MealEntry>> result = new HashMap<>();
        for (MealEntry entry : mealEntries) {
            String date = entry.getDate();
            if (!result.containsKey(date)) {
                result.put(date, new ArrayList<>());
            }
            result.get(date).add(entry);
        }
        return result;
    }

    /**
     * Verilen yıl ve ay için MonthRange oluşturur.
     * Geçmiş aylar için elapsedDays = daysInMonth (ay tamamen geçmiş).
     * Mevcut ay için elapsedDays = bugünün günü.
     */
    private MonthRange buildMonthRange(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        MonthRange range = new MonthRange();
        range.year = year;
        range.month = month;
        range.daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        range.firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        range.startDate = String.format(Locale.US, "%04d-%02d-01", year, month);
        range.endDate = String.format(Locale.US, "%04d-%02d-%02d", year, month, range.daysInMonth);
        range.title = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.getTime());

        // Mevcut ay mı yoksa geçmiş ay mı?
        Calendar now = Calendar.getInstance();
        int nowYear = now.get(Calendar.YEAR);
        int nowMonth = now.get(Calendar.MONTH) + 1;
        if (year == nowYear && month == nowMonth) {
            range.elapsedDays = now.get(Calendar.DAY_OF_MONTH);
        } else {
            range.elapsedDays = range.daysInMonth;
        }

        return range;
    }

    private String[] getWeekdayLabels() {
        return new String[]{
            getString(R.string.weekday_short_sun),
            getString(R.string.weekday_short_mon),
            getString(R.string.weekday_short_tue),
            getString(R.string.weekday_short_wed),
            getString(R.string.weekday_short_thu),
            getString(R.string.weekday_short_fri),
            getString(R.string.weekday_short_sat)
        };
    }

    private String buildTopFoodsText(List<MealEntry> mealEntries) {
        if (mealEntries.isEmpty()) {
            return getString(R.string.report_top_food_empty);
        }

        Map<String, Integer> counts = new HashMap<>();
        for (MealEntry entry : mealEntries) {
            String foodName = entry.getFoodName();
            counts.put(foodName, counts.getOrDefault(foodName, 0) + 1);
        }

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        StringBuilder builder = new StringBuilder();
        int limit = Math.min(3, sorted.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = sorted.get(i);
            if (i > 0)
                builder.append("\n");
            builder.append(i + 1)
                    .append(". ")
                    .append(entry.getKey())
                    .append(" (")
                    .append(entry.getValue())
                    .append(")");
        }
        return builder.toString();
    }

    private String buildFireStreak(int streak) {
        if (streak <= 0) {
            return getString(R.string.report_streak_empty);
        }
        StringBuilder builder = new StringBuilder();
        int count = Math.max(1, Math.min(streak, 7));
        for (int i = 0; i < count; i++) {
            builder.append("🔥");
        }
        return builder.toString();
    }

    private int sum(List<Integer> values) {
        int total = 0;
        for (int value : values) {
            total += value;
        }
        return total;
    }

    private static class MonthRange {
        int year;
        int month;
        int daysInMonth;
        int elapsedDays;
        int firstDayOfWeek;
        String startDate;
        String endDate;
        String title;
    }

    private static class MonthlyStats {
        int averageCalories;
        int adherenceRate;
        String topFoodsText;
    }
}
