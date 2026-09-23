-- 0015_apply_math_mission.sql
-- In migration 0012 we introduced the 'math' enum and 'content' JSONB column.
-- Because of Supabase CLI constraints (implicit transactions), we could not use the newly created enum
-- value in an UPDATE within the same transaction without breaking migration history tracking via manual COMMITs.

-- This migration runs in its own transaction safely after 0012 has registered the enum.

UPDATE public.missions_config
SET
  mission_type = 'math',
  content = '{
    "question": "¿Cuánto es 2 + 3?",
    "options": ["4", "5", "6"],
    "correct_answer_index": 1
  }'::jsonb
WHERE title = 'Suma de Manzanas';
