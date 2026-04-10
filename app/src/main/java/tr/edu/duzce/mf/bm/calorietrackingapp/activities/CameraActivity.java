package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.MealEntry;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.DateUtils;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.GeminiFoodService;

/**
 * CameraActivity: Kamera ile fotoğraf çeker, Gemini Vision API ile
 * yemeği tanır, kalori tahminini düzenlenebilir alanlara yazar ve
 * onaylanınca bugünün öğün listesine kaydeder.
 */
public class CameraActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int CAMERA_REQUEST_CODE    = 102;

    private ImageView ivPhoto;
    private CircularProgressIndicator progressAnalyzing;
    private View cardResult;
    private TextInputEditText etFoodName, etCalories;
    private MaterialButton btnTakePhoto, btnConfirm;

    private Uri photoUri;           // FileProvider URI
    private File photoFile;         // gerçek dosya yolu
    private Bitmap capturedBitmap;  // onayda kayıt için

    private final GeminiFoodService geminiService = new GeminiFoodService();

    // ──────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ivPhoto           = findViewById(R.id.ivPhoto);
        progressAnalyzing = findViewById(R.id.progressAnalyzing);
        cardResult        = findViewById(R.id.cardResult);
        etFoodName        = findViewById(R.id.etFoodName);
        etCalories        = findViewById(R.id.etCalories);
        btnTakePhoto      = findViewById(R.id.btnTakePhoto);
        btnConfirm        = findViewById(R.id.btnConfirm);

        btnTakePhoto.setOnClickListener(v -> checkPermissionAndOpenCamera());
        btnConfirm.setOnClickListener(v -> confirmAndSave());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // İzin kontrolü
    // ──────────────────────────────────────────────────────────────────────────

    private void checkPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Snackbar.make(btnTakePhoto, R.string.camera_permission_denied, Snackbar.LENGTH_SHORT).show();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Kamera açma
    // ──────────────────────────────────────────────────────────────────────────

    private void openCamera() {
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Snackbar.make(btnTakePhoto, "Dosya oluşturulamadı: " + e.getMessage(),
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        photoUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                photoFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    /** Dış depolama Pictures klasöründe zaman damgalı jpg dosyası oluşturur. */
    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("FOOD_" + timestamp, ".jpg", storageDir);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Kamera sonucu
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != CAMERA_REQUEST_CODE || resultCode != RESULT_OK) return;
        if (photoFile == null || !photoFile.exists()) {
            Snackbar.make(btnTakePhoto, "Fotoğraf alınamadı.", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Dosyadan bitmap yükle ve önizlemede göster
        capturedBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        if (capturedBitmap == null) {
            Snackbar.make(btnTakePhoto, "Fotoğraf okunamadı.", Snackbar.LENGTH_SHORT).show();
            return;
        }
        ivPhoto.setImageBitmap(capturedBitmap);

        // Gemini analizi başlat
        startAnalysis(capturedBitmap);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Gemini analizi
    // ──────────────────────────────────────────────────────────────────────────

    private void startAnalysis(Bitmap bitmap) {
        progressAnalyzing.setVisibility(View.VISIBLE);
        cardResult.setVisibility(View.GONE);
        btnConfirm.setVisibility(View.GONE);
        btnTakePhoto.setEnabled(false);

        geminiService.analyze(bitmap, new GeminiFoodService.AnalysisCallback() {
            @Override
            public void onSuccess(String foodName, int calories) {
                runOnUiThread(() -> showResult(foodName, calories));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressAnalyzing.setVisibility(View.GONE);
                    btnTakePhoto.setEnabled(true);
                    Snackbar.make(btnTakePhoto, "Analiz hatası: " + message,
                            Snackbar.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showResult(String foodName, int calories) {
        progressAnalyzing.setVisibility(View.GONE);
        btnTakePhoto.setEnabled(true);

        etFoodName.setText(foodName);
        etCalories.setText(String.valueOf(calories));

        cardResult.setVisibility(View.VISIBLE);
        btnConfirm.setVisibility(View.VISIBLE);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Kaydet
    // ──────────────────────────────────────────────────────────────────────────

    private void confirmAndSave() {
        String foodName = etFoodName.getText() != null
                ? etFoodName.getText().toString().trim() : "";
        String caloriesStr = etCalories.getText() != null
                ? etCalories.getText().toString().trim() : "";

        if (TextUtils.isEmpty(foodName)) {
            etFoodName.setError("Yemek adı gerekli");
            return;
        }

        int calories = 0;
        try {
            calories = Integer.parseInt(caloriesStr);
        } catch (NumberFormatException ignored) {}

        final int finalCalories = calories;
        final String finalFoodName = foodName;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppRepository repo = new AppRepository(getApplicationContext());
            MealEntry entry = new MealEntry();
            entry.setFoodId(0);                              // manuel / AI tahmini
            entry.setFoodName(finalFoodName + " 📷");
            entry.setDate(DateUtils.getToday());
            entry.setMealType(DateUtils.getSuggestedMealType());
            entry.setAmountGrams(0);                         // porsiyon bazlı, gram bilinmiyor
            entry.setTotalCalories(finalCalories);
            repo.insertMealEntry(entry);

            runOnUiThread(() -> {
                Snackbar.make(btnConfirm, R.string.meal_added_success, Snackbar.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
