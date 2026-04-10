package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;

/**
 * ProfileEditActivity: Mevcut kullanıcı profilini düzenleme ekranı.
 */
public class ProfileEditActivity extends AppCompatActivity {

    private TextInputEditText etName, etAge, etHeight, etWeight, etCalorieGoal;
    private AppRepository repository;
    private User currentUser;

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

        MaterialButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveProfile());
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
                });
            }
        });
    }

    private void saveProfile() {
        if (currentUser == null) return;

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String ageStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";
        String heightStr = etHeight.getText() != null ? etHeight.getText().toString().trim() : "";
        String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
        String goalStr = etCalorieGoal.getText() != null ? etCalorieGoal.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageStr)
                || TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(weightStr)
                || TextUtils.isEmpty(goalStr)) {
            Snackbar.make(etName, R.string.error_required_field, Snackbar.LENGTH_SHORT).show();
            return;
        }

        currentUser.setName(name);
        currentUser.setAge(Integer.parseInt(ageStr));
        currentUser.setHeight(Float.parseFloat(heightStr));
        currentUser.setWeight(Float.parseFloat(weightStr));
        currentUser.setDailyCalorieGoal(Integer.parseInt(goalStr));

        AppDatabase.databaseWriteExecutor.execute(() -> {
            repository.updateUser(currentUser);
            runOnUiThread(() -> {
                Snackbar.make(etName, R.string.profile_updated, Snackbar.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
