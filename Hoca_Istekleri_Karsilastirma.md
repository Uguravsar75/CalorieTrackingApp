# Hoca İstekleri ile Proje Karşılaştırması

Bu dosya, hocanın istediği özellikleri mevcut `CalorieTrackingApp` projesiyle karşılaştırır. Odak noktası henüz yapılmamış olanlar ve geliştirilmesi gereken maddelerdir.

## Mevcut Projede Karşılanan Özellikler

- E-posta ve şifre ile kayıt/giriş Firebase Auth üzerinden yapılmış.
- Profil oluşturma ekranında yaş, cinsiyet, boy, kilo, aktivite seviyesi, günlük kalori hedefi ve hastalık bilgileri alınmış.
- Diyabet, tansiyon, kolesterol ve diğer hastalık alanları kullanıcı modelinde bulunuyor.
- Dashboard ekranında günlük hedef kalori, alınan kalori, kalan kalori ve yüzde/progress göstergesi var.
- Günlük kalori limiti aşılınca uyarı ve sağlıklı alternatif önerisi gösteriliyor.
- Cihaz saatine göre kahvaltı/öğle/akşam/geç saat önerileri üretiliyor.
- Yaygın Türk yemekleri için hazır yemek kataloğu eklenmiş: simit, poğaça, tost, çorba, ev yemekleri vb.
- Diyabet için şekerli gıdalarda uyarı, tansiyon için tuzlu gıda uyarısı mantığı kısmen mevcut.
- Kamera veya galeri üzerinden yemek fotoğrafı alınıp Gemini ile yemek adı, kalori ve makro değer tahmini yapılabiliyor.
- AI çalışmazsa manuel giriş yapılabilecek fallback akışı var.
- Haftalık rapor ekranında ortalama kalori, en çok tüketilen besin, diyete uyum oranı ve basit AI analiz raporu mevcut.
- Bildirim altyapısı, kahvaltı hatırlatma, su hatırlatma, limit aşımı ve hedefe yaklaşma bildirimi için eklenmiş.
- Firebase Auth ve Firestore ile kullanıcı, öğün ve günlük istatistik verileri cloud tarafında saklanıyor.

## Henüz Yapılmamış Olanlar

### 1. Google ile Giriş

Durum: Kod tarafı eklendi. Login ve register ekranlarındaki Google butonları artık Google Sign-In akışını başlatıyor ve Firebase Auth ile oturum açıyor.

Tamamlanan işler:

- Google Sign-In dependency eklendi.
- Google hesabından alınan ID token Firebase credential'a çevriliyor.
- Google ile girişten sonra kullanıcı profili Firestore'da oluşturuluyor veya mevcut profil okunuyor.
- Google butonlarına gerçek click listener eklendi.

Kalan dış yapılandırma:

- Firebase Authentication Google provider aktif edilmeli.
- Firebase Console'dan Android SHA-1/SHA-256 parmak izleri eklenmeli.
- Firebase'den güncel `google-services.json` indirilip `app/google-services.json` ile değiştirilmelidir. Mevcut dosyada OAuth client alanı boş olduğu için Google ile giriş çalışma anında yapılandırma eksik uyarısı gösterebilir.

### 2. Konum Algılama

Durum: Kod tarafı eklendi. Dashboard açıldığında konum izni isteniyor; izin verilirse Fused Location Provider ile konum alınarak öneri sistemindeki saat hesabına bağlanıyor. İzin verilmezse uygulama cihaz saat dilimiyle çalışmaya devam ediyor.

Tamamlanan işler:

- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` izinleri eklendi.
- Google Play Services Location dependency eklendi.
- Kullanıcının güncel veya son bilinen konumu alınmaya başlandı.
- Konuma göre saat dilimi bağlamı üretilip dashboard önerilerine aktarıldı.
- Konum izni verilmezse cihaz saat dilimine dönen fallback korundu.

Geliştirilmesi gereken işler:

- Konumdan saat dilimi belirleme şu an Türkiye için `Europe/Istanbul`, diğer bölgeler için boylama dayalı yaklaşık GMT hesabı kullanıyor. Daha hassas sonuç için timezone API veya daha kapsamlı offline timezone verisi eklenebilir.
- Gerçek cihazda izin verme/reddetme senaryoları test edilmeli.
- Kullanıcıya konum kullanım amacı için daha açıklayıcı bir izin öncesi bilgilendirme ekranı eklenebilir.

### 3. Güneşe Göre Algılama

Durum: Yapılmayacak. Bu özellik opsiyonel gelişmiş özellik olduğu için projeden çıkarılmasına karar verildi.

Not:

- Mevcut uygulama saat ve konum tabanlı öneri üretmeye devam ediyor.
- Gün doğumu/gün batımı API entegrasyonu yapılmayacak.

### 4. Takvim Görünümü

Durum: Kod tarafı eklendi. Haftalık rapor ekranına mevcut ay için takvim görünümü eklendi. Günler kalori durumuna göre renklendiriliyor ve güne tıklanınca o günün özeti gösteriliyor.

Tamamlanan işler:

- Günleri takvim düzeninde gösteren grid eklendi.
- Yeşil: hedefin güvenli altında kalan günler.
- Kırmızı: limit aşılan günler.
- Sarı: hedefe yakın günler.
- Gri: öğün kaydı olmayan günler.
- Gün seçilince alınan kalori, hedef kalori ve öğün listesi gösteriliyor.

Geliştirilmesi gereken işler:

- Önceki/sonraki ay geçişi için buton eklenebilir.
- Takvim renkleri için küçük bir açıklama/legend eklenebilir.
- Gün detayında öğünleri düzenleme ekranına geçiş eklenebilir.

### 5. Aylık Rapor

Durum: Kod tarafı eklendi. Rapor ekranına aylık rapor bölümü eklendi.

Tamamlanan işler:

- Aylık ortalama kalori hesaplanıyor.
- Ay içindeki en çok tüketilen besinler gösteriliyor.
- Aylık diyete uyum oranı hesaplanıyor.
- Haftalık rapor ve aylık rapor aynı rapor ekranında birlikte gösteriliyor.

Geliştirilmesi gereken işler:

- Haftalık/aylık geçiş için tab veya segment kontrol eklenebilir.
- Önceki ayların raporunu görmek için ay seçici eklenebilir.
- Aylık rapora grafik veya trend çizgisi eklenebilir.

### 6. Tam Bildirim İzin Akışı

Durum: Kod tarafı eklendi. Android 13+ bildirim izni ana ekranda isteniyor; izin kapalıysa kullanıcı ayarlara yönlendirilebiliyor.

Tamamlanan işler:

- `POST_NOTIFICATIONS` runtime permission kullanıcıdan isteniyor.
- Kullanıcı izin vermezse uygulama bildirim ayarlarına yönlendirme gösteriliyor.
- Ayarlar ekranında bildirimlerin açık/kapalı durumu gösteriliyor.
- Ayarlar ekranından uygulama bildirim ayarlarına gidilebiliyor.

Geliştirilmesi gereken işler:

- Hatırlatma saatleri kullanıcı tarafından düzenlenebilir yapılabilir.
- Kahvaltı, su ve hedefe yaklaşma bildirimleri ayrı ayrı açılıp kapatılabilir hale getirilebilir.
- Aynı gün içinde tekrar eden hedefe yaklaşma bildirimleri için daha güçlü tek-sefer kontrolü eklenebilir.

### 7. Tam Çoklu Cihaz Senkronizasyon Testi

Durum: Kod tarafı geliştirildi. Firestore işlemlerinde son başarılı senkronizasyon zamanı ve son hata bilgisi yerel olarak tutuluyor; profil ekranında kullanıcıya gösteriliyor.

Tamamlanan işler:

- Kullanıcı, öğün, yemek kataloğu ve günlük istatistik Firestore işlemlerinde senkron başarı/hata durumu kaydediliyor.
- Profil ekranında son başarılı senkronizasyon zamanı gösteriliyor.
- Senkron hatası oluşursa profil ekranında hata mesajı gösteriliyor.
- Yeni cihazdan girişte veriler Firestore üzerinden kullanıcı UID altından okunmaya devam ediyor.

Geliştirilmesi gereken işler:

- Yeni cihazdan giriş sonrası tüm geçmiş öğünlerin eksiksiz geldiği gerçek cihaz/emülatör ile test edilmeli.
- Offline durumda veri ekleme ve sonradan senkron davranışı ayrıca test edilmeli.
- Çakışma yönetimi için aynı öğünün iki cihazda düzenlenmesi senaryosu netleştirilebilir.
- Senkron hataları sadece profil ekranında değil, ilgili işlem ekranlarında Snackbar/dialog ile de gösterilebilir.

## Geliştirilmesi Gerekenler

### 1. Profil Sistemi

Durum: Geliştirme başladı. Profil oluşturma ve profil düzenleme ekranlarına temel değer validasyonları eklendi.

Tamamlanan işler:

- Yaş için minimum/maksimum değer kontrolü eklendi.
- Boy için minimum/maksimum değer kontrolü eklendi.
- Kilo için minimum/maksimum değer kontrolü eklendi.
- Günlük kalori hedefi için minimum/maksimum değer kontrolü eklendi.
- Hatalı değerlerde ilgili input üzerinde kullanıcıya açıklayıcı hata gösteriliyor.
- Günlük kalori hedefi hesaplanırken BMR ve aktivite katsayısına dayalı kısa açıklama gösteriliyor.

Geliştirilmeye devam edilecek işler:

- Hastalık seçimlerinde birden fazla hastalık için daha ayrıntılı öneri üretilebilir.
- Diğer kronik hastalıklar serbest metin olarak alınıyor; istenirse daha düzenli kategori seçimi eklenebilir.

### 2. Dashboard

Durum: Geliştirme başladı. Limit aşımı görselleştirmesi güçlendirildi.

Tamamlanan işler:

- Günlük limit aşılırsa kalan kalori alanında artık `0` yerine kaç kcal aşıldığı gösteriliyor.
- Progress yazısı gerçek yüzdeyi gösteriyor; dairesel gösterge görsel taşmayı önlemek için 100'de kalıyor.
- Limit aşımı metni kırmızı renkle daha belirgin hale getirildi.
- Sağlıklı alternatif önerisi son eklenen öğüne göre özelleştiriliyor.

Geliştirilmeye devam edilecek işler:

- Günlük su, adım ve egzersiz verileri manuel giriliyor; cihaz sensörleri veya sağlık uygulamalarıyla entegrasyon eklenebilir.

### 3. Akıllı Yemek Öneri Sistemi

Durum: Geliştirme başladı. Öneriler kalan kalori ve hastalık risklerine göre daha hedefli hale getirildi.

Tamamlanan işler:

- Kalan kalori 300 kcal altına düşerse daha hafif öneriler gösteriliyor.
- Günlük hedef aşıldıysa su/bitki çayı, ara öğün atlama ve hafif sebze gibi düşük kalorili öneriler gösteriliyor.
- `Food` modeline `saltWarning` alanı eklendi.
- Simit, poğaça, döner, cips, pizza, tost gibi tuzlu/işlenmiş gıdalar tansiyon uyarısı için işaretlendi.
- Öğün ekleme ekranında diyabet, tansiyon ve kolesterol için daha kapsamlı risk uyarıları gösteriliyor.
- Seçilen porsiyonun günlük hedefi ne kadar etkilediği, kalan/aşan kalori olarak gösteriliyor.

Geliştirilmeye devam edilecek işler:

- Öneriler kullanıcının geçmiş tüketim alışkanlıklarına göre kişiselleştirilebilir.
- Risk etiketleri besin verisi arttıkça daha hassas hale getirilebilir.

### 4. Kamera ile Ürün Tanıma

Durum: Geliştirme başladı. Kamera/galeri ile alınan AI sonucu artık daha açık şekilde "tahmini" olarak gösteriliyor ve makro değerler öğün kaydına kalıcı olarak yazılıyor.

Tamamlanan işler:

- AI sonuç kartına tahmini değer uyarısı eklendi.
- AI çalışmazsa manuel giriş notu ayrı gösteriliyor.
- Kamera sonucundaki protein, karbonhidrat ve yağ değerleri `MealEntry` alanlarına kaydediliyor.
- Kamera ile eklenen öğünler ana sayfadaki öğün listesinde makro özeti ve `AI tahmini` etiketiyle gösteriliyor.
- Room veritabanı şeması makro alanları için 5. versiyona taşındı.

Geliştirilmeye devam edilecek işler:

- Gerçek güven skoru Gemini cevabından alınmıyor; istenirse prompt ve parser buna göre genişletilebilir.
- Kullanıcı makro değerleri doğrudan düzenleyemiyor; sadece yemek adı ve kaloriyi düzenleyebiliyor.
- Yemek fotoğrafı cloud storage'a yüklenmiyor; cihaz içinde saklanıyor. Çoklu cihazda fotoğrafın da görünmesi istenirse Firebase Storage eklenmeli.

### 5. Raporlama

Durum: Geliştirme başladı. Rapor AI analizi artık sadece toplam kaloriye bakmıyor; kamera ile kaydedilen makro değerleri ve akşam öğünü/ekleme saatini de değerlendiriyor.

Tamamlanan işler:

- AI analiz raporu, kayıtlı makro verisi varsa karbonhidrat oranını kullanarak yorum üretiyor.
- Akşam öğünü veya akşam saatinde eklenen kayıtların toplam kalori içindeki payı analiz ediliyor.
- Akşam kalori yükü yüksekse raporda buna özel uyarı gösteriliyor.
- Takvim gün detayında makro özeti ve `AI tahmini` etiketi gösteriliyor.

Geliştirilmeye devam edilecek işler:

- Rapor ekranında grafik veya trend çizgisi eklenebilir.
- Hedefe yakın gün, limit aşımı ve hedefe uyum renkleri için daha görünür legend eklenebilir.
- Makro analizleri kamera dışındaki manuel öğünlerde de dolacak şekilde genişletilebilir.

### 6. Bildirim Sistemi

Durum: Geliştirme başladı. Bildirimler artık ayarlar ekranından senaryo bazlı açılıp kapatılabiliyor ve günlük tekrar kontrolü güçlendirildi.

Tamamlanan işler:

- Kahvaltı/öğün hatırlatması için ayrı aç/kapat ayarı eklendi.
- Su içme hatırlatması için ayrı aç/kapat ayarı eklendi.
- Günlük limit aşımı uyarısı için ayrı aç/kapat ayarı eklendi.
- Hedefe yaklaşma uyarısı için ayrı aç/kapat ayarı eklendi.
- Limit aşımı, hedefe yaklaşma ve kahvaltı hatırlatması aynı gün içinde tekrar tekrar gönderilmemesi için tek-gün kontrolüne bağlandı.
- Mevcut Worker ve dashboard bildirimleri aynı `NotificationHelper` tercihlerini kullanıyor.

Geliştirilmeye devam edilecek işler:

- Hatırlatma saatleri kullanıcı tarafından seçilebilir yapılabilir.
- WorkManager periyodu 6 saat olduğu için bazı saat aralıkları kaçabilir; daha net zamanlanmış işler kullanılabilir.

### 7. Firebase / Cloud Tabanlı Veri Saklama

Durum: Geliştirme başladı. Firestore ana veri kaynağı olarak netleştirildi ve teslim için güvenlik kuralları dokümantasyonu eklendi.

Tamamlanan işler:

- `Firebase_Guvenlik_Kurallari.md` dosyası eklendi.
- Kullanıcı profilinin `users/{uid}` altında tutulduğu dokümante edildi.
- Öğün kayıtları ve günlük istatistiklerin kullanıcı alt koleksiyonlarında saklandığı açıklandı.
- `foods` katalog koleksiyonu için okuma/yazma yaklaşımı belirtildi.
- Kullanıcı verilerinin sadece ilgili Firebase Auth UID sahibi tarafından okunup yazılması için örnek Firestore rules eklendi.
- Room tarafının yerel cache/eski destek katmanı, Firestore'un ana senkron veri kaynağı olduğu belirtildi.

Geliştirilmeye devam edilecek işler:

- Önerilen kurallar Firebase Console'a uygulanıp gerçek kullanıcılarla test edilmeli.
- Offline çalışma desteklenecekse Room cache stratejisi kod seviyesinde daha açık hale getirilebilir.
- Aynı öğünün iki cihazda düzenlenmesi gibi çakışma senaryoları için son yazan kazanır dışında özel politika eklenebilir.

## Öncelik Sırası

1. Google ile giriş akışını tamamla.
2. Takvim görünümü ve aylık raporu ekle.
3. Bildirim izin isteme ve bildirim ayarlarını tamamla.
4. Konum ve güneş verisi özelliklerini ekle.
5. Yemek önerilerini gerçek tüketim geçmişi, kalan kalori ve hastalık risklerine göre geliştirme.
6. Makro besin değerlerini öğün kayıtlarına kalıcı olarak ekleyip rapor analizini güçlendirme.
7. Çoklu cihaz senkronizasyonunu offline/online durumlarıyla test edip hata durumlarını kullanıcıya gösterme.
