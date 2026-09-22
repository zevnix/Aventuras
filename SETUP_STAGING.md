# Guía de Configuración Supabase (Staging)

Esta guía documenta los pasos exactos y variables de entorno necesarias para conectar el Vertical Slice de "Project Game" a un proyecto real de Supabase.

## 1. Configuración de Base de Datos (Migraciones)
El proyecto utiliza 9 archivos SQL de migración secuencial. En una instancia limpia de Supabase, debes aplicar las migraciones estrictamente en este orden:

1. `0001_initial_schema.sql` (Esquema base de perfiles y usuarios)
2. `0002_security_and_adult_child_links.sql` (Enums, solicitudes de vínculo temporal y constraints concurrentes)
3. `0003_vertical_slice.sql` (Esquemas Data-driven, Misiones, Ledger Inmutable)
4. `0004_functions_and_policies.sql` (Función `is_admin`, RPC `approve_mission`, y setup RLS)
5. `0005_audit_fixes_and_security_enhancements.sql` (Endurecimiento de RLS, RPC de evidencias y control del rol Admin)
6. `0006_player_balances_and_digital_missions.sql` (Vista materializada de XP/Coins/Gems y completado digital)
7. `0007_identity_rls_fixes.sql` (RPC de búsqueda de usuario por username)
8. `0008_auth_triggers.sql` (Creación automática de perfiles públicos vía Supabase Auth)
9. `0009_dummy_kiro_and_progression_configs.sql` (Metadatos visuales "Kiro", configuraciones PENDING de thresholds).

*Nota:* Asegúrate de correr la migración `0008` con permisos de *superuser* (`postgres`), ya que añade un trigger sobre la tabla protegida `auth.users`.

## 2. Configuración de Storage
Debes ir al dashboard de Supabase (Sección "Storage") y crear **dos buckets**:
- `assets` (Configurado como **PÚBLICO**). (Para fotos de Kiro y skins).
- `mission_evidence` (Configurado como **PRIVADO**).

Las políticas de seguridad (RLS) para proteger los accesos a los menores ya se crearán automáticamente durante la migración `0004`.

## 3. Variables de Entorno

### Panel Web (Next.js - `.env.local`)
Crea un archivo `.env.local` en la carpeta `web-admin` con:
```env
NEXT_PUBLIC_SUPABASE_URL=https://tu-proyecto.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=eyJhbG... (Tu clave anon/publica)
```
*(Recuerda NUNCA usar la clave `service_role` en el `.env.local` público de Next.js).*

### Cliente Android (`local.properties`)
Abre el archivo `android-client/local.properties` (no lo comitees a git) y añade:
```properties
SUPABASE_URL="https://tu-proyecto.supabase.co"
SUPABASE_KEY="eyJhbG..."
```
El archivo `build.gradle.kts` se encargará de inyectar estas claves en el objeto `BuildConfig` para que la aplicación las consuma en el `SupabaseModule.kt`.

## 4. Validaciones Pendientes
Una vez levantado el entorno y configurados los endpoints, debemos proceder a ejecutar la suite de Validaciones E2E indicadas en el Documento Maestro (Secciones: Identidad, Vinculación, Seguridad, Home y Flujos Base).
