-- 0018_fix_grants_for_play_first.sql
-- Resolves "permission denied" errors when RLS policies perform subqueries.
-- RLS ensures that `auth.uid() = child_id` applies, so a user can only ever select their own rows.

GRANT SELECT ON public.adult_child_links TO anon;
GRANT SELECT ON public.adult_child_links TO authenticated;

GRANT SELECT ON public.player_houses TO anon;
GRANT SELECT ON public.player_houses TO authenticated;

GRANT SELECT ON public.player_inventory TO anon;
GRANT SELECT ON public.player_inventory TO authenticated;

GRANT SELECT ON public.world_zones TO anon;
GRANT SELECT ON public.world_zones TO authenticated;

GRANT SELECT ON public.player_unlocked_zones TO anon;
GRANT SELECT ON public.player_unlocked_zones TO authenticated;

GRANT SELECT ON public.purchases TO anon;
GRANT SELECT ON public.purchases TO authenticated;

GRANT SELECT ON public.items_config TO anon;
GRANT SELECT ON public.items_config TO authenticated;
