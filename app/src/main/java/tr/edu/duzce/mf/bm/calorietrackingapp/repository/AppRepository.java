package tr.edu.duzce.mf.bm.calorietrackingapp.repository;

import android.content.Context;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import tr.edu.duzce.mf.bm.calorietrackingapp.database.dao.MealEntryDao;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.Food;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.MealEntry;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.SessionManager;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.SyncStatusManager;

public class AppRepository {

    private static final String COLLECTION_USERS        = "users";
    private static final String COLLECTION_FOODS         = "foods";
    private static final String COLLECTION_MEAL_ENTRIES  = "meal_entries";
    private static final String COLLECTION_DAILY_STATS   = "daily_stats";  // su/adim/egzersiz

    private static volatile List<Food> cachedFoods;
    private static volatile boolean foodsSeededInSession = false;

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final SessionManager sessionManager;
    private final SyncStatusManager syncStatusManager;

    public AppRepository(Context context) {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(context);
        syncStatusManager = new SyncStatusManager(context);
    }

    public void insertUser(User user) {
        updateUser(user);
    }

    public void updateUser(User user) {
        try {
            FirebaseUser currentUser = requireCurrentUser();
            user.setEmail(currentUser.getEmail());
            user.setAuthProvider(currentUser.getProviderId());
            user.setTimezoneId(resolveTimezone(user.getTimezoneId()));
            user.setLastSyncedAt(System.currentTimeMillis());
            user.setPasswordHash(null);
            user.setId(0);
            Tasks.await(users().document(currentUser.getUid()).set(user));
            syncStatusManager.markSuccess();
        } catch (Exception e) {
            syncStatusManager.markFailure(e);
            throw new RuntimeException("Failed to update user profile", e);
        }
    }

    public User getUser() {
        try {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                return null;
            }

            DocumentSnapshot snapshot = Tasks.await(users().document(currentUser.getUid()).get());
            if (snapshot.exists()) {
                User user = snapshot.toObject(User.class);
                if (user != null) {
                    user.setEmail(currentUser.getEmail());
                    user.setPasswordHash(null);
                    syncStatusManager.markSuccess();
                    return user;
                }
            }

            User fallback = new User();
            fallback.setEmail(currentUser.getEmail());
            fallback.setAuthProvider(currentUser.getProviderId());
            fallback.setTimezoneId(TimeZone.getDefault().getID());
            fallback.setLastSyncedAt(System.currentTimeMillis());
            syncStatusManager.markSuccess();
            return fallback;
        } catch (Exception e) {
            android.util.Log.w("AppRepository", "getUser failed", e);
            syncStatusManager.markFailure(e);
            return null;
        }
    }

    public boolean isUserSetup() {
        User user = getUser();
        return user != null && user.getName() != null && !user.getName().trim().isEmpty();
    }

    public boolean isProfileComplete() {
        return isUserSetup();
    }

    public boolean isEmailTaken(String email) {
        try {
            QuerySnapshot snapshot = Tasks.await(users().whereEqualTo("email", email).limit(1).get());
            syncStatusManager.markSuccess();
            return !snapshot.isEmpty();
        } catch (Exception e) {
            syncStatusManager.markFailure(e);
            throw new RuntimeException("Failed to check email availability", e);
        }
    }

    public User login(String email, String password) {
        try {
            AuthResult result = Tasks.await(auth.signInWithEmailAndPassword(email, password));
            FirebaseUser firebaseUser = result.getUser();
            if (firebaseUser == null) {
                return null;
            }
            sessionManager.saveSession(email);
            ensureUserProfile(firebaseUser);
            syncStatusManager.markSuccess();
            return getUser();
        } catch (Exception e) {
            syncStatusManager.markFailure(e);
            return null;
        }
    }

    public void saveEmailAndPassword(String email, String password) {
        try {
            AuthResult result = Tasks.await(auth.createUserWithEmailAndPassword(email, password));
            FirebaseUser firebaseUser = result.getUser();
            if (firebaseUser == null) {
                throw new IllegalStateException("User was not created");
            }
            sessionManager.saveSession(email);
            ensureUserProfile(firebaseUser);
            syncStatusManager.markSuccess();
        } catch (Exception e) {
            syncStatusManager.markFailure(e);
            throw new RuntimeException("Failed to register user", e);
        }
    }

    public void changePassword(String currentPassword, String newPassword) throws Exception {
        FirebaseUser user = requireCurrentUser();
        String email = user.getEmail();
        if (email == null || email.isEmpty()) {
            throw new IllegalStateException("No email address associated with this account");
        }

        // Mevcut şifreyi doğrulamak için sign-in yap — reauthenticate'den daha güvenilir
        try {
            AuthResult result = Tasks.await(auth.signInWithEmailAndPassword(email, currentPassword));
            if (result.getUser() == null) {
                throw new Exception("WRONG_PASSWORD");
            }
        } catch (java.util.concurrent.ExecutionException e) {
            android.util.Log.e("AppRepository", "changePassword: wrong current password", e.getCause());
            throw new Exception("WRONG_PASSWORD", e.getCause());
        }

        // Doğrulama geçti — şifreyi güncelle
        try {
            Tasks.await(user.updatePassword(newPassword));
        } catch (java.util.concurrent.ExecutionException e) {
            android.util.Log.e("AppRepository", "changePassword: updatePassword failed", e.getCause());
            throw new Exception("UPDATE_FAILED", e.getCause());
        }
        syncStatusManager.markSuccess();
    }

    public boolean isEmailPasswordUser() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return false;
        for (com.google.firebase.auth.UserInfo info : user.getProviderData()) {
            if (EmailAuthProvider.PROVIDER_ID.equals(info.getProviderId())) return true;
        }
        return false;
    }

    public User loginWithGoogle(String idToken) {
        try {
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            AuthResult result = Tasks.await(auth.signInWithCredential(credential));
            FirebaseUser firebaseUser = result.getUser();
            if (firebaseUser == null) {
                return null;
            }
            sessionManager.saveSession(firebaseUser.getEmail());
            ensureUserProfile(firebaseUser);
            syncStatusManager.markSuccess();
            return getUser();
        } catch (Exception e) {
            syncStatusManager.markFailure(e);
            throw new RuntimeException("Failed to sign in with Google", e);
        }
    }

    public List<Food> getAllFoods() {
        List<Food> currentCache = cachedFoods;
        if (currentCache != null && !currentCache.isEmpty()) {
            return new ArrayList<>(currentCache);
        }
        try {
            if (!foodsSeededInSession) {
                ensureFoodsSeeded();
                foodsSeededInSession = true;
            }
            QuerySnapshot snapshot = Tasks.await(foods().get());
            List<Food> foods = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                Food food = document.toObject(Food.class);
                if (food != null) {
                    foods.add(food);
                }
            }
            if (!foods.isEmpty()) {
                foods.sort(Comparator
                        .comparing(Food::getCategory, Comparator.nullsLast(String::compareTo))
                        .thenComparing(Food::getNameTr, Comparator.nullsLast(String::compareTo)));
                cachedFoods = new ArrayList<>(foods);
                syncStatusManager.markSuccess();
                return foods;
            }
        } catch (Exception e) {
            android.util.Log.w("AppRepository", "Firestore foods fetch failed, using local catalog", e);
            syncStatusManager.markFailure(e);
        }
        List<Food> local = FoodCatalog.getPredefinedFoods();
        local.sort(Comparator
                .comparing(Food::getCategory, Comparator.nullsLast(String::compareTo))
                .thenComparing(Food::getNameTr, Comparator.nullsLast(String::compareTo)));
        cachedFoods = new ArrayList<>(local);
        return local;
    }

    public List<Food> getFoodsByCategory(String category) {
        List<Food> allFoods = getAllFoods();
        List<Food> filtered = new ArrayList<>();
        for (Food food : allFoods) {
            if (category.equals(food.getCategory())) {
                filtered.add(food);
            }
        }
        return filtered;
    }

    public List<Food> searchFoods(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllFoods();
        }
        String normalized = normalizeForSearch(query);
        List<Food> result = new ArrayList<>();
        for (Food food : getAllFoods()) {
            String nameTr = normalizeForSearch(food.getNameTr());
            String nameEn = normalizeForSearch(food.getNameEn());
            if (nameTr.contains(normalized) || nameEn.contains(normalized)) {
                result.add(food);
            }
        }
        return result;
    }

    public void insertMealEntry(MealEntry entry) {
        try {
            FirebaseUser currentUser = requireCurrentUser();
            if (entry.getUserEmail() == null || entry.getUserEmail().isEmpty()) {
                entry.setUserEmail(currentUser.getEmail());
            }
            if (entry.getCreatedAt() == 0) {
                entry.setCreatedAt(System.currentTimeMillis());
            }
            String firestoreId = entry.getFirestoreId();
            if (firestoreId == null || firestoreId.isEmpty()) {
                firestoreId = meals(currentUser.getUid()).document().getId();
                entry.setFirestoreId(firestoreId);
            }
            Tasks.await(meals(currentUser.getUid()).document(firestoreId).set(entry));
            syncStatusManager.markSuccess();
        } catch (Exception e) {
            syncStatusManager.markFailure(e);
            throw new RuntimeException("Failed to insert meal entry", e);
        }
    }

    public void updateMealEntry(MealEntry entry) {
        if (entry.getFirestoreId() == null || entry.getFirestoreId().isEmpty()) {
            throw new IllegalArgumentException("Meal entry is missing firestoreId");
        }
        insertMealEntry(entry);
    }

    public void deleteMealEntry(MealEntry entry) {
        try {
            FirebaseUser currentUser = requireCurrentUser();
            if (entry.getFirestoreId() == null || entry.getFirestoreId().isEmpty()) {
                throw new IllegalArgumentException("Meal entry is missing firestoreId");
            }
            Tasks.await(meals(currentUser.getUid()).document(entry.getFirestoreId()).delete());
            syncStatusManager.markSuccess();
        } catch (Exception e) {
            syncStatusManager.markFailure(e);
            throw new RuntimeException("Failed to delete meal entry", e);
        }
    }

    public List<MealEntry> getMealEntriesByDate(String date) {
        try {
            FirebaseUser currentUser = requireCurrentUser();
            QuerySnapshot snapshot = Tasks.await(
                    meals(currentUser.getUid())
                            .whereEqualTo("date", date)
                            .get()
            );
            List<MealEntry> entries = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                MealEntry entry = document.toObject(MealEntry.class);
                if (entry != null) {
                    entry.setFirestoreId(document.getId());
                    entries.add(entry);
                }
            }
            entries.sort(Comparator
                    .comparing(MealEntry::getMealType, Comparator.nullsLast(String::compareTo))
                    .thenComparingLong(MealEntry::getCreatedAt));
            syncStatusManager.markSuccess();
            return entries;
        } catch (Exception e) {
            android.util.Log.w("AppRepository", "getMealEntriesByDate failed", e);
            syncStatusManager.markFailure(e);
            return new ArrayList<>();
        }
    }

    public int getTotalCaloriesForDate(String date) {
        int total = 0;
        for (MealEntry entry : getMealEntriesByDate(date)) {
            total += entry.getTotalCalories();
        }
        return total;
    }

    public List<MealEntryDao.DailyCalorieSummary> getDailyCaloriesBetween(String startDate, String endDate) {
        List<MealEntry> entries = getMealEntriesBetween(startDate, endDate);
        Map<String, Integer> totals = new HashMap<>();
        for (MealEntry entry : entries) {
            int currentTotal = totals.containsKey(entry.getDate()) ? totals.get(entry.getDate()) : 0;
            totals.put(entry.getDate(), currentTotal + entry.getTotalCalories());
        }

        List<MealEntryDao.DailyCalorieSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, Integer> item : totals.entrySet()) {
            MealEntryDao.DailyCalorieSummary summary = new MealEntryDao.DailyCalorieSummary();
            summary.date = item.getKey();
            summary.totalCalories = item.getValue();
            summaries.add(summary);
        }
        Collections.sort(summaries, Comparator.comparing(summary -> summary.date));
        return summaries;
    }

    public List<MealEntry> getMealEntriesBetweenDates(String startDate, String endDate) {
        return getMealEntriesBetween(startDate, endDate);
    }

    public MealEntryDao.TopFoodSummary getTopFoodBetween(String startDate, String endDate) {
        List<MealEntry> entries = getMealEntriesBetween(startDate, endDate);
        if (entries.isEmpty()) {
            return null;
        }

        Map<String, Integer> counts = new HashMap<>();
        Map<String, Integer> calories = new HashMap<>();
        for (MealEntry entry : entries) {
            String foodName = entry.getFoodName();
            counts.put(foodName, counts.getOrDefault(foodName, 0) + 1);
            calories.put(foodName, calories.getOrDefault(foodName, 0) + entry.getTotalCalories());
        }

        String bestFood = null;
        int bestCount = -1;
        int bestCalories = -1;
        for (String foodName : counts.keySet()) {
            int count = counts.get(foodName);
            int totalCalories = calories.get(foodName);
            if (count > bestCount || (count == bestCount && totalCalories > bestCalories)) {
                bestFood = foodName;
                bestCount = count;
                bestCalories = totalCalories;
            }
        }

        if (bestFood == null) {
            return null;
        }

        MealEntryDao.TopFoodSummary summary = new MealEntryDao.TopFoodSummary();
        summary.foodName = bestFood;
        summary.count = bestCount;
        summary.totalCalories = bestCalories;
        return summary;
    }

    public int getMealCountForDate(String date) {
        return getMealEntriesByDate(date).size();
    }

    public String getCurrentUserEmail() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
            return user.getEmail();
        }
        return sessionManager.getLoggedInEmail();
    }

    private List<MealEntry> getMealEntriesBetween(String startDate, String endDate) {
        try {
            FirebaseUser currentUser = requireCurrentUser();
            QuerySnapshot snapshot = Tasks.await(
                    meals(currentUser.getUid())
                            .whereGreaterThanOrEqualTo("date", startDate)
                            .whereLessThanOrEqualTo("date", endDate)
                            .get()
            );
            List<MealEntry> entries = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                MealEntry entry = document.toObject(MealEntry.class);
                if (entry != null) {
                    entry.setFirestoreId(document.getId());
                    entries.add(entry);
                }
            }
            syncStatusManager.markSuccess();
            return entries;
        } catch (Exception e) {
            android.util.Log.w("AppRepository", "getMealEntriesBetween failed", e);
            syncStatusManager.markFailure(e);
            return new ArrayList<>();
        }
    }

    private void ensureUserProfile(FirebaseUser firebaseUser) throws Exception {
        DocumentSnapshot snapshot = Tasks.await(users().document(firebaseUser.getUid()).get());
        if (snapshot.exists()) {
            return;
        }

        User user = new User();
        user.setEmail(firebaseUser.getEmail());
        user.setAuthProvider(firebaseUser.getProviderId());
        user.setActivityLevel("MODERATE");
        user.setDiseaseType("NONE");
        user.setTimezoneId(TimeZone.getDefault().getID());
        user.setLastSyncedAt(System.currentTimeMillis());
        Tasks.await(users().document(firebaseUser.getUid()).set(user));
    }

    private void ensureFoodsSeeded() {
        try {
            WriteBatch batch = firestore.batch();
            for (Food food : FoodCatalog.getPredefinedFoods()) {
                batch.set(foods().document(String.valueOf(food.getId())), food, SetOptions.merge());
            }
            Tasks.await(batch.commit());
            syncStatusManager.markSuccess();
        } catch (Exception e) {
            android.util.Log.w("AppRepository", "Food seeding to Firestore failed (will use local catalog)", e);
        }
    }

    private FirebaseUser requireCurrentUser() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("No authenticated user");
        }
        return currentUser;
    }

    private CollectionReference users() {
        return firestore.collection(COLLECTION_USERS);
    }

    private CollectionReference foods() {
        return firestore.collection(COLLECTION_FOODS);
    }

    private CollectionReference meals(String uid) {
        return users().document(uid).collection(COLLECTION_MEAL_ENTRIES);
    }

    private CollectionReference dailyStats(String uid) {
        return users().document(uid).collection(COLLECTION_DAILY_STATS);
    }

    // ── Günlük istatistikler (su / adım / egzersiz) ───────────────────────────────

    /**
     * Belirtilen günün su / adım / egzersiz değerlerini Firestore'a kaydeder.
     * Yol: users/{uid}/daily_stats/{date}
     */
    public void saveDailyStats(String date, int waterGlasses, int steps, int exerciseKcal) {
        try {
            FirebaseUser currentUser = requireCurrentUser();
            Map<String, Object> data = new HashMap<>();
            data.put("date", date);
            data.put("water", waterGlasses);
            data.put("steps", steps);
            data.put("exerciseKcal", exerciseKcal);
            data.put("updatedAt", System.currentTimeMillis());
            Tasks.await(dailyStats(currentUser.getUid()).document(date).set(data));
            syncStatusManager.markSuccess();
        } catch (Exception e) {
            android.util.Log.e("AppRepository", "saveDailyStats failed", e);
            syncStatusManager.markFailure(e);
        }
    }

    /**
     * Belirtilen günün istatistiklerini Firestore'dan çeker.
     * @return int[3] = { waterGlasses, steps, exerciseKcal }
     *         Veri yoksa { 0, 0, 0 } döner.
     */
    public int[] getDailyStats(String date) {
        try {
            FirebaseUser currentUser = requireCurrentUser();
            DocumentSnapshot snapshot = Tasks.await(
                    dailyStats(currentUser.getUid()).document(date).get()
            );
            if (snapshot.exists()) {
                int water    = toInt(snapshot.getLong("water"));
                int steps    = toInt(snapshot.getLong("steps"));
                int exercise = toInt(snapshot.getLong("exerciseKcal"));
                syncStatusManager.markSuccess();
                return new int[]{water, steps, exercise};
            }
            syncStatusManager.markSuccess();
        } catch (Exception e) {
            android.util.Log.e("AppRepository", "getDailyStats failed", e);
            syncStatusManager.markFailure(e);
        }
        return new int[]{0, 0, 0};
    }

    private int toInt(Long value) {
        return value != null ? value.intValue() : 0;
    }

    private String resolveTimezone(String timezoneId) {
        return timezoneId == null || timezoneId.trim().isEmpty()
                ? TimeZone.getDefault().getID()
                : timezoneId;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String normalizeForSearch(String value) {
        if (value == null) return "";
        return value.trim()
                .replace('İ', 'i')
                .replace('I', 'i')
                .replace('ı', 'i')
                .replace('Ç', 'c')
                .replace('ç', 'c')
                .replace('Ğ', 'g')
                .replace('ğ', 'g')
                .replace('Ö', 'o')
                .replace('ö', 'o')
                .replace('Ş', 's')
                .replace('ş', 's')
                .replace('Ü', 'u')
                .replace('ü', 'u')
                .toLowerCase(Locale.ROOT);
    }
}
