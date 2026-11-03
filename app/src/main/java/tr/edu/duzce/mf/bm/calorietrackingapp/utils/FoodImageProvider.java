package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * FoodImageProvider: Türk ve yaygın yemek adlarını fotoğraf URL'sine eşler.
 * Kaynak: Wikimedia Commons (CC lisanslı, kararlı URL'ler) + TheMealDB CDN.
 *
 * Kullanım: FoodImageProvider.getUrl("Tavuklu Pilav")
 * → URL döner (Glide ile yüklenebilir) veya null döner (placeholder kullanılır).
 */
public class FoodImageProvider {

    private static final Map<String, String> URL_MAP = new LinkedHashMap<>();

    static {
        // ── Türk yemekleri ─────────────────────────────────────────────────────
        put("tavuklu pilav",   "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/White_rice_plain.jpg/320px-White_rice_plain.jpg");
        put("bulgur pilavi",   "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5a/Bulgur_pilavı.jpg/320px-Bulgur_pilavı.jpg");
        put("pilav",           "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/White_rice_plain.jpg/320px-White_rice_plain.jpg");
        put("bulgur",          "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5a/Bulgur_pilavı.jpg/320px-Bulgur_pilavı.jpg");
        put("lahmacun",        "https://upload.wikimedia.org/wikipedia/commons/thumb/1/13/Lahmacun.jpg/320px-Lahmacun.jpg");
        put("doner kebab",     "https://upload.wikimedia.org/wikipedia/commons/thumb/1/18/Döner_kebab.jpg/320px-Döner_kebab.jpg");
        put("doner",           "https://upload.wikimedia.org/wikipedia/commons/thumb/1/18/Döner_kebab.jpg/320px-Döner_kebab.jpg");
        put("kebap",           "https://upload.wikimedia.org/wikipedia/commons/thumb/1/18/Döner_kebab.jpg/320px-Döner_kebab.jpg");
        put("izgara tavuk",    "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6d/Good_Food_Display_-_NCI_Visuals_Online.jpg/320px-Good_Food_Display_-_NCI_Visuals_Online.jpg");
        put("tavuk",           "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6d/Good_Food_Display_-_NCI_Visuals_Online.jpg/320px-Good_Food_Display_-_NCI_Visuals_Online.jpg");
        put("kofte",           "https://upload.wikimedia.org/wikipedia/commons/thumb/0/07/Köfte.jpg/320px-Köfte.jpg");
        put("mercimek corbasi","https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/Lentil_soup.jpg/320px-Lentil_soup.jpg");
        put("mercimek",        "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/Lentil_soup.jpg/320px-Lentil_soup.jpg");
        put("corba",           "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/Lentil_soup.jpg/320px-Lentil_soup.jpg");
        put("baklava",         "https://upload.wikimedia.org/wikipedia/commons/thumb/3/31/Baklava_-_Turkish_special%2C_80-ply.JPEG/320px-Baklava_-_Turkish_special%2C_80-ply.JPEG");
        put("acma",            "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Açma.jpg/320px-Açma.jpg");
        put("simit",           "https://upload.wikimedia.org/wikipedia/commons/thumb/3/37/Simit.jpg/320px-Simit.jpg");
        put("pide",            "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d2/Pide-3.jpg/320px-Pide-3.jpg");
        put("gozleme",         "https://upload.wikimedia.org/wikipedia/commons/thumb/2/29/Gözleme.jpg/320px-Gözleme.jpg");
        put("manti",           "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d0/Mantı.jpg/320px-Mantı.jpg");
        put("menemen",         "https://upload.wikimedia.org/wikipedia/commons/thumb/0/07/Menemen.jpg/320px-Menemen.jpg");
        put("borek",           "https://upload.wikimedia.org/wikipedia/commons/thumb/5/57/Börek.JPG/320px-Börek.JPG");
        put("dolma",           "https://upload.wikimedia.org/wikipedia/commons/thumb/4/49/Sarma.jpg/320px-Sarma.jpg");
        put("sarma",           "https://upload.wikimedia.org/wikipedia/commons/thumb/4/49/Sarma.jpg/320px-Sarma.jpg");
        put("yogurt",          "https://upload.wikimedia.org/wikipedia/commons/thumb/8/88/Strained_yogurt.jpg/320px-Strained_yogurt.jpg");
        put("bal",             "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/Runny_hunny.jpg/320px-Runny_hunny.jpg");
        put("peynir",          "https://upload.wikimedia.org/wikipedia/commons/thumb/1/18/Cheese_platter.jpg/320px-Cheese_platter.jpg");
        put("ekmek",           "https://upload.wikimedia.org/wikipedia/commons/thumb/3/33/Fresh_made_bread_05.jpg/320px-Fresh_made_bread_05.jpg");
        put("salata",          "https://upload.wikimedia.org/wikipedia/commons/thumb/8/88/Fresh_salad.jpg/320px-Fresh_salad.jpg");
        put("zeytinyagi",      "https://upload.wikimedia.org/wikipedia/commons/thumb/5/57/Olive-oil-362x272.jpg/320px-Olive-oil-362x272.jpg");

        // ── Kahvaltı ────────────────────────────────────────────────────────────
        put("sahanda yumurta", "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8d/Fried-Egg.jpg/320px-Fried-Egg.jpg");
        put("yumurta",         "https://upload.wikimedia.org/wikipedia/commons/thumb/2/26/Chicken_egg_2009-06-04.jpg/320px-Chicken_egg_2009-06-04.jpg");
        put("omlet",           "https://upload.wikimedia.org/wikipedia/commons/thumb/7/76/Omelette.jpg/320px-Omelette.jpg");
        put("tereyagi",        "https://upload.wikimedia.org/wikipedia/commons/thumb/2/24/Kultura_Butter.jpg/320px-Kultura_Butter.jpg");
        put("recel",           "https://upload.wikimedia.org/wikipedia/commons/thumb/7/74/Strawberry-jam.jpg/320px-Strawberry-jam.jpg");

        // ── Meyve & Sebze ───────────────────────────────────────────────────────
        put("elma",            "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Red_Apple.jpg/320px-Red_Apple.jpg");
        put("muz",             "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Banana-Single.jpg/320px-Banana-Single.jpg");
        put("portakal",        "https://upload.wikimedia.org/wikipedia/commons/thumb/4/43/Oranges_and_orange_juice.jpg/320px-Oranges_and_orange_juice.jpg");
        put("domates",         "https://upload.wikimedia.org/wikipedia/commons/thumb/8/88/Salad_garden_tomatoes.jpg/320px-Salad_garden_tomatoes.jpg");
        put("salatalik",       "https://upload.wikimedia.org/wikipedia/commons/thumb/9/96/ARS_cucumber.jpg/320px-ARS_cucumber.jpg");

        // ── İçecek ──────────────────────────────────────────────────────────────
        put("turk kahvesi",    "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e4/Turkish_coffee.jpg/320px-Turkish_coffee.jpg");
        put("kahve",           "https://upload.wikimedia.org/wikipedia/commons/thumb/4/45/A_small_cup_of_coffee.JPG/320px-A_small_cup_of_coffee.JPG");
        put("cay",             "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b4/Turkish_tea.JPG/320px-Turkish_tea.JPG");
        put("ayran",           "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9e/Ayran.jpg/320px-Ayran.jpg");
        put("su",              "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ef/Glass-of-water.jpg/320px-Glass-of-water.jpg");

        // ── Uluslararası ────────────────────────────────────────────────────────
        put("pizza",           "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/Eq_it-na_pizza-margherita_sep2005_sml.jpg/320px-Eq_it-na_pizza-margherita_sep2005_sml.jpg");
        put("burger",          "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0b/RedDot_Burger.jpg/320px-RedDot_Burger.jpg");
        put("hamburger",       "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0b/RedDot_Burger.jpg/320px-RedDot_Burger.jpg");
        put("makarna",         "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d8/Pasta_salad_veg.jpg/320px-Pasta_salad_veg.jpg");
        put("sushi",           "https://upload.wikimedia.org/wikipedia/commons/thumb/6/60/Sushi_platter.jpg/320px-Sushi_platter.jpg");
        put("sandvic",         "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e6/BLT_sandwich_on_toast.jpg/320px-BLT_sandwich_on_toast.jpg");
    }

    /**
     * Yemek adını normalize edip haritada arar.
     * Tam eşleşme yoksa kelime bazlı veya kısmi arama yapar.
     *
     * @return Fotoğraf URL'si veya null (placeholder kullanılacak).
     */
    public static String getUrl(String foodName) {
        if (foodName == null || foodName.trim().isEmpty()) return null;
        String normalized = normalize(foodName);

        // Tam eşleşme
        if (URL_MAP.containsKey(normalized)) return URL_MAP.get(normalized);

        List<String> words = Arrays.asList(normalized.split("[^a-z0-9]+"));

        // Kısmi eşleşme — kısa anahtarlar (<= 3 harf, örn. "su", "bal", "cay") yalnızca tam kelime olarak eşleşir
        for (Map.Entry<String, String> entry : URL_MAP.entrySet()) {
            String key = entry.getKey();
            if (key.length() <= 3) {
                if (words.contains(key)) {
                    return entry.getValue();
                }
            } else if (normalized.contains(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static void put(String key, String url) {
        URL_MAP.put(normalize(key), url);
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return value.trim()
                .replace('İ', 'i')
                .replace('I', 'i')
                .replace('ı', 'i')
                .replace('Ç', 'c')
                .replace('ç', 'c')
                .replace('Ğ', 'g')
                .replace('ğ', 'g')
                .replace('Ö', 'o')
                .replace('ö', 'o')
                .replace('Ş', 's')
                .replace('ş', 's')
                .replace('Ü', 'u')
                .replace('ü', 'u')
                .toLowerCase(Locale.ROOT);
    }
}
