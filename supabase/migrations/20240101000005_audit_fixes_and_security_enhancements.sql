-- 0005_audit_fixes_and_security_enhancements.sql
-- Fixes and enhancements discovered during the technical audit of the Vertical Slice foundation.

-- =====================================================================================
-- 1. SECURITY DEFINER SEARCH PATH FIXES
-- =====================================================================================
-- Recreate existing functions enforcing a safe search_path to prevent malicious overrides.

CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN AS $$
  SELECT EXISTS (
    SELECT 1 FROM public.users
    WHERE id = auth.uid()
      AND role = 'admin'
  );
$$ LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public;

CREATE OR REPLACE FUNCTION public.check_max_adults_per_child()
RETURNS TRIGGER AS $$
DECLARE
  active_and_pending_count INTEGER;
  child_uuid UUID;
BEGIN
  IF TG_OP = 'DELETE' THEN
    child_uuid := OLD.child_id;
  ELSE
    child_uuid := NEW.child_id;
  END IF;

  PERFORM pg_advisory_xact_lock(hashtext(child_uuid::text));

  -- ONLY count link_requests that are strictly 'pending' AND not expired
  SELECT (
    (SELECT COUNT(*) FROM public.adult_child_links WHERE child_id = child_uuid AND status IN ('pending', 'active'))
    +
    (SELECT COUNT(*) FROM public.link_requests WHERE child_id = child_uuid AND status = 'pending' AND expires_at > NOW())
  ) INTO active_and_pending_count;

  IF TG_OP IN ('INSERT', 'UPDATE') THEN
    IF TG_TABLE_NAME = 'adult_child_links' AND NEW.status IN ('pending', 'active') THEN
      IF TG_OP = 'UPDATE' AND OLD.status IN ('pending', 'active') THEN
        -- Status was already counting towards limit, no net increase.
      ELSIF active_and_pending_count >= 2 THEN
        RAISE EXCEPTION 'A child cannot have more than 2 active or pending adults.';
      END IF;
    ELSIF TG_TABLE_NAME = 'link_requests' AND NEW.status = 'pending' THEN
       IF TG_OP = 'UPDATE' AND OLD.status = 'pending' THEN
        -- No net increase
       ELSIF active_and_pending_count >= 2 THEN
         RAISE EXCEPTION 'A child cannot have more than 2 active or pending adults.';
       END IF;
    END IF;
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

CREATE OR REPLACE FUNCTION public.approve_mission(p_instance_id UUID, p_is_approved BOOLEAN, p_feedback TEXT DEFAULT NULL)
RETURNS BOOLEAN AS $$
DECLARE
  v_instance public.mission_instances%ROWTYPE;
  v_mission_config public.missions_config%ROWTYPE;
  v_caller_is_valid_adult BOOLEAN;
BEGIN
  SELECT * INTO v_instance FROM public.mission_instances WHERE id = p_instance_id FOR UPDATE;
  IF NOT FOUND THEN
    RAISE EXCEPTION 'Mission instance not found.';
  END IF;

  SELECT EXISTS (
    SELECT 1 FROM public.adult_child_links
    WHERE adult_id = auth.uid()
      AND child_id = v_instance.child_id
      AND status = 'active'
  ) INTO v_caller_is_valid_adult;

  IF NOT v_caller_is_valid_adult AND NOT public.is_admin() THEN
    RAISE EXCEPTION 'Unauthorized to approve this mission.';
  END IF;

  IF v_instance.status != 'pending_approval' THEN
    RAISE EXCEPTION 'Mission is not in pending_approval state.';
  END IF;

  IF p_is_approved THEN
    SELECT * INTO v_mission_config FROM public.missions_config WHERE id = v_instance.mission_config_id;

    IF v_mission_config.reward_xp > 0 THEN
      INSERT INTO public.economy_ledger (child_id, currency, amount, tx_type, reference_id, idempotency_key)
      VALUES (v_instance.child_id, 'xp', v_mission_config.reward_xp, 'reward', v_instance.id, v_instance.idempotency_key::text || '-xp');
    END IF;

    IF v_mission_config.reward_coins > 0 THEN
      INSERT INTO public.economy_ledger (child_id, currency, amount, tx_type, reference_id, idempotency_key)
      VALUES (v_instance.child_id, 'coins', v_mission_config.reward_coins, 'reward', v_instance.id, v_instance.idempotency_key::text || '-coins');
    END IF;

    UPDATE public.mission_instances
    SET status = 'completed', adult_feedback = p_feedback, completed_at = NOW()
    WHERE id = p_instance_id;
  ELSE
    UPDATE public.mission_instances
    SET status = 'rejected', adult_feedback = p_feedback
    WHERE id = p_instance_id;
  END IF;

  RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

-- =====================================================================================
-- 2. USERS TABLE RLS PROTECTION
-- =====================================================================================
-- Prevent standard users from inserting 'admin' role directly into public.users.
DROP POLICY IF EXISTS "Users can insert their own record" ON public.users;
DROP POLICY IF EXISTS "Users can read all users" ON public.users;

-- Admin can do anything
CREATE POLICY "Admins have full access to users"
ON public.users USING (public.is_admin());

-- Users can only insert themselves with 'child' or 'adult' role. Admin creation must be handled via DB root.
CREATE POLICY "Users can insert non-admin profile"
ON public.users FOR INSERT WITH CHECK (
  auth.uid() = id AND role IN ('child', 'adult')
);

CREATE POLICY "Users can read own record"
ON public.users FOR SELECT USING (auth.uid() = id);

-- =====================================================================================
-- 3. LINK_REQUESTS SAFE CONFIRMATION (RPC instead of direct UPDATE)
-- =====================================================================================
-- We remove the direct UPDATE policy from children to prevent arbitrary data manipulation.
DROP POLICY IF EXISTS "Children can confirm their requests" ON public.link_requests;

CREATE POLICY "Children can only revoke own requests directly"
ON public.link_requests FOR UPDATE USING (auth.uid() = child_id) WITH CHECK (status = 'expired');

CREATE OR REPLACE FUNCTION public.confirm_link_request(p_code TEXT)
RETURNS BOOLEAN AS $$
DECLARE
  v_request public.link_requests%ROWTYPE;
BEGIN
  -- 1. Find and lock the pending request
  SELECT * INTO v_request FROM public.link_requests
  WHERE temporary_code = p_code AND status = 'pending' FOR UPDATE;

  IF NOT FOUND THEN
    RAISE EXCEPTION 'Invalid or already processed link request code.';
  END IF;

  IF v_request.expires_at < NOW() THEN
    UPDATE public.link_requests SET status = 'expired' WHERE id = v_request.id;
    RAISE EXCEPTION 'This link request has expired.';
  END IF;

  -- 2. Validate the caller IS the targeted child
  IF v_request.child_id != auth.uid() THEN
    RAISE EXCEPTION 'Only the target child can confirm this request.';
  END IF;

  -- 3. Mark request as confirmed
  UPDATE public.link_requests
  SET status = 'confirmed', confirmed_at = NOW()
  WHERE id = v_request.id;

  -- 4. Safely insert the adult_child_links record
  INSERT INTO public.adult_child_links (adult_id, child_id, status)
  VALUES (v_request.adult_id, v_request.child_id, 'active')
  ON CONFLICT (adult_id, child_id) DO UPDATE SET status = 'active', updated_at = NOW();

  RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

-- =====================================================================================
-- 4. MISSION EVIDENCES SAFE SUBMISSION (RPC instead of direct UPDATE)
-- =====================================================================================
-- Direct UPDATE policy opened up potential modifications to idempotency keys or references.
DROP POLICY IF EXISTS "Child can update mission to pending approval and upload evidence" ON public.mission_instances;

CREATE OR REPLACE FUNCTION public.submit_mission_evidence(p_instance_id UUID, p_storage_path TEXT)
RETURNS BOOLEAN AS $$
DECLARE
  v_instance public.mission_instances%ROWTYPE;
BEGIN
  SELECT * INTO v_instance FROM public.mission_instances WHERE id = p_instance_id FOR UPDATE;
  IF NOT FOUND THEN
    RAISE EXCEPTION 'Mission instance not found.';
  END IF;

  IF v_instance.child_id != auth.uid() THEN
    RAISE EXCEPTION 'Unauthorized.';
  END IF;

  IF v_instance.status NOT IN ('active', 'rejected') THEN
    RAISE EXCEPTION 'Mission cannot accept evidence in its current state.';
  END IF;

  UPDATE public.mission_instances
  SET status = 'pending_approval', evidence_storage_path = p_storage_path
  WHERE id = p_instance_id;

  RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;
