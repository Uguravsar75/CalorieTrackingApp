package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.HashUtils;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.SessionManager;

/**
 * RegisterActivity: Yeni kullanıcı kaydı.
 * Email benzersizliği kontrol edilir, şifre SHA-256 ile hashlenerek saklanır.
 * Kayıt başarılı olursa ProfileSetupActivity'e yönlendirilir.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword, tilPasswordConfirm;
    private TextInputEditText etEmail, etPassword, etPasswordConfirm;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(this);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilPasswordConfirm = findViewById(R.id.tilPasswordConfirm);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);

        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvGoLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilPasswordConfirm.setError(null);

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String passwordConfirm = etPasswordConfirm.getText() != null ? etPasswordConfirm.getText().toString() : "";

        boolean valid = true;

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_short));
            valid = false;
        }

        if (!password.equals(passwordConfirm)) {
            tilPasswordConfirm.setError(getString(R.string.error_passwords_not_match));
            valid = false;
        }

        if (!valid) return;

        String finalEmail = email;
        String passwordHash = HashUtils.sha256(password);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppRepository repo = new AppRepository(getApplicationContext());
            boolean emailTaken = repo.isEmailTaken(finalEmail);

            if (emailTaken) {
                runOnUiThread(() -> tilEmail.setError(getString(R.string.error_email_taken)));
            } else {
                // Email'i kullanıcıya kaydet (şifre hash ile) — arka plan thread'inde yapılmalı
                repo.saveEmailAndPassword(finalEmail, passwordHash);
                runOnUiThread(() -> {
                    sessionManager.saveSession(finalEmail);
                    // Profil kurulumuna yönlendir
                    Intent intent = new Intent(RegisterActivity.this, ProfileSetupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }
}
