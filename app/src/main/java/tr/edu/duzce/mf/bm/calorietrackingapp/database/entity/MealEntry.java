package tr.edu.duzce.mf.bm.calorietrackingapp.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "meal_entries")
public class MealEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String firestoreId;

    private int foodId;         // foods tablosuna referans
    private String userEmail;   // oturumdaki kullanıcıya ait kayıt
    private String foodName;    // Join yapmamak için yemek adını saklıyoruz
    private String date;        // "yyyy-MM-dd" formatı
    private String mealType;    // "BREAKFAST", "LUNCH", "DINNER", "SNACK"
    private float amountGrams;  // gram cinsinden miktar
    private long createdAt;     // unix timestamp — ekleme zamanı
    private int totalCalories;  // hesaplanan toplam kalori
    private String imageUrl;    // lokal yol (file://...) veya web URL — yemek fotoğrafı
    private int proteinGrams;
    private int carbGrams;
    private int fatGrams;
    private boolean aiEstimated;

    public MealEntry() {}

    // --- Getter ve Setter ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirestoreId() { return firestoreId; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }

    public int getFoodId() { return foodId; }
    public void setFoodId(int foodId) { this.foodId = foodId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

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

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getProteinGrams() { return proteinGrams; }
    public void setProteinGrams(int proteinGrams) { this.proteinGrams = proteinGrams; }

    public int getCarbGrams() { return carbGrams; }
    public void setCarbGrams(int carbGrams) { this.carbGrams = carbGrams; }

    public int getFatGrams() { return fatGrams; }
    public void setFatGrams(int fatGrams) { this.fatGrams = fatGrams; }

    public boolean isAiEstimated() { return aiEstimated; }
    public void setAiEstimated(boolean aiEstimated) { this.aiEstimated = aiEstimated; }
}
