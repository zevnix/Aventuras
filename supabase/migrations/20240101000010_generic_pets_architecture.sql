-- 0010_generic_pets_architecture.sql
-- Upgrades the pets system to be fully generic and data-driven, removing hardcoded Kiro UUIDs.

-- 1. Add is_starter flag to pets_config to identify default pets without hardcoding IDs
ALTER TABLE public.pets_config ADD COLUMN IF NOT EXISTS is_starter BOOLEAN DEFAULT false;

-- 2. Mark Kiro as the current starter pet
UPDATE public.pets_config SET is_starter = true WHERE id = '11111111-1111-1111-1111-111111111111';

-- 3. Update the assign_initial_pet function to dynamically assign the starter pet
CREATE OR REPLACE FUNCTION public.assign_initial_pet()
RETURNS TRIGGER AS $$
DECLARE
  starter_pet_id UUID;
  starter_evolution_id UUID;
BEGIN
  -- Find the active starter pet
  SELECT id INTO starter_pet_id
  FROM public.pets_config
  WHERE is_starter = true AND status = 'published'
  LIMIT 1;

  -- Find the base evolution (lowest level required, e.g., Egg) for the starter pet
  IF starter_pet_id IS NOT NULL THEN
    SELECT id INTO starter_evolution_id
    FROM public.pet_evolutions
    WHERE pet_id = starter_pet_id AND status = 'published'
    ORDER BY level_required ASC
    LIMIT 1;

    NEW.current_pet_id := starter_pet_id;
    NEW.current_evolution_id := starter_evolution_id;
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;
