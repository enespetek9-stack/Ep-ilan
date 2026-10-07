# EP İlan gerçek sistem kurulumu

## 1) Supabase
1. Yeni Supabase projesi oluştur.
2. `001_schema.sql`, `002_cron.sql`, `003_notifications.sql` dosyalarını SQL Editor'da sırayla çalıştır.
3. Dashboard > Integrations > Vault içinde `project_url`, `publishable_key`, `sync_secret` oluştur.
4. Edge Function'ı deploy et:

```bash
supabase functions deploy sync-announcements --no-verify-jwt
supabase secrets set SYNC_SECRET="GUCLU_DEGER"
supabase secrets set FCM_SERVICE_ACCOUNT_JSON='{"type":"service_account",...}'
```

5. `002_cron.sql` içindeki cron job'ı çalıştır. Supabase Cron/pg_cron + pg_net Edge Function çağırabilir; resmi dokümantasyon bu modeli destekliyor.

## 2) Firebase
- Firebase projesi oluştur.
- Android uygulamasını `com.epilan.app` paket adıyla ekle.
- `google-services.json` dosyasını `android/app/` altına koy.
- Firebase Cloud Messaging aktif olsun.
- Service Account JSON'u APK'ya koyma; yalnızca Supabase secret olarak sakla.

## 3) Android
`local.properties`/BuildConfig içinde:
- SUPABASE_URL
- SUPABASE_ANON_KEY

tanımlanır. Android tarafı ilanları Supabase REST üzerinden okur; FCM token cihazda alınır.

## 4) Kaynaklar
- Kariyer Kapısı: https://kariyerkapisi.gov.tr/isealim
- ilan.gov.tr: Personel Alımı > Kamu-Akademik Personel
- Yerel Yönetimler GM: https://yerelyonetimler.csb.gov.tr/duyurular

Kaynak sayfaları dinamik olarak değişebileceği için scraper'ın periyodik olarak kontrol edilmesi gerekir. Başvuru için her zaman resmi ilan bağlantısı esas alınmalıdır.
