package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.graphics.Bitmap;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tr.edu.duzce.mf.bm.calorietrackingapp.BuildConfig;

/**
 * GeminiFoodService: Bir Bitmap'i Gemini 2.0 Flash Vision API'ye gönderir.
 * Yemeğin adını ve tahmini kalorisini (1 porsiyon) döner.
 */
public class GeminiFoodService {

    public interface AnalysisCallback {
        /** API başarıyla yanıt döndürdüğünde çağrılır. */
        void onSuccess(String foodName, int calories);
        /** Ağ hatası veya parse hatası oluştuğunda çağrılır. */
        void onError(String message);
    }

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private static final String PROMPT =
            "Bu fotoğraftaki yemeği tanımla. " +
            "Cevap olarak SADECE aşağıdaki JSON formatını döndür, başka hiçbir şey yazma:\n" +
            "{\"foodName\": \"yemek adı Türkçe\", \"calories\": 350}\n" +
            "calories değeri 1 porsiyon (yaklaşık 1 tabak veya standart servis) için tahmini kcal olmalı. " +
            "Eğer yemek tanınamıyorsa: {\"foodName\": \"Bilinmeyen yemek\", \"calories\": 0}";

    private final OkHttpClient client = new OkHttpClient();

    /**
     * Verilen bitmap'i Gemini API'ye gönderir, sonucu callback'e iletir.
     * Bu metod arka plan thread'inde çağrılmalıdır (OkHttp zaten async yapar).
     */
    public void analyze(Bitmap bitmap, AnalysisCallback callback) {
        String apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("Gemini API anahtarı eksik. local.properties dosyasına GEMINI_API_KEY ekleyin.");
            return;
        }

        // Bitmap → JPEG → Base64
        String base64Image;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Görüntüyü yeniden boyutlandır (API limiti için)
            Bitmap scaled = scaleBitmap(bitmap, 768);
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            base64Image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
        } catch (Exception e) {
            callback.onError("Görüntü işleme hatası: " + e.getMessage());
            return;
        }

        // Gemini API isteği oluştur
        String requestJson;
        try {
            JSONObject imageData = new JSONObject()
                    .put("mime_type", "image/jpeg")
                    .put("data", base64Image);

            JSONObject imagePart = new JSONObject().put("inline_data", imageData);
            JSONObject textPart = new JSONObject().put("text", PROMPT);

            JSONArray parts = new JSONArray().put(imagePart).put(textPart);
            JSONObject content = new JSONObject().put("parts", parts);
            JSONArray contents = new JSONArray().put(content);

            requestJson = new JSONObject().put("contents", contents).toString();
        } catch (Exception e) {
            callback.onError("İstek oluşturma hatası: " + e.getMessage());
            return;
        }

        String url = ENDPOINT + "?key=" + apiKey;
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestJson,
                        MediaType.parse("application/json; charset=utf-8")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Ağ hatası: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (response) {
                    if (!response.isSuccessful()) {
                        callback.onError("API hatası: HTTP " + response.code());
                        return;
                    }
                    String body = response.body() != null ? response.body().string() : "";
                    parseAndDeliver(body, callback);
                }
            }
        });
    }

    /** Gemini yanıtından foodName ve calories JSON'ını parse eder. */
    private void parseAndDeliver(String responseBody, AnalysisCallback callback) {
        try {
            JSONObject root = new JSONObject(responseBody);
            String text = root
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim();

            // Gemini bazen ```json ... ``` bloğu içinde gönderir, temizle
            if (text.startsWith("```")) {
                text = text.replaceAll("```json", "").replaceAll("```", "").trim();
            }

            JSONObject result = new JSONObject(text);
            String foodName = result.optString("foodName", "Bilinmeyen yemek");
            int calories = result.optInt("calories", 0);
            callback.onSuccess(foodName, calories);
        } catch (Exception e) {
            callback.onError("Yanıt parse hatası: " + e.getMessage());
        }
    }

    /** En uzun kenarı maxSize'a orantılı küçültür, boşa hafıza harcamaz. */
    private Bitmap scaleBitmap(Bitmap src, int maxSize) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w <= maxSize && h <= maxSize) return src;
        float scale = (float) maxSize / Math.max(w, h);
        return Bitmap.createScaledBitmap(src, (int)(w * scale), (int)(h * scale), true);
    }
}
