package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.SessionManager;

/**
 * SettingsActivity: Kullanıcı profil özeti, tema seçimi (Açık/Koyu/Sistem)
 * ve dil seçimi (Türkçe/İngilizce) bu ekrandan yapılır.
 * Tercihler SharedPreferences'a kaydedilir.
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "app_prefs";
    public static final String KEY_THEME = "theme_mode";
    public static final String KEY_LANG = "language";

    private RadioGroup rgTheme, rgLanguage;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        rgTheme = findViewById(R.id.rgTheme);
        rgLanguage = findViewById(R.id.rgLanguage);

        loadProfileSummary();
        loadCurrentSelections();
        setupListeners();

        MaterialButton btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.btn_logout, (dialog, which) -> {
                    new SessionManager(this).clearSession();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void loadProfileSummary() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppRepository repo = new AppRepository(getApplicationContext());
            User user = repo.getUser();
            if (user == null) return;

            String genderLabel = "MALE".equals(user.getGender())
                    ? getString(R.string.gender_male)
                    : getString(R.string.gender_female);

            String details = user.getAge() + " " + getString(R.string.hint_age).toLowerCase()
                    + " • " + (int) user.getHeight() + " cm"
                    + " • " + (int) user.getWeight() + " kg"
                    + " • " + genderLabel;

            runOnUiThread(() -> {
                TextView tvName = findViewById(R.id.tvProfileName);
                TextView tvDetails = findViewById(R.id.tvProfileDetails);
                tvName.setText(user.getName());
                tvDetails.setText(details);
            });
        });
    }

    private void loadCurrentSelections() {
        // Tema
        int savedTheme = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (savedTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            rgTheme.check(R.id.rbThemeLight);
        } else if (savedTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            rgTheme.check(R.id.rbThemeDark);
        } else {
            rgTheme.check(R.id.rbThemeSystem);
        }

        // Dil
        String savedLang = prefs.getString(KEY_LANG, "tr");
        if ("en".equals(savedLang)) {
            rgLanguage.check(R.id.rbLangEn);
        } else {
            rgLanguage.check(R.id.rbLangTr);
        }
    }

    private void setupListeners() {
        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int mode;
            if (checkedId == R.id.rbThemeLight) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.rbThemeDark) {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
            prefs.edit().putInt(KEY_THEME, mode).apply();
            AppCompatDelegate.setDefaultNightMode(mode);
        });

        rgLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String lang = (checkedId == R.id.rbLangEn) ? "en" : "tr";
            prefs.edit().putString(KEY_LANG, lang).apply();
            // API 33+ per-app locale — uygulama dili anında değişir, Activity yeniden başlar
            LocaleListCompat localeList = LocaleListCompat.forLanguageTags(lang);
            AppCompatDelegate.setApplicationLocales(localeList);
        });
    }
}
