package tr.edu.duzce.mf.bm.calorietrackingapp.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "meal_entries")
public class MealEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int foodId;         // foods tablosuna referans
    private String foodName;    // Join yapmamak için yemek adını saklıyoruz
    private String date;        // "yyyy-MM-dd" formatı
    private String mealType;    // "BREAKFAST", "LUNCH", "DINNER", "SNACK"
    private float amountGrams;  // gram cinsinden miktar
    private int totalCalories;  // hesaplanan toplam kalori

    public MealEntry() {}

    // --- Getter ve Setter ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFoodId() { return foodId; }
    public void setFoodId(int foodId) { this.foodId = foodId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public float getAmountGrams() { return amountGrams; }
    public void setAmountGrams(float amountGrams) { this.amountGrams = amountGrams; }

    public int getTotalCalories() { return totalCalories; }
    public void setTotalCalories(int totalCalories) { this.totalCalories = totalCalories; }
}
