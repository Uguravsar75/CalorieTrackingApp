package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.adapters.FoodAdapter;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.Food;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;

/**
 * FoodSearchActivity: Tüm ön tanımlı gıdaları listeler ve arama yapılmasını sağlar.
 * Bir gıdaya tıklanınca AddMealActivity açılır ve gıda önceden seçilmiş olur.
 */
public class FoodSearchActivity extends AppCompatActivity {

    private FoodAdapter adapter;
    private AppRepository repository;
    private LinearProgressIndicator progressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_search);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        repository = new AppRepository(getApplicationContext());

        progressIndicator = findViewById(R.id.progressIndicator);

        RecyclerView rvFoods = findViewById(R.id.rvFoods);
        adapter = new FoodAdapter(this, List.of());
        rvFoods.setLayoutManager(new LinearLayoutManager(this));
        rvFoods.setAdapter(adapter);

        // Gıdaya tıklayınca AddMealActivity'e yönlendir — yemek önceden seçili açılır
        adapter.setClickListener(food -> {
            Intent intent = new Intent(this, AddMealActivity.class);
            intent.putExtra(AddMealActivity.EXTRA_PRESELECTED_FOOD_ID, food.getId());
            startActivity(intent);
        });

        TextInputEditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                search(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadAll();
    }

    private void loadAll() {
        progressIndicator.setVisibility(View.VISIBLE);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Food> foods = repository.getAllFoods();
            runOnUiThread(() -> {
                adapter.updateData(foods);
                progressIndicator.setVisibility(View.GONE);
            });
        });
    }

    private void search(String query) {
        progressIndicator.setVisibility(View.VISIBLE);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Food> foods = TextUtils.isEmpty(query)
                    ? repository.getAllFoods()
                    : repository.searchFoods(query);
            runOnUiThread(() -> {
                adapter.updateData(foods);
                progressIndicator.setVisibility(View.GONE);
            });
        });
    }
}
