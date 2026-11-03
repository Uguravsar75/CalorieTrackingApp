package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.adapters.MealEntryAdapter;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.MealEntry;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.DailyStatsManager;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.DateUtils;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.LocationTimeProvider;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.NotificationHelper;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.RecommendationEngine;

public class MainActivity extends AppCompatActivity {

    private TextView tvTodayDate, tvMealPeriod, tvTimezoneContext, tvSmartInsight, tvGoal,
            tvConsumed, tvRemaining, tvProgressPercent, tvWaterValue, tvStepsValue,
            tvExerciseValue, tvBreakfastCount, tvLunchCount, tvDinnerCount, tvSnackCount,
            tvHealthyAlternative, tvDiseaseNote, tvWarningText, tvNoMeals;
    private CircularProgressIndicator progressCalorie;
    private ChipGroup chipSuggestions;
    private MaterialCardView cardWarning;
    private RecyclerView rvMealEntries;
    private MealEntryAdapter adapter;

    private AppRepository repository;
    private DailyStatsManager dailyStatsManager;
    private String today;
    private LocationTimeProvider.LocationTimeContext locationTimeContext;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new AppRepository(getApplicationContext());
        dailyStatsManager = new DailyStatsManager(this);
        today = DateUtils.getToday();
        locationTimeContext = LocationTimeProvider.defaultContext();
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    refreshLocationContext();
                    requestNotificationPermissionIfNeeded();
                }
        );
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (!granted && !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                        showNotificationSettingsPrompt();
                    }
                }
        );

        initViews();
        setupToolbar();
        setupNavigation();
        setupLocationDetection();
        NotificationHelper.createNotificationChannel(this);
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews() {
        tvTodayDate = findViewById(R.id.tvTodayDate);
        tvMealPeriod = findViewById(R.id.tvMealPeriod);
        tvTimezoneContext = findViewById(R.id.tvTimezoneContext);
        tvSmartInsight = findViewById(R.id.tvSmartInsight);
        tvGoal = findViewById(R.id.tvGoal);
        tvConsumed = findViewById(R.id.tvConsumed);
        tvRemaining = findViewById(R.id.tvRemaining);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvWaterValue = findViewById(R.id.tvWaterValue);
        tvStepsValue = findViewById(R.id.tvStepsValue);
        tvExerciseValue = findViewById(R.id.tvExerciseValue);
        tvBreakfastCount = findViewById(R.id.tvBreakfastCount);
        tvLunchCount = findViewById(R.id.tvLunchCount);
        tvDinnerCount = findViewById(R.id.tvDinnerCount);
        tvSnackCount = findViewById(R.id.tvSnackCount);
        tvHealthyAlternative = findViewById(R.id.tvHealthyAlternative);
        tvDiseaseNote = findViewById(R.id.tvDiseaseNote);
        tvWarningText = findViewById(R.id.tvWarningText);
        tvNoMeals = findViewById(R.id.tvNoMeals);
        progressCalorie = findViewById(R.id.progressCalorie);
        chipSuggestions = findViewById(R.id.chipSuggestions);
        cardWarning = findViewById(R.id.cardWarning);
        rvMealEntries = findViewById(R.id.rvMealEntries);

        adapter = new MealEntryAdapter(this, new ArrayList<>());
        rvMealEntries.setLayoutManager(new LinearLayoutManager(this));
        rvMealEntries.setAdapter(adapter);

        // Tek tıklama → Düzenle / Sil bottom sheet
        adapter.setClickListener(this::showMealOptionsBottomSheet);

        MaterialButton btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(v -> startActivity(new Intent(this, CameraActivity.class)));

        MaterialButton btnWeeklyReport = findViewById(R.id.btnWeeklyReport);
        btnWeeklyReport.setOnClickListener(v -> startActivity(new Intent(this, WeeklyReportActivity.class)));

        MaterialButton btnAddSuggested = findViewById(R.id.btnAddSuggestedMeal);
        btnAddSuggested.setOnClickListener(v -> startActivity(new Intent(this, AddMealActivity.class)));

        FloatingActionButton fabAddMeal = findViewById(R.id.fabAddMeal);
        fabAddMeal.setOnClickListener(v -> startActivity(new Intent(this, AddMealActivity.class)));

        // Su / Adım / Egzersiz kartlarına tıklanınca manuel giriş dialogu açılır
        tvWaterValue.setOnClickListener(v ->
                showStatInputDialog("water", getString(R.string.dialog_enter_water)));
        tvStepsValue.setOnClickListener(v ->
                showStatInputDialog("steps", getString(R.string.dialog_enter_steps)));
        tvExerciseValue.setOnClickListener(v ->
                showStatInputDialog("exercise", getString(R.string.dialog_enter_exercise)));
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_center_placeholder) {
                return false;
            }
            if (id == R.id.nav_search) {
                startActivity(new Intent(this, FoodSearchActivity.class));
                return false;
            }
            if (id == R.id.nav_reports) {
                startActivity(new Intent(this, WeeklyReportActivity.class));
                return false;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return false;
            }
            return true;
        });
    }

    private void setupLocationDetection() {
        if (LocationTimeProvider.hasLocationPermission(this)) {
            refreshLocationContext();
            requestNotificationPermissionIfNeeded();
            return;
        }

        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        });
    }

    private void refreshLocationContext() {
        if (!LocationTimeProvider.hasLocationPermission(this)) {
            locationTimeContext = LocationTimeProvider.defaultContext();
            loadData();
            return;
        }

        LocationTimeProvider.resolve(this, context -> runOnUiThread(() -> {
            locationTimeContext = context;
            loadData();
        }));
    }

    private void requestNotificationPermissionIfNeeded() {
        if (NotificationHelper.hasNotificationPermission(this)) {
            return;
        }

        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.notification_permission_title)
                    .setMessage(R.string.notification_permission_message)
                    .setPositiveButton(R.string.btn_continue,
                            (dialog, which) -> notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS))
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();
        } else {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void showNotificationSettingsPrompt() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.notification_permission_title)
                .setMessage(R.string.notification_permission_settings_message)
                .setPositiveButton(R.string.notification_open_settings,
                        (dialog, which) -> startActivity(NotificationHelper.buildNotificationSettingsIntent(this)))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void loadData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User currentUser = repository.getUser();
            List<MealEntry> entries = repository.getMealEntriesByDate(today);
            int consumed = repository.getTotalCaloriesForDate(today);

            // Firestore'dan günlük istatistikleri çek, lokal cache'i güncelle
            int[] stats = repository.getDailyStats(today);
            if (stats[0] > 0 || stats[1] > 0 || stats[2] > 0) {
                dailyStatsManager.setWaterGlasses(today, stats[0]);
                dailyStatsManager.setSteps(today, stats[1]);
                dailyStatsManager.setExerciseKcal(today, stats[2]);
            }

            runOnUiThread(() -> updateUI(currentUser, entries, consumed));
        });
    }

    private void updateUI(User user, List<MealEntry> entries, int consumed) {
        if (user == null) return;

        int goal = user.getDailyCalorieGoal();
        int remaining = Math.max(goal - consumed, 0);
        RecommendationEngine.DashboardInsight insight =
                RecommendationEngine.buildDashboardInsight(
                        user,
                        consumed,
                        goal,
                        locationTimeContext.timeZone,
                        locationTimeContext.label
                );

        tvTodayDate.setText(DateUtils.toDisplayFormat(today));
        tvMealPeriod.setText(insight.mealPeriodTitle);
        tvTimezoneContext.setText(insight.timezoneLabel);
        tvSmartInsight.setText(insight.healthyAlternative);
        tvGoal.setText(String.valueOf(goal));
        tvConsumed.setText(String.valueOf(consumed));
        if (consumed > goal) {
            tvRemaining.setText(getString(R.string.dashboard_over_limit_format, consumed - goal));
            tvRemaining.setTextColor(ContextCompat.getColor(this, R.color.warning_red));
        } else {
            tvRemaining.setText(String.valueOf(remaining));
            tvRemaining.setTextColor(ContextCompat.getColor(this, R.color.calorie_warning));
        }
        tvProgressPercent.setText(getString(R.string.dashboard_percent_format, insight.percent));
        progressCalorie.setProgress(Math.min(insight.percent, 100));

        // Gerçek istatistikler — kullanıcı manuel giriş yapar
        int water = dailyStatsManager.getWaterGlasses(today);
        int steps = dailyStatsManager.getSteps(today);
        int exercise = dailyStatsManager.getExerciseKcal(today);
        tvWaterValue.setText(getString(R.string.dashboard_water_format, water));
        tvStepsValue.setText(getString(R.string.dashboard_steps_format, steps));
        tvExerciseValue.setText(getString(R.string.dashboard_exercise_format, exercise));

        int breakfast = 0, lunch = 0, dinner = 0, snack = 0;
        for (MealEntry entry : entries) {
            String mealType = entry.getMealType();
            if ("BREAKFAST".equals(mealType)) breakfast++;
            else if ("LUNCH".equals(mealType)) lunch++;
            else if ("DINNER".equals(mealType)) dinner++;
            else snack++;
        }
        tvBreakfastCount.setText(String.valueOf(breakfast));
        tvLunchCount.setText(String.valueOf(lunch));
        tvDinnerCount.setText(String.valueOf(dinner));
        tvSnackCount.setText(String.valueOf(snack));

        String lastMealAlternative = RecommendationEngine.buildLastMealAlternative(
                user,
                findLastMeal(entries),
                consumed > goal
        );
        tvHealthyAlternative.setText(lastMealAlternative != null
                ? lastMealAlternative
                : insight.lateMealWarning != null
                    ? insight.lateMealWarning
                    : getString(R.string.dashboard_default_balance_note));
        tvDiseaseNote.setText(insight.diseaseWarning != null
                ? insight.diseaseWarning
                : getString(R.string.dashboard_no_health_warning));

        cardWarning.setVisibility(consumed > goal ? View.VISIBLE : View.GONE);
        tvWarningText.setText(insight.limitMessage != null
                ? insight.limitMessage
                : getString(R.string.warning_limit_exceeded));

        if (consumed > goal) {
            NotificationHelper.sendLimitExceededNotification(this, consumed - goal);
        } else if (goal - consumed > 0 && goal - consumed <= 300) {
            NotificationHelper.sendGoalCloseNotification(this, goal - consumed);
        }

        renderSuggestionChips(insight.recommendationChips);
        adapter.updateData(entries);
        tvNoMeals.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
        rvMealEntries.setVisibility(entries.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void renderSuggestionChips(List<String> suggestions) {
        chipSuggestions.removeAllViews();
        for (String suggestion : suggestions) {
            Chip chip = new Chip(this);
            chip.setText(suggestion);
            chip.setCheckable(false);
            chip.setClickable(false);
            chipSuggestions.addView(chip);
        }
    }

    private MealEntry findLastMeal(List<MealEntry> entries) {
        MealEntry latest = null;
        for (MealEntry entry : entries) {
            if (latest == null || entry.getCreatedAt() > latest.getCreatedAt()) {
                latest = entry;
            }
        }
        return latest;
    }

    private void showStatInputDialog(String type, String title) {
        if ("water".equals(type)) {
            showWaterCounterDialog();
        } else {
            showNumberInputDialog(type, title);
        }
    }

    /**
     * Su bardağı için +/- sayı dialogu.
     * Tema-uyumlu renkler kullanır (light / dark mode).
     */
    private void showWaterCounterDialog() {
        int dp = (int) getResources().getDisplayMetrics().density;
        final int[] count = {dailyStatsManager.getWaterGlasses(today)};

        // Tema renklerini çöz
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.textColorPrimary, tv, true);
        int colorText = ContextCompat.getColor(this, tv.resourceId);

        getTheme().resolveAttribute(android.R.attr.textColorSecondary, tv, true);
        int colorHint = ContextCompat.getColor(this, tv.resourceId);

        // Buton arka planı için colorSecondaryContainer
        int colorBtnBg;
        if (getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorSecondaryContainer, tv, true)) {
            colorBtnBg = tv.data;
        } else {
            colorBtnBg = 0xFF4CAF50; // fallback
        }

        // Buton metin rengi için colorOnSecondaryContainer
        int colorBtnText;
        if (getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorOnSecondaryContainer, tv, true)) {
            colorBtnText = tv.data;
        } else {
            colorBtnText = colorText;
        }

        // Ana container
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp * 24, dp * 16, dp * 24, dp * 8);

        // Su damlacığı emojisi
        TextView tvEmoji = new TextView(this);
        tvEmoji.setText(R.string.emoji_water);
        tvEmoji.setTextSize(52f);
        tvEmoji.setGravity(Gravity.CENTER);
        root.addView(tvEmoji);

        // Sayaç satırı: [ − ]  sayı  [ + ]
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = dp * 16;
        root.addView(row, rowLp);

        // Dairesel buton yardımcısı
        int btnSize = dp * 54;
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(btnSize, btnSize);

        // − butonu
        TextView btnMinus = makeCircleButton("−", btnSize, colorBtnBg, colorBtnText);
        row.addView(btnMinus, btnLp);

        // Sayı gösterimi
        TextView tvCount = new TextView(this);
        tvCount.setText(String.valueOf(count[0]));
        tvCount.setTextSize(44f);
        tvCount.setTypeface(Typeface.DEFAULT_BOLD);
        tvCount.setTextColor(colorText);
        tvCount.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams countLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        countLp.leftMargin = dp * 8;
        countLp.rightMargin = dp * 8;
        row.addView(tvCount, countLp);

        // + butonu
        TextView btnPlus = makeCircleButton("+", btnSize, colorBtnBg, colorBtnText);
        row.addView(btnPlus, btnLp);

        // "/ 8 bardak" etiketi
        TextView tvLabel = new TextView(this);
        tvLabel.setText(getString(R.string.water_counter_label));
        tvLabel.setTextSize(14f);
        tvLabel.setGravity(Gravity.CENTER);
        tvLabel.setTextColor(colorHint);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        labelLp.topMargin = dp * 8;
        labelLp.bottomMargin = dp * 4;
        root.addView(tvLabel, labelLp);

        // Tıklama dinleyicileri
        btnMinus.setOnClickListener(v -> {
            if (count[0] > 0) { count[0]--; tvCount.setText(String.valueOf(count[0])); }
        });
        btnPlus.setOnClickListener(v -> {
            if (count[0] < 20) { count[0]++; tvCount.setText(String.valueOf(count[0])); }
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.dialog_enter_water))
                .setView(root)
                .setPositiveButton(getString(R.string.btn_save),
                        (dialog, which) -> saveStats("water", count[0]))
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    /** Dairesel arka planlı, tema-uyumlu buton TextView'u oluşturur. */
    private TextView makeCircleButton(String label, int sizePx, int bgColor, int textColor) {
        TextView btn = new TextView(this);
        btn.setText(label);
        btn.setTextSize(26f);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setTextColor(textColor);
        btn.setGravity(Gravity.CENTER);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(bgColor);
        btn.setBackground(circle);
        // Ripple etkisi için
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless,
                outValue, true);
        btn.setForeground(ContextCompat.getDrawable(this, outValue.resourceId));
        btn.setClickable(true);
        btn.setFocusable(true);
        return btn;
    }

    /**
     * Adım / egzersiz için sayısal klavye girişi dialogu.
     */
    private void showNumberInputDialog(String type, String title) {
        int dpPad = (int) (16 * getResources().getDisplayMetrics().density);

        TextInputLayout til = new TextInputLayout(this, null,
                com.google.android.material.R.attr.textInputOutlinedStyle);
        TextInputEditText et = new TextInputEditText(til.getContext());
        et.setInputType(InputType.TYPE_CLASS_NUMBER);

        int current = "steps".equals(type)
                ? dailyStatsManager.getSteps(today)
                : dailyStatsManager.getExerciseKcal(today);
        til.setHint("steps".equals(type)
                ? getString(R.string.dashboard_steps_title)
                : getString(R.string.dashboard_exercise_title));

        if (current > 0) et.setText(String.valueOf(current));
        et.setSelection(et.getText() != null ? et.getText().length() : 0);
        til.addView(et);
        til.setPadding(dpPad, dpPad / 2, dpPad, dpPad / 2);

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(til)
                .setPositiveButton(getString(R.string.btn_save), (dialog, which) -> {
                    try {
                        int value = Integer.parseInt(
                                et.getText() != null ? et.getText().toString().trim() : "0");
                        saveStats(type, value);
                    } catch (NumberFormatException ignored) {}
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    /**
     * Ortak kaydetme mantığı: lokal cache → UI güncelle → Firestore senkronize et.
     */
    private void saveStats(String type, int value) {
        // 1. Lokal cache
        if ("water".equals(type)) {
            dailyStatsManager.setWaterGlasses(today, value);
        } else if ("steps".equals(type)) {
            dailyStatsManager.setSteps(today, value);
        } else {
            dailyStatsManager.setExerciseKcal(today, value);
        }

        // 2. UI güncelle
        tvWaterValue.setText(getString(R.string.dashboard_water_format,
                dailyStatsManager.getWaterGlasses(today)));
        tvStepsValue.setText(getString(R.string.dashboard_steps_format,
                dailyStatsManager.getSteps(today)));
        tvExerciseValue.setText(getString(R.string.dashboard_exercise_format,
                dailyStatsManager.getExerciseKcal(today)));

        // 3. Firestore — arka planda
        int w = dailyStatsManager.getWaterGlasses(today);
        int s = dailyStatsManager.getSteps(today);
        int e = dailyStatsManager.getExerciseKcal(today);
        AppDatabase.databaseWriteExecutor.execute(() ->
                repository.saveDailyStats(today, w, s, e)
        );
    }

    // ── Öğün Bottom Sheet ───────────────────────────────────────────────────────

    /**
     * Öğüne tıklanınca düzenle / sil seçenekleri sunan bottom sheet açılır.
     */
    private void showMealOptionsBottomSheet(MealEntry entry) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_meal_options, null);
        bottomSheet.setContentView(view);

        String mealLabel = getMealLabel(entry.getMealType());
        ((TextView) view.findViewById(R.id.tvBottomSheetTitle)).setText(entry.getFoodName());
        ((TextView) view.findViewById(R.id.tvBottomSheetSubtitle))
                .setText(getString(R.string.meal_subtitle_format, entry.getTotalCalories(), mealLabel));

        view.findViewById(R.id.btnBottomSheetEdit).setOnClickListener(v -> {
            bottomSheet.dismiss();
            showEditMealDialog(entry);
        });
        view.findViewById(R.id.btnBottomSheetDelete).setOnClickListener(v -> {
            bottomSheet.dismiss();
            showDeleteDialog(entry);
        });

        bottomSheet.show();
    }

    /**
     * Öğün düzenleme dialogu: kalori ve gram değerleri değiştirilebilir.
     * Öğün gram değeri 0 ise (AI kaydı) sadece kalori düzenlenebilir.
     */
    private void showEditMealDialog(MealEntry entry) {
        int dpPad = (int) (16 * getResources().getDisplayMetrics().density);
        int dpGap = (int) (12 * getResources().getDisplayMetrics().density);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dpPad, dpPad / 2, dpPad, 0);

        // Kalori alanı
        TextInputLayout tilCal = new TextInputLayout(this, null,
                com.google.android.material.R.attr.textInputOutlinedStyle);
        tilCal.setHint(getString(R.string.hint_edit_calories));
        TextInputEditText etCal = new TextInputEditText(tilCal.getContext());
        etCal.setInputType(InputType.TYPE_CLASS_NUMBER);
        etCal.setText(String.valueOf(entry.getTotalCalories()));
        tilCal.addView(etCal);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dpGap;
        container.addView(tilCal, lp);

        // Gram alanı (sadece gram > 0 ise göster)
        TextInputLayout tilGram = null;
        TextInputEditText etGram = null;
        if (entry.getAmountGrams() > 0) {
            tilGram = new TextInputLayout(this, null,
                    com.google.android.material.R.attr.textInputOutlinedStyle);
            tilGram.setHint(getString(R.string.hint_edit_grams));
            etGram = new TextInputEditText(tilGram.getContext());
            etGram.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            etGram.setText(String.valueOf((int) entry.getAmountGrams()));
            tilGram.addView(etGram);
            container.addView(tilGram);
        }

        final TextInputEditText finalEtGram = etGram;

        new MaterialAlertDialogBuilder(this)
                .setTitle(entry.getFoodName())
                .setView(container)
                .setPositiveButton(getString(R.string.btn_save), (dialog, which) -> {
                    try {
                        String calStr = etCal.getText() != null
                                ? etCal.getText().toString().trim() : "";
                        if (!calStr.isEmpty()) {
                            entry.setTotalCalories(Integer.parseInt(calStr));
                        }
                        if (finalEtGram != null && finalEtGram.getText() != null) {
                            String gStr = finalEtGram.getText().toString().trim();
                            if (!gStr.isEmpty()) {
                                entry.setAmountGrams(Float.parseFloat(gStr));
                            }
                        }
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            try {
                                repository.updateMealEntry(entry);
                                runOnUiThread(() -> {
                                    loadData();
                                    Snackbar.make(rvMealEntries,
                                            R.string.meal_updated_success,
                                            Snackbar.LENGTH_SHORT).show();
                                });
                            } catch (RuntimeException exception) {
                                runOnUiThread(() ->
                                        Snackbar.make(rvMealEntries,
                                                R.string.sync_status_error,
                                                Snackbar.LENGTH_LONG).show());
                            }
                        });
                    } catch (NumberFormatException ignored) {}
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private void showDeleteDialog(MealEntry entry) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.btn_delete))
                .setMessage(getString(R.string.meal_subtitle_format, entry.getTotalCalories(), entry.getFoodName()))
                .setPositiveButton(getString(R.string.btn_delete),
                        (dialog, which) -> AppDatabase.databaseWriteExecutor.execute(() -> {
                            try {
                                repository.deleteMealEntry(entry);
                                runOnUiThread(this::loadData);
                            } catch (RuntimeException exception) {
                                runOnUiThread(() ->
                                        Snackbar.make(rvMealEntries,
                                                R.string.sync_status_error,
                                                Snackbar.LENGTH_LONG).show());
                            }
                        }))
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private String getMealLabel(String mealType) {
        if (mealType == null) return getString(R.string.label_meal_snack);
        switch (mealType) {
            case "BREAKFAST": return getString(R.string.label_meal_breakfast);
            case "LUNCH":     return getString(R.string.label_meal_lunch);
            case "DINNER":    return getString(R.string.label_meal_dinner);
            default:          return getString(R.string.label_meal_snack);
        }
    }
}
