package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private TextInputLayout tilEmail, tilPassword, tilPasswordConfirm;
    private TextInputEditText etEmail, etPassword, etPasswordConfirm;
    private MaterialButton btnRegister, btnGoogleRegister;
    private SessionManager sessionManager;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
        tilPasswordConfirm = findViewById(R.id.tilPasswordConfirm);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);

        btnRegister = findViewById(R.id.btnRegister);
        btnGoogleRegister = findViewById(R.id.btnGoogleRegister);
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> attemptRegister());
        btnGoogleRegister.setOnClickListener(v -> startGoogleSignIn());
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

        if (!valid) {
            return;
        }

        btnRegister.setEnabled(false);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                AppRepository repo = new AppRepository(getApplicationContext());
                repo.saveEmailAndPassword(email, password);

                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    sessionManager.saveSession(email);
                    Intent intent = new Intent(RegisterActivity.this, ProfileSetupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            } catch (RuntimeException exception) {
                Log.e(TAG, "Registration failed", exception);
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    String message = readableMessage(exception);
                    if (message.toLowerCase().contains("email address is already in use")) {
                        tilEmail.setError(getString(R.string.error_email_taken));
                    } else {
                        Snackbar.make(btnRegister,
                                getString(R.string.error_registration_failed_format, message),
                                Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private String readableMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.trim().isEmpty() ? getString(R.string.error_unknown) : message;
    }

    private void startGoogleSignIn() {
        if (!GoogleSignInHelper.isConfigured(this)) {
            Snackbar.make(btnGoogleRegister, R.string.error_google_config_missing, Snackbar.LENGTH_LONG).show();
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
                Snackbar.make(btnGoogleRegister, R.string.error_google_config_missing, Snackbar.LENGTH_LONG).show();
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
                                    ? new Intent(RegisterActivity.this, MainActivity.class)
                                    : new Intent(RegisterActivity.this, ProfileSetupActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Snackbar.make(btnGoogleRegister, R.string.error_google_login_failed, Snackbar.LENGTH_LONG).show();
                        }
                    });
                } catch (RuntimeException exception) {
                    Log.e(TAG, "Google registration failed", exception);
                    runOnUiThread(() -> {
                        setAuthButtonsEnabled(true);
                        Snackbar.make(btnGoogleRegister, R.string.error_google_login_failed, Snackbar.LENGTH_LONG).show();
                    });
                }
            });
        } catch (Exception exception) {
            setAuthButtonsEnabled(true);
            Snackbar.make(btnGoogleRegister, R.string.error_google_login_failed, Snackbar.LENGTH_LONG).show();
        }
    }

    private void setAuthButtonsEnabled(boolean enabled) {
        btnRegister.setEnabled(enabled);
        btnGoogleRegister.setEnabled(enabled);
    }
}
