-- Initial schema for children, adults and relationships

-- Users table extension (conceptually handled by Supabase Auth, but we can store extra metadata here)
CREATE TABLE IF NOT EXISTS public.users (
  id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  role TEXT NOT NULL CHECK (role IN ('child', 'adult', 'admin')),
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Children profile table
CREATE TABLE IF NOT EXISTS public.children (
  id UUID PRIMARY KEY REFERENCES public.users(id) ON DELETE CASCADE,
  username TEXT UNIQUE NOT NULL,
  display_name TEXT NOT NULL,
  date_of_birth DATE,
  pin_hash TEXT,
  level INTEGER DEFAULT 1,
  xp INTEGER DEFAULT 0,
  coins INTEGER DEFAULT 0,
  gems INTEGER DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Adults profile table
CREATE TABLE IF NOT EXISTS public.adults (
  id UUID PRIMARY KEY REFERENCES public.users(id) ON DELETE CASCADE,
  display_name TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Adult-Child links
CREATE TABLE IF NOT EXISTS public.adult_child_links (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  adult_id UUID NOT NULL REFERENCES public.adults(id) ON DELETE CASCADE,
  child_id UUID NOT NULL REFERENCES public.children(id) ON DELETE CASCADE,
  status TEXT NOT NULL CHECK (status IN ('pending', 'approved', 'revoked')),
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE (adult_id, child_id)
);

-- Row Level Security (RLS) setup
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.children ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.adults ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.adult_child_links ENABLE ROW LEVEL SECURITY;

-- Basic Policies (example placeholders to respect the document)
-- In a real scenario, we'd check JWT claims or use helper functions.

-- Children can only read their own profile
CREATE POLICY "Children can view own profile"
ON public.children FOR SELECT
USING (auth.uid() = id);

-- Adults can read profiles of linked children
CREATE POLICY "Adults can view linked children"
ON public.children FOR SELECT
USING (
  EXISTS (
    SELECT 1 FROM public.adult_child_links
    WHERE adult_id = auth.uid()
      AND child_id = children.id
      AND status = 'approved'
  )
);
