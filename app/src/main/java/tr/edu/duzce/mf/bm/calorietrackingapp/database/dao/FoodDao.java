package tr.edu.duzce.mf.bm.calorietrackingapp.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.Food;

@Dao
public interface FoodDao {

    @Insert
    void insert(Food food);

    @Insert
    void insertAll(List<Food> foods);

    // Tüm gıdaları getir
    @Query("SELECT * FROM foods ORDER BY nameTr ASC")
    List<Food> getAllFoods();

    // Kategoriye göre filtrele (öğün zamanı mantığı için)
    @Query("SELECT * FROM foods WHERE category = :category ORDER BY nameTr ASC")
    List<Food> getFoodsByCategory(String category);

    // Türkçe veya İngilizce ada göre arama
    @Query("SELECT * FROM foods WHERE nameTr LIKE '%' || :query || '%' OR nameEn LIKE '%' || :query || '%' ORDER BY nameTr ASC")
    List<Food> searchFoods(String query);

    @Query("SELECT COUNT(*) FROM foods WHERE isPredefined = 1")
    int getPredefinedFoodCount();

    @Query("DELETE FROM foods WHERE isPredefined = 1")
    void deleteAllPredefined();
}
