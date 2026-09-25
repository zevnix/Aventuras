-- 0019_add_theme_config.sql
-- Adds the data-driven theme configuration to game_configs so the Android client can dynamically style the world.

INSERT INTO public.game_configs (key, value, status)
VALUES (
  'active_theme',
  '{
    "parallax_sky_top": "#81D4FA",
    "parallax_sky_bottom": "#B3E5FC",
    "parallax_ground_top": "#4CAF50",
    "parallax_ground_bottom": "#1B5E20",
    "board_color": "#5D4037",
    "board_accent": "#8D6E63",
    "button_primary": "#43A047",
    "button_primary_accent": "#A5D6A7"
  }'::jsonb,
  'published'
) ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value;
