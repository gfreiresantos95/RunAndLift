# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Android app (`com.gabrielfreire.runandlift`, "Run & Lift"), Kotlin + Jetpack Compose with Material 3. Three modules, dependencies flowing one way only: `:app` → `:core`, `:app` → `:data`, `:data` → `:core`; `:core` depends on nothing.

- `:core` — the design system (`core/…/core/designsystem`). All Compose UI lives here for now.
- `:data` — Room as the UI's source of truth, Firestore behind it, cache-first repositories. Entities/DAOs/data sources are `internal`; only interfaces and `model/` domain types cross out, built by `DataContainer`. See `data/README.md` and `docs/adr/0006`.
- `:feature-auth` — welcome, sign in, sign up, password recovery, role selection. Exposes only `AuthRoutes` and `authGraph()`; everything else is `internal`. Repositories arrive as parameters so the feature never depends on `:app`. The graph **starts on the welcome screen**, where the role is picked before authenticating; the choice travels as a nav argument and is written by sign-up, so nobody is asked twice. Sign-in *reads* the role and never writes it. See `docs/adr/0010`.
  - The path is **linear**: welcome → sign in → sign up, and back. The sign-in footer is the **only** door into sign-up — that single entrance is what guarantees the chosen role reaches the write. Sign-up collects name, e-mail, password, birth date, phone and two separate consents; it deliberately collects **no health data** (that is the anamnesis, in `students/{uid}`) and has no Google button. See `docs/adr/0012`.
  - **One sign-up screen serves both roles**, and the role changes exactly three things: the purpose stated in each field's supporting text, the block between contact and consent (health-data notice for the student, CREF registration for the trainer), and whether the phone is required (it is, for trainers). The CREF is mandatory — prescribing exercise is restricted to registered professionals (Lei 9.696/1998) — masked as `######-A/AA`, validated for *format only*, stored as content (`012345GSP`) and written to `trainerProfiles/{uid}` as `012345-G/SP`, not to `users/{uid}`, because the linked student must be able to read it. The whole role rule lives in `SignUpFormState.validated(...)`; don't spread it across screen and ViewModel. See `docs/adr/0013`.
  - **Google sign-in creates accounts that are authenticated but incomplete** — no birth date, no CREF, no consent. `ProfileCompletion.missing()` says what's absent and the `complete-profile` screen asks for exactly that, writing the role at the same time (which is why the role-selection screen now only appears when there is no role at all). `MainViewModel` runs the same check at launch, so force-quitting isn't a way to skip it. A failed read answers "complete" — never block someone on a guess. See `docs/adr/0014`.
  - Rules the whole flow relies on: password ≥ 8, minimum age 18, e-mail needs a dotted domain and a 2+ letter TLD. Both auth screens anchor content at the top and scroll everything, footer included — there is no fixed bottom bar.
- `:app` — `MainActivity`, `MainViewModel`, `AppContainer` (manual DI), and the root `NavHost` with the `trainer`/`student` graphs.

Navigation is **three sibling graphs**, never one graph with role conditionals — a trainer screen must not be reachable from a student's back stack. `MainViewModel` resolves the start destination *before* the NavHost is composed, so the app never opens on the wrong screen for a frame. See `docs/adr/0009`. MVVM is the pattern: state out as a read-only `StateFlow`, mutation confined to the ViewModel, no `Context` or UI types in it. Rationale and revisit triggers for the structure, the DI choice, and the deferral of convention plugins are in `docs/adr/0003`.

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

- **AGP 9.3.1 / Gradle 9.5 / Kotlin 2.4.10 / compileSdk 37, minSdk 26.** The Gradle daemon runs on a **JDK 21** toolchain auto-provisioned via foojay (`gradle/gradle-daemon-jvm.properties`), so no local `JAVA_HOME` setup is needed. Java source/target compatibility is 11. The daemon is pinned to 21 rather than 25 because detekt 1.23.x cannot run on JDK 25 — its embedded Kotlin compiler fails parsing the JVM version. Change that pin only together with the detekt decision, and regenerate it with `./gradlew updateDaemonJvm --jvm-version=<n>` so the download URLs stay in sync.
- **No `kotlin-android` plugin.** AGP 9 handles Kotlin compilation itself; only `com.android.application` and `org.jetbrains.kotlin.plugin.compose` are applied. Adding `kotlin-android` will break the build.
- **New AGP 9 DSL** in `app/build.gradle.kts`: `compileSdk { version = release(37) }` and `buildTypes { release { optimization { enable = false } } }` (replaces `isMinifyEnabled`). R8 is currently off for release.
- **Keep rules live in `app/src/main/keepRules/`**, not `proguard-rules.pro`; AGP merges every file in that directory.
- **Configuration cache is on** (`org.gradle.configuration-cache=true`). Build logic must be configuration-cache-safe — no reading `System.getenv`/project state at execution time.

## Dependencies

All versions go through the version catalog at `gradle/libs.versions.toml` and are referenced as `libs.*` aliases — never hardcode a coordinate in `build.gradle.kts`. `settings.gradle.kts` sets `FAIL_ON_PROJECT_REPOS`, so repositories are declared only there. Compose artifacts are versionless in the catalog because they are pinned by the Compose BOM. Note the BOM is a `platform()` dependency in **both** `implementation` and `androidTestImplementation` — dropping it from the latter leaves `compose-ui-test-junit4` with no version.

## Code quality

`./gradlew spotlessApply` formats (ktlint), `spotlessCheck` verifies without writing, `detekt` runs static analysis. CI runs `spotlessCheck detekt lint test`; there is deliberately **no detekt baseline**, so the violation count must stay at zero — fix the cause rather than suppress.

`main` is protected by a GitHub ruleset: work enters through a branch and a Pull Request, the `verify` and `firestore-rules` checks must pass before merging, history is linear (squash only), and force-push and deletion are blocked. Those two check names are the **job ids** in `ci.yml` — renaming a job without updating the ruleset stalls every later PR. See `docs/adr/0015`.

Write new code inside these limits instead of discovering them at commit time: **6 parameters** per function and **7** per constructor (defaults don't count, so Compose slot APIs are fine), **11 functions** per file and per class, **60 lines** per function, **120 columns**.

Rule exceptions live in `config/detekt/detekt.yml` with the reason in a comment — mostly Compose frictions (`@Composable` PascalCase, unused private `@Preview`, colour tokens as magic numbers).

Compose-specific checks come from `compose-lint-checks` (Slack), wired through `lintChecks` and run by `./gradlew lint`. Its exceptions live in `core/lint.xml`, scoped by path rather than disabled globally — a rule that gets in the way inside the design system is usually still right in screen code. Every module holding Compose code needs its own `lintChecks(libs.compose.lint.checks)`.

ktlint settings live in `.editorconfig` and nowhere else. Note the plumbing: Spotless hands ktlint an in-memory string, so ktlint can't discover `.editorconfig` on disk and `setEditorConfigPath` doesn't help either. `editorConfigProperties()` in `build.gradle.kts` therefore parses the `[*]` and `[*.{kt,kts}]` sections and feeds them to `editorConfigOverride`, which is the only channel ktlint honours. Don't add ktlint keys directly to that map — put them in `.editorconfig` so the IDE and the build stay on one source.

## Data layer rules

Four rules every repository follows — they are what makes offline real rather than "a cache that sometimes works" (`OfflineFirstExerciseRepository` is the reference implementation):

1. **Reads never touch the network.** `observe*` comes from Room and re-emits on table change.
2. **Sync is an explicit call**; its result arrives through the same `Flow`, not the return value.
3. **Network failure is a return value, not an exception** — the app keeps working on disk data.
4. **Every network call states its Firestore read cost** in KDoc, and avoids the call when it can.

Room schemas are exported to `data/schemas/` and committed — migrations (E0-13) have nothing to migrate from otherwise. Never add `fallbackToDestructiveMigration`. Tests use hand-written fakes, not MockK; `./gradlew koverHtmlReport` gives coverage, with no minimum threshold by design.

## Firebase

`google-services.json` is gitignored (public repo — see `docs/adr/0004`). The `google-services` and `crashlytics` plugins are therefore applied **conditionally** in `app/build.gradle.kts`: present file → Firebase on; absent → loud warning and a build that still succeeds. Never make that unconditional without also solving CI, which builds without the file. Firestore and Auth belong to `:data`; Crashlytics, Analytics, Remote Config and Performance to `:app`. Debug builds disable Crashlytics/Analytics collection via `app/src/debug/AndroidManifest.xml` so development noise stays out of the crash-free rate and the funnel.

## Firestore Security Rules

`firestore/firestore.rules`, deny-by-default. Tests run against the emulator: `cd firestore && npm test` (needs `java` on PATH; CI has its own job). **Link documents must use the id `{trainerId}_{studentId}`** — rules can't query, only `get()` by exact path, so that format is what makes "trainer only reads students with an active link" expressible at all. Every `get()`/`exists()` in a rule costs a document read, so keep them off hot paths; see `docs/adr/0007`.

## Decision records

`docs/adr/` holds the decisions whose rationale the code can't carry — currently the quality-tooling stack and the JDK 21 pin. Before changing build tooling, versions, or theming fundamentals, check whether an ADR already covers it; if a decision gets reversed, add a new ADR rather than editing the old one.

## Design system (`:core`, package `core.designsystem`)

Four layers, and code should always consume the highest one:

- `Color.kt` — brand tonal ramps (Cobalto/Aço/Brasa + state families) as `internal` tokens. **Never referenced from a screen.**
- `ColorScheme.kt` — maps those tokens onto Material 3 roles for light and dark. The two schemes are mirrored (tone 40 in light ↔ tone 80 in dark); keep that symmetry or screens stop working in one theme.
- `ExtendedColors.kt` — the roles M3 lacks: `ok` / `attention` / `critical` (the adherence semaphore) and `highlight` (records). Read via `MaterialTheme.extendedColors`. Each is a `ColorRole` with `color`/`onColor`/`container`/`onContainer`.
- `Type.kt`, `Shape.kt`, `Dimens.kt` — `AppTypography`, `AppShapes`, plus the spacing grid and `Dimens.MinTouchTarget` (48 dp) with the `Modifier.minimumTouchTarget()` helper.

`AppMaskedTextField` masks take `#` for a digit and `A` for a letter; anything else is a separator. The filter is **positional** — a letter typed where a digit belongs never enters — and the state holds content only, never separators. Formatting for storage happens once, in the feature layer.

Hard rules, all of them decisions rather than preferences:

- **No dynamic color (Material You).** `RunAndLiftTheme` takes only `darkTheme`. The adherence semaphore must mean the same thing on every device, and Material You would repaint both it and the brand. An in-app theme preference is a later backlog item and must feed `darkTheme`, not introduce a parallel theme.
- **Colour is never the only channel.** Any use of the semaphore carries an icon and a text label — an accessibility requirement in the backlog, not a nicety. Contrast stays at WCAG AA; touch targets never go below 48 dp; text sizes are always `sp`.
- `MetricTextStyles` (in `Type.kt`) exists for measured numbers — load, reps, RPE, adherence — because they need tabular figures so the layout doesn't shift as values change.
- `ThemePreviews.kt` is a light/dark gallery of every colour role and text style. Run it before and after touching any token.

**Every file holding Compose layout carries a `@Preview`** — screens declare their own `@Preview` pair because they need a per-screen `heightDp`; components use `@LightDarkPreviews`, the project multipreview that renders light and dark from one annotation. Its name is listed by hand in `config/detekt/detekt.yml` (`LongMethod`, `UnusedPrivateMember`), since detekt matches the annotation as written, not what it expands to.

`MainActivity` calls `enableEdgeToEdge()`, and `RunAndLiftTheme` flips the system-bar icon appearance to match the theme, so new screens must consume `Scaffold` inner padding and window insets.

## Splash

Uses the Splash Screen API via `androidx.core:core-splashscreen`, so behaviour is identical above and below Android 12. `MainActivity` calls `installSplashScreen()` **before** `super.onCreate()`.

- `Theme.RunAndLift.Splash` (`res/values/themes.xml`) is the launcher activity's theme; `postSplashScreenTheme` hands off to `Theme.RunAndLift`.
- The splash background, the window background, and the Compose `surface` colour are deliberately the same value (`@color/window_background`, config-overridden in `values-night/`) so there is no colour flash on handoff. Changing one means changing all three.
- The icon is still the template launcher foreground and is temporary; it sits on a coloured circle because that artwork is white.
- `isAppReady` gates `setKeepOnScreenCondition`. Session restore, active-role lookup, and Room warm-up belong in the `lifecycleScope` block that flips it — no artificial delay and no blocking network I/O, since the product promises the workout screen opens in ≤2 s offline.
