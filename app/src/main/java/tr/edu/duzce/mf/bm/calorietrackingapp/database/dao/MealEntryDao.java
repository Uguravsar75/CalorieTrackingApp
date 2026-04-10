package tr.edu.duzce.mf.bm.calorietrackingapp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.MealEntry;

@Dao
public interface MealEntryDao {

    @Insert
    void insert(MealEntry mealEntry);

    @Delete
    void delete(MealEntry mealEntry);

    // Belirli bir günün tüm öğünlerini getir
    @Query("SELECT * FROM meal_entries WHERE date = :date ORDER BY mealType ASC")
    List<MealEntry> getMealEntriesByDate(String date);

    // Belirli bir günün toplam kalorisini hesapla
    @Query("SELECT COALESCE(SUM(totalCalories), 0) FROM meal_entries WHERE date = :date")
    int getTotalCaloriesForDate(String date);

    // Son 7 günün günlük kalori toplamlarını getir (haftalık rapor için)
    @Query("SELECT date, SUM(totalCalories) as totalCalories FROM meal_entries WHERE date >= :startDate AND date <= :endDate GROUP BY date ORDER BY date ASC")
    List<DailyCalorieSummary> getDailyCaloriesBetween(String startDate, String endDate);

    // Haftalık rapor için yardımcı sınıf
    class DailyCalorieSummary {
        public String date;
        public int totalCalories;
    }
}
