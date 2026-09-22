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
