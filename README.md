# FamRuta - Navegación Familiar en Tiempo Real (Android Nativo)

FamRuta es una aplicación Android nativa desarrollada en **Kotlin** con **Jetpack Compose**, **Google Sign-In** y **Firebase Firestore**.

---

## 🚀 ¿Por qué ocurrió el error en Codemagic y cómo se solucionó?

### 1. Error de "The selected instance type is not available with the current billing plan"
En el plan gratuito de Codemagic (500 minutos mensuales gratuitos), las máquinas Linux (`linux_x2`) están bloqueadas tras un plan de pago o tarjeta. Las máquinas gratuitas de Codemagic son **`mac_mini_m2`**.
Ya se actualizó `codemagic.yaml` con:
```yaml
instance_type: mac_mini_m2
```
Esto permite compilar el APK de Android de forma 100% gratuita dentro de los 500 minutos libres de Codemagic.

### 2. Error previo de Flutter ("Found no pubspec.yaml file")
Codemagic detectó inicialmente el repositorio como Flutter. Con `codemagic.yaml`, ahora ejecuta directamente `./gradlew assembleDebug` en el entorno Android nativo.

#### Opción B: Cambiar el tipo de proyecto en el Workflow Editor
1. En Codemagic, entra a la configuración del proyecto.
2. Si usas el editor visual, cambia el tipo de proyecto de **Flutter App** a **Android App**.
3. En la sección de Build, selecciona **Gradle wrapper** (`./gradlew assembleDebug`).

---

## ⚡ Alternativa directa: GitHub Actions (Automático y Gratis)

Ya incluimos el flujo automático en `.github/workflows/build-apk.yml`.
1. Cada vez que hagas `git push` a tu repositorio en GitHub:
2. Ve a la pestaña **Actions** en tu repositorio de GitHub (`github.com/daniellugoxd/Fam/actions`).
3. Verás la compilación ejecutándose automáticamente.
4. Al terminar (tarda ~2 minutos), haz clic en la ejecución y en la sección **Artifacts** descarga directamente `FamRuta-app-debug.apk` e instálalo en tu Android.

---

## 📂 Estructura del Proyecto Android

La estructura del proyecto sigue la arquitectura oficial de Google para Android:
- `/app`: Módulo principal de la aplicación (`src/main/java`, `res`, `AndroidManifest.xml`).
- `/gradle/wrapper`: Binarios y propiedades del Gradle Wrapper (`gradle-wrapper.jar`, `gradlew`).
- `build.gradle.kts` & `settings.gradle.kts`: Configuración de compilación Gradle Kotlin DSL.
- `codemagic.yaml`: Pipeline de CI/CD para compilación en Codemagic.
- `.github/workflows/build-apk.yml`: Pipeline de CI/CD para GitHub Actions.

---

## 📱 Compilación Local

Para compilar el APK en tu computadora o terminal:
```bash
./gradlew assembleDebug
```
El APK generado estará en:
`app/build/outputs/apk/debug/app-debug.apk`
