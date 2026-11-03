package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.CalorieCalculator;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.CalorieGoalExplainer;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.ProfileInputValidator;

public class ProfileSetupActivity extends AppCompatActivity {

    private static final String TAG = "ProfileSetup";

    private TextInputEditText etName, etAge, etHeight, etWeight, etCalorieGoal, etOtherDiseases;
    private TextView tvCalorieGoalExplanation;
    private RadioGroup rgGender;
    private AutoCompleteTextView spinnerActivityLevel;
    private CheckBox cbDiabetes, cbHypertension, cbCholesterol;
    private MaterialButton btnSave;
    private final String[] activityKeys = {"LOW", "MODERATE", "HIGH", "VERY_HIGH"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);
        initViews();
        setupActivityDropdown();
        setupAutoCalculate();
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
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void setupActivityDropdown() {
        String[] activityLabels = {
                getString(R.string.activity_low),
                getString(R.string.activity_moderate),
                getString(R.string.activity_high),
                getString(R.string.activity_very_high)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, activityLabels);
        spinnerActivityLevel.setAdapter(adapter);
        spinnerActivityLevel.setText(activityLabels[1], false);
    }

    private void setupAutoCalculate() {
        View.OnFocusChangeListener listener = (v, hasFocus) -> {
            if (!hasFocus) {
                recalculateCalories();
            }
        };
        etAge.setOnFocusChangeListener(listener);
        etHeight.setOnFocusChangeListener(listener);
        etWeight.setOnFocusChangeListener(listener);
        rgGender.setOnCheckedChangeListener((group, checkedId) -> recalculateCalories());
        spinnerActivityLevel.setOnItemClickListener((parent, view, position, id) -> recalculateCalories());

        TextWatcher explanationWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateCalorieGoalExplanation(true);
            }
        };
        etAge.addTextChangedListener(explanationWatcher);
        etHeight.addTextChangedListener(explanationWatcher);
        etWeight.addTextChangedListener(explanationWatcher);
    }

    private void recalculateCalories() {
        try {
            String ageStr = getText(etAge);
            String heightStr = getText(etHeight);
            String weightStr = getText(etWeight);
            if (TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(weightStr)) {
                return;
            }

            User temp = new User();
            temp.setAge(Integer.parseInt(ageStr));
            temp.setHeight(Float.parseFloat(heightStr));
            temp.setWeight(Float.parseFloat(weightStr));
            temp.setGender(rgGender.getCheckedRadioButtonId() == R.id.rbFemale ? "FEMALE" : "MALE");
            temp.setActivityLevel(getSelectedActivityKey());

            int suggested = CalorieCalculator.calculateDailyGoal(temp);
            etCalorieGoal.setText(String.valueOf(suggested));
            updateCalorieGoalExplanation(false);
        } catch (NumberFormatException ignored) {
        }
    }

    private void updateCalorieGoalExplanation(boolean clearWhenIncomplete) {
        try {
            String ageStr = getText(etAge);
            String heightStr = getText(etHeight);
            String weightStr = getText(etWeight);
            if (TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(weightStr)) {
                if (clearWhenIncomplete) tvCalorieGoalExplanation.setText("");
                return;
            }

            User temp = new User();
            temp.setAge(Integer.parseInt(ageStr));
            temp.setHeight(Float.parseFloat(heightStr));
            temp.setWeight(Float.parseFloat(weightStr));
            temp.setGender(rgGender.getCheckedRadioButtonId() == R.id.rbFemale ? "FEMALE" : "MALE");
            temp.setActivityLevel(getSelectedActivityKey());
            tvCalorieGoalExplanation.setText(CalorieGoalExplainer.build(this, temp));
        } catch (NumberFormatException ignored) {
            if (clearWhenIncomplete) tvCalorieGoalExplanation.setText("");
        }
    }

    private void saveProfile() {
        String name = getText(etName);
        String ageStr = getText(etAge);
        String heightStr = getText(etHeight);
        String weightStr = getText(etWeight);
        String goalStr = getText(etCalorieGoal);
        String otherDiseases = getText(etOtherDiseases);

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageStr)
                || TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(weightStr)
                || TextUtils.isEmpty(goalStr)) {
            Snackbar.make(btnSave, R.string.error_required_field, Snackbar.LENGTH_SHORT).show();
            return;
        }

        clearInputErrors();
        ProfileInputValidator.ValidationResult validation =
                ProfileInputValidator.validate(this, ageStr, heightStr, weightStr, goalStr);
        if (!validation.isValid()) {
            showValidationErrors(validation);
            return;
        }

        final User user = new User();
        user.setName(name);
        user.setAge(validation.age);
        user.setHeight(validation.height);
        user.setWeight(validation.weight);
        user.setGender(rgGender.getCheckedRadioButtonId() == R.id.rbFemale ? "FEMALE" : "MALE");
        user.setActivityLevel(getSelectedActivityKey());
        user.setHasDiabetes(cbDiabetes.isChecked());
        user.setHasHypertension(cbHypertension.isChecked());
        user.setHasCholesterol(cbCholesterol.isChecked());
        user.setOtherDiseases(otherDiseases);
        user.setDiseaseType(resolvePrimaryDisease(user));
        user.setDailyCalorieGoal(validation.goal);

        btnSave.setEnabled(false);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                AppRepository repo = new AppRepository(getApplicationContext());
                User existing = repo.getUser();
                if (existing != null) {
                    existing.setName(user.getName());
                    existing.setAge(user.getAge());
                    existing.setHeight(user.getHeight());
                    existing.setWeight(user.getWeight());
                    existing.setGender(user.getGender());
                    existing.setActivityLevel(user.getActivityLevel());
                    existing.setHasDiabetes(user.isHasDiabetes());
                    existing.setHasHypertension(user.isHasHypertension());
                    existing.setHasCholesterol(user.isHasCholesterol());
                    existing.setOtherDiseases(user.getOtherDiseases());
                    existing.setDiseaseType(user.getDiseaseType());
                    existing.setDailyCalorieGoal(user.getDailyCalorieGoal());
                    repo.updateUser(existing);
                } else {
                    repo.insertUser(user);
                }

                runOnUiThread(() -> {
                    btnSave.setEnabled(true);
                    startActivity(new Intent(ProfileSetupActivity.this, MainActivity.class));
                    finish();
                });
            } catch (Exception e) {
                Log.e(TAG, "Profile save failed", e);
                runOnUiThread(() -> {
                    btnSave.setEnabled(true);
                    Snackbar.make(btnSave,
                            getString(R.string.error_profile_save_failed_format, readableMessage(e)),
                            Snackbar.LENGTH_LONG).show();
                });
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

    private String resolvePrimaryDisease(User user) {
        if (user.isHasDiabetes()) return "DIABETES";
        if (user.isHasHypertension()) return "HYPERTENSION";
        if (user.isHasCholesterol()) return "HEART";
        if (user.getOtherDiseases() != null && !user.getOtherDiseases().trim().isEmpty()) return "OTHER";
        return "NONE";
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
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
        Snackbar.make(btnSave, R.string.error_profile_values_invalid, Snackbar.LENGTH_SHORT).show();
    }

    private String readableMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.trim().isEmpty() ? getString(R.string.error_unknown) : message;
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
