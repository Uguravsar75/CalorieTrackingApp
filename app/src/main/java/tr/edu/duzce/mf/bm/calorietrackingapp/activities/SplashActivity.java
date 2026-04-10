package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.SessionManager;

/**
 * SplashActivity: Uygulamanın giriş noktasıdır.
 * Kayıtlı tema ve dil tercihlerini uygular.
 * Kullanıcı profili var mı kontrol eder:
 * - Varsa → MainActivity
 * - Yoksa → ProfileSetupActivity
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Tema ve dil tercihlerini Activity oluşmadan önce uygula
        applyPreferences();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SessionManager sessionManager = new SessionManager(this);

        // Oturum kontrolü: giriş yapılmamışsa LoginActivity'e yönlendir
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Giriş yapılmışsa profil tamamlanmış mı kontrol et
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppRepository repository = new AppRepository(getApplicationContext());
            boolean profileComplete = repository.isProfileComplete();

            runOnUiThread(() -> {
                Intent intent;
                if (profileComplete) {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, ProfileSetupActivity.class);
                }
                startActivity(intent);
                finish();
            });
        });
    }

    private void applyPreferences() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);

        // Tema uygula
        int themeMode = prefs.getInt(SettingsActivity.KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        // Dil uygula
        String lang = prefs.getString(SettingsActivity.KEY_LANG, "tr");
        LocaleListCompat localeList = LocaleListCompat.forLanguageTags(lang);
        AppCompatDelegate.setApplicationLocales(localeList);
    }
}
