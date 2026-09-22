-- 0003_vertical_slice.sql
-- Core schemas for Data-Driven Configuration, Player State, Missions and Economy Ledger

-- 1. DATA-DRIVEN CONFIGURATIONS (Read-only for clients, managed by Admin)
CREATE TABLE IF NOT EXISTS public.game_configs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  key TEXT UNIQUE NOT NULL,
  value JSONB NOT NULL,
  status content_status DEFAULT 'draft'
);

CREATE TABLE IF NOT EXISTS public.pets_config (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  description TEXT,
  base_asset_url TEXT,
  status content_status DEFAULT 'draft'
);

CREATE TABLE IF NOT EXISTS public.pet_evolutions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  pet_id UUID NOT NULL REFERENCES public.pets_config(id) ON DELETE CASCADE,
  stage_name TEXT NOT NULL,
  level_required INTEGER NOT NULL,
  asset_url TEXT,
  status content_status DEFAULT 'draft'
);

CREATE TABLE IF NOT EXISTS public.items_config (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  item_type TEXT NOT NULL, -- decor, skin, etc.
  rarity item_rarity DEFAULT 'common',
  price_coins INTEGER,
  price_gems INTEGER,
  status content_status DEFAULT 'draft'
);

CREATE TABLE IF NOT EXISTS public.missions_config (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title TEXT NOT NULL,
  description TEXT,
  category mission_category NOT NULL,
  mission_type mission_type NOT NULL,
  reward_xp INTEGER DEFAULT 0,
  reward_coins INTEGER DEFAULT 0,
  repeatable BOOLEAN DEFAULT false,
  cooldown_hours INTEGER DEFAULT 0,
  status content_status DEFAULT 'draft'
);

-- 1.5 Cleanup legacy mutable columns from 0001
ALTER TABLE public.children
  DROP COLUMN IF EXISTS xp,
  DROP COLUMN IF EXISTS coins,
  DROP COLUMN IF EXISTS gems;

-- 2. PLAYER STATE (Structured worlds, profiles, inventory)
CREATE TABLE IF NOT EXISTS public.player_profiles (
  child_id UUID PRIMARY KEY REFERENCES public.children(id) ON DELETE CASCADE,
  current_pet_id UUID REFERENCES public.pets_config(id),
  current_evolution_id UUID REFERENCES public.pet_evolutions(id),
  level INTEGER DEFAULT 1,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.world_zones (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  unlock_level INTEGER DEFAULT 1,
  status content_status DEFAULT 'draft'
);

CREATE TABLE IF NOT EXISTS public.player_unlocked_zones (
  child_id UUID NOT NULL REFERENCES public.children(id) ON DELETE CASCADE,
  zone_id UUID NOT NULL REFERENCES public.world_zones(id) ON DELETE CASCADE,
  unlocked_at TIMESTAMPTZ DEFAULT NOW(),
  PRIMARY KEY (child_id, zone_id)
);

CREATE TABLE IF NOT EXISTS public.player_houses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  child_id UUID UNIQUE NOT NULL REFERENCES public.children(id) ON DELETE CASCADE,
  layout_data JSONB DEFAULT '{}'::jsonb,
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.player_inventory (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  child_id UUID NOT NULL REFERENCES public.children(id) ON DELETE CASCADE,
  item_id UUID NOT NULL REFERENCES public.items_config(id) ON DELETE CASCADE,
  acquired_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. MISSIONS INSTANCES (Dynamic state for active missions)
CREATE TABLE IF NOT EXISTS public.mission_instances (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  child_id UUID NOT NULL REFERENCES public.children(id) ON DELETE CASCADE,
  mission_config_id UUID NOT NULL REFERENCES public.missions_config(id) ON DELETE CASCADE,
  status mission_state DEFAULT 'active',
  evidence_storage_path TEXT,
  adult_feedback TEXT,
  idempotency_key UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
  expires_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  completed_at TIMESTAMPTZ
);
CREATE INDEX idx_mission_instances_child_status ON public.mission_instances(child_id, status);

-- 4. ECONOMY LEDGER & TRANSACTIONS (Immutable, Server-Authoritative)
CREATE TABLE IF NOT EXISTS public.economy_ledger (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  child_id UUID NOT NULL REFERENCES public.children(id) ON DELETE CASCADE,
  currency currency_type NOT NULL,
  amount INTEGER NOT NULL, -- can be negative for spends
  tx_type transaction_type NOT NULL,
  reference_id UUID, -- links to mission_instance_id or purchase_id
  idempotency_key TEXT UNIQUE NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_economy_ledger_child ON public.economy_ledger(child_id, currency);

CREATE TABLE IF NOT EXISTS public.purchases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  child_id UUID NOT NULL REFERENCES public.children(id) ON DELETE CASCADE,
  item_id UUID REFERENCES public.items_config(id),
  platform TEXT NOT NULL, -- e.g., 'play_store'
  receipt_data TEXT NOT NULL,
  status TEXT NOT NULL,
  idempotency_key TEXT UNIQUE NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. AUDIT LOGS
CREATE TABLE IF NOT EXISTS public.audit_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  admin_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
  action TEXT NOT NULL,
  target_table TEXT NOT NULL,
  target_id UUID,
  old_data JSONB,
  new_data JSONB,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable RLS everywhere
ALTER TABLE public.game_configs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pets_config ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pet_evolutions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.items_config ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.missions_config ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.player_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.world_zones ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.player_unlocked_zones ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.player_houses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.player_inventory ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.mission_instances ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.economy_ledger ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.purchases ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;