-- 0007_identity_rls_fixes.sql

-- 1. Fix the legacy 'approved' string in the RLS policies from 0001
DROP POLICY IF EXISTS "Adults can view linked children" ON public.children;

CREATE POLICY "Adults can view linked children"
ON public.children FOR SELECT
USING (
  EXISTS (
    SELECT 1 FROM public.adult_child_links
    WHERE adult_id = auth.uid()
      AND child_id = children.id
      AND status = 'active'
  )
);

-- 2. Create a secure RPC for adults to lookup a child ID by username
-- This is necessary because RLS prevents adults from querying the children table directly
-- before they have a confirmed link.
CREATE OR REPLACE FUNCTION public.get_child_id_by_username(p_username TEXT)
RETURNS UUID AS $$
DECLARE
  v_child_id UUID;
BEGIN
  SELECT id INTO v_child_id FROM public.children WHERE username = p_username;
  RETURN v_child_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;
