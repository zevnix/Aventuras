-- 0017_fix_player_balances_rls_for_anon.sql
-- Ensure that the newly created anonymous session can read from its own player_balances entry.

DROP POLICY IF EXISTS "Child and approved adults can view balance" ON public.player_balances;

CREATE POLICY "Child and approved adults can view balance"
ON public.player_balances FOR SELECT USING (
  auth.uid() = child_id OR EXISTS (
    SELECT 1 FROM public.adult_child_links
    WHERE adult_id = auth.uid() AND child_id = player_balances.child_id AND status = 'active'
  )
);

GRANT SELECT ON public.player_balances TO anon;
GRANT SELECT ON public.player_balances TO authenticated;


-- Apply explicit GRANTs to other player state tables so anonymous users are not blocked
GRANT SELECT, UPDATE ON public.player_profiles TO anon;
GRANT SELECT, UPDATE ON public.player_profiles TO authenticated;

GRANT SELECT, INSERT, UPDATE ON public.mission_instances TO anon;
GRANT SELECT, INSERT, UPDATE ON public.mission_instances TO authenticated;

GRANT SELECT ON public.economy_ledger TO anon;
GRANT SELECT ON public.economy_ledger TO authenticated;

GRANT SELECT ON public.children TO anon;
GRANT SELECT ON public.children TO authenticated;
