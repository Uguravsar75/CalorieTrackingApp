package tr.edu.duzce.mf.bm.calorietrackingapp;

import org.junit.Test;

import java.util.List;

import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.Food;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.FoodCatalog;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.CalorieCalculator;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.FoodImageProvider;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.HashUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for core domain utilities: CalorieCalculator, FoodCatalog, FoodImageProvider, and HashUtils.
 */
public class ExampleUnitTest {

    @Test
    public void calorieCalculator_calculatesPortionCaloriesCorrectly() {
        assertEquals(390, CalorieCalculator.calculateCalories(150f, 260));
        assertEquals(0, CalorieCalculator.calculateCalories(0f, 260));
    }

    @Test
    public void calorieCalculator_calculatesDailyGoalFromMifflinStJeor() {
        User user = new User();
        user.setAge(25);
        user.setGender("MALE");
        user.setHeight(180f);
        user.setWeight(75f);
        user.setActivityLevel("MODERATE");
        user.setDiseaseType("NONE");

        int goal = CalorieCalculator.calculateDailyGoal(user);
        assertTrue("Daily calorie goal should be within healthy adult range", goal >= 2000 && goal <= 3000);
    }

    @Test
    public void foodCatalog_containsAllPredefinedFoodsWithValidValues() {
        List<Food> foods = FoodCatalog.getPredefinedFoods();
        assertEquals(FoodCatalog.PREDEFINED_FOOD_COUNT, foods.size());
        for (Food food : foods) {
            assertNotNull(food.getNameTr());
            assertNotNull(food.getNameEn());
            assertFalse(food.getNameTr().trim().isEmpty());
            assertFalse(food.getNameEn().trim().isEmpty());
            assertTrue(food.getCaloriesPer100g() > 0);
        }
    }

    @Test
    public void foodImageProvider_matchesExactWordsForShortKeys() {
        assertNotNull(FoodImageProvider.getUrl("Su"));
        assertNull("Short key 'su' should not false-match 'Sucuk'", FoodImageProvider.getUrl("Sucuk"));
        assertNotNull(FoodImageProvider.getUrl("Mercimek Çorbası"));
    }

    @Test
    public void hashUtils_producesConsistentSha256Hex() {
        String hash1 = HashUtils.sha256("password123");
        String hash2 = HashUtils.sha256("password123");
        assertEquals(64, hash1.length());
        assertEquals(hash1, hash2);
    }
}
