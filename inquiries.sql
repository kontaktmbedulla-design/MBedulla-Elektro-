-- Kërkesa oferte nga website (formulari publik anfrage.html). Ekzekuto një herë në SQL Editor, pasi schema.sql është ekzekutuar.
create table if not exists public.inquiries (
  id          bigint generated always as identity primary key,
  created_at  timestamptz not null default now(),
  name        text not null check (char_length(name) between 2 and 120),
  phone       text not null default '' check (char_length(phone) <= 40),
  email       text not null default '' check (char_length(email) <= 160),
  address     text not null default '' check (char_length(address) <= 300),
  kind        text not null default '' check (char_length(kind) <= 60),
  message     text not null check (char_length(message) between 5 and 3000),
  consent_at  timestamptz not null,
  status      text not null default 'new' check (status in ('new','contacted','offer','closed')),
  note        text not null default '' check (char_length(note) <= 2000),
  check (phone <> '' or email <> '')
);

alter table public.inquiries enable row level security;

-- anyone (the public form uses the anon key) may INSERT, but never read; status must start as 'new'
drop policy if exists inq_insert on public.inquiries;
create policy inq_insert on public.inquiries for insert to anon, authenticated
  with check (status = 'new' and note = '');

-- managers and admin read / update / delete
drop policy if exists inq_select on public.inquiries;
create policy inq_select on public.inquiries for select to authenticated using (public.my_level() >= 2);
drop policy if exists inq_update on public.inquiries;
create policy inq_update on public.inquiries for update to authenticated using (public.my_level() >= 2) with check (public.my_level() >= 2);
drop policy if exists inq_delete on public.inquiries;
create policy inq_delete on public.inquiries for delete to authenticated using (public.my_level() >= 2);

grant insert on public.inquiries to anon;
grant select, insert, update, delete on public.inquiries to authenticated;
