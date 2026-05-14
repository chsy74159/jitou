-- Explicit grants required from Supabase's new Data API policy (effective May 30 for new projects,
-- October 30 for all existing projects).
--
-- Only `authenticated` role is granted here because:
-- - All tables have RLS enabled; `anon` has no business accessing any of them.
-- - `service_role` bypasses RLS by default and does not need explicit table grants.

grant select, insert, update, delete
  on public.profiles
  to authenticated;

grant select, insert, update, delete
  on public.haircut_pairs
  to authenticated;

grant select, insert, update, delete
  on public.haircut_pair_members
  to authenticated;

grant select, insert, update, delete
  on public.haircut_records
  to authenticated;

grant select, insert, update, delete
  on public.joint_haircut_plans
  to authenticated;

grant select, insert, update, delete
  on public.reminder_preferences
  to authenticated;
