# Firebase / Cloud Veri Saklama Notlari

Bu dosya, CalorieTrackingApp icin Firestore tarafinda beklenen veri modeli ve temel guvenlik kurallarini dokumante eder.

## Veri Sahipligi

- Ana veri kaynagi Firestore'dur.
- Kullanici profili `users/{uid}` dokumaninda saklanir.
- Ogunnler `users/{uid}/meal_entries/{mealId}` alt koleksiyonunda saklanir.
- Gunluk su/adim/egzersiz istatistikleri `users/{uid}/daily_stats/{date}` alt koleksiyonunda saklanir.
- Yemek katalog verisi `foods/{foodId}` koleksiyonunda tutulur (uygulama tarafindan tohum veri olarak yazilir).

## Firebase Console'da Uygulanacak Firestore Kuralları

Asagidaki kurallari Firebase Console → Firestore → Rules sekmesine yapistir ve "Publish" butonuna bas.

```js
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    function signedIn() {
      return request.auth != null;
    }

    function isOwner(userId) {
      return signedIn() && request.auth.uid == userId;
    }

    // Kullanici profili ve alt koleksiyonlari — sadece hesap sahibi erisebilir
    match /users/{userId} {
      allow read, create, update, delete: if isOwner(userId);

      match /meal_entries/{mealId} {
        allow read, create, update, delete: if isOwner(userId);
      }

      match /daily_stats/{date} {
        allow read, create, update, delete: if isOwner(userId);
      }
    }

    // Yemek katalogu — oturum acmis herkes okuyabilir ve yazabilir (uygulama tohum verisi icin)
    match /foods/{foodId} {
      allow read, write: if signedIn();
    }

    // Baglanti testi dokumani — oturum acmis kullanicilar okuyup yazabilir
    match /diagnostics/{docId} {
      allow read, write: if signedIn();
    }
  }
}
```

## Nasıl Uygulanır (Adım Adım)

1. Firebase Console'a git: https://console.firebase.google.com
2. Projeyi seç
3. Sol menüden "Firestore Database" → "Rules" sekmesi
4. Mevcut kurallarin tamamini sil
5. Yukaridaki kurallari kopyala ve yapistir
6. "Publish" butonuna tikla

## Test Senaryolari

- Kullanici A, kendi `users/{uid}` profilini okuyup yazabilmeli.
- Kullanici A, Kullanici B'nin profilini veya ogunlerini okuyamamali.
- Oturum acmamis kullanici hicbir veriye erisememeli.
- Oturum acmis kullanici yemek katalog verisini okuyup yazabilmeli (tohum data icin gerekli).
