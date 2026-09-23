-- 0011_phase_3a_reusable_activities.sql
-- Injects foundational data for the Reusable Activities System (Phase 3A)
-- Replaces mock missions with actual dynamic content from the database.

-- 1. Insert Initial Reusable Activities (Missions)
INSERT INTO public.missions_config (id, title, description, category, mission_type, reward_xp, reward_coins, repeatable, cooldown_hours, status)
VALUES
(
  '33333333-3333-3333-3333-333333333331',
  'Suma de Manzanas',
  'Resuelve este pequeño puzzle matemático.',
  'mind',
  'quiz', -- Changed from 'digital_quiz' to 'quiz' to match existing Enum in 0002
  30,
  10,
  true,
  1,
  'published'
),
(
  '33333333-3333-3333-3333-333333333332',
  'Detective de colores',
  'Encuentra 3 cosas rojas en casa y tómales una foto.',
  'real_world',
  'photo_evidence',
  50,
  20,
  true,
  24,
  'published'
),
(
  '33333333-3333-3333-3333-333333333333',
  'Dibuja a tu mascota',
  'Usa tus lápices para hacer un retrato de tu mascota.',
  'creativity',
  'photo_evidence',
  60,
  25,
  true,
  24,
  'published'
) ON CONFLICT DO NOTHING;

-- 2. Optional: Seed Active Mission Instances for existing children (or rely on a mechanic to assign them)
-- For now, we will let the Android client query active missions or pick from available configs.
