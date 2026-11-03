package tr.edu.duzce.mf.bm.calorietrackingapp.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.dao.FoodDao;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.dao.MealEntryDao;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.dao.UserDao;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.Food;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.MealEntry;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.FoodCatalog;

@Database(entities = {User.class, Food.class, MealEntry.class}, version = 5, exportSchema = false)
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
                                    context.getString(R.string.db_name)
                            )
                            .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                            .fallbackToDestructiveMigration() // Şema değişikliğinde migration başarısız olursa DB sıfırlanır
                            .addCallback(prepopulateCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Migration 3 → 4: MealEntry tablosuna createdAt (unix timestamp) sütunu eklendi
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE meal_entries ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0"
            );
        }
    };

    // Migration 4 -> 5: Kamera AI makro tahminleri öğün kaydına eklendi
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE meal_entries ADD COLUMN proteinGrams INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE meal_entries ADD COLUMN carbGrams INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE meal_entries ADD COLUMN fatGrams INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE meal_entries ADD COLUMN aiEstimated INTEGER NOT NULL DEFAULT 0");
        }
    };

    // Ön tanımlı gıdaları ekler — onCreate ve güncel olmayan DB'lerde onOpen'da çalışır
    private static final RoomDatabase.Callback prepopulateCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                FoodDao foodDao = INSTANCE.foodDao();
                foodDao.deleteAllPredefined();
                foodDao.insertAll(FoodCatalog.getPredefinedFoods());
            });
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            databaseWriteExecutor.execute(() -> {
                FoodDao foodDao = INSTANCE.foodDao();
                if (foodDao.getPredefinedFoodCount() < FoodCatalog.PREDEFINED_FOOD_COUNT) {
                    foodDao.deleteAllPredefined();
                    foodDao.insertAll(FoodCatalog.getPredefinedFoods());
                }
            });
        }
    };
}
