# PROJECT_GAME (Aventuras Educativas)

Este repositorio contiene el ecosistema completo del proyecto "Aventuras Educativas" (nombre comercial pendiente). Es un juego de aventura infantil data-driven, de arquitectura server-authoritative, en el que las actividades reales generan progreso en el mundo virtual.

## Estructura del Repositorio

El proyecto se divide en las siguientes áreas:

*   **`/android-client`**: La aplicación para niños, desarrollada en Kotlin con Jetpack Compose.
*   **`/web-admin`**: El panel administrativo y Landing Page, desarrollado en Next.js y React.
*   **`/supabase`**: Configuración y migraciones de la base de datos PostgreSQL, diseñada con políticas estrictas de Row Level Security (RLS).

## Cómo visualizar el estado actual

Dado que estamos en la Fase 1 (Fundación), los entornos son un punto de partida vacío para demostrar que las herramientas compilan correctamente.

### 1. Panel Administrativo Web (Next.js)

1.  Asegúrate de tener Node.js instalado (v18 o superior).
2.  Abre un terminal y dirígete al directorio web:
    \`cd web-admin\`
3.  Instala las dependencias necesarias:
    \`npm install\`
4.  Levanta el servidor local en modo desarrollo:
    \`npm run dev & \`
5.  Abre [http://localhost:3000](http://localhost:3000) en tu navegador para ver la página por defecto.

### 2. Aplicación Infantil (Android / Jetpack Compose)

Para visualizar la base de la aplicación de Android:

1.  Descarga e instala **Android Studio**.
2.  Desde Android Studio, selecciona "Open" u "Open Project".
3.  Navega hasta la carpeta raíz de este repositorio y **selecciona específicamente la carpeta `/android-client`**.
4.  Espera a que Gradle sincronice el proyecto.
5.  Puedes previsualizar los componentes Compose abriendo el archivo `MainActivity.kt` o correr la aplicación pulsando el botón "Run" (si tienes un emulador configurado o un dispositivo físico conectado).

## FASE 2: Identidad y Vinculación

- **App Android (Niño)**: Configurado Jetpack Compose y Supabase Client (`auth-kt`, `postgrest-kt`). Creadas pantallas `ChildAuthScreen` y `ChildLinkScreen` (para el flujo de vinculación RPC).
- **Panel Web (Adulto)**: Creadas páginas Next.js App Router para el Login (email/password provisional) y el Dashboard administrativo del padre. Se incluyen Server Actions para iniciar la petición de vinculación temporal (creando el código de 6 dígitos que expira en 1h).

*Pendiente/Configurable*: Integración real con Google Sign-In para adultos y el sistema de PIN seguro para niños están listos en arquitectura pero requieren despliegue real en Supabase Dashboard.

### Verificación y Auditoría Fase 2 (Estado: En Progreso)

Según la arquitectura macro validada, se completó la programación de los clientes y el despliegue del entorno base:

- Implementado: ✅
- Revisado estáticamente: ✅ (Se confirmó que los clientes Android y Next.js no emiten \`service_role\`, usan Supabase RLS y delegan cálculos de la economía/inventario al PostgreSQL RPC).
- Compilación: ✅ (Tanto Gradle Compose como Next.js App Router compilan limpio sin fallas de memoria o errores estructurales).
- Tests ejecutados: \`ChildAuthViewModelTest\` en Android (validación de estados sin credenciales).
- Tests pendientes de entorno Supabase real (Staging): Login E2E, Persistencia de sesión, Generación y caducidad de códigos de \`link_requests\` de forma síncrona en BD, y Verificación final de la inmutabilidad de \`player_balances\`.

### Verificación y Auditoría Fase 3 (Motor del Juego)

Se ha completado el diseño arquitectónico de la Fase 3, donde Android funciona estrictamente como un visor del Single Source of Truth local alimentado por la autoridad remota.

- Implementado: ✅ (Room Entities, PlayerDao, PlayerRepository, HomeViewModel y HomeScreen).
- Revisado estáticamente: ✅
- Compilación: PENDING (Debido a incidencias HTTP 429 con Maven Central al descargar KSP/Kapt para Room).
- Tests ejecutados: \`HomeViewModelTest\` validando que el % de progreso respeta el Nivel otorgado por el backend y los saltos de XP.
- Tests PENDING de entorno Supabase / Compilación limpia: Room Flow Data Emission, Evolución por Asset URL y Sync Network Error Handling.

### Cierre Provisional: Fase 3 (Motor del Juego)
El motor de juego principal se ha completado a nivel de arquitectura y código local en Android.

#### Estado de Cierre:
*   **Implementado:** ✅ UI (Compose), Lógica visual de progreso (ViewModel), Capa de red (Repository) y configuración SQL de BBDD (`0009`).
*   **Revisado estáticamente:** ✅ Confirmado que Android no puede mutar datos de la economía, operando exclusivamente como un visor pasivo sincronizado por Supabase.
*   **Tests ejecutados:** ✅ Tests del ViewModel determinando correctamente el porcentaje de progreso de acuerdo a límites estrictos remotos.
*   **Bloqueado por infraestructura:** 🔴 Compilación del procesador de anotaciones (KSP/Kapt) de **Room** debido a límites de velocidad (`HTTP 429`) en Maven Central desde el sandbox. El DAO permanece como una interfaz limpia lista para habilitarse.
*   **Pendiente de Supabase staging/E2E:** 🟡 Comprobación End-to-End del flujo de `Repository.syncProfileData()` hidratando el ViewModel desde un backend real.


### Validación Final (Preparación para Staging)

A continuación, se detalla el estado actual de las características y pruebas antes de conectarlas a un entorno de **Supabase Staging**:

| Componente | Implementado | Compilado | Test Local | Test E2E (Staging) | Estado Final |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **BBDD (Migraciones 1 al 9)** | ✅ | ✅ | N/A | ⏳ Pendiente | Listo para Staging |
| **Android: KSP/Room** | ✅ | 🔴 Bloqueado | ✅ (Mocks) | ⏳ Pendiente | 🔴 Bloqueado (Maven 429) |
| **Android: Config. de Entorno** | ✅ | ✅ | N/A | ⏳ Pendiente | Listo para Staging |
| **Next.js Admin** | ✅ | ✅ | N/A | ⏳ Pendiente | Listo para Staging |
| **Identidad (Flujo de Vínculo)** | ✅ | ✅ | ✅ | ⏳ Pendiente | Listo para Staging |
| **Home (Progresión Visual)** | ✅ | ✅ | ✅ | ⏳ Pendiente | Listo para Staging |

*Consulta `SETUP_STAGING.md` para las instrucciones de inyección de variables.*
