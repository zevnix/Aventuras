-- 0008_auth_triggers.sql
-- Handles automatic creation of profiles in public tables upon auth.users creation

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
DECLARE
  v_role TEXT;
  v_display_name TEXT;
  v_username TEXT;
BEGIN
  -- We extract metadata provided during signup
  v_role := NEW.raw_user_meta_data->>'role';
  v_display_name := NEW.raw_user_meta_data->>'display_name';
  v_username := NEW.raw_user_meta_data->>'username';

  -- Enforce strict role validation:
  -- Users signing up through the public API can ONLY be 'child' or 'adult'.
  -- Anyone attempting to pass 'admin' in metadata will be downgraded to 'child'.
  -- Actual admin accounts must be created/elevated manually by a superuser in the DB.
  IF v_role NOT IN ('child', 'adult') OR v_role IS NULL THEN
    v_role := 'child';
  END IF;

  IF v_display_name IS NULL THEN
    v_display_name := 'User';
  END IF;

  -- 1. Insert into public.users
  INSERT INTO public.users (id, role)
  VALUES (NEW.id, v_role);

  -- 2. Insert into specific profile tables
  IF v_role = 'adult' THEN
    INSERT INTO public.adults (id, display_name)
    VALUES (NEW.id, v_display_name);
  ELSIF v_role = 'child' THEN
    -- Generate a fallback username if none provided
    IF v_username IS NULL THEN
      v_username := 'User' || substr(NEW.id::text, 1, 6);
    END IF;

    INSERT INTO public.children (id, username, display_name)
    VALUES (NEW.id, v_username, v_display_name);

    -- Also create the initial player profile
    INSERT INTO public.player_profiles (child_id, level)
    VALUES (NEW.id, 1);

    -- Balance row is handled by trigger in 0006
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

-- Bind the trigger to auth.users
-- Note: In a fully managed Supabase environment this needs to be applied using
-- postgres role or superuser since auth schema is protected.
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_catalog.pg_namespace WHERE nspname = 'auth') THEN
    EXECUTE '
      DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
      CREATE TRIGGER on_auth_user_created
        AFTER INSERT ON auth.users
        FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
    ';
  END IF;
END $$;
