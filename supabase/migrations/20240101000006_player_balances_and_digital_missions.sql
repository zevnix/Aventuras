-- 0006_player_balances_and_digital_missions.sql
-- Implements materialized balances for quick reads and support for digital mission autocompletion.

CREATE TABLE IF NOT EXISTS public.player_balances (
  child_id UUID PRIMARY KEY REFERENCES public.children(id) ON DELETE CASCADE,
  xp_balance INTEGER DEFAULT 0,
  coins_balance INTEGER DEFAULT 0,
  gems_balance INTEGER DEFAULT 0,
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.player_balances ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Child and approved adults can view balance"
ON public.player_balances FOR SELECT USING (
  auth.uid() = child_id OR EXISTS (
    SELECT 1 FROM public.adult_child_links
    WHERE adult_id = auth.uid() AND child_id = player_balances.child_id AND status = 'active'
  )
);
CREATE POLICY "Clients cannot update balances directly"
ON public.player_balances FOR UPDATE USING (false);
CREATE POLICY "Clients cannot insert balances directly"
ON public.player_balances FOR INSERT WITH CHECK (false);

CREATE OR REPLACE FUNCTION public.create_player_balance_on_profile()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.player_balances (child_id) VALUES (NEW.child_id) ON CONFLICT DO NOTHING;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

CREATE TRIGGER on_player_profile_created
  AFTER INSERT ON public.player_profiles
  FOR EACH ROW EXECUTE FUNCTION public.create_player_balance_on_profile();

CREATE OR REPLACE FUNCTION public.update_balance_from_ledger()
RETURNS TRIGGER AS $$
BEGIN
  IF NEW.currency = 'xp' THEN
    INSERT INTO public.player_balances (child_id, xp_balance)
    VALUES (NEW.child_id, NEW.amount)
    ON CONFLICT (child_id) DO UPDATE SET xp_balance = public.player_balances.xp_balance + NEW.amount, updated_at = NOW();
  ELSIF NEW.currency = 'coins' THEN
    INSERT INTO public.player_balances (child_id, coins_balance)
    VALUES (NEW.child_id, NEW.amount)
    ON CONFLICT (child_id) DO UPDATE SET coins_balance = public.player_balances.coins_balance + NEW.amount, updated_at = NOW();
  ELSIF NEW.currency = 'gems' THEN
    INSERT INTO public.player_balances (child_id, gems_balance)
    VALUES (NEW.child_id, NEW.amount)
    ON CONFLICT (child_id) DO UPDATE SET gems_balance = public.player_balances.gems_balance + NEW.amount, updated_at = NOW();
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

CREATE TRIGGER on_economy_ledger_insert
  AFTER INSERT ON public.economy_ledger
  FOR EACH ROW EXECUTE FUNCTION public.update_balance_from_ledger();

CREATE OR REPLACE FUNCTION public.complete_digital_mission(p_instance_id UUID)
RETURNS BOOLEAN AS $$
DECLARE
  v_instance public.mission_instances%ROWTYPE;
  v_mission_config public.missions_config%ROWTYPE;
BEGIN
  SELECT * INTO v_instance FROM public.mission_instances WHERE id = p_instance_id FOR UPDATE;
  IF NOT FOUND THEN
    RAISE EXCEPTION 'Mission instance not found.';
  END IF;

  IF v_instance.child_id != auth.uid() THEN
    RAISE EXCEPTION 'Unauthorized. Only the owner can complete this mission.';
  END IF;

  IF v_instance.status != 'active' THEN
    RAISE EXCEPTION 'Mission is not in an active state.';
  END IF;

  SELECT * INTO v_mission_config FROM public.missions_config WHERE id = v_instance.mission_config_id;

  IF v_mission_config.mission_type = 'photo_evidence' OR v_mission_config.mission_type = 'parental_approval_only' THEN
    RAISE EXCEPTION 'This mission type requires adult approval and cannot be auto-completed.';
  END IF;

  IF v_mission_config.reward_xp > 0 THEN
    INSERT INTO public.economy_ledger (child_id, currency, amount, tx_type, reference_id, idempotency_key)
    VALUES (v_instance.child_id, 'xp', v_mission_config.reward_xp, 'reward', v_instance.id, v_instance.idempotency_key::text || '-xp');
  END IF;

  IF v_mission_config.reward_coins > 0 THEN
    INSERT INTO public.economy_ledger (child_id, currency, amount, tx_type, reference_id, idempotency_key)
    VALUES (v_instance.child_id, 'coins', v_mission_config.reward_coins, 'reward', v_instance.id, v_instance.idempotency_key::text || '-coins');
  END IF;

  UPDATE public.mission_instances
  SET status = 'completed', completed_at = NOW()
  WHERE id = p_instance_id;

  RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;
