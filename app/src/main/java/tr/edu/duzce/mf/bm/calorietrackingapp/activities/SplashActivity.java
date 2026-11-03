package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.FirebaseConnectivityChecker;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.NotificationHelper;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.ReminderScheduler;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyPreferences();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View rootView = getWindow().getDecorView().getRootView();
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        rootView.startAnimation(fadeIn);

        NotificationHelper.createNotificationChannel(this);
        scheduleReminderWorker();
        FirebaseConnectivityChecker.verifyFirestoreConnection();

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            sessionManager.clearSession();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                AppRepository repository = new AppRepository(getApplicationContext());
                boolean profileComplete = repository.isProfileComplete();

                runOnUiThread(() -> {
                    Intent intent = profileComplete
                            ? new Intent(SplashActivity.this, MainActivity.class)
                            : new Intent(SplashActivity.this, ProfileSetupActivity.class);
                    startActivity(intent);
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    sessionManager.clearSession();
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                });
            }
        });
    }

    private void applyPreferences() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);

        int themeMode = prefs.getInt(SettingsActivity.KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        String lang = prefs.getString(SettingsActivity.KEY_LANG, "tr");
        LocaleListCompat localeList = LocaleListCompat.forLanguageTags(lang);
        AppCompatDelegate.setApplicationLocales(localeList);
    }

    private void scheduleReminderWorker() {
        ReminderScheduler.scheduleAll(this);
    }
}
