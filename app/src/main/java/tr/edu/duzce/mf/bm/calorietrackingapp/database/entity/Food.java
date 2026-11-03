package tr.edu.duzce.mf.bm.calorietrackingapp.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "foods")
public class Food {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String nameTr;          // Türkçe adı
    private String nameEn;          // İngilizce adı
    private int caloriesPer100g;    // 100 gram başına kalori
    private String category;        // "BREAKFAST", "LUNCH", "DINNER", "SNACK"
    private boolean isPredefined;   // Ön tanımlı gıda mı
    private boolean sugarWarning;   // Diyabet uyarısı gerekli mi (şekerli)

    private boolean saltWarning;
    private boolean fatWarning;

    public Food() {}

    // --- Getter ve Setter ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNameTr() { return nameTr; }
    public void setNameTr(String nameTr) { this.nameTr = nameTr; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public int getCaloriesPer100g() { return caloriesPer100g; }
    public void setCaloriesPer100g(int caloriesPer100g) { this.caloriesPer100g = caloriesPer100g; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isPredefined() { return isPredefined; }
    public void setPredefined(boolean predefined) { isPredefined = predefined; }

    public boolean isSugarWarning() { return sugarWarning; }
    public void setSugarWarning(boolean sugarWarning) { this.sugarWarning = sugarWarning; }

    public boolean isSaltWarning() { return saltWarning; }
    public void setSaltWarning(boolean saltWarning) { this.saltWarning = saltWarning; }

    public boolean isFatWarning() { return fatWarning; }
    public void setFatWarning(boolean fatWarning) { this.fatWarning = fatWarning; }
}
