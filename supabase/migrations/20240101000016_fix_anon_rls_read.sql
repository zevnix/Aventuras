-- 0016_fix_anon_rls_read.sql
-- Ensure the anon and authenticated roles have explicit access to read from missions_config.

DROP POLICY IF EXISTS "Anyone can read published missions" ON public.missions_config;
CREATE POLICY "Anyone can read published missions"
ON public.missions_config FOR SELECT
USING (status = 'published');

GRANT SELECT ON public.missions_config TO anon;
GRANT SELECT ON public.missions_config TO authenticated;

-- Do the same for other configs
DROP POLICY IF EXISTS "Anyone can read published configs" ON public.game_configs;
CREATE POLICY "Anyone can read published configs"
ON public.game_configs FOR SELECT
USING (status = 'published');
GRANT SELECT ON public.game_configs TO anon;
GRANT SELECT ON public.game_configs TO authenticated;

DROP POLICY IF EXISTS "Anyone can read published pets" ON public.pets_config;
CREATE POLICY "Anyone can read published pets"
ON public.pets_config FOR SELECT
USING (status = 'published');
GRANT SELECT ON public.pets_config TO anon;
GRANT SELECT ON public.pets_config TO authenticated;

DROP POLICY IF EXISTS "Anyone can read published evolutions" ON public.pet_evolutions;
CREATE POLICY "Anyone can read published evolutions"
ON public.pet_evolutions FOR SELECT
USING (status = 'published');
GRANT SELECT ON public.pet_evolutions TO anon;
GRANT SELECT ON public.pet_evolutions TO authenticated;
