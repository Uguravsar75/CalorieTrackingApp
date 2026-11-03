# Kalori Takip (CalorieTrackingApp)

Yapay zeka (Google Gemini Vision) destekli yemek tanıma, kişiselleştirilmiş kalori & makro besin takibi, sağlık durumuna özel beslenme önerileri ve Firebase bulut senkronizasyonu sunan modern Android uygulaması.

---

## Özellikler

- **Kimlik Doğrulama & Bulut Senkronizasyonu**: Firebase Authentication (E-posta/Şifre ve Google ile Giriş) + Cloud Firestore ile cihazlar arası gerçek zamanlı profil, öğün ve günlük istatistik senkronizasyonu.
- **Kişiselleştirilmiş Kalori & BMR Hesabı**: Mifflin-St Jeor formülüyle yaş, boy, kilo, cinsiyet ve aktivite düzeyine göre BMR/TDEE ve günlük hedef kalori hesaplama.
- **AI Kamera ile Yemek Tanıma**: Google Gemini 2.5 Flash Vision API kullanarak yemek fotoğrafından yemek adı, tahmini gramaj, kalori ve makro besin (Protein, Karbonhidrat, Yağ) değerlerini otomatik çıkarma.
- **Zengin Yemek Kataloğu (100+ Besin)**: Türk ve dünya mutfağından 103 ön tanımlı gıda; kategori filtreleme, Türkçe/İngilizce karakter duyarsız anlık arama ve porsiyon/gram dönüşümü.
- **Sağlık Durumu Duyarlı Akıllı Öneriler**: Diyabet (şeker uyarısı), Hipertansiyon (tuz uyarısı) ve Kolesterol (yağ uyarısı) profillerine göre anlık besin uyarıları ve günün saatine/konuma duyarlı alternatif öğün tavsiyeleri.
- **Günlük Takip & Haftalık Rapor**: Su tüketimi, adım sayısı, egzersiz kalorisi takibi ve son 7 günlük kalori/makro grafik analizi.
- **Akıllı Hatırlatıcılar**: WorkManager tabanlı öğün ve su hatırlatma bildirimleri.
- **Çoklu Dil & Tema Desteği**: Türkçe ve İngilizce dil desteği, Açık/Koyu/Sistem teması (Material Design 3).

---

## Teknoloji Yığını

- **Dil & SDK**: Java 11, Android SDK (Min SDK 28, Target/Compile SDK 35)
- **Arayüz**: Material Design 3, ConstraintLayout, ViewBinding/RecyclerView, Özel Grafik Görünümü (`WeeklyChartView`)
- **Backend & Cloud**: Firebase Authentication, Cloud Firestore, Google Play Services Auth & Location
- **Yapay Zeka**: Google Gemini REST API (`gemini-2.5-flash`)
- **Yerel Veritabanı & Arka Plan İşlemleri**: Room Database, WorkManager, SharedPreferences
- **Görsel İşleme**: CameraX, Glide

---

## Kurulum

1. **Projeyi klonlayın:**
   ```bash
   git clone https://github.com/Uguravsar75/CalorieTrackingApp.git
   cd CalorieTrackingApp
   ```

2. **`local.properties` dosyasını oluşturun:**
   Proje kök dizinindeki `local.properties.example` dosyasını `local.properties` olarak kopyalayın ve kendi Android SDK yolunuz ile Gemini API anahtarınızı girin:
   ```properties
   sdk.dir=/Users/<kullanici-adi>/Library/Android/sdk
   GEMINI_API_KEY=AIzaSy...
   ```

3. **Firebase Yapılandırması:**
   - `app/google-services.json` dosyasının mevcut olduğundan emin olun.
   - Firestore güvenlik kuralları için [`Firebase_Guvenlik_Kurallari.md`](Firebase_Guvenlik_Kurallari.md) dosyasındaki kuralları Firebase Console üzerinden yayınlayın.

4. **Derleme ve Çalıştırma:**
   Android Studio ile projeyi açıp çalıştırabilir veya terminalden derleyebilirsiniz:
   ```bash
   ./gradlew assembleDebug
   ```

---

## Proje Yapısı

```text
app/src/main/java/tr/edu/duzce/mf/bm/calorietrackingapp/
├── activities/      # Ekranlar (Splash, Login, Register, Main, AddMeal, Camera, WeeklyReport, Settings vb.)
├── adapters/        # RecyclerView adaptörleri (FoodAdapter, MealEntryAdapter, WeeklyReportAdapter)
├── database/        # Room veritabanı, DAO'lar ve Entity sınıfları (User, Food, MealEntry)
├── repository/      # Firestore + yerel katalog veri yönetimi (AppRepository, FoodCatalog)
└── utils/           # Yardımcı servisler (GeminiFoodService, CalorieCalculator, RecommendationEngine, WorkManager vb.)
```

