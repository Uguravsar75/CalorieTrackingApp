package tr.edu.duzce.mf.bm.calorietrackingapp.repository;

import android.content.Context;

import java.util.List;

import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.dao.MealEntryDao;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.Food;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.MealEntry;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;

/**
 * AppRepository: Tüm database işlemlerini tek yerden yönetir.
 * Activity'ler bu sınıf üzerinden veri okur/yazar.
 * Not: Room, ana thread'de database işlemine izin vermez.
 * Bu nedenle bu metodlar arka planda thread içinde çağrılmalıdır.
 */
public class AppRepository {

    private final AppDatabase db;

    public AppRepository(Context context) {
        db = AppDatabase.getInstance(context);
    }

    // --- Kullanıcı İşlemleri ---

    public void insertUser(User user) {
        db.userDao().insert(user);
    }

    public void updateUser(User user) {
        db.userDao().update(user);
    }

    public User getUser() {
        return db.userDao().getUser();
    }

    public boolean isUserSetup() {
        return db.userDao().getUserCount() > 0;
    }

    // Email ile kullanıcı var mı kontrol et (kayıt olurken)
    public boolean isEmailTaken(String email) {
        return db.userDao().getUserByEmail(email) != null;
    }

    // Email + şifre hash ile giriş yap
    public User login(String email, String passwordHash) {
        return db.userDao().login(email, passwordHash);
    }

    // Kayıt aşamasında email + şifre hash'i kaydeder (profil bilgileri sonra doldurulur)
    public void saveEmailAndPassword(String email, String passwordHash) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        db.userDao().insert(user);
    }

    // Profil bilgileri (ad, yaş vb.) doldurulmuş mu kontrol eder
    public boolean isProfileComplete() {
        User user = db.userDao().getUser();
        return user != null && user.getName() != null && !user.getName().isEmpty();
    }

    // --- Gıda İşlemleri ---

    public List<Food> getAllFoods() {
        return db.foodDao().getAllFoods();
    }

    public List<Food> getFoodsByCategory(String category) {
        return db.foodDao().getFoodsByCategory(category);
    }

    public List<Food> searchFoods(String query) {
        return db.foodDao().searchFoods(query);
    }

    // --- Öğün Kaydı İşlemleri ---

    public void insertMealEntry(MealEntry entry) {
        db.mealEntryDao().insert(entry);
    }

    public void deleteMealEntry(MealEntry entry) {
        db.mealEntryDao().delete(entry);
    }

    public List<MealEntry> getMealEntriesByDate(String date) {
        return db.mealEntryDao().getMealEntriesByDate(date);
    }

    public int getTotalCaloriesForDate(String date) {
        return db.mealEntryDao().getTotalCaloriesForDate(date);
    }

    public List<MealEntryDao.DailyCalorieSummary> getDailyCaloriesBetween(String startDate, String endDate) {
        return db.mealEntryDao().getDailyCaloriesBetween(startDate, endDate);
    }
}
