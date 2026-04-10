package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

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
 * Bağımsız bir ekran olarak da kullanılabilir.
 */
public class FoodSearchActivity extends AppCompatActivity {

    private FoodAdapter adapter;
    private AppRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_search);

        repository = new AppRepository(getApplicationContext());

        RecyclerView rvFoods = findViewById(R.id.rvFoods);
        adapter = new FoodAdapter(this, List.of());
        rvFoods.setLayoutManager(new LinearLayoutManager(this));
        rvFoods.setAdapter(adapter);

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
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Food> foods = repository.getAllFoods();
            runOnUiThread(() -> adapter.updateData(foods));
        });
    }

    private void search(String query) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Food> foods = TextUtils.isEmpty(query)
                    ? repository.getAllFoods()
                    : repository.searchFoods(query);
            runOnUiThread(() -> adapter.updateData(foods));
        });
    }
}
