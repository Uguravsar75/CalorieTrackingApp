package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.MealEntry;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.DateUtils;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.GeminiFoodService;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";

    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;
    private static final int GALLERY_REQUEST_CODE = 103;

    private ImageView ivPhoto;
    private CircularProgressIndicator progressAnalyzing;
    private View cardResult;
    private TextInputEditText etFoodName;
    private TextInputEditText etCalories;
    private TextView tvProtein;
    private TextView tvCarb;
    private TextView tvFat;
    private TextView tvEstimateNote;
    private MaterialButton btnTakePhoto;
    private MaterialButton btnPickFromGallery;
    private MaterialButton btnConfirm;

    private Uri photoUri;
    private File photoFile;
    private Bitmap capturedBitmap;
    private int detectedProtein;
    private int detectedCarb;
    private int detectedFat;
    private boolean aiEstimatedResult;

    private final GeminiFoodService geminiService = new GeminiFoodService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ivPhoto             = findViewById(R.id.ivPhoto);
        progressAnalyzing   = findViewById(R.id.progressAnalyzing);
        cardResult          = findViewById(R.id.cardResult);
        etFoodName          = findViewById(R.id.etFoodName);
        etCalories          = findViewById(R.id.etCalories);
        btnTakePhoto        = findViewById(R.id.btnTakePhoto);
        btnPickFromGallery  = findViewById(R.id.btnPickFromGallery);
        btnConfirm          = findViewById(R.id.btnConfirm);

        // Makro text view'ları — layout'ta var, içlerini AI cevabına göre dolduracağız
        tvProtein = findViewById(R.id.tvProtein);
        tvCarb    = findViewById(R.id.tvCarb);
        tvFat     = findViewById(R.id.tvFat);
        tvEstimateNote = findViewById(R.id.tvEstimateNote);

        btnTakePhoto.setOnClickListener(v -> checkPermissionAndOpenCamera());
        btnPickFromGallery.setOnClickListener(v -> openGallery());
        btnConfirm.setOnClickListener(v -> confirmAndSave());
    }

    // ── Kamera ──────────────────────────────────────────────────────────────────

    private void checkPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE
            );
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

    private void openCamera() {
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Snackbar.make(btnTakePhoto,
                    getString(R.string.camera_file_error, e.getMessage()),
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        photoUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                photoFile
        );

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    // ── Galeri ──────────────────────────────────────────────────────────────────

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(
                Intent.createChooser(intent, getString(R.string.camera_pick_gallery)),
                GALLERY_REQUEST_CODE
        );
    }

    // ── Dosya yardımcısı ─────────────────────────────────────────────────────────

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("FOOD_" + timestamp, ".jpg", storageDir);
    }

    // ── onActivityResult ─────────────────────────────────────────────────────────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        if (requestCode == CAMERA_REQUEST_CODE) {
            handleCameraResult();
        } else if (requestCode == GALLERY_REQUEST_CODE) {
            handleGalleryResult(data);
        }
    }

    private void handleCameraResult() {
        if (photoFile == null || !photoFile.exists()) {
            Snackbar.make(btnTakePhoto, R.string.camera_capture_failed, Snackbar.LENGTH_SHORT).show();
            return;
        }

        Bitmap bmp = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        if (bmp == null) {
            Snackbar.make(btnTakePhoto, R.string.camera_photo_read_failed, Snackbar.LENGTH_SHORT).show();
            return;
        }

        capturedBitmap = bmp;
        showPreviewAndAnalyze(capturedBitmap);
    }

    private void handleGalleryResult(@Nullable Intent data) {
        Uri selectedUri = data != null ? data.getData() : null;
        if (selectedUri == null) {
            Snackbar.make(btnPickFromGallery, R.string.camera_gallery_pick_failed, Snackbar.LENGTH_SHORT).show();
            return;
        }

        try {
            Bitmap bmp = decodeBitmapFromUri(selectedUri);
            if (bmp == null) throw new IOException("Bitmap null");
            capturedBitmap = bmp;
            showPreviewAndAnalyze(capturedBitmap);
        } catch (IOException e) {
            Log.e(TAG, "Gallery bitmap decode failed", e);
            Snackbar.make(btnPickFromGallery, R.string.camera_photo_read_failed, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * API 28+ üzeri deprecate olan getBitmap yerine ImageDecoder kullanır.
     */
    private Bitmap decodeBitmapFromUri(Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
            return ImageDecoder.decodeBitmap(source, (decoder, info, src) -> {
                decoder.setMutableRequired(true);
                // Maksimum 2048px — büyük görüntülerde OOM riskini azaltır
                int maxPx = 2048;
                int w = info.getSize().getWidth();
                int h = info.getSize().getHeight();
                if (w > maxPx || h > maxPx) {
                    float scale = (float) maxPx / Math.max(w, h);
                    decoder.setTargetSize(Math.round(w * scale), Math.round(h * scale));
                }
            });
        } else {
            // API < 28 için InputStream ile decode
            try (InputStream is = getContentResolver().openInputStream(uri)) {
                if (is == null) return null;
                return BitmapFactory.decodeStream(is);
            }
        }
    }

    // ── Analiz akışı ─────────────────────────────────────────────────────────────

    private void showPreviewAndAnalyze(Bitmap bitmap) {
        ivPhoto.setImageBitmap(bitmap);
        startAnalysis(bitmap);
    }

    private void startAnalysis(Bitmap bitmap) {
        progressAnalyzing.setVisibility(View.VISIBLE);
        cardResult.setVisibility(View.GONE);
        btnConfirm.setVisibility(View.GONE);
        btnTakePhoto.setEnabled(false);
        btnPickFromGallery.setEnabled(false);

        geminiService.analyze(bitmap, new GeminiFoodService.AnalysisCallback() {
            @Override
            public void onSuccess(String foodName, int calories, int protein, int carb, int fat) {
                runOnUiThread(() -> showResult(foodName, calories, protein, carb, fat));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Gemini analysis failed: " + message);
                    progressAnalyzing.setVisibility(View.GONE);
                    btnTakePhoto.setEnabled(true);
                    btnPickFromGallery.setEnabled(true);
                    showManualFallback(message);
                });
            }
        });
    }

    private void showResult(String foodName, int calories, int protein, int carb, int fat) {
        progressAnalyzing.setVisibility(View.GONE);
        btnTakePhoto.setEnabled(true);
        btnPickFromGallery.setEnabled(true);

        etFoodName.setText(foodName);
        etCalories.setText(String.valueOf(calories));
        detectedProtein = Math.max(0, protein);
        detectedCarb = Math.max(0, carb);
        detectedFat = Math.max(0, fat);
        aiEstimatedResult = true;

        // Makro değerlerini güncelle
        if (tvProtein != null) tvProtein.setText(getString(R.string.camera_macro_protein_val, detectedProtein));
        if (tvCarb != null)    tvCarb.setText(getString(R.string.camera_macro_carb_val, detectedCarb));
        if (tvFat != null)     tvFat.setText(getString(R.string.camera_macro_fat_val, detectedFat));
        if (tvEstimateNote != null) tvEstimateNote.setText(R.string.camera_estimated_note);

        cardResult.setVisibility(View.VISIBLE);
        btnConfirm.setVisibility(View.VISIBLE);
    }

    private void showManualFallback(String errorCode) {
        etFoodName.setText("");
        etCalories.setText("");
        detectedProtein = 0;
        detectedCarb = 0;
        detectedFat = 0;
        aiEstimatedResult = false;

        if (tvProtein != null) tvProtein.setText(getString(R.string.camera_macro_protein_val, 0));
        if (tvCarb != null)    tvCarb.setText(getString(R.string.camera_macro_carb_val, 0));
        if (tvFat != null)     tvFat.setText(getString(R.string.camera_macro_fat_val, 0));
        if (tvEstimateNote != null) tvEstimateNote.setText(R.string.camera_manual_note);

        cardResult.setVisibility(View.VISIBLE);
        btnConfirm.setVisibility(View.VISIBLE);

        // Hatanın tam metnini dialog ile göster (tanımlama için)
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.camera_error_dialog_title)
                .setMessage(getString(R.string.camera_error_dialog_message_format, errorCode))
                .setPositiveButton(R.string.btn_ok, null)
                .show();

        int messageResId = R.string.camera_analysis_error_generic;
        if (GeminiFoodService.ERROR_RATE_LIMIT.equals(errorCode)) {
            messageResId = R.string.camera_analysis_rate_limited;
        } else if (GeminiFoodService.ERROR_API_KEY_MISSING.equals(errorCode)) {
            messageResId = R.string.camera_analysis_missing_key;
        } else if (errorCode != null && errorCode.startsWith(GeminiFoodService.ERROR_NETWORK)) {
            messageResId = R.string.camera_analysis_network_error;
        }

        Snackbar.make(btnTakePhoto, getString(messageResId), Snackbar.LENGTH_LONG).show();
    }

    // ── Fotoğraf kaydetme ─────────────────────────────────────────────────────────

    /**
     * Mevcut bitmap'i uygulamanın özel depolama alanına kaydeder.
     * @return "file://.../meal_images/xxx.jpg" veya null
     */
    private String saveBitmapToInternalStorage(Bitmap bitmap) {
        if (bitmap == null) return null;
        try {
            File dir = new File(getFilesDir(), "meal_images");
            if (!dir.exists()) dir.mkdirs();

            String filename = "meal_" + System.currentTimeMillis() + ".jpg";
            File file = new File(dir, filename);

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                // Gösterim için yeterli kalite; dosya boyutunu küçük tut
                Bitmap scaled = scaleBitmapForStorage(bitmap, 512);
                scaled.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            }
            return "file://" + file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Photo save failed", e);
            return null;
        }
    }

    private Bitmap scaleBitmapForStorage(Bitmap src, int maxPx) {
        int w = src.getWidth(), h = src.getHeight();
        if (w <= maxPx && h <= maxPx) return src;
        float scale = (float) maxPx / Math.max(w, h);
        return Bitmap.createScaledBitmap(src, Math.round(w * scale), Math.round(h * scale), true);
    }

    // ── Kaydet ───────────────────────────────────────────────────────────────────

    private void confirmAndSave() {
        String foodName = etFoodName.getText() != null
                ? etFoodName.getText().toString().trim() : "";
        String caloriesStr = etCalories.getText() != null
                ? etCalories.getText().toString().trim() : "";

        if (TextUtils.isEmpty(foodName)) {
            etFoodName.setError(getString(R.string.camera_food_name_required));
            return;
        }

        int calories = 0;
        try {
            calories = Integer.parseInt(caloriesStr);
        } catch (NumberFormatException ignored) {}

        final int finalCalories = calories;
        final String finalFoodName = foodName;

        // Fotoğrafı kaydet (UI thread'de bitmap alınabilir)
        Bitmap currentBitmap = null;
        if (ivPhoto.getDrawable() != null) {
            try {
                currentBitmap = ((android.graphics.drawable.BitmapDrawable) ivPhoto.getDrawable()).getBitmap();
            } catch (Exception ignored) {}
        }
        final String savedImageUrl = saveBitmapToInternalStorage(currentBitmap);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                AppRepository repo = new AppRepository(getApplicationContext());
                MealEntry entry = new MealEntry();
                entry.setFoodId(0);
                entry.setUserEmail(repo.getCurrentUserEmail());
                entry.setFoodName(finalFoodName);
                entry.setDate(DateUtils.getToday());
                entry.setMealType(DateUtils.getSuggestedMealType());
                entry.setAmountGrams(0);
                entry.setTotalCalories(finalCalories);
                entry.setCreatedAt(System.currentTimeMillis());
                entry.setImageUrl(savedImageUrl);   // fotoğraf yolu
                entry.setProteinGrams(detectedProtein);
                entry.setCarbGrams(detectedCarb);
                entry.setFatGrams(detectedFat);
                entry.setAiEstimated(aiEstimatedResult);
                repo.insertMealEntry(entry);

                runOnUiThread(() -> {
                    Snackbar.make(btnConfirm, R.string.meal_added_success, Snackbar.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to save meal entry", e);
                runOnUiThread(() ->
                        Snackbar.make(btnConfirm,
                                getString(R.string.camera_analysis_error_generic),
                                Snackbar.LENGTH_LONG).show()
                );
            }
        });
    }
}
