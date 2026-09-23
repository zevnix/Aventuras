-- 0013_fix_mission_instances_insert_policy.sql
-- Adds missing RLS policy for clients to safely insert new mission instances

CREATE POLICY "Child can create active mission instances"
ON public.mission_instances FOR INSERT
WITH CHECK (
  auth.uid() = child_id
  AND status = 'active'
);
