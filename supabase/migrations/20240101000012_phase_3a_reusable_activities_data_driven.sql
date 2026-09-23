-- 0012_phase_3a_reusable_activities_data_driven.sql
-- Upgrades the ENUMs to support new activity types and adds JSONB data for dynamic mission rendering.

-- 1. Add JSONB content payload column for data-driven rendering of activities
ALTER TABLE public.missions_config ADD COLUMN IF NOT EXISTS content JSONB DEFAULT '{}'::jsonb;

-- 2. Extend ENUMs using ALTER TYPE (Note: we cannot do this in a transaction block easily if used as a column, so we add new types individually)
-- Extend mission_type
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'math';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'story';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'riddle';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'word_game';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'memory';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'maze';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'science';

-- Extend mission_category
ALTER TYPE mission_category ADD VALUE IF NOT EXISTS 'science';

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
