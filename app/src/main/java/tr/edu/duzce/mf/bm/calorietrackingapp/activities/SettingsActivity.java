package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import tr.edu.duzce.mf.bm.calorietrackingapp.utils.ReminderScheduler;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.NotificationHelper;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.SessionManager;

/**
 * SettingsActivity: Kullanıcı profil özeti, tema seçimi (Açık/Koyu/Sistem)
 * ve dil seçimi (Türkçe/İngilizce) bu ekrandan yapılır.
 * Tercihler SharedPreferences'a kaydedilir.
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "app_prefs";
    public static final String KEY_THEME = "theme_mode";
    public static final String KEY_LANG = "language";

    private RadioGroup rgTheme, rgLanguage;
    private SharedPreferences prefs;
    private TextView tvNotificationStatus, tvMealTime, tvWaterTime;
    private SwitchMaterial swMealReminder, swWaterReminder, swLimitAlert, swGoalAlert;
    private LinearLayout rowMealTime, rowWaterTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        rgTheme    = findViewById(R.id.rgTheme);
        rgLanguage = findViewById(R.id.rgLanguage);
        tvNotificationStatus = findViewById(R.id.tvNotificationStatus);
        swMealReminder  = findViewById(R.id.swMealReminder);
        swWaterReminder = findViewById(R.id.swWaterReminder);
        swLimitAlert    = findViewById(R.id.swLimitAlert);
        swGoalAlert     = findViewById(R.id.swGoalAlert);
        tvMealTime  = findViewById(R.id.tvMealTime);
        tvWaterTime = findViewById(R.id.tvWaterTime);
        rowMealTime  = findViewById(R.id.rowMealTime);
        rowWaterTime = findViewById(R.id.rowWaterTime);

        loadProfileSummary();
        loadCurrentSelections();
        loadNotificationSelections();
        setupListeners();
        updateNotificationStatus();

        MaterialButton btnMealTimePicker = findViewById(R.id.btnMealTimePicker);
        btnMealTimePicker.setOnClickListener(v -> showTimePicker(
                getString(R.string.settings_meal_time_title),
                NotificationHelper.getMealReminderHour(this),
                NotificationHelper.getMealReminderMinute(this),
                (hour, minute) -> {
                    NotificationHelper.setMealReminderTime(this, hour, minute);
                    updateTimeLabel(tvMealTime, hour, minute);
                    // Yeni saate gore WorkManager'i yeniden planla
                    ReminderScheduler.rescheduleMealReminder(this, hour, minute);
                }
        ));

        MaterialButton btnWaterTimePicker = findViewById(R.id.btnWaterTimePicker);
        btnWaterTimePicker.setOnClickListener(v -> showTimePicker(
                getString(R.string.settings_water_time_title),
                NotificationHelper.getWaterReminderHour(this),
                NotificationHelper.getWaterReminderMinute(this),
                (hour, minute) -> {
                    NotificationHelper.setWaterReminderTime(this, hour, minute);
                    updateTimeLabel(tvWaterTime, hour, minute);
                    // Yeni saate gore WorkManager'i yeniden planla
                    ReminderScheduler.rescheduleWaterReminder(this, hour, minute);
                }
        ));

        MaterialButton btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        MaterialButton btnChangePassword = findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(v -> handleChangePasswordClick());

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        MaterialButton btnNotificationSettings = findViewById(R.id.btnNotificationSettings);
        btnNotificationSettings.setOnClickListener(v ->
                startActivity(NotificationHelper.buildNotificationSettingsIntent(this)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationStatus();
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.btn_logout, (dialog, which) -> {
                    new SessionManager(this).clearSession();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void loadProfileSummary() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppRepository repo = new AppRepository(getApplicationContext());
            User user = repo.getUser();
            if (user == null) return;

            String genderLabel = "MALE".equals(user.getGender())
                    ? getString(R.string.gender_male)
                    : getString(R.string.gender_female);

            String details = user.getAge() + " " + getString(R.string.hint_age).toLowerCase()
                    + " • " + (int) user.getHeight() + " cm"
                    + " • " + (int) user.getWeight() + " kg"
                    + " • " + genderLabel;

            runOnUiThread(() -> {
                TextView tvName = findViewById(R.id.tvProfileName);
                TextView tvDetails = findViewById(R.id.tvProfileDetails);
                tvName.setText(user.getName());
                tvDetails.setText(details);
            });
        });
    }

    private void loadCurrentSelections() {
        // Tema
        int savedTheme = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (savedTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            rgTheme.check(R.id.rbThemeLight);
        } else if (savedTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            rgTheme.check(R.id.rbThemeDark);
        } else {
            rgTheme.check(R.id.rbThemeSystem);
        }

        // Dil
        String savedLang = prefs.getString(KEY_LANG, "tr");
        if ("en".equals(savedLang)) {
            rgLanguage.check(R.id.rbLangEn);
        } else {
            rgLanguage.check(R.id.rbLangTr);
        }
    }

    private void loadNotificationSelections() {
        swMealReminder.setChecked(NotificationHelper.isMealReminderEnabled(this));
        swWaterReminder.setChecked(NotificationHelper.isWaterReminderEnabled(this));
        swLimitAlert.setChecked(NotificationHelper.isLimitAlertEnabled(this));
        swGoalAlert.setChecked(NotificationHelper.isGoalCloseAlertEnabled(this));

        // Saat etiketlerini goster
        updateTimeLabel(tvMealTime,
                NotificationHelper.getMealReminderHour(this),
                NotificationHelper.getMealReminderMinute(this));
        updateTimeLabel(tvWaterTime,
                NotificationHelper.getWaterReminderHour(this),
                NotificationHelper.getWaterReminderMinute(this));

        // Switch kapali ise zaman satirini gizle
        rowMealTime.setVisibility(swMealReminder.isChecked() ? View.VISIBLE : View.GONE);
        rowWaterTime.setVisibility(swWaterReminder.isChecked() ? View.VISIBLE : View.GONE);
    }

    private void setupListeners() {
        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int mode;
            if (checkedId == R.id.rbThemeLight) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.rbThemeDark) {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
            prefs.edit().putInt(KEY_THEME, mode).apply();
            AppCompatDelegate.setDefaultNightMode(mode);
            getDelegate().applyDayNight();
            recreate();
        });

        rgLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String lang = (checkedId == R.id.rbLangEn) ? "en" : "tr";
            prefs.edit().putString(KEY_LANG, lang).apply();
            // API 33+ per-app locale — uygulama dili anında değişir, Activity yeniden başlar
            LocaleListCompat localeList = LocaleListCompat.forLanguageTags(lang);
            AppCompatDelegate.setApplicationLocales(localeList);
        });

        swMealReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationHelper.setMealReminderEnabled(this, isChecked);
            rowMealTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        swWaterReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationHelper.setWaterReminderEnabled(this, isChecked);
            rowWaterTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        swLimitAlert.setOnCheckedChangeListener((buttonView, isChecked) ->
                NotificationHelper.setLimitAlertEnabled(this, isChecked));
        swGoalAlert.setOnCheckedChangeListener((buttonView, isChecked) ->
                NotificationHelper.setGoalCloseAlertEnabled(this, isChecked));
    }

    /** Saat etiketi günceller ("08:30" formatı). */
    private void updateTimeLabel(TextView tv, int hour, int minute) {
        tv.setText(getString(R.string.settings_time_format, hour, minute));
    }

    /** TimePickerDialog açar, seçilen saati callback'e iletir. */
    private void showTimePicker(String title, int initHour, int initMinute,
                               TimeSelectedCallback callback) {
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hour, minute) -> callback.onTimeSelected(hour, minute),
                initHour, initMinute,
                true  // 24 saat formatı
        );
        dialog.setTitle(title);
        dialog.show();
    }

    /** TimePickerDialog seçim callback'i. */
    private interface TimeSelectedCallback {
        void onTimeSelected(int hour, int minute);
    }

    private void handleChangePasswordClick() {
        AppRepository repo = new AppRepository(this);
        if (!repo.isEmailPasswordUser()) {
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.settings_google_no_password, Snackbar.LENGTH_LONG).show();
            return;
        }
        showChangePasswordDialog();
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        TextInputLayout tilCurrent = dialogView.findViewById(R.id.tilCurrentPassword);
        TextInputLayout tilNew = dialogView.findViewById(R.id.tilNewPassword);
        TextInputLayout tilConfirm = dialogView.findViewById(R.id.tilConfirmPassword);
        TextInputEditText etCurrent = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNew = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirm = dialogView.findViewById(R.id.etConfirmPassword);

        // setPositiveButton listener null — show() sonrası override ediyoruz
        // Böylece validasyon hatası olunca dialog kapanmıyor
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_change_password_title)
                .setView(dialogView)
                .setPositiveButton(R.string.btn_save, null)
                .setNegativeButton(R.string.btn_cancel, null)
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String current = etCurrent.getText() != null ? etCurrent.getText().toString() : "";
            String next    = etNew.getText() != null ? etNew.getText().toString() : "";
            String confirm = etConfirm.getText() != null ? etConfirm.getText().toString() : "";

            tilCurrent.setError(null);
            tilNew.setError(null);
            tilConfirm.setError(null);

            if (current.isEmpty()) {
                tilCurrent.setError(getString(R.string.error_required_field));
                return;
            }
            if (next.length() < 6) {
                tilNew.setError(getString(R.string.error_password_short));
                return;
            }
            if (!next.equals(confirm)) {
                tilConfirm.setError(getString(R.string.error_password_mismatch));
                return;
            }

            // Validasyon geçti — dialog kapat ve şifreyi güncelle
            dialog.dismiss();
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    AppRepository repo = new AppRepository(getApplicationContext());
                    repo.changePassword(current, next);
                    runOnUiThread(() ->
                            Snackbar.make(findViewById(android.R.id.content),
                                    R.string.password_changed_success,
                                    Snackbar.LENGTH_LONG).show());
                } catch (Exception e) {
                    String key = e.getMessage() != null ? e.getMessage() : "";
                    int msgRes = "WRONG_PASSWORD".equals(key)
                            ? R.string.error_current_password_wrong
                            : R.string.error_unknown;
                    runOnUiThread(() ->
                            Snackbar.make(findViewById(android.R.id.content),
                                    msgRes, Snackbar.LENGTH_LONG).show());
                }
            });
        });
    }

    private void updateNotificationStatus() {
        if (tvNotificationStatus == null) {
            return;
        }
        tvNotificationStatus.setText(NotificationHelper.hasNotificationPermission(this)
                ? R.string.settings_notifications_enabled
                : R.string.settings_notifications_disabled);
    }
}
