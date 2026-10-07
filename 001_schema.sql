create extension if not exists pgcrypto;
create extension if not exists pg_cron;
create extension if not exists pg_net;

create table if not exists public.profiles (
 id uuid primary key references auth.users(id) on delete cascade,
 ad text default '',
 kpss_p3 numeric,
 nitelik_kodlari text[] default '{}',
 created_at timestamptz default now(), updated_at timestamptz default now()
);

create table if not exists public.announcements (
 id uuid primary key default gen_random_uuid(),
 kurum text not null,
 pozisyon text not null,
 aciklama text default '',
 son_tarih timestamptz,
 taban_puan numeric,
 nitelik_kodlari text[] default '{}',
 sozlesme_turu text,
 link text,
 source text not null,
 external_id text,
 published_at timestamptz,
 created_at timestamptz default now(), updated_at timestamptz default now(),
 unique(source, external_id)
);
create index if not exists announcements_source_idx on public.announcements(source);
create index if not exists announcements_deadline_idx on public.announcements(son_tarih);

create table if not exists public.favorites (
 user_id uuid references auth.users(id) on delete cascade,
 announcement_id uuid references public.announcements(id) on delete cascade,
 created_at timestamptz default now(),
 primary key(user_id, announcement_id)
);

create table if not exists public.device_tokens (
 id uuid primary key default gen_random_uuid(),
 user_id uuid references auth.users(id) on delete cascade,
 token text unique not null,
 platform text default 'android',
 active boolean default true,
 created_at timestamptz default now(), updated_at timestamptz default now()
);

create table if not exists public.last_sync (
 source text primary key,
 synced_at timestamptz default now(),
 item_count integer default 0
);

alter table public.profiles enable row level security;
alter table public.announcements enable row level security;
alter table public.favorites enable row level security;
alter table public.device_tokens enable row level security;

create policy "profiles own" on public.profiles for all using(auth.uid()=id) with check(auth.uid()=id);
create policy "announcements public read" on public.announcements for select using(true);
create policy "favorites own" on public.favorites for all using(auth.uid()=user_id) with check(auth.uid()=user_id);
create policy "tokens own" on public.device_tokens for all using(auth.uid()=user_id) with check(auth.uid()=user_id);
