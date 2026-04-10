package tr.edu.duzce.mf.bm.calorietrackingapp.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;

@Dao
public interface UserDao {

    @Insert
    void insert(User user);

    @Update
    void update(User user);

    // Tek kullanıcı: oturumu açık olan kullanıcıyı döner
    @Query("SELECT * FROM users LIMIT 1")
    User getUser();

    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();

    // Email ile kullanıcı bul (giriş doğrulaması için)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    // Email + passwordHash eşleşmesi (login kontrolü)
    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    User login(String email, String passwordHash);
}
