# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Android app (`com.gabrielfreire.runandlift`, "Run & Lift"), single `:app` module, Kotlin + Jetpack Compose with Material 3. Currently the freshly-scaffolded template: `MainActivity` renders a `Greeting` composable inside a `Scaffold`. There is no data layer, navigation, or DI yet — those decisions are still open.

## Commands

Run from the repo root. On Windows use `.\gradlew.bat`; the Bash tool can use `./gradlew`.

```powershell
.\gradlew.bat assembleDebug                 # build debug APK
.\gradlew.bat installDebug                  # build + install on connected device/emulator
.\gradlew.bat test                          # JVM unit tests (app/src/test)
.\gradlew.bat testDebugUnitTest --tests "com.gabrielfreire.runandlift.ExampleUnitTest"   # single test class
.\gradlew.bat testDebugUnitTest --tests "*.ExampleUnitTest.addition_isCorrect"           # single test method
.\gradlew.bat connectedAndroidTest          # instrumented + Compose UI tests (needs a device)
.\gradlew.bat lint                          # Android Lint; HTML/XML report in app/build/reports/lint-results-*.
.\gradlew.bat clean
```

There is no ktlint/detekt/spotless configuration — formatting is whatever Android Studio applies (`kotlin.code.style=official`).

## Toolchain specifics

These are newer than most Android docs and samples assume; don't "fix" them back to the older idioms:

- **AGP 9.3.1 / Gradle 9.5 / Kotlin 2.2.10 / compileSdk 37, minSdk 24.** The Gradle daemon runs on a JDK 25 toolchain auto-provisioned via foojay (`gradle/gradle-daemon-jvm.properties`), so no local `JAVA_HOME` setup is needed. Java source/target compatibility is 11.
- **No `kotlin-android` plugin.** AGP 9 handles Kotlin compilation itself; only `com.android.application` and `org.jetbrains.kotlin.plugin.compose` are applied. Adding `kotlin-android` will break the build.
- **New AGP 9 DSL** in `app/build.gradle.kts`: `compileSdk { version = release(37) }` and `buildTypes { release { optimization { enable = false } } }` (replaces `isMinifyEnabled`). R8 is currently off for release.
- **Keep rules live in `app/src/main/keepRules/`**, not `proguard-rules.pro`; AGP merges every file in that directory.
- **Configuration cache is on** (`org.gradle.configuration-cache=true`). Build logic must be configuration-cache-safe — no reading `System.getenv`/project state at execution time.

## Dependencies

All versions go through the version catalog at `gradle/libs.versions.toml` and are referenced as `libs.*` aliases — never hardcode a coordinate in `build.gradle.kts`. `settings.gradle.kts` sets `FAIL_ON_PROJECT_REPOS`, so repositories are declared only there. Compose artifacts are versionless in the catalog because they are pinned by the Compose BOM (`composeBom = "2026.02.01"`).

## Theming

`RunAndLiftTheme` (`ui/theme/Theme.kt`) wraps `MaterialTheme` and defaults `dynamicColor = true`, so on API 31+ the Material You system palette wins and the hand-written `Purple40`/`Purple80` schemes in `Color.kt` only apply on API 24–30. When checking color work, verify on both an API 31+ device and an older one, or pass `dynamicColor = false`. `MainActivity` calls `enableEdgeToEdge()`, so new screens must consume the `Scaffold` inner padding (and window insets) rather than assuming a system-bar-free area.

The XML theme (`res/values/themes.xml`, `Theme.RunAndLift`) is a bare `android:Theme.Material.Light.NoActionBar` used only for the activity window before Compose takes over.
