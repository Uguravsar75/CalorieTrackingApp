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
    private String diseaseType;  // "NONE", "DIABETES", "HYPERTENSION", "HEART"
    private int dailyCalorieGoal;

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

    public String getDiseaseType() { return diseaseType; }
    public void setDiseaseType(String diseaseType) { this.diseaseType = diseaseType; }

    public int getDailyCalorieGoal() { return dailyCalorieGoal; }
    public void setDailyCalorieGoal(int dailyCalorieGoal) { this.dailyCalorieGoal = dailyCalorieGoal; }
}
