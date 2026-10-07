# EP İlan — gerçek sistem

Bu paket Android uygulaması + Supabase backend + 30 dakikalık cron + yeni ilan FCM bildirimi için hazırlanmıştır.

### Mimari
Android → Supabase REST → announcements
                         ↑
Supabase Cron (*/30) → Edge Function → 3 resmi kaynak
                                      → yeni kayıt
                                      → FCM → cihaz

Supabase Cron, pg_cron ve pg_net ile Edge Function'ı periyodik çağırabilir. Bu nedenle 30 dakikalık tarama cihazın arka planda açık kalmasına bağlı değildir.

### Önemli
Kariyer Kapısı ve ilan.gov.tr sayfaları dinamik olabilir. Parser başarısız olduğunda uygulama yanlış ilan üretmemeli; bu yüzden her ilan kartında resmi kaynak bağlantısı gösterilmelidir.

Kurulum: `supabase/DEPLOY.md`.
