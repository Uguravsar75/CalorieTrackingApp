package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.GoogleSignInHelper;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogleLogin;
    private SessionManager sessionManager;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleGoogleSignInResult(result.getData());
                    } else {
                        setAuthButtonsEnabled(true);
                    }
                }
        );

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnGoogleLogin.setOnClickListener(v -> startGoogleSignIn());
        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        boolean valid = true;

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_short));
            valid = false;
        }

        if (!valid) {
            return;
        }

        setAuthButtonsEnabled(false);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppRepository repo = new AppRepository(getApplicationContext());
            User user = repo.login(email, password);
            boolean profileComplete = user != null && repo.isProfileComplete();

            runOnUiThread(() -> {
                setAuthButtonsEnabled(true);
                if (user != null) {
                    sessionManager.saveSession(email);
                    Intent intent = profileComplete
                            ? new Intent(LoginActivity.this, MainActivity.class)
                            : new Intent(LoginActivity.this, ProfileSetupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    tilPassword.setError(getString(R.string.error_login_failed));
                }
            });
        });
    }

    private void startGoogleSignIn() {
        if (!GoogleSignInHelper.isConfigured(this)) {
            Snackbar.make(btnGoogleLogin, R.string.error_google_config_missing, Snackbar.LENGTH_LONG).show();
            return;
        }

        setAuthButtonsEnabled(false);
        googleSignInLauncher.launch(GoogleSignInHelper.createSignInIntent(this));
    }

    private void handleGoogleSignInResult(Intent data) {
        try {
            GoogleSignInAccount account = GoogleSignInHelper.getAccountFromResult(data);
            String idToken = account.getIdToken();
            if (idToken == null || idToken.trim().isEmpty()) {
                setAuthButtonsEnabled(true);
                Snackbar.make(btnGoogleLogin, R.string.error_google_config_missing, Snackbar.LENGTH_LONG).show();
                return;
            }

            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    AppRepository repo = new AppRepository(getApplicationContext());
                    User user = repo.loginWithGoogle(idToken);
                    boolean profileComplete = repo.isProfileComplete();
                    runOnUiThread(() -> {
                        setAuthButtonsEnabled(true);
                        if (user != null) {
                            sessionManager.saveSession(user.getEmail());
                            Intent intent = profileComplete
                                    ? new Intent(LoginActivity.this, MainActivity.class)
                                    : new Intent(LoginActivity.this, ProfileSetupActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Snackbar.make(btnGoogleLogin, R.string.error_google_login_failed, Snackbar.LENGTH_LONG).show();
                        }
                    });
                } catch (RuntimeException exception) {
                    runOnUiThread(() -> {
                        setAuthButtonsEnabled(true);
                        Snackbar.make(btnGoogleLogin, R.string.error_google_login_failed, Snackbar.LENGTH_LONG).show();
                    });
                }
            });
        } catch (Exception exception) {
            setAuthButtonsEnabled(true);
            Snackbar.make(btnGoogleLogin, R.string.error_google_login_failed, Snackbar.LENGTH_LONG).show();
        }
    }

    private void setAuthButtonsEnabled(boolean enabled) {
        btnLogin.setEnabled(enabled);
        btnGoogleLogin.setEnabled(enabled);
    }
}
