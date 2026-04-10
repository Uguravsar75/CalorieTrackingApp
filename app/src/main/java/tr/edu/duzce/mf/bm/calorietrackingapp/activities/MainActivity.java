package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.adapters.MealEntryAdapter;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.MealEntry;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.DateUtils;

/**
 * MainActivity: Günlük kalori dashboard ekranıdır.
 * - Günlük hedef, tüketilen ve kalan kalori gösterilir
 * - Progress bar ile yüzdelik doluluğu gösterir
 * - Bugün eklenen öğünler listelenir
 * - Limit aşılınca uyarı kartı belirir
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvGoal, tvConsumed, tvRemaining, tvNoMeals;
    private LinearProgressIndicator progressCalorie;
    private MaterialCardView cardWarning;
    private RecyclerView rvMealEntries;
    private MealEntryAdapter adapter;

    private AppRepository repository;
    private User currentUser;
    private String today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new AppRepository(getApplicationContext());
        today = DateUtils.getToday();

        initViews();
        setupToolbar();
        setupBottomNav();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // AddMealActivity'den döndükten sonra verileri yenile
        loadData();
    }

    private void initViews() {
        tvGoal = findViewById(R.id.tvGoal);
        tvConsumed = findViewById(R.id.tvConsumed);
        tvRemaining = findViewById(R.id.tvRemaining);
        tvNoMeals = findViewById(R.id.tvNoMeals);
        progressCalorie = findViewById(R.id.progressCalorie);
        cardWarning = findViewById(R.id.cardWarning);
        rvMealEntries = findViewById(R.id.rvMealEntries);

        adapter = new MealEntryAdapter(this, new ArrayList<>());
        rvMealEntries.setLayoutManager(new LinearLayoutManager(this));
        rvMealEntries.setAdapter(adapter);

        // Uzun basınca öğün silme diyaloğu
        adapter.setLongClickListener(entry -> showDeleteDialog(entry));

        MaterialButton btnAddMeal = findViewById(R.id.btnAddMeal);
        btnAddMeal.setOnClickListener(v -> startActivity(new Intent(this, AddMealActivity.class)));

        MaterialButton btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(v -> startActivity(new Intent(this, CameraActivity.class)));

        MaterialButton btnWeeklyReport = findViewById(R.id.btnWeeklyReport);
        btnWeeklyReport.setOnClickListener(v -> startActivity(new Intent(this, WeeklyReportActivity.class)));
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.action_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return false; // Home seçili kalsın
            }
            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return false;
            }
            return true; // nav_home
        });
    }

    private void loadData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentUser = repository.getUser();
            List<MealEntry> entries = repository.getMealEntriesByDate(today);
            int consumed = repository.getTotalCaloriesForDate(today);

            runOnUiThread(() -> updateUI(currentUser, entries, consumed));
        });
    }

    private void updateUI(User user, List<MealEntry> entries, int consumed) {
        if (user == null)
            return;

        int goal = user.getDailyCalorieGoal();
        int remaining = goal - consumed;

        tvGoal.setText(String.valueOf(goal));
        tvConsumed.setText(String.valueOf(consumed));
        tvRemaining.setText(String.valueOf(Math.max(remaining, 0)));

        // Progress bar (0–100 aralığı, max 100'de kırpılır)
        int progress = goal > 0 ? Math.min((consumed * 100) / goal, 100) : 0;
        progressCalorie.setProgress(progress);

        // Limit aşımı uyarısı
        cardWarning.setVisibility(consumed > goal ? View.VISIBLE : View.GONE);

        // Öğün listesi güncelle
        adapter.updateData(entries);
        tvNoMeals.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
        rvMealEntries.setVisibility(entries.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showDeleteDialog(MealEntry entry) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.btn_delete))
                .setMessage(entry.getFoodName() + " — " + entry.getTotalCalories() + " kcal")
                .setPositiveButton(getString(R.string.btn_delete), (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        repository.deleteMealEntry(entry);
                        runOnUiThread(this::loadData);
                    });
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }
}
