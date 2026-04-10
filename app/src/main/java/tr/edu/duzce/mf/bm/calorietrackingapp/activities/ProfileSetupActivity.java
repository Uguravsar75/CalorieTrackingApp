package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.CalorieCalculator;

/**
 * ProfileSetupActivity: Kullanıcıdan profil bilgileri (yaş, kilo, boy, cinsiyet,
 * kronik hastalık) alınır. Harris-Benedict formülüyle kalori hedefi hesaplanır.
 * Kullanıcı manuel olarak da değiştirebilir.
 */
public class ProfileSetupActivity extends AppCompatActivity {

    private TextInputEditText etName, etAge, etHeight, etWeight, etCalorieGoal;
    private RadioGroup rgGender;
    private AutoCompleteTextView spinnerDisease;
    private MaterialButton btnSave;

    private final String[] diseaseKeys = {"NONE", "DIABETES", "HYPERTENSION", "HEART"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);
        initViews();
        setupDiseaseSpinner();
        setupAutoCalculate();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        etCalorieGoal = findViewById(R.id.etCalorieGoal);
        rgGender = findViewById(R.id.rgGender);
        spinnerDisease = findViewById(R.id.spinnerDisease);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void setupDiseaseSpinner() {
        String[] diseaseLabels = {
                getString(R.string.disease_none),
                getString(R.string.disease_diabetes),
                getString(R.string.disease_hypertension),
                getString(R.string.disease_heart)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, diseaseLabels);
        spinnerDisease.setAdapter(adapter);
        spinnerDisease.setText(diseaseLabels[0], false);
    }

    // Boy/kilo/yaş/cinsiyet girilince kaloriyi otomatik hesapla
    private void setupAutoCalculate() {
        View.OnFocusChangeListener listener = (v, hasFocus) -> {
            if (!hasFocus) recalculateCalories();
        };
        etAge.setOnFocusChangeListener(listener);
        etHeight.setOnFocusChangeListener(listener);
        etWeight.setOnFocusChangeListener(listener);
        rgGender.setOnCheckedChangeListener((group, checkedId) -> recalculateCalories());
    }

    private void recalculateCalories() {
        try {
            String ageStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";
            String heightStr = etHeight.getText() != null ? etHeight.getText().toString().trim() : "";
            String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
            if (TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(weightStr)) return;

            User temp = new User();
            temp.setAge(Integer.parseInt(ageStr));
            temp.setHeight(Float.parseFloat(heightStr));
            temp.setWeight(Float.parseFloat(weightStr));
            temp.setGender(rgGender.getCheckedRadioButtonId() == R.id.rbMale ? "MALE" : "FEMALE");

            int suggested = CalorieCalculator.calculateDailyGoal(temp);
            etCalorieGoal.setText(String.valueOf(suggested));
        } catch (NumberFormatException ignored) {
        }
    }

    private void saveProfile() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String ageStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";
        String heightStr = etHeight.getText() != null ? etHeight.getText().toString().trim() : "";
        String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
        String goalStr = etCalorieGoal.getText() != null ? etCalorieGoal.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageStr)
                || TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(weightStr)
                || TextUtils.isEmpty(goalStr)) {
            Snackbar.make(btnSave, R.string.error_required_field, Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Seçili hastalık indexini bul
        String[] diseaseLabels = {
                getString(R.string.disease_none),
                getString(R.string.disease_diabetes),
                getString(R.string.disease_hypertension),
                getString(R.string.disease_heart)
        };
        String selectedLabel = spinnerDisease.getText().toString();
        String diseaseKey = "NONE";
        for (int i = 0; i < diseaseLabels.length; i++) {
            if (diseaseLabels[i].equals(selectedLabel)) {
                diseaseKey = diseaseKeys[i];
                break;
            }
        }

        final String finalDiseaseKey = diseaseKey;
        final User user = new User();
        user.setName(name);
        user.setAge(Integer.parseInt(ageStr));
        user.setHeight(Float.parseFloat(heightStr));
        user.setWeight(Float.parseFloat(weightStr));
        user.setGender(rgGender.getCheckedRadioButtonId() == R.id.rbMale ? "MALE" : "FEMALE");
        user.setDiseaseType(finalDiseaseKey);
        user.setDailyCalorieGoal(Integer.parseInt(goalStr));

        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppRepository repo = new AppRepository(getApplicationContext());
            User existing = repo.getUser();
            if (existing != null) {
                // Kayıt aşamasında oluşturulan kısmi kullanıcıyı güncelle
                existing.setName(user.getName());
                existing.setAge(user.getAge());
                existing.setHeight(user.getHeight());
                existing.setWeight(user.getWeight());
                existing.setGender(user.getGender());
                existing.setDiseaseType(user.getDiseaseType());
                existing.setDailyCalorieGoal(user.getDailyCalorieGoal());
                repo.updateUser(existing);
            } else {
                repo.insertUser(user);
            }
            runOnUiThread(() -> {
                startActivity(new Intent(ProfileSetupActivity.this, MainActivity.class));
                finish();
            });
        });
    }
}
