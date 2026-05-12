create extension if not exists pgcrypto;

create type public.joint_plan_status as enum (
  'pending',
  'confirmed',
  'cancelled',
  'completed'
);

create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  nickname text not null default 'Sion',
  avatar_url text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.haircut_pairs (
  id uuid primary key default gen_random_uuid(),
  name text not null default '约剪小组',
  created_by uuid not null references auth.users(id) on delete cascade,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.haircut_pair_members (
  pair_id uuid not null references public.haircut_pairs(id) on delete cascade,
  user_id uuid not null references auth.users(id) on delete cascade,
  display_name text not null,
  joined_at timestamptz not null default now(),
  primary key (pair_id, user_id)
);

create table public.haircut_records (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  haircut_date date not null,
  note text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz
);

create table public.joint_haircut_plans (
  id uuid primary key default gen_random_uuid(),
  pair_id uuid not null references public.haircut_pairs(id) on delete cascade,
  proposer_id uuid not null references auth.users(id) on delete cascade,
  proposed_at timestamptz not null,
  status public.joint_plan_status not null default 'pending',
  confirmed_by uuid references auth.users(id) on delete set null,
  confirmed_at timestamptz,
  cancelled_by uuid references auth.users(id) on delete set null,
  cancelled_at timestamptz,
  completed_by uuid references auth.users(id) on delete set null,
  completed_at timestamptz,
  reminder_days_before integer not null default 1 check (reminder_days_before between 0 and 14),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create unique index one_active_joint_plan_per_pair
  on public.joint_haircut_plans(pair_id)
  where status in ('pending', 'confirmed');

create table public.reminder_preferences (
  user_id uuid primary key references auth.users(id) on delete cascade,
  enabled boolean not null default true,
  days_before integer not null default 2 check (days_before between 0 and 14),
  reminder_time time not null default time '20:30',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create or replace function public.touch_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create trigger touch_profiles_updated_at
before update on public.profiles
for each row execute function public.touch_updated_at();

create trigger touch_haircut_pairs_updated_at
before update on public.haircut_pairs
for each row execute function public.touch_updated_at();

create trigger touch_haircut_records_updated_at
before update on public.haircut_records
for each row execute function public.touch_updated_at();

create trigger touch_joint_haircut_plans_updated_at
before update on public.joint_haircut_plans
for each row execute function public.touch_updated_at();

create trigger touch_reminder_preferences_updated_at
before update on public.reminder_preferences
for each row execute function public.touch_updated_at();

create or replace function public.is_pair_member(target_pair_id uuid)
returns boolean
language sql
security definer
set search_path = public
as $$
  select exists (
    select 1
    from public.haircut_pair_members member
    where member.pair_id = target_pair_id
      and member.user_id = auth.uid()
  );
$$;

alter table public.profiles enable row level security;
alter table public.haircut_pairs enable row level security;
alter table public.haircut_pair_members enable row level security;
alter table public.haircut_records enable row level security;
alter table public.joint_haircut_plans enable row level security;
alter table public.reminder_preferences enable row level security;

create policy "profiles_select_self"
on public.profiles for select
using (id = auth.uid());

create policy "profiles_insert_self"
on public.profiles for insert
with check (id = auth.uid());

create policy "profiles_update_self"
on public.profiles for update
using (id = auth.uid())
with check (id = auth.uid());

create policy "pairs_select_member"
on public.haircut_pairs for select
using (created_by = auth.uid() or public.is_pair_member(id));

create policy "pairs_insert_creator"
on public.haircut_pairs for insert
with check (created_by = auth.uid());

create policy "pair_members_select_member"
on public.haircut_pair_members for select
using (public.is_pair_member(pair_id));

create policy "pair_members_insert_self_or_creator"
on public.haircut_pair_members for insert
with check (
  user_id = auth.uid()
  or exists (
    select 1
    from public.haircut_pairs pair
    where pair.id = pair_id
      and pair.created_by = auth.uid()
  )
);

create policy "records_select_self"
on public.haircut_records for select
using (user_id = auth.uid() and deleted_at is null);

create policy "records_insert_self"
on public.haircut_records for insert
with check (user_id = auth.uid());

create policy "records_update_self"
on public.haircut_records for update
using (user_id = auth.uid())
with check (user_id = auth.uid());

create policy "plans_select_pair_member"
on public.joint_haircut_plans for select
using (public.is_pair_member(pair_id));

create policy "plans_insert_pair_member"
on public.joint_haircut_plans for insert
with check (
  proposer_id = auth.uid()
  and public.is_pair_member(pair_id)
);

create policy "plans_update_pair_member"
on public.joint_haircut_plans for update
using (public.is_pair_member(pair_id))
with check (public.is_pair_member(pair_id));

create policy "reminders_select_self"
on public.reminder_preferences for select
using (user_id = auth.uid());

create policy "reminders_insert_self"
on public.reminder_preferences for insert
with check (user_id = auth.uid());

create policy "reminders_update_self"
on public.reminder_preferences for update
using (user_id = auth.uid())
with check (user_id = auth.uid());
