-- 0012_phase_3a_reusable_activities_data_driven.sql
-- Upgrades the ENUMs to support new activity types and adds JSONB data for dynamic mission rendering.

-- Note: Because Supabase CLI wraps each migration in a BEGIN/COMMIT block by default,
-- updating the column to use the newly created enum value within the *same* migration block
-- can cause the "unsafe use of new value" error.
-- We must explicitly commit the current transaction before adding and using the new values.
COMMIT;

-- 1. Extend ENUMs using ALTER TYPE
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'math';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'story';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'riddle';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'word_game';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'memory';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'maze';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'science';

ALTER TYPE mission_category ADD VALUE IF NOT EXISTS 'science';

-- Now start a new transaction block for the rest of the changes
BEGIN;

-- 2. Add JSONB content payload column for data-driven rendering of activities
ALTER TABLE public.missions_config ADD COLUMN IF NOT EXISTS content JSONB DEFAULT '{}'::jsonb;

-- 3. Update the existing mock mission to be fully data-driven math mission
UPDATE public.missions_config
SET
  mission_type = 'math',
  content = '{
    "question": "¿Cuánto es 2 + 3?",
    "options": ["4", "5", "6"],
    "correct_answer_index": 1
  }'::jsonb
WHERE title = 'Suma de Manzanas';

-- The Supabase CLI will issue a COMMIT at the end of the file.
