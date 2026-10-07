# FCM bağlantısı

1. Firebase Console'da Android app: `com.epilan.app`
2. `google-services.json` -> android/app/
3. Firebase service account JSON oluştur.
4. Supabase secret olarak `FCM_SERVICE_ACCOUNT_JSON` koy.
5. Edge Function'da OAuth2 access token üretip `https://fcm.googleapis.com/v1/projects/PROJECT_ID/messages:send` endpointine POST et.
6. Her yeni ilan için `device_tokens.active=true` kayıtlarına bildirim gönder.

Üretim ortamında service account JSON'u APK'ya koyma. Sadece Supabase secret olarak sakla.
