package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager: Kullanıcının oturum durumunu SharedPreferences ile yönetir.
 * Giriş yapıldığında oturum açılır, çıkışta temizlenir.
 */
public class SessionManager {

    private static final String PREFS_NAME = "session_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_EMAIL = "user_email";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String email) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_EMAIL, email)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getLoggedInEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
