package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;

/**
 * ProfileActivity: Kullanıcı profil bilgilerini görüntüler.
 * Ad, yaş, cinsiyet, boy, kilo, VKİ (BMI), hastalık durumu ve
 * günlük kalori hedefi bu ekranda gösterilir.
 */
public class ProfileActivity extends AppCompatActivity {

    private TextView tvAvatar, tvProfileName, tvProfileSubtitle;
    private TextView tvAge, tvGender, tvHeight, tvWeight;
    private TextView tvBmiValue, tvBmiLabel;
    private TextView tvDisease, tvCalorieGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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
        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvBmiLabel = findViewById(R.id.tvBmiLabel);
        tvDisease = findViewById(R.id.tvDisease);
        tvCalorieGoal = findViewById(R.id.tvCalorieGoal);
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
        tvProfileSubtitle.setText(genderLabel + " • " + user.getAge() + " " +
                getString(R.string.profile_label_age).toLowerCase());

        // Fiziksel bilgiler
        tvAge.setText(user.getAge() + " yaş");
        tvGender.setText(genderLabel);
        tvHeight.setText((int) user.getHeight() + " " + getString(R.string.unit_cm));
        tvWeight.setText((int) user.getWeight() + " " + getString(R.string.unit_kg));

        // VKİ hesapla: kilo / (boy_m)^2
        float heightM = user.getHeight() / 100f;
        float bmi = user.getWeight() / (heightM * heightM);
        tvBmiValue.setText(String.format("%.1f", bmi));
        setBmiLabel(bmi);

        // Sağlık bilgileri
        tvDisease.setText(getDiseaseLabel(user.getDiseaseType()));
        tvCalorieGoal.setText(user.getDailyCalorieGoal() + " " + getString(R.string.unit_kcal_day));
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
            case "HEART":        return getString(R.string.disease_heart);
            default:             return getString(R.string.disease_none);
        }
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
