package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import android.widget.TextView;

import java.util.List;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.adapters.FoodAdapter;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.Food;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.MealEntry;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.CalorieCalculator;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.DateUtils;

/**
 * AddMealActivity: Kullanıcı gıda listesinden seçim yapar,
 * gram cinsinden miktar girer, kalori otomatik hesaplanır ve kaydedilir.
 * Diyabetik kullanıcı şekerli gıda seçerse uyarı kartı görünür.
 */
public class AddMealActivity extends AppCompatActivity {

    private TextInputEditText etSearchFood, etAmount;
    private RecyclerView rvFoodList;
    private MaterialCardView cardSelectedFood, cardDiabetesWarning;
    private TextView tvSelectedFoodName, tvSelectedFoodCal, tvCalculatedCalories;
    private MaterialButton btnSaveMeal;
    private FoodAdapter foodAdapter;

    private AppRepository repository;
    private Food selectedFood;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        repository = new AppRepository(getApplicationContext());
        initViews();
        loadUserAndFoods();
    }

    private void initViews() {
        etSearchFood = findViewById(R.id.etSearchFood);
        etAmount = findViewById(R.id.etAmount);
        rvFoodList = findViewById(R.id.rvFoodList);
        cardSelectedFood = findViewById(R.id.cardSelectedFood);
        cardDiabetesWarning = findViewById(R.id.cardDiabetesWarning);
        tvSelectedFoodName = findViewById(R.id.tvSelectedFoodName);
        tvSelectedFoodCal = findViewById(R.id.tvSelectedFoodCal);
        tvCalculatedCalories = findViewById(R.id.tvCalculatedCalories);
        btnSaveMeal = findViewById(R.id.btnSaveMeal);

        foodAdapter = new FoodAdapter(this, List.of());
        rvFoodList.setLayoutManager(new LinearLayoutManager(this));
        rvFoodList.setAdapter(foodAdapter);

        // Gıda seçilince
        foodAdapter.setClickListener(food -> {
            selectedFood = food;
            showSelectedFood(food);
            recalculate();
        });

        // Arama kutusuna yazınca filtrele
        etSearchFood.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFoods(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Miktar değişince kaloriyi yeniden hesapla
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { recalculate(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSaveMeal.setOnClickListener(v -> saveMeal());
    }

    private void loadUserAndFoods() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentUser = repository.getUser();
            // Günün saatine göre önerilen kategoriyi ön yükle
            String suggestedCategory = DateUtils.getSuggestedMealType();
            List<Food> foods = repository.getAllFoods();

            runOnUiThread(() -> foodAdapter.updateData(foods));
        });
    }

    private void filterFoods(String query) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Food> filtered = TextUtils.isEmpty(query)
                    ? repository.getAllFoods()
                    : repository.searchFoods(query);
            runOnUiThread(() -> foodAdapter.updateData(filtered));
        });
    }

    private void showSelectedFood(Food food) {
        String lang = getResources().getConfiguration().getLocales().get(0).getLanguage();
        String name = "tr".equals(lang) ? food.getNameTr() : food.getNameEn();
        tvSelectedFoodName.setText(name);
        tvSelectedFoodCal.setText(food.getCaloriesPer100g() + " kcal / 100g");
        cardSelectedFood.setVisibility(View.VISIBLE);

        // Diyabet uyarısı: kullanıcı diyabetikse ve gıda şeker uyarılıysa göster
        boolean showDiabetesWarning = food.isSugarWarning()
                && currentUser != null
                && "DIABETES".equals(currentUser.getDiseaseType());
        cardDiabetesWarning.setVisibility(showDiabetesWarning ? View.VISIBLE : View.GONE);
    }

    private void recalculate() {
        if (selectedFood == null) return;
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        if (TextUtils.isEmpty(amountStr)) {
            tvCalculatedCalories.setVisibility(View.GONE);
            return;
        }
        try {
            float amount = Float.parseFloat(amountStr);
            int cal = CalorieCalculator.calculateCalories(amount, selectedFood.getCaloriesPer100g());
            tvCalculatedCalories.setText(cal + " kcal");
            tvCalculatedCalories.setVisibility(View.VISIBLE);
        } catch (NumberFormatException e) {
            tvCalculatedCalories.setVisibility(View.GONE);
        }
    }

    private void saveMeal() {
        if (selectedFood == null) {
            Snackbar.make(btnSaveMeal, R.string.error_select_food, Snackbar.LENGTH_SHORT).show();
            return;
        }
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        if (TextUtils.isEmpty(amountStr)) {
            Snackbar.make(btnSaveMeal, R.string.error_invalid_amount, Snackbar.LENGTH_SHORT).show();
            return;
        }

        float amount;
        try {
            amount = Float.parseFloat(amountStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Snackbar.make(btnSaveMeal, R.string.error_invalid_amount, Snackbar.LENGTH_SHORT).show();
            return;
        }

        String lang = getResources().getConfiguration().getLocales().get(0).getLanguage();
        String foodName = "tr".equals(lang) ? selectedFood.getNameTr() : selectedFood.getNameEn();
        int totalCalories = CalorieCalculator.calculateCalories(amount, selectedFood.getCaloriesPer100g());

        MealEntry entry = new MealEntry();
        entry.setFoodId(selectedFood.getId());
        entry.setFoodName(foodName);
        entry.setDate(DateUtils.getToday());
        entry.setMealType(DateUtils.getSuggestedMealType());
        entry.setAmountGrams(amount);
        entry.setTotalCalories(totalCalories);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            repository.insertMealEntry(entry);
            runOnUiThread(() -> {
                Snackbar.make(btnSaveMeal, R.string.meal_added_success, Snackbar.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
