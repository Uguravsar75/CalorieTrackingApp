package tr.edu.duzce.mf.bm.calorietrackingapp.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tr.edu.duzce.mf.bm.calorietrackingapp.database.dao.FoodDao;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.dao.MealEntryDao;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.dao.UserDao;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.Food;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.MealEntry;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;

@Database(entities = {User.class, Food.class, MealEntry.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(2);

    public abstract UserDao userDao();
    public abstract FoodDao foodDao();
    public abstract MealEntryDao mealEntryDao();

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "calorie_tracking_db"
                            )
                            .fallbackToDestructiveMigration() // geliştirme aşaması: şema değişince DB sıfırlanır
                            .addCallback(prepopulateCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Veritabanı ilk oluşturulduğunda ön tanımlı gıdaları ekler
    private static final RoomDatabase.Callback prepopulateCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                FoodDao foodDao = INSTANCE.foodDao();
                if (foodDao.getPredefinedFoodCount() == 0) {
                    foodDao.insertAll(getPredefinedFoods());
                }
            });
        }
    };

    // Türkiye'de yaygın yiyecekler — kategori ve şeker uyarısı ile
    private static List<Food> getPredefinedFoods() {
        List<Food> foods = new ArrayList<>();

        // KAHVALTI
        foods.add(makeFood("Simit", "Sesame Bagel", 265, "BREAKFAST", false));
        foods.add(makeFood("Poğaça", "Poğaça (Pastry)", 320, "BREAKFAST", false));
        foods.add(makeFood("Açma", "Açma (Soft Roll)", 290, "BREAKFAST", false));
        foods.add(makeFood("Beyaz Peynir", "White Cheese (Feta)", 264, "BREAKFAST", false));
        foods.add(makeFood("Kaşar Peyniri", "Yellow Cheese", 380, "BREAKFAST", false));
        foods.add(makeFood("Yumurta (haşlanmış)", "Boiled Egg", 155, "BREAKFAST", false));
        foods.add(makeFood("Omlet", "Omelette", 154, "BREAKFAST", false));
        foods.add(makeFood("Menemen", "Menemen (Egg & Tomato)", 130, "BREAKFAST", false));
        foods.add(makeFood("Bal", "Honey", 304, "BREAKFAST", true));
        foods.add(makeFood("Reçel", "Jam", 250, "BREAKFAST", true));
        foods.add(makeFood("Tereyağı", "Butter", 717, "BREAKFAST", false));
        foods.add(makeFood("Ekmek (beyaz)", "White Bread", 265, "BREAKFAST", false));
        foods.add(makeFood("Ekmek (tam buğday)", "Whole Wheat Bread", 247, "BREAKFAST", false));
        foods.add(makeFood("Çay (sütsüz)", "Black Tea", 2, "BREAKFAST", false));
        foods.add(makeFood("Süt (tam yağlı)", "Whole Milk", 61, "BREAKFAST", true));

        // ÖĞLE
        foods.add(makeFood("Lahmacun", "Lahmacun (Turkish Pizza)", 270, "LUNCH", false));
        foods.add(makeFood("Pide (peynirli)", "Cheese Pide", 280, "LUNCH", false));
        foods.add(makeFood("Döner (tavuk)", "Chicken Doner", 215, "LUNCH", false));
        foods.add(makeFood("Döner (et)", "Meat Doner", 250, "LUNCH", false));
        foods.add(makeFood("İskender Kebap", "İskender Kebab", 220, "LUNCH", false));
        foods.add(makeFood("Köfte", "Meatball", 235, "LUNCH", false));
        foods.add(makeFood("Pilav (beyaz pirinç)", "White Rice", 130, "LUNCH", false));
        foods.add(makeFood("Bulgur Pilavı", "Bulgur Pilaf", 112, "LUNCH", false));
        foods.add(makeFood("Mercimek Çorbası", "Red Lentil Soup", 56, "LUNCH", false));
        foods.add(makeFood("Ezogelin Çorbası", "Ezogelin Soup", 58, "LUNCH", false));
        foods.add(makeFood("Tavuk Göğsü (ızgara)", "Grilled Chicken Breast", 165, "LUNCH", false));
        foods.add(makeFood("Şiş Kebap", "Shish Kebab", 195, "LUNCH", false));
        foods.add(makeFood("Salata (karışık)", "Mixed Salad", 20, "LUNCH", false));
        foods.add(makeFood("Ayran", "Ayran (Yogurt Drink)", 36, "LUNCH", false));
        foods.add(makeFood("Cacık", "Cacık (Yogurt & Cucumber)", 45, "LUNCH", false));

        // AKŞAM
        foods.add(makeFood("Kuru Fasulye", "White Bean Stew", 137, "DINNER", false));
        foods.add(makeFood("Nohut Yemeği", "Chickpea Stew", 164, "DINNER", false));
        foods.add(makeFood("Mercimek Yemeği", "Lentil Dish", 116, "DINNER", false));
        foods.add(makeFood("Etli Sebze (karışık)", "Mixed Vegetable with Meat", 95, "DINNER", false));
        foods.add(makeFood("Izgara Balık", "Grilled Fish", 150, "DINNER", false));
        foods.add(makeFood("Hamsi Tava", "Fried Anchovy", 190, "DINNER", false));
        foods.add(makeFood("Makarna (domates soslu)", "Pasta (Tomato Sauce)", 158, "DINNER", false));
        foods.add(makeFood("Börek (ispanaklı)", "Spinach Börek", 245, "DINNER", false));
        foods.add(makeFood("Türk Usulü Yoğurt", "Turkish Yogurt", 61, "DINNER", false));
        foods.add(makeFood("Domates Çorbası", "Tomato Soup", 42, "DINNER", false));

        // ATISTIRMALIK
        foods.add(makeFood("Hamburger", "Hamburger", 295, "SNACK", false));
        foods.add(makeFood("Kola (330ml)", "Cola (330ml)", 139, "SNACK", true));
        foods.add(makeFood("Ayçiçeği Çekirdeği", "Sunflower Seeds", 584, "SNACK", false));
        foods.add(makeFood("Cips (patates)", "Potato Chips", 536, "SNACK", false));
        foods.add(makeFood("Çikolata (sütlü)", "Milk Chocolate", 535, "SNACK", true));
        foods.add(makeFood("Elma", "Apple", 52, "SNACK", true));
        foods.add(makeFood("Muz", "Banana", 89, "SNACK", true));
        foods.add(makeFood("Portakal", "Orange", 47, "SNACK", true));
        foods.add(makeFood("Üzüm", "Grape", 67, "SNACK", true));
        foods.add(makeFood("Baklava", "Baklava", 428, "SNACK", true));
        foods.add(makeFood("Lokum", "Turkish Delight", 322, "SNACK", true));
        foods.add(makeFood("Sütlaç", "Rice Pudding", 122, "SNACK", true));
        foods.add(makeFood("Helva (tahin)", "Halva", 516, "SNACK", true));
        foods.add(makeFood("Kek", "Cake", 350, "SNACK", true));
        foods.add(makeFood("Sandviç (tavuklu)", "Chicken Sandwich", 210, "SNACK", false));
        foods.add(makeFood("Pizza (dilim)", "Pizza Slice", 266, "SNACK", false));
        foods.add(makeFood("Tost", "Toast", 230, "SNACK", false));
        foods.add(makeFood("Fındık", "Hazelnut", 628, "SNACK", false));
        foods.add(makeFood("Ceviz", "Walnut", 654, "SNACK", false));

        return foods;
    }

    private static Food makeFood(String nameTr, String nameEn, int cal100g, String category, boolean sugarWarning) {
        Food f = new Food();
        f.setNameTr(nameTr);
        f.setNameEn(nameEn);
        f.setCaloriesPer100g(cal100g);
        f.setCategory(category);
        f.setPredefined(true);
        f.setSugarWarning(sugarWarning);
        return f;
    }
}
