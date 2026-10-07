-- EP İlan: 30 dakikalık sunucu senkronizasyonu.
-- Önce Supabase Dashboard > Integrations > Vault'a şu secret'ları ekleyin:
-- project_url = https://YOUR_PROJECT.supabase.co
-- publishable_key = sb_publishable_...
-- sync_secret = güçlü rastgele değer

create extension if not exists pg_cron;
create extension if not exists pg_net;

select cron.unschedule('ep-ilan-sync-30m')
where exists (select 1 from cron.job where jobname='ep-ilan-sync-30m');

select cron.schedule(
  'ep-ilan-sync-30m',
  '*/30 * * * *',
  $$
  select net.http_post(
    url := (select decrypted_secret from vault.decrypted_secrets where name='project_url') || '/functions/v1/sync-announcements',
    headers := jsonb_build_object(
      'Content-Type','application/json',
      'apikey',(select decrypted_secret from vault.decrypted_secrets where name='publishable_key'),
      'x-sync-secret',(select decrypted_secret from vault.decrypted_secrets where name='sync_secret')
    ),
    body := jsonb_build_object('scheduled',true,'time',now())
  );
  $$
);
