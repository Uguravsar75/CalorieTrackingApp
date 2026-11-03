package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.CalorieGoalExplainer;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.ProfileInputValidator;

/**
 * ProfileEditActivity: Mevcut kullanıcı profilini düzenleme ekranı.
 */
public class ProfileEditActivity extends AppCompatActivity {

    private TextInputEditText etName, etAge, etHeight, etWeight, etCalorieGoal, etOtherDiseases;
    private TextView tvCalorieGoalExplanation;
    private RadioGroup rgGender;
    private AutoCompleteTextView spinnerActivityLevel;
    private CheckBox cbDiabetes, cbHypertension, cbCholesterol;
    private AppRepository repository;
    private User currentUser;
    private final String[] activityKeys = {"LOW", "MODERATE", "HIGH", "VERY_HIGH"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        repository = new AppRepository(getApplicationContext());
        initViews();
        loadProfile();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        etCalorieGoal = findViewById(R.id.etCalorieGoal);
        etOtherDiseases = findViewById(R.id.etOtherDiseases);
        tvCalorieGoalExplanation = findViewById(R.id.tvCalorieGoalExplanation);
        rgGender = findViewById(R.id.rgGender);
        spinnerActivityLevel = findViewById(R.id.spinnerActivityLevel);
        cbDiabetes = findViewById(R.id.cbDiabetes);
        cbHypertension = findViewById(R.id.cbHypertension);
        cbCholesterol = findViewById(R.id.cbCholesterol);

        String[] activityLabels = {
                getString(R.string.activity_low),
                getString(R.string.activity_moderate),
                getString(R.string.activity_high),
                getString(R.string.activity_very_high)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, activityLabels);
        spinnerActivityLevel.setAdapter(adapter);

        MaterialButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveProfile());
        setupExplanationListeners();
    }

    private void loadProfile() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentUser = repository.getUser();
            if (currentUser != null) {
                runOnUiThread(() -> {
                    etName.setText(currentUser.getName());
                    etAge.setText(String.valueOf(currentUser.getAge()));
                    etHeight.setText(String.valueOf(currentUser.getHeight()));
                    etWeight.setText(String.valueOf(currentUser.getWeight()));
                    etCalorieGoal.setText(String.valueOf(currentUser.getDailyCalorieGoal()));
                    etOtherDiseases.setText(currentUser.getOtherDiseases());
                    rgGender.check("MALE".equals(currentUser.getGender()) ? R.id.rbMale : R.id.rbFemale);
                    spinnerActivityLevel.setText(getActivityLabel(currentUser.getActivityLevel()), false);
                    cbDiabetes.setChecked(currentUser.isHasDiabetes());
                    cbHypertension.setChecked(currentUser.isHasHypertension());
                    cbCholesterol.setChecked(currentUser.isHasCholesterol());
                    updateCalorieGoalExplanation(true);
                });
            }
        });
    }

    private void setupExplanationListeners() {
        TextWatcher watcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateCalorieGoalExplanation(true);
            }
        };
        etAge.addTextChangedListener(watcher);
        etHeight.addTextChangedListener(watcher);
        etWeight.addTextChangedListener(watcher);
        rgGender.setOnCheckedChangeListener((group, checkedId) -> updateCalorieGoalExplanation(true));
        spinnerActivityLevel.setOnItemClickListener((parent, view, position, id) -> updateCalorieGoalExplanation(true));
    }

    private void updateCalorieGoalExplanation(boolean clearWhenIncomplete) {
        try {
            String ageStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";
            String heightStr = etHeight.getText() != null ? etHeight.getText().toString().trim() : "";
            String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
            if (TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(weightStr)) {
                if (clearWhenIncomplete) tvCalorieGoalExplanation.setText("");
                return;
            }

            User temp = new User();
            temp.setAge(Integer.parseInt(ageStr));
            temp.setHeight(Float.parseFloat(heightStr));
            temp.setWeight(Float.parseFloat(weightStr));
            temp.setGender(rgGender.getCheckedRadioButtonId() == R.id.rbMale ? "MALE" : "FEMALE");
            temp.setActivityLevel(getSelectedActivityKey());
            tvCalorieGoalExplanation.setText(CalorieGoalExplainer.build(this, temp));
        } catch (NumberFormatException ignored) {
            if (clearWhenIncomplete) tvCalorieGoalExplanation.setText("");
        }
    }

    private void saveProfile() {
        if (currentUser == null) return;

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String ageStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";
        String heightStr = etHeight.getText() != null ? etHeight.getText().toString().trim() : "";
        String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
        String goalStr = etCalorieGoal.getText() != null ? etCalorieGoal.getText().toString().trim() : "";
        String otherDiseases = etOtherDiseases.getText() != null ? etOtherDiseases.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageStr)
                || TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(weightStr)
                || TextUtils.isEmpty(goalStr)) {
            Snackbar.make(etName, R.string.error_required_field, Snackbar.LENGTH_SHORT).show();
            return;
        }

        clearInputErrors();
        ProfileInputValidator.ValidationResult validation =
                ProfileInputValidator.validate(this, ageStr, heightStr, weightStr, goalStr);
        if (!validation.isValid()) {
            showValidationErrors(validation);
            return;
        }

        currentUser.setName(name);
        currentUser.setAge(validation.age);
        currentUser.setHeight(validation.height);
        currentUser.setWeight(validation.weight);
        currentUser.setGender(rgGender.getCheckedRadioButtonId() == R.id.rbMale ? "MALE" : "FEMALE");
        currentUser.setActivityLevel(getSelectedActivityKey());
        currentUser.setHasDiabetes(cbDiabetes.isChecked());
        currentUser.setHasHypertension(cbHypertension.isChecked());
        currentUser.setHasCholesterol(cbCholesterol.isChecked());
        currentUser.setOtherDiseases(otherDiseases);
        currentUser.setDiseaseType(resolvePrimaryDisease());
        currentUser.setDailyCalorieGoal(validation.goal);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                repository.updateUser(currentUser);
                runOnUiThread(() -> {
                    Snackbar.make(etName, R.string.profile_updated, Snackbar.LENGTH_SHORT).show();
                    finish();
                });
            } catch (RuntimeException exception) {
                runOnUiThread(() ->
                        Snackbar.make(etName, R.string.sync_status_error, Snackbar.LENGTH_LONG).show());
            }
        });
    }

    private String getSelectedActivityKey() {
        String selectedLabel = spinnerActivityLevel.getText() != null
                ? spinnerActivityLevel.getText().toString()
                : "";
        String[] activityLabels = {
                getString(R.string.activity_low),
                getString(R.string.activity_moderate),
                getString(R.string.activity_high),
                getString(R.string.activity_very_high)
        };
        for (int i = 0; i < activityLabels.length; i++) {
            if (activityLabels[i].equals(selectedLabel)) {
                return activityKeys[i];
            }
        }
        return "MODERATE";
    }

    private String getActivityLabel(String activityKey) {
        if ("LOW".equals(activityKey)) return getString(R.string.activity_low);
        if ("HIGH".equals(activityKey)) return getString(R.string.activity_high);
        if ("VERY_HIGH".equals(activityKey)) return getString(R.string.activity_very_high);
        return getString(R.string.activity_moderate);
    }

    private String resolvePrimaryDisease() {
        if (cbDiabetes.isChecked()) return "DIABETES";
        if (cbHypertension.isChecked()) return "HYPERTENSION";
        if (cbCholesterol.isChecked()) return "HEART";
        if (etOtherDiseases.getText() != null && !etOtherDiseases.getText().toString().trim().isEmpty()) return "OTHER";
        return "NONE";
    }

    private void clearInputErrors() {
        etAge.setError(null);
        etHeight.setError(null);
        etWeight.setError(null);
        etCalorieGoal.setError(null);
    }

    private void showValidationErrors(ProfileInputValidator.ValidationResult validation) {
        if (validation.ageError != null) etAge.setError(validation.ageError);
        if (validation.heightError != null) etHeight.setError(validation.heightError);
        if (validation.weightError != null) etWeight.setError(validation.weightError);
        if (validation.goalError != null) etCalorieGoal.setError(validation.goalError);
        Snackbar.make(etName, R.string.error_profile_values_invalid, Snackbar.LENGTH_SHORT).show();
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
