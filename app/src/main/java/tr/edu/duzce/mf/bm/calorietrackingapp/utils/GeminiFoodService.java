package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tr.edu.duzce.mf.bm.calorietrackingapp.BuildConfig;

public class GeminiFoodService {

    public static final String ERROR_RATE_LIMIT     = "ERROR_RATE_LIMIT";
    public static final String ERROR_API_KEY_MISSING = "ERROR_API_KEY_MISSING";
    public static final String ERROR_NETWORK        = "ERROR_NETWORK";
    public static final String ERROR_PARSE          = "ERROR_PARSE";
    public static final String ERROR_API            = "ERROR_API";

    private static final String TAG = "GeminiFoodService";
    // gemini-2.0-flash: bu hesapta 0 kota → 429 hatası
    // gemini-2.5-flash: ücretsiz kota var (5 RPM, 20 RPD)
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";



    /**
     * Prompt'ta responseMimeType kullanmıyoruz — bazı bölgelerde / modellerde
     * bu mod etkinleştirilmemiş ve boş parts dönüyor. Serbest metin alıp
     * regex ile JSON ayıklıyoruz.
     */
    private static final String PROMPT =
            "Fotograftaki yemegi tani. Asagidaki JSON formatinda SADECE JSON don, "
            + "baska hicbir sey yazma:\n"
            + "{\"foodName\":\"Turkce yemek adi\","
            + "\"calories\":350,"
            + "\"protein\":18,"
            + "\"carb\":34,"
            + "\"fat\":12}\n"
            + "Degerler 1 porsiyon icin tahmini olsun (tam sayi). "
            + "Yemek taninamazsa: "
            + "{\"foodName\":\"Bilinmeyen yemek\",\"calories\":0,\"protein\":0,\"carb\":0,\"fat\":0}";

    public interface AnalysisCallback {
        void onSuccess(String foodName, int calories, int protein, int carb, int fat);
        void onError(String message);
    }

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)
            .build();

    public void analyze(Bitmap bitmap, AnalysisCallback callback) {
        String apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            callback.onError(ERROR_API_KEY_MISSING);
            return;
        }

        // Bitmap → Base64
        String base64Image;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap scaled = scaleBitmap(bitmap, 768);           // 768px — daha iyi tanıma
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            base64Image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Image processing failed", e);
            callback.onError(ERROR_PARSE + ": " + e.getMessage());
            return;
        }

        // İstek JSON
        String requestJson;
        try {
            JSONObject imageData = new JSONObject()
                    .put("mime_type", "image/jpeg")
                    .put("data", base64Image);

            JSONArray parts = new JSONArray()
                    .put(new JSONObject().put("text", PROMPT))
                    .put(new JSONObject().put("inline_data", imageData));

            // responseMimeType: gemini-2.5-flash bu modu destekliyor
            // modeli kesinlikle JSON döndürmeye zorluyoruz
            JSONObject generationConfig = new JSONObject()
                    .put("temperature", 0.1)
                    .put("maxOutputTokens", 512)
                    .put("responseMimeType", "application/json");

            requestJson = new JSONObject()
                    .put("contents", new JSONArray()
                            .put(new JSONObject().put("parts", parts)))
                    .put("generationConfig", generationConfig)
                    .toString();
        } catch (Exception e) {
            Log.e(TAG, "Request build failed", e);
            callback.onError(ERROR_PARSE + ": " + e.getMessage());
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
                Log.e(TAG, "Network failure", e);
                callback.onError(ERROR_NETWORK + ": " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (response) {
                    String body = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "HTTP " + response.code());
                    Log.d(TAG, "Response body: " + body);

                    if (!response.isSuccessful()) {
                        Log.e(TAG, "API error " + response.code() + " body=" + body);
                        if (response.code() == 429) {
                            callback.onError(ERROR_RATE_LIMIT);
                        } else {
                            callback.onError(ERROR_API + "_" + response.code() + ": " + body);
                        }
                        return;
                    }
                    parseAndDeliver(body, callback);
                }
            }
        });
    }

    // ── Parse ─────────────────────────────────────────────────────────────────

    private void parseAndDeliver(String responseBody, AnalysisCallback callback) {
        try {
            JSONObject root = new JSONObject(responseBody);

            // API seviyesi hata nesnesi
            if (root.has("error")) {
                JSONObject err = root.getJSONObject("error");
                int code = err.optInt("code", 0);
                String msg = err.optString("message", "unknown");
                Log.e(TAG, "API error object: code=" + code + " msg=" + msg);
                if (code == 429) {
                    callback.onError(ERROR_RATE_LIMIT);
                } else {
                    callback.onError(ERROR_API + "_" + code + ": " + msg);
                }
                return;
            }

            JSONArray candidates = root.optJSONArray("candidates");
            if (candidates == null || candidates.length() == 0) {
                String feedback = root.optString("promptFeedback", "");
                Log.e(TAG, "No candidates. promptFeedback=" + feedback + " full=" + responseBody);
                callback.onError(ERROR_PARSE + ": no candidates");
                return;
            }

            JSONObject firstCandidate = candidates.getJSONObject(0);
            String finishReason = firstCandidate.optString("finishReason", "STOP");
            Log.d(TAG, "finishReason=" + finishReason);

            // SAFETY veya RECITATION: içerik engellendi → bilinmeyen yemek dön
            if ("SAFETY".equals(finishReason) || "RECITATION".equals(finishReason)) {
                Log.w(TAG, "Content blocked: " + finishReason);
                callback.onSuccess("Bilinmeyen yemek", 0, 0, 0, 0);
                return;
            }

            JSONObject content = firstCandidate.optJSONObject("content");
            JSONArray parts = content != null ? content.optJSONArray("parts") : null;

            if (parts == null || parts.length() == 0) {
                Log.e(TAG, "Empty content parts. candidate=" + firstCandidate);
                callback.onError(ERROR_PARSE + ": empty content parts");
                return;
            }

            String text = null;
            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.getJSONObject(i);
                if (part.has("text")) {
                    text = part.getString("text");
                    break;
                }
            }

            if (text == null || text.trim().isEmpty()) {
                Log.e(TAG, "Empty text in response parts");
                callback.onError(ERROR_PARSE + ": empty text");
                return;
            }

            Log.d(TAG, "Raw Gemini text: " + text);

            String json = extractJson(text);
            if (json == null) {
                // Ham yanıtın ilk 300 karakterini hataya ekle
                String preview = text.length() > 300 ? text.substring(0, 300) + "..." : text;
                Log.e(TAG, "Could not extract JSON from: " + text);
                callback.onError(ERROR_PARSE + ": no JSON.\nModel yanıtı:\n" + preview);
                return;
            }

            JSONObject result = new JSONObject(json);
            String foodName = result.optString("foodName", "Bilinmeyen yemek");
            int calories    = parseIntSafe(result, "calories");
            int protein     = parseIntSafe(result, "protein");
            int carb        = parseIntSafe(result, "carb");
            int fat         = parseIntSafe(result, "fat");

            Log.d(TAG, "Parsed → foodName=" + foodName
                    + " cal=" + calories
                    + " pro=" + protein
                    + " carb=" + carb
                    + " fat=" + fat);

            callback.onSuccess(foodName,
                    Math.max(calories, 0),
                    Math.max(protein, 0),
                    Math.max(carb, 0),
                    Math.max(fat, 0));

        } catch (Exception e) {
            Log.e(TAG, "Parse failed. body=" + responseBody, e);
            callback.onError(ERROR_PARSE + ": " + e.getMessage());
        }
    }

    // ── Yardımcılar ──────────────────────────────────────────────────────────

    /**
     * Metin içindeki ilk geçerli JSON nesnesini çıkarır.
     * Gemini bazen markdown (```json ... ```) veya açıklama metni ekleyebilir.
     */
    private String extractJson(String text) {
        if (text == null) return null;

        // Markdown kod bloğu temizle
        String cleaned = text
                .replace("```json", "")
                .replace("```JSON", "")
                .replace("```", "")
                .trim();

        // foodName içeren JSON bloğunu bul
        Pattern specific = Pattern.compile("\\{[^{}]*\"foodName\"[^{}]*\\}");
        Matcher m = specific.matcher(cleaned);
        if (m.find()) return m.group();

        // Genel JSON nesne dene
        Pattern general = Pattern.compile("\\{[^{}]+\\}");
        Matcher mg = general.matcher(cleaned);
        while (mg.find()) {
            String candidate = mg.group();
            try {
                JSONObject obj = new JSONObject(candidate);
                if (obj.has("foodName") || obj.has("calories")) return candidate;
            } catch (Exception ignored) {}
        }

        // Tüm metin doğrudan JSON mu?
        try {
            new JSONObject(cleaned);
            return cleaned;
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * JSON alanı int veya string olarak gelmiş olabilir; her ikisini de handle eder.
     */
    private int parseIntSafe(JSONObject obj, String key) {
        if (!obj.has(key)) return 0;
        try {
            return obj.getInt(key);
        } catch (Exception e) {
            try {
                String val = obj.optString(key, "0").replaceAll("[^0-9]", "");
                return val.isEmpty() ? 0 : Integer.parseInt(val);
            } catch (Exception ignored) {
                return 0;
            }
        }
    }

    private Bitmap scaleBitmap(Bitmap src, int maxSize) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w <= maxSize && h <= maxSize) return src;
        float scale = (float) maxSize / Math.max(w, h);
        return Bitmap.createScaledBitmap(src,
                Math.round(w * scale),
                Math.round(h * scale),
                true);
    }
}
