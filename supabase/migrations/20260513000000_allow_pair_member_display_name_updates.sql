create policy "pair_members_update_own_display_name"
on public.haircut_pair_members for update
using (user_id = auth.uid())
with check (user_id = auth.uid());
