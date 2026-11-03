package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

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

public class AddMealActivity extends AppCompatActivity {

    /** FoodSearchActivity'den intent ile gelen önseçili gıda ID'si */
    public static final String EXTRA_PRESELECTED_FOOD_ID = "preselected_food_id";

    private TextInputEditText etSearchFood, etAmount, etProtein, etCarb, etFat;
    private AutoCompleteTextView dropdownMealType, dropdownPortionType;
    private RecyclerView rvFoodList;
    private MaterialCardView cardSelectedFood, cardDiabetesWarning;
    private TextView tvSelectedFoodName, tvSelectedFoodCal, tvCalculatedCalories, tvPortionHint;
    private MaterialButton btnSaveMeal;
    private FoodAdapter foodAdapter;
    private LinearLayout layoutMacros;
    private ImageView ivMacroArrow;
    private boolean macroExpanded = false;

    private AppRepository repository;
    private Food selectedFood;
    private User currentUser;
    private int todayConsumed;
    private int preselectedFoodId = -1; // FoodSearchActivity'den gelen ID

    private final String[] mealTypeKeys = {"BREAKFAST", "LUNCH", "DINNER", "SNACK"};
    private final String[] portionKeys = {"GRAM", "PLATE", "BOWL", "TABLESPOON", "TEASPOON", "GLASS", "PORTION", "PIECE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        repository = new AppRepository(getApplicationContext());
        preselectedFoodId = getIntent().getIntExtra(EXTRA_PRESELECTED_FOOD_ID, -1);
        initViews();
        setupDropdowns();
        loadUserAndFoods();
    }

    private void initViews() {
        etSearchFood = findViewById(R.id.etSearchFood);
        etAmount = findViewById(R.id.etAmount);
        etProtein = findViewById(R.id.etProtein);
        etCarb    = findViewById(R.id.etCarb);
        etFat     = findViewById(R.id.etFat);
        layoutMacros  = findViewById(R.id.layoutMacros);
        ivMacroArrow  = findViewById(R.id.ivMacroArrow);
        dropdownMealType = findViewById(R.id.dropdownMealType);
        dropdownPortionType = findViewById(R.id.dropdownPortionType);
        rvFoodList = findViewById(R.id.rvFoodList);
        cardSelectedFood = findViewById(R.id.cardSelectedFood);
        cardDiabetesWarning = findViewById(R.id.cardDiabetesWarning);
        tvSelectedFoodName = findViewById(R.id.tvSelectedFoodName);
        tvSelectedFoodCal = findViewById(R.id.tvSelectedFoodCal);
        tvCalculatedCalories = findViewById(R.id.tvCalculatedCalories);
        tvPortionHint = findViewById(R.id.tvPortionHint);
        btnSaveMeal = findViewById(R.id.btnSaveMeal);

        foodAdapter = new FoodAdapter(this, List.of());
        rvFoodList.setLayoutManager(new LinearLayoutManager(this));
        rvFoodList.setAdapter(foodAdapter);

        foodAdapter.setClickListener(food -> {
            selectedFood = food;
            showSelectedFood(food);
            recalculate();
        });

        etSearchFood.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFoods(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { recalculate(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        dropdownPortionType.setOnItemClickListener((parent, view, position, id) -> {
            updatePortionHint();
            recalculate();
        });

        // Makro bolumu ac/kapa toggle
        View rowMacroToggle = findViewById(R.id.rowMacroToggle);
        rowMacroToggle.setOnClickListener(v -> toggleMacroSection());

        btnSaveMeal.setOnClickListener(v -> saveMeal());
    }

    private void setupDropdowns() {
        String[] mealTypeLabels = {
                getString(R.string.label_meal_breakfast),
                getString(R.string.label_meal_lunch),
                getString(R.string.label_meal_dinner),
                getString(R.string.label_meal_snack)
        };
        dropdownMealType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, mealTypeLabels));
        dropdownMealType.setText(getSuggestedMealLabel(), false);

        String[] portionLabels = {
                getString(R.string.portion_gram),
                getString(R.string.portion_plate),
                getString(R.string.portion_bowl),
                getString(R.string.portion_tablespoon),
                getString(R.string.portion_teaspoon),
                getString(R.string.portion_glass),
                getString(R.string.portion_serving),
                getString(R.string.portion_piece)
        };
        dropdownPortionType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, portionLabels));
        dropdownPortionType.setText(portionLabels[0], false);
        updatePortionHint();
    }

    private void loadUserAndFoods() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentUser = repository.getUser();
            todayConsumed = repository.getTotalCaloriesForDate(DateUtils.getToday());
            List<Food> foods = repository.getAllFoods();
            runOnUiThread(() -> {
                foodAdapter.updateData(foods);
                // FoodSearchActivity'den yönlendirildiyse yemeği önceden seç
                if (preselectedFoodId >= 0) {
                    for (Food food : foods) {
                        if (food.getId() == preselectedFoodId) {
                            selectedFood = food;
                            showSelectedFood(food);
                            recalculate();
                            break;
                        }
                    }
                }
            });
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
        tvSelectedFoodCal.setText(getString(R.string.selected_food_calorie_format, food.getCaloriesPer100g()));
        cardSelectedFood.setVisibility(View.VISIBLE);

        boolean showDiabetesWarning = food.isSugarWarning()
                && currentUser != null
                && (currentUser.isHasDiabetes() || "DIABETES".equals(currentUser.getDiseaseType()));
        String warningText = buildFoodRiskWarning(food);
        if (showDiabetesWarning && warningText == null) {
            warningText = getString(R.string.warning_diabetes);
        }
        if (warningText == null) {
            cardDiabetesWarning.setVisibility(View.GONE);
        } else {
            TextView tvWarning = cardDiabetesWarning.findViewById(R.id.tvFoodRiskWarning);
            tvWarning.setText(warningText);
            cardDiabetesWarning.setVisibility(View.VISIBLE);
        }
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
            float grams = convertToGrams(amount, getSelectedPortionKey());
            int cal = CalorieCalculator.calculateCalories(grams, selectedFood.getCaloriesPer100g());
            int remainingAfter = calculateRemainingAfter(cal);
            String remainingText = remainingAfter >= 0
                    ? getString(R.string.meal_fit_remaining_format, remainingAfter)
                    : getString(R.string.meal_over_remaining_format, Math.abs(remainingAfter));
            tvCalculatedCalories.setText(getString(R.string.calculated_calorie_format, Math.round(grams), cal)
                    + "\n" + remainingText);
            tvCalculatedCalories.setVisibility(View.VISIBLE);
        } catch (NumberFormatException e) {
            tvCalculatedCalories.setVisibility(View.GONE);
        }
    }

    private String buildFoodRiskWarning(Food food) {
        if (currentUser == null) return null;
        StringBuilder builder = new StringBuilder();
        if (food.isSugarWarning() && currentUser.isHasDiabetes()) {
            builder.append(getString(R.string.warning_diabetes));
        }
        if (food.isSaltWarning() && currentUser.isHasHypertension()) {
            if (builder.length() > 0) builder.append("\n");
            builder.append(getString(R.string.warning_hypertension));
        }
        if (food.isFatWarning() && currentUser.isHasCholesterol()) {
            if (builder.length() > 0) builder.append("\n");
            builder.append(getString(R.string.warning_cholesterol));
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    private int calculateRemainingAfter(int mealCalories) {
        if (currentUser == null) return 0;
        return currentUser.getDailyCalorieGoal() - todayConsumed - mealCalories;
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
        float grams = convertToGrams(amount, getSelectedPortionKey());
        int totalCalories = CalorieCalculator.calculateCalories(grams, selectedFood.getCaloriesPer100g());

        MealEntry entry = new MealEntry();
        entry.setFoodId(selectedFood.getId());
        entry.setUserEmail(repository.getCurrentUserEmail());
        entry.setFoodName(foodName);
        entry.setDate(DateUtils.getToday());
        entry.setMealType(getSelectedMealTypeKey());
        entry.setAmountGrams(grams);
        entry.setTotalCalories(totalCalories);

        // Makro degerlerini oku (bos birakilmissa 0 kalir)
        entry.setProteinGrams(parseInt(etProtein));
        entry.setCarbGrams(parseInt(etCarb));
        entry.setFatGrams(parseInt(etFat));
        // Kullanici girdiyse gercek deger — AI tahminini isaretleme
        entry.setAiEstimated(false);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                repository.insertMealEntry(entry);
                runOnUiThread(() -> {
                    Snackbar.make(btnSaveMeal, R.string.meal_added_success, Snackbar.LENGTH_SHORT).show();
                    finish();
                });
            } catch (RuntimeException exception) {
                runOnUiThread(() ->
                        Snackbar.make(btnSaveMeal, R.string.sync_status_error, Snackbar.LENGTH_LONG).show());
            }
        });
    }

    /** Makro bolumunu ac/kapa, ok simgesini dondur. */
    private void toggleMacroSection() {
        macroExpanded = !macroExpanded;
        layoutMacros.setVisibility(macroExpanded ? View.VISIBLE : View.GONE);
        float from = macroExpanded ? 0f : 90f;
        float to   = macroExpanded ? 90f : 0f;
        RotateAnimation rotate = new RotateAnimation(
                from, to,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(200);
        rotate.setFillAfter(true);
        ivMacroArrow.startAnimation(rotate);
    }

    /** EditText icerigini integer'a cevirir, bos/hataliysa 0 doner. */
    private int parseInt(TextInputEditText et) {
        try {
            String s = et.getText() != null ? et.getText().toString().trim() : "";
            return TextUtils.isEmpty(s) ? 0 : Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updatePortionHint() {
        String key = getSelectedPortionKey();
        int grams = Math.round(convertToGrams(1f, key));
        tvPortionHint.setText(getString(R.string.portion_hint_format, grams));
    }

    private String getSuggestedMealLabel() {
        String suggested = DateUtils.getSuggestedMealType();
        if ("BREAKFAST".equals(suggested)) return getString(R.string.label_meal_breakfast);
        if ("LUNCH".equals(suggested)) return getString(R.string.label_meal_lunch);
        if ("DINNER".equals(suggested)) return getString(R.string.label_meal_dinner);
        return getString(R.string.label_meal_snack);
    }

    private String getSelectedMealTypeKey() {
        String selected = dropdownMealType.getText() != null ? dropdownMealType.getText().toString() : "";
        String[] labels = {
                getString(R.string.label_meal_breakfast),
                getString(R.string.label_meal_lunch),
                getString(R.string.label_meal_dinner),
                getString(R.string.label_meal_snack)
        };
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].equals(selected)) return mealTypeKeys[i];
        }
        return DateUtils.getSuggestedMealType();
    }

    private String getSelectedPortionKey() {
        String selected = dropdownPortionType.getText() != null ? dropdownPortionType.getText().toString() : "";
        String[] labels = {
                getString(R.string.portion_gram),
                getString(R.string.portion_plate),
                getString(R.string.portion_bowl),
                getString(R.string.portion_tablespoon),
                getString(R.string.portion_teaspoon),
                getString(R.string.portion_glass),
                getString(R.string.portion_serving),
                getString(R.string.portion_piece)
        };
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].equals(selected)) return portionKeys[i];
        }
        return "GRAM";
    }

    private float convertToGrams(float amount, String portionKey) {
        switch (portionKey) {
            case "PLATE":
                return amount * 250f;
            case "BOWL":
                return amount * 180f;
            case "TABLESPOON":
                return amount * 15f;
            case "TEASPOON":
                return amount * 5f;
            case "GLASS":
                return amount * 200f;
            case "PORTION":
                return amount * 120f;
            case "PIECE":
                return amount * 90f;
            default:
                return amount;
        }
    }
}
