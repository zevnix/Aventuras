-- 0012_phase_3a_reusable_activities_data_driven.sql
-- Upgrades the ENUMs to support new activity types and adds JSONB data for dynamic mission rendering.

-- 1. Extend ENUMs using ALTER TYPE
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'math';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'story';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'riddle';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'word_game';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'memory';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'maze';
ALTER TYPE mission_type ADD VALUE IF NOT EXISTS 'science';

ALTER TYPE mission_category ADD VALUE IF NOT EXISTS 'science';

-- 2. Add JSONB content payload column for data-driven rendering of activities
ALTER TABLE public.missions_config ADD COLUMN IF NOT EXISTS content JSONB DEFAULT '{}'::jsonb;

-- Note: The UPDATE statement setting mission_type = 'math' has been moved to 0015
-- to prevent the PostgreSQL "unsafe use of new value" error within the same transaction block,
-- and to avoid using an explicit COMMIT; which breaks the Supabase CLI migration tracking.
