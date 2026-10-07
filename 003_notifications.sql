create table if not exists public.user_preferences (
  user_id uuid primary key references auth.users(id) on delete cascade,
  kpss_p3 numeric,
  nitelik_kodlari text[] default '{}',
  notify_new boolean default true,
  notify_score_match boolean default true,
  notify_qualification_match boolean default true,
  sound_enabled boolean default true,
  updated_at timestamptz default now()
);

alter table public.user_preferences enable row level security;
create policy if not exists "preferences own" on public.user_preferences
for all using(auth.uid()=user_id) with check(auth.uid()=user_id);

create index if not exists device_tokens_user_idx on public.device_tokens(user_id,active);
