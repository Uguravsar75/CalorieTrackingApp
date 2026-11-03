package tr.edu.duzce.mf.bm.calorietrackingapp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.MealEntry;

@Dao
public interface MealEntryDao {

    @Insert
    void insert(MealEntry mealEntry);

    @Update
    void update(MealEntry mealEntry);

    @Delete
    void delete(MealEntry mealEntry);

    // Belirli bir günün tüm öğünlerini getir (öğün tipi → ekleme zamanı sırasıyla)
    @Query("SELECT * FROM meal_entries WHERE userEmail = :userEmail AND date = :date ORDER BY mealType ASC, createdAt ASC")
    List<MealEntry> getMealEntriesByDate(String userEmail, String date);

    // Belirli bir günün toplam kalorisini hesapla
    @Query("SELECT COALESCE(SUM(totalCalories), 0) FROM meal_entries WHERE userEmail = :userEmail AND date = :date")
    int getTotalCaloriesForDate(String userEmail, String date);

    // Son 7 günün günlük kalori toplamlarını getir (haftalık rapor için)
    @Query("SELECT date, SUM(totalCalories) as totalCalories FROM meal_entries WHERE userEmail = :userEmail AND date >= :startDate AND date <= :endDate GROUP BY date ORDER BY date ASC")
    List<DailyCalorieSummary> getDailyCaloriesBetween(String userEmail, String startDate, String endDate);

    @Query("SELECT foodName, COUNT(*) as count, SUM(totalCalories) as totalCalories FROM meal_entries WHERE userEmail = :userEmail AND date >= :startDate AND date <= :endDate GROUP BY foodName ORDER BY count DESC, totalCalories DESC LIMIT 1")
    TopFoodSummary getTopFoodBetween(String userEmail, String startDate, String endDate);

    @Query("SELECT COUNT(*) FROM meal_entries WHERE userEmail = :userEmail AND date = :date")
    int getMealCountForDate(String userEmail, String date);

    // Haftalık rapor için yardımcı sınıf
    class DailyCalorieSummary {
        public String date;
        public int totalCalories;
    }

    class TopFoodSummary {
        public String foodName;
        public int count;
        public int totalCalories;
    }
}
