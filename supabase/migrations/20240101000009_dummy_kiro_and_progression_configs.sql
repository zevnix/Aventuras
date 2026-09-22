-- 0009_dummy_kiro_and_progression_configs.sql
-- Injects foundational dummy data for the Vertical Slice to test Home Screen & Progress.
-- ALL PRODUCT DECISIONS (CURVE, ASSETS, LEVELS) ARE PENDING AND CONFIGURABLE.

-- 1. Insert Kiro into pets_config
INSERT INTO public.pets_config (id, name, description, base_asset_url, status)
VALUES (
  '11111111-1111-1111-1111-111111111111',
  'Kiro',
  'A brave little dragon ready for adventures.',
  'https://placeholder.com/kiro_base.png',
  'published'
) ON CONFLICT DO NOTHING;

-- 2. Insert Pet Evolutions for Kiro (Huevo, Bebé, Joven)
INSERT INTO public.pet_evolutions (id, pet_id, stage_name, level_required, asset_url, status)
VALUES
(
  '22222222-2222-2222-2222-222222222221',
  '11111111-1111-1111-1111-111111111111',
  'Huevo',
  1,
  'https://placeholder.com/kiro_egg.png',
  'published'
),
(
  '22222222-2222-2222-2222-222222222222',
  '11111111-1111-1111-1111-111111111111',
  'Bebé',
  2,
  'https://placeholder.com/kiro_baby.png',
  'published'
),
(
  '22222222-2222-2222-2222-222222222223',
  '11111111-1111-1111-1111-111111111111',
  'Joven',
  5,
  'https://placeholder.com/kiro_young.png',
  'published'
) ON CONFLICT DO NOTHING;

-- 3. Insert Progression Configuration (level_thresholds)
-- This determines the visual bar filling on Android. True level is server-authoritative.
INSERT INTO public.game_configs (key, value, status)
VALUES (
  'level_thresholds',
  '[
    {"level": 1, "xp_required": 0},
    {"level": 2, "xp_required": 100},
    {"level": 3, "xp_required": 250},
    {"level": 4, "xp_required": 500},
    {"level": 5, "xp_required": 1000}
  ]'::jsonb,
  'published'
) ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value;

-- 4. Automatically assign Kiro (Egg form) to newly created child profiles
CREATE OR REPLACE FUNCTION public.assign_initial_pet()
RETURNS TRIGGER AS $$
BEGIN
  -- Assign Kiro egg (level 1) by default
  NEW.current_pet_id := '11111111-1111-1111-1111-111111111111';
  NEW.current_evolution_id := '22222222-2222-2222-2222-222222222221';
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

-- Drop and recreate the trigger to run BEFORE insert so we can modify the NEW record
DROP TRIGGER IF EXISTS on_player_profile_created_assign_pet ON public.player_profiles;
CREATE TRIGGER on_player_profile_created_assign_pet
  BEFORE INSERT ON public.player_profiles
  FOR EACH ROW EXECUTE FUNCTION public.assign_initial_pet();
