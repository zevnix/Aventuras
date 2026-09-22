-- 0004_functions_and_policies.sql
-- Contains helper functions, RLS policies for strict boundaries, and safe RPCs.

-- 1. Admin Helper
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN AS $$
  SELECT EXISTS (
    SELECT 1 FROM public.users
    WHERE id = auth.uid()
      AND role = 'admin'
  );
$$ LANGUAGE sql SECURITY DEFINER STABLE;

-- 2. Configuration Tables RLS (Public read if published, Admin full access)
-- Applies to game_configs, pets_config, pet_evolutions, items_config, missions_config, world_zones

CREATE POLICY "Anyone can read published configs"
ON public.game_configs FOR SELECT USING (status = 'published' OR is_admin());
CREATE POLICY "Admins can manage game_configs"
ON public.game_configs USING (is_admin());

CREATE POLICY "Anyone can read published pets"
ON public.pets_config FOR SELECT USING (status = 'published' OR is_admin());
CREATE POLICY "Admins can manage pets_config"
ON public.pets_config USING (is_admin());

CREATE POLICY "Anyone can read published evolutions"
ON public.pet_evolutions FOR SELECT USING (status = 'published' OR is_admin());
CREATE POLICY "Admins can manage pet_evolutions"
ON public.pet_evolutions USING (is_admin());

CREATE POLICY "Anyone can read published items"
ON public.items_config FOR SELECT USING (status = 'published' OR is_admin());
CREATE POLICY "Admins can manage items_config"
ON public.items_config USING (is_admin());

CREATE POLICY "Anyone can read published missions"
ON public.missions_config FOR SELECT USING (status = 'published' OR is_admin());
CREATE POLICY "Admins can manage missions_config"
ON public.missions_config USING (is_admin());

CREATE POLICY "Anyone can read published zones"
ON public.world_zones FOR SELECT USING (status = 'published' OR is_admin());
CREATE POLICY "Admins can manage world_zones"
ON public.world_zones USING (is_admin());


-- 3. Security around link_requests and adult_child_links
-- link_requests: Adults can create. Child can view their own and update.
CREATE POLICY "Adults can insert pending requests"
ON public.link_requests FOR INSERT WITH CHECK (
  auth.uid() = adult_id AND status = 'pending'
);
CREATE POLICY "Children can view and update their requests"
ON public.link_requests FOR SELECT USING (auth.uid() = child_id);
CREATE POLICY "Children can confirm their requests"
ON public.link_requests FOR UPDATE USING (auth.uid() = child_id) WITH CHECK (status IN ('confirmed', 'expired'));
CREATE POLICY "Adults can view their sent requests"
ON public.link_requests FOR SELECT USING (auth.uid() = adult_id);

-- adult_child_links:
CREATE POLICY "Children and linked adults can view links"
ON public.adult_child_links FOR SELECT USING (
  auth.uid() = child_id OR auth.uid() = adult_id
);
CREATE POLICY "Only system or admin can insert links directly"
ON public.adult_child_links FOR INSERT WITH CHECK (is_admin());
CREATE POLICY "Users can only revoke links"
ON public.adult_child_links FOR UPDATE USING (auth.uid() = child_id OR auth.uid() = adult_id) WITH CHECK (status = 'revoked');
CREATE POLICY "No one can delete links"
ON public.adult_child_links FOR DELETE USING (false);


-- 4. Player State (Read for child + linked adults, Update only for child or Admin)
CREATE POLICY "Child and approved adults can view profile"
ON public.player_profiles FOR SELECT USING (
  auth.uid() = child_id OR EXISTS (
    SELECT 1 FROM public.adult_child_links
    WHERE adult_id = auth.uid() AND child_id = player_profiles.child_id AND status = 'active'
  )
);
CREATE POLICY "Child can update own profile"
ON public.player_profiles FOR UPDATE USING (auth.uid() = child_id);

CREATE POLICY "Child and approved adults can view houses"
ON public.player_houses FOR SELECT USING (
  auth.uid() = child_id OR EXISTS (
    SELECT 1 FROM public.adult_child_links
    WHERE adult_id = auth.uid() AND child_id = player_houses.child_id AND status = 'active'
  )
);
CREATE POLICY "Child can update own house"
ON public.player_houses FOR UPDATE USING (auth.uid() = child_id);

CREATE POLICY "Child and approved adults can view inventory"
ON public.player_inventory FOR SELECT USING (
  auth.uid() = child_id OR EXISTS (
    SELECT 1 FROM public.adult_child_links
    WHERE adult_id = auth.uid() AND child_id = player_inventory.child_id AND status = 'active'
  )
);
-- Inventory modification only by system RPC
CREATE POLICY "Inventory is immutable by client"
ON public.player_inventory FOR INSERT WITH CHECK (is_admin());
CREATE POLICY "Inventory is immutable by client update"
ON public.player_inventory FOR UPDATE USING (is_admin());


-- 5. Missions Instances
CREATE POLICY "Child and approved adults can view missions"
ON public.mission_instances FOR SELECT USING (
  auth.uid() = child_id OR EXISTS (
    SELECT 1 FROM public.adult_child_links
    WHERE adult_id = auth.uid() AND child_id = mission_instances.child_id AND status = 'active'
  )
);
CREATE POLICY "Child can update mission to pending approval and upload evidence"
ON public.mission_instances FOR UPDATE USING (auth.uid() = child_id) WITH CHECK (status = 'pending_approval');
-- Adults modifying mission instances is handled via RPC to ensure ledger is updated safely.


-- 6. Economy Ledger (Immutable)
CREATE POLICY "Child and approved adults can view ledger"
ON public.economy_ledger FOR SELECT USING (
  auth.uid() = child_id OR EXISTS (
    SELECT 1 FROM public.adult_child_links
    WHERE adult_id = auth.uid() AND child_id = economy_ledger.child_id AND status = 'active'
  )
);
-- No inserts, updates, or deletes allowed directly from clients
CREATE POLICY "Ledger is append-only by system"
ON public.economy_ledger FOR INSERT WITH CHECK (is_admin());
CREATE POLICY "Ledger is immutable"
ON public.economy_ledger FOR UPDATE USING (false);
CREATE POLICY "Ledger cannot be deleted"
ON public.economy_ledger FOR DELETE USING (false);


-- 7. RPC for approving/rejecting missions safely
CREATE OR REPLACE FUNCTION public.approve_mission(p_instance_id UUID, p_is_approved BOOLEAN, p_feedback TEXT DEFAULT NULL)
RETURNS BOOLEAN AS $$
DECLARE
  v_instance public.mission_instances%ROWTYPE;
  v_mission_config public.missions_config%ROWTYPE;
  v_caller_is_valid_adult BOOLEAN;
BEGIN
  -- 1. Fetch instance
  SELECT * INTO v_instance FROM public.mission_instances WHERE id = p_instance_id FOR UPDATE;
  IF NOT FOUND THEN
    RAISE EXCEPTION 'Mission instance not found.';
  END IF;

  -- 2. Validate caller is an active linked adult for this child
  SELECT EXISTS (
    SELECT 1 FROM public.adult_child_links
    WHERE adult_id = auth.uid()
      AND child_id = v_instance.child_id
      AND status = 'active'
  ) INTO v_caller_is_valid_adult;

  IF NOT v_caller_is_valid_adult AND NOT is_admin() THEN
    RAISE EXCEPTION 'Unauthorized to approve this mission.';
  END IF;

  -- 3. Check status
  IF v_instance.status != 'pending_approval' THEN
    RAISE EXCEPTION 'Mission is not in pending_approval state.';
  END IF;

  -- 4. Process approval or rejection
  IF p_is_approved THEN
    -- Fetch config for rewards
    SELECT * INTO v_mission_config FROM public.missions_config WHERE id = v_instance.mission_config_id;

    -- Insert Ledger entry for XP if applicable
    IF v_mission_config.reward_xp > 0 THEN
      INSERT INTO public.economy_ledger (child_id, currency, amount, tx_type, reference_id, idempotency_key)
      VALUES (v_instance.child_id, 'xp', v_mission_config.reward_xp, 'reward', v_instance.id, v_instance.idempotency_key::text || '-xp');
    END IF;

    -- Insert Ledger entry for Coins if applicable
    IF v_mission_config.reward_coins > 0 THEN
      INSERT INTO public.economy_ledger (child_id, currency, amount, tx_type, reference_id, idempotency_key)
      VALUES (v_instance.child_id, 'coins', v_mission_config.reward_coins, 'reward', v_instance.id, v_instance.idempotency_key::text || '-coins');
    END IF;

    -- Mark completed
    UPDATE public.mission_instances
    SET status = 'completed', adult_feedback = p_feedback, completed_at = NOW()
    WHERE id = p_instance_id;
  ELSE
    -- Mark rejected
    UPDATE public.mission_instances
    SET status = 'rejected', adult_feedback = p_feedback
    WHERE id = p_instance_id;
  END IF;

  RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;


-- 8. Storage Policy setup (Assuming a bucket 'mission_evidence' exists)
-- (We cannot create the bucket in pure SQL easily without inserting into storage.buckets directly,
-- but we define the policies assuming it exists).
-- Note: Requires Supabase Storage schema to be available. We wrap it in a DO block to prevent
-- failure if storage schema is not initialized in the local dev environment yet.
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_catalog.pg_namespace WHERE nspname = 'storage') THEN

    -- Child insert own evidence
    EXECUTE 'CREATE POLICY "Child insert evidence" ON storage.objects FOR INSERT WITH CHECK (bucket_id = ''mission_evidence'' AND auth.uid()::text = (string_to_array(name, ''/''))[1])';

    -- Child read own evidence
    EXECUTE 'CREATE POLICY "Child read own evidence" ON storage.objects FOR SELECT USING (bucket_id = ''mission_evidence'' AND auth.uid()::text = (string_to_array(name, ''/''))[1])';

    -- Adult read linked child evidence
    EXECUTE 'CREATE POLICY "Adult read linked evidence" ON storage.objects FOR SELECT USING (
      bucket_id = ''mission_evidence'' AND EXISTS (
        SELECT 1 FROM public.adult_child_links
        WHERE adult_id = auth.uid() AND child_id::text = (string_to_array(name, ''/''))[1] AND status = ''active''
      )
    )';

  END IF;
END $$;
