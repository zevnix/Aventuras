-- 0018_fix_grants_for_play_first.sql
-- Resolves "permission denied for table adult_child_links" and other related tables.
-- The RLS policies on tables like mission_instances and player_balances perform subqueries
-- on adult_child_links. Without a base SELECT grant on adult_child_links, PostgreSQL blocks the entire query.
-- The underlying data remains secure because adult_child_links has its own strict RLS policies.

GRANT SELECT ON public.adult_child_links TO anon;
GRANT SELECT ON public.adult_child_links TO authenticated;

-- Let's also ensure all other tables involved in standard gameplay have baseline SELECT access.
-- Again, their data is protected by the RLS policies already defined.
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
