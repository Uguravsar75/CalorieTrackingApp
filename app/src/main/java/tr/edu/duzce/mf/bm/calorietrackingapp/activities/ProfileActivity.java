package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.SyncStatusManager;

/**
 * ProfileActivity: Kullanıcı profil bilgilerini görüntüler.
 * Ad, yaş, cinsiyet, boy, kilo, VKİ (BMI), hastalık durumu ve
 * günlük kalori hedefi bu ekranda gösterilir.
 */
public class ProfileActivity extends AppCompatActivity {

    private TextView tvAvatar, tvProfileName, tvProfileSubtitle;
    private TextView tvAge, tvGender, tvHeight, tvWeight, tvActivity;
    private TextView tvBmiValue, tvBmiLabel;
    private TextView tvDisease, tvCalorieGoal, tvTimezone, tvSyncStatus, tvSyncDetails, tvOtherDiseases;
    private SyncStatusManager syncStatusManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        syncStatusManager = new SyncStatusManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();

        MaterialButton btnEdit = findViewById(R.id.btnEditProfile);
        btnEdit.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileEditActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ProfileEditActivity'den döndükten sonra bilgileri yenile
        loadProfile();
    }

    private void initViews() {
        tvAvatar = findViewById(R.id.tvAvatar);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileSubtitle = findViewById(R.id.tvProfileSubtitle);
        tvAge = findViewById(R.id.tvAge);
        tvGender = findViewById(R.id.tvGender);
        tvHeight = findViewById(R.id.tvHeight);
        tvWeight = findViewById(R.id.tvWeight);
        tvActivity = findViewById(R.id.tvActivity);
        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvBmiLabel = findViewById(R.id.tvBmiLabel);
        tvDisease = findViewById(R.id.tvDisease);
        tvCalorieGoal = findViewById(R.id.tvCalorieGoal);
        tvTimezone = findViewById(R.id.tvTimezone);
        tvSyncStatus = findViewById(R.id.tvSyncStatus);
        tvSyncDetails = findViewById(R.id.tvSyncDetails);
        tvOtherDiseases = findViewById(R.id.tvOtherDiseases);
    }

    private void loadProfile() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppRepository repo = new AppRepository(getApplicationContext());
            User user = repo.getUser();
            if (user == null) return;
            runOnUiThread(() -> bindUser(user));
        });
    }

    private void bindUser(User user) {
        // Avatar: ismin baş harfleri
        String initials = getInitials(user.getName());
        tvAvatar.setText(initials);

        // Ad ve alt başlık
        tvProfileName.setText(user.getName());
        String genderLabel = "MALE".equals(user.getGender())
                ? getString(R.string.gender_male)
                : getString(R.string.gender_female);
        tvProfileSubtitle.setText(getString(R.string.profile_subtitle_format,
                genderLabel, user.getAge(), getString(R.string.unit_years)));

        // Fiziksel bilgiler
        tvAge.setText(getString(R.string.profile_value_age_format, user.getAge(), getString(R.string.unit_years)));
        tvGender.setText(genderLabel);
        tvHeight.setText(getString(R.string.profile_value_cm_format, (int) user.getHeight(), getString(R.string.unit_cm)));
        tvWeight.setText(getString(R.string.profile_value_kg_format, (int) user.getWeight(), getString(R.string.unit_kg)));
        tvActivity.setText(getActivityLabel(user.getActivityLevel()));

        // VKİ hesapla: kilo / (boy_m)^2
        float heightM = user.getHeight() / 100f;
        float bmi = user.getWeight() / (heightM * heightM);
        tvBmiValue.setText(String.format("%.1f", bmi));
        setBmiLabel(bmi);

        // Sağlık bilgileri
        tvDisease.setText(getHealthSummary(user));
        tvCalorieGoal.setText(getString(R.string.profile_value_kcal_format,
                user.getDailyCalorieGoal(), getString(R.string.unit_kcal_day)));
        tvTimezone.setText(user.getTimezoneId() != null ? user.getTimezoneId() : "-");
        bindSyncStatus();
        tvOtherDiseases.setText(user.getOtherDiseases() == null || user.getOtherDiseases().trim().isEmpty()
                ? getString(R.string.disease_none)
                : user.getOtherDiseases());
    }

    private void bindSyncStatus() {
        if (syncStatusManager.hasError()) {
            String errorText = getString(R.string.sync_error_format, syncStatusManager.getLastError());
            tvSyncStatus.setText(errorText);
            tvSyncDetails.setText(errorText);
            return;
        }

        long lastSuccessAt = syncStatusManager.getLastSuccessAt();
        if (lastSuccessAt > 0) {
            String formatted = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                    .format(new Date(lastSuccessAt));
            String statusText = getString(R.string.sync_last_success_format, formatted);
            tvSyncStatus.setText(statusText);
            tvSyncDetails.setText(statusText);
        } else {
            tvSyncStatus.setText(R.string.sync_pending);
            tvSyncDetails.setText(R.string.sync_pending);
        }
    }

    private void setBmiLabel(float bmi) {
        String label;
        int colorRes;

        if (bmi < 18.5f) {
            label = getString(R.string.bmi_underweight);
            colorRes = R.color.report_grey;
        } else if (bmi < 25f) {
            label = getString(R.string.bmi_normal);
            colorRes = R.color.report_green;
        } else if (bmi < 30f) {
            label = getString(R.string.bmi_overweight);
            colorRes = R.color.calorie_warning;
        } else {
            label = getString(R.string.bmi_obese);
            colorRes = R.color.report_red;
        }

        tvBmiLabel.setText(label);
        GradientDrawable bg = (GradientDrawable)
                ContextCompat.getDrawable(this, R.drawable.bg_report_status);
        if (bg != null) {
            bg = (GradientDrawable) bg.mutate();
            bg.setColor(ContextCompat.getColor(this, colorRes));
            tvBmiLabel.setBackground(bg);
        }
        tvBmiLabel.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private String getDiseaseLabel(String diseaseType) {
        if (diseaseType == null) return getString(R.string.disease_none);
        switch (diseaseType) {
            case "DIABETES":     return getString(R.string.disease_diabetes);
            case "HYPERTENSION": return getString(R.string.disease_hypertension);
            case "OTHER":        return getString(R.string.label_other_conditions);
            case "HEART":        return getString(R.string.disease_heart);
            default:             return getString(R.string.disease_none);
        }
    }

    private String getActivityLabel(String activityLevel) {
        if ("LOW".equals(activityLevel)) return getString(R.string.activity_low);
        if ("HIGH".equals(activityLevel)) return getString(R.string.activity_high);
        if ("VERY_HIGH".equals(activityLevel)) return getString(R.string.activity_very_high);
        return getString(R.string.activity_moderate);
    }

    private String getHealthSummary(User user) {
        StringBuilder builder = new StringBuilder();
        if (user.isHasDiabetes()) builder.append(getString(R.string.disease_diabetes)).append(" • ");
        if (user.isHasHypertension()) builder.append(getString(R.string.disease_hypertension)).append(" • ");
        if (user.isHasCholesterol()) builder.append(getString(R.string.label_cholesterol)).append(" • ");
        if (builder.length() == 0) return getDiseaseLabel(user.getDiseaseType());
        return builder.substring(0, builder.length() - 3);
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return String.valueOf(parts[0].charAt(0)).toUpperCase()
                    + String.valueOf(parts[1].charAt(0)).toUpperCase();
        }
        return String.valueOf(parts[0].charAt(0)).toUpperCase();
    }
}
