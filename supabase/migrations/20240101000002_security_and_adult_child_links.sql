-- 0002_security_and_adult_child_links.sql
-- Enums and strictly controlled Adult-Child links

-- 1. Create global ENUMs
CREATE TYPE link_status AS ENUM ('pending', 'active', 'revoked');
CREATE TYPE transaction_type AS ENUM ('earn', 'spend', 'reward', 'purchase', 'refund', 'admin_adjustment');
CREATE TYPE currency_type AS ENUM ('xp', 'coins', 'gems');
CREATE TYPE content_status AS ENUM ('draft', 'published', 'scheduled', 'unpublished');
CREATE TYPE item_rarity AS ENUM ('common', 'rare', 'epic', 'legendary');
CREATE TYPE mission_category AS ENUM ('mind', 'creativity', 'real_world', 'family');
CREATE TYPE mission_type AS ENUM ('photo_evidence', 'quiz', 'digital_activity', 'parental_approval_only');
CREATE TYPE mission_state AS ENUM ('locked', 'active', 'pending_approval', 'completed', 'rejected');

-- Alter existing adult_child_links from 0001 to use the new ENUM and strictly enforce active state.
-- Since 0001 defined status as TEXT, we will alter it.
-- We MUST drop the policy from 0001 that relies on this column as TEXT before altering.
DROP POLICY IF EXISTS "Adults can view linked children" ON public.children;

ALTER TABLE public.adult_child_links DROP CONSTRAINT adult_child_links_status_check;
ALTER TABLE public.adult_child_links ALTER COLUMN status TYPE link_status USING status::link_status;

-- Recreate the policy with the new ENUM value ('active' instead of 'approved')
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

-- 2. Create link_requests for the strict invitation flow
CREATE TABLE IF NOT EXISTS public.link_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  adult_id UUID NOT NULL REFERENCES public.adults(id) ON DELETE CASCADE,
  child_id UUID NOT NULL REFERENCES public.children(id) ON DELETE CASCADE,
  temporary_code TEXT UNIQUE NOT NULL,
  status TEXT NOT NULL CHECK (status IN ('pending', 'confirmed', 'expired')),
  expires_at TIMESTAMPTZ NOT NULL,
  attempt_count INTEGER DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  confirmed_at TIMESTAMPTZ
);

CREATE INDEX idx_link_requests_code ON public.link_requests(temporary_code);
CREATE INDEX idx_link_requests_child_adult ON public.link_requests(child_id, adult_id);

-- Enable RLS on link_requests
ALTER TABLE public.link_requests ENABLE ROW LEVEL SECURITY;

-- 3. Concurrency Lock for Maximum 2 Adults limitation
-- We create a function that will be used as a trigger on link_requests and adult_child_links

CREATE OR REPLACE FUNCTION check_max_adults_per_child()
RETURNS TRIGGER AS $$
DECLARE
  active_and_pending_count INTEGER;
  child_uuid UUID;
BEGIN
  -- Determine child_id depending on the operation
  IF TG_OP = 'DELETE' THEN
    child_uuid := OLD.child_id;
  ELSE
    child_uuid := NEW.child_id;
  END IF;

  -- 1. Acquire transaction-level advisory lock using a hash of the child_id
  -- This ensures no two transactions can evaluate the limit for the same child simultaneously
  PERFORM pg_advisory_xact_lock(hashtext(child_uuid::text));

  -- 2. Calculate current links (active in adult_child_links OR pending in link_requests)
  SELECT (
    (SELECT COUNT(*) FROM public.adult_child_links WHERE child_id = child_uuid AND status IN ('pending', 'active'))
    +
    (SELECT COUNT(*) FROM public.link_requests WHERE child_id = child_uuid AND status = 'pending')
  ) INTO active_and_pending_count;

  -- 3. If inserting/updating and the limit is breached, abort.
  -- We allow an update if it doesn't increase the count (e.g. revoking a link)
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
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 4. Apply triggers
CREATE TRIGGER enforce_max_adults_links
  BEFORE INSERT OR UPDATE ON public.adult_child_links
  FOR EACH ROW EXECUTE FUNCTION check_max_adults_per_child();

CREATE TRIGGER enforce_max_adults_requests
  BEFORE INSERT OR UPDATE ON public.link_requests
  FOR EACH ROW EXECUTE FUNCTION check_max_adults_per_child();
