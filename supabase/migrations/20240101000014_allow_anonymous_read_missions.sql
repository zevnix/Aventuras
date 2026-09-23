-- 0014_allow_anonymous_read_missions.sql
-- In Play-First mode, children might be using anonymous sessions.
-- The previous RLS for config tables checked for `status = 'published' OR is_admin()`,
-- but did not strictly depend on authenticated roles, however we should ensure
-- that the policy applies to both authenticated and anon roles, or simply 'anon' is not blocked.

-- Let's redefine the policy to be explicitly permissive for 'anon' users to read published configs.
-- (By default, Supabase applies policies to public role which includes anon/authenticated,
-- but sometimes clients connecting via anon key without a user session might fail if not explicit).

DROP POLICY IF EXISTS "Anyone can read published missions" ON public.missions_config;
CREATE POLICY "Anyone can read published missions"
ON public.missions_config FOR SELECT
USING (status = 'published');

-- Do the same for other config tables that are needed before fully logging in.
DROP POLICY IF EXISTS "Anyone can read published configs" ON public.game_configs;
CREATE POLICY "Anyone can read published configs"
ON public.game_configs FOR SELECT
USING (status = 'published');

DROP POLICY IF EXISTS "Anyone can read published pets" ON public.pets_config;
CREATE POLICY "Anyone can read published pets"
ON public.pets_config FOR SELECT
USING (status = 'published');

DROP POLICY IF EXISTS "Anyone can read published evolutions" ON public.pet_evolutions;
CREATE POLICY "Anyone can read published evolutions"
ON public.pet_evolutions FOR SELECT
USING (status = 'published');
