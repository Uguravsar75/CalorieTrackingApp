package tr.edu.duzce.mf.bm.calorietrackingapp.database.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = "email", unique = true)})
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String email;        // Giriş için benzersiz email
    private String passwordHash; // SHA-256 ile hashlenmiş şifre

    private String name;
    private int age;
    private String gender;       // "MALE" veya "FEMALE"
    private float height;        // cm
    private float weight;        // kg
    private String activityLevel;
    private String diseaseType;  // "NONE", "DIABETES", "HYPERTENSION", "HEART"
    private boolean hasDiabetes;
    private boolean hasHypertension;
    private boolean hasCholesterol;
    private String otherDiseases;
    private int dailyCalorieGoal;
    private String authProvider;
    private String timezoneId;
    private long lastSyncedAt;

    public User() {}

    // --- Getter ve Setter ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public String getDiseaseType() { return diseaseType; }
    public void setDiseaseType(String diseaseType) { this.diseaseType = diseaseType; }

    public boolean isHasDiabetes() { return hasDiabetes; }
    public void setHasDiabetes(boolean hasDiabetes) { this.hasDiabetes = hasDiabetes; }

    public boolean isHasHypertension() { return hasHypertension; }
    public void setHasHypertension(boolean hasHypertension) { this.hasHypertension = hasHypertension; }

    public boolean isHasCholesterol() { return hasCholesterol; }
    public void setHasCholesterol(boolean hasCholesterol) { this.hasCholesterol = hasCholesterol; }

    public String getOtherDiseases() { return otherDiseases; }
    public void setOtherDiseases(String otherDiseases) { this.otherDiseases = otherDiseases; }

    public int getDailyCalorieGoal() { return dailyCalorieGoal; }
    public void setDailyCalorieGoal(int dailyCalorieGoal) { this.dailyCalorieGoal = dailyCalorieGoal; }

    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

    public String getTimezoneId() { return timezoneId; }
    public void setTimezoneId(String timezoneId) { this.timezoneId = timezoneId; }

    public long getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(long lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
}
