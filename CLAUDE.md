# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Android app (`com.gabrielfreire.runandlift`, "Run & Lift"), Kotlin + Jetpack Compose with Material 3. Dependencies flow one way only: `:app` → `:core`, `:app` → `:data`, `:app` → `:feature:*`, `:feature:*` → `:core`/`:data`, `:data` → `:core`; `:core` depends on nothing. A feature module never depends on `:app` or on another feature.

**The repository root holds exactly three module folders — `core/`, `data/` and `feature/` — and every feature is a module inside `feature/`**, never a `:feature-name` sitting at the root. With one module per feature, root-level naming would grow into ten sibling folders where only a prefix says what belongs to what. Adding a feature means `feature/<name>/` plus one `include(":feature:<name>")` line; the `:feature` project itself has no code and no `build.gradle.kts`, it is only the umbrella. ADRs written before this layout call the auth module `:feature-auth`; it is today's `:feature:auth`.

- `:core` — the design system (`core/…/core/designsystem`). All Compose UI lives here for now.
- `:data` — Room as the UI's source of truth, Firestore behind it, cache-first repositories. Entities/DAOs/data sources are `internal`; only interfaces and `model/` domain types cross out, built by `DataContainer`. See `data/README.md` and `docs/adr/0006`.
  - **`LocationRepository` is the one repository that talks to something other than Firebase**: states and municipalities come from the IBGE Localidades API at runtime, over `HttpURLConnection` and kotlinx-serialization's tree API (runtime only — no compiler plugin). It declares its cost in another currency: no Firestore read, but ~380 KB for the largest state's municipality list, because the API nests the whole territorial hierarchy in every item and offers no field selection. `CachedLocationRepository` holds the answers **in memory for the process**, which is what makes that price payable once per state per session; a disk cache is the next step if the wait ever gets noticed, not before. `state(uf)` never throws — a profile must open even when the IBGE is down, showing just the UF.
  - **Only the UF is stored** (`SP`), never the full name: `BrazilState.label` rebuilds `São Paulo - SP` for display, and a second spelling in the database would be a second state at grouping time. `LocationSearch` lives here rather than in a feature because two feature modules that can't see each other need the same accent- and case-insensitive answer, and matching a place name is knowledge about places, not about layout.
- `:feature:auth` — welcome, sign in, sign up, password recovery, role selection. Exposes only `navigation.AuthRoutes`, `navigation.authGraph()` and `completeprofile.ProfileCompletion`/`MissingProfileData` (which `:app` needs at launch); everything else is `internal`. Repositories arrive as parameters so the feature never depends on `:app`. The graph **starts on the welcome screen**, where the role is picked before authenticating; the choice travels as a nav argument and is written by sign-up, so nobody is asked twice. Sign-in *reads* the role and never writes it. See `docs/adr/0010`.
  - The path is **linear**: welcome → sign in → sign up, and back. The sign-in footer is the **only** door into sign-up — that single entrance is what guarantees the chosen role reaches the write. Sign-up collects name, e-mail, password, birth date, phone and two separate consents; it deliberately collects **no health data** (that is the anamnesis, in `students/{uid}`) and has no Google button. See `docs/adr/0012`.
  - **One sign-up screen serves both roles**, and the role changes exactly three things: the purpose stated in each field's supporting text, the block between contact and consent (health-data notice for the student, CREF registration for the trainer), and whether the phone is required (it is, for trainers). The CREF is mandatory — prescribing exercise is restricted to registered professionals (Lei 9.696/1998) — masked as `######-A/AA`, stored as content (`012345GSP`) and written to `trainerProfiles/{uid}` as `012345-G/SP`, not to `users/{uid}`, because the linked student must be able to read it. Validation checks length, a **category of `G` or `P`** (the two that may prescribe) and a real state code — never that the registration exists, since there is no public CONFEF API. The category has its own error case: whoever typed `E` got the number and the state right, and "invalid registration" would send them hunting where nothing is wrong. The whole role rule lives in `ProfileFormState.validated(...)`; don't spread it across screen and ViewModel. See `docs/adr/0013`.
  - **Google sign-in creates accounts that are authenticated but incomplete** — no birth date, no CREF, no consent. `ProfileCompletion.missing()` says what's absent and the `complete-profile` screen asks for exactly that, writing the role at the same time (which is why the role-selection screen now only appears when there is no role at all). `MainViewModel` runs the same check at launch, so force-quitting isn't a way to skip it. A failed read answers "complete" — never block someone on a guess. See `docs/adr/0014`.
  - **The provider's name reaches the database only through complete-profile.** `UserAccount.displayName` carries what Google returned (empty for e-mail sign-up), and `toCompletionDetails(providerName = …)` writes it — that screen is the *only* write in the Google flow, and it used to send everything except the name, on the false premise that "the provider supplied it, so it is already stored". Nothing stored it. Stored name wins over the provider's, and `FirestoreUserRepository` only writes a name when there is none, so nobody's edited name gets overwritten.
  - **State and city are required of both roles**, unlike phone and CREF: location is not a contact channel, it is what puts a student in front of a trainer nearby. Both fields open a full screen with a search box (`AppSearchablePicker`) instead of a dropdown — 853 municipalities in Minas Gerais — and the choice returns through the previous back stack entry's `SavedStateHandle`, which is this navigation's equivalent of "start for result". City stays disabled until a state exists. Their rule lives in `validation/LocationValidation.kt` and not in `AuthFormValidation`, because everything in the latter is a *format* rule with a mask beside it, and a value picked from a closed list has no format to check.
  - **`ProfileFormController` owns the profile-form state**, because sign-up and complete-profile edit the same form and used to carry the same eight mutations written twice — which had already started to drift, one filtering the birth-date digits and the other not.
  - Rules the whole flow relies on: password ≥ 8, minimum age 18, e-mail needs a dotted domain and a 2+ letter TLD. All four auth screens anchor content at the top and scroll everything, footer included — there is no fixed bottom bar.
  - **Packages are one per context, and the shared ones say who shares them.** `signin/`, `signup/`, `completeprofile/`, `recovery/`, `onboarding/` own a flow each, *including its `…Destination.kt`* — the ViewModel wiring lives with the screen it wires, so `navigation/AuthGraph.kt` stays a map of routes and nothing else. Two packages exist purely because two flows share them: `credentials/` is the e-mail-and-password base of **sign-in + sign-up**, and `profileform/` is the name/birth/phone/CREF/consent form of **sign-up + complete-profile**. That is why those types are `ProfileFormState`/`ProfileFormActions` and not `SignUp…`: complete-profile uses them too, and the old name claimed otherwise. The rest: `component/` for screen-agnostic auth UI, `validation/` for the rules and their error enums, `text/` for the `:data` enums that need `R.string`, `google/` for the federated sheet.
- `:feature:student` / `:feature:trainer` — the three tabs of each role: home, workouts, menu. **One module per role, not one shared module**, so a student screen cannot import a trainer route by mistake: it isn't on the classpath. Each exposes only its `navigation.*Routes` and `navigation.*Graph()`; repositories arrive as parameters, and `:app` supplies `onSignedOut`/`onSwitchRole` because a feature knows neither the auth route nor the other role. The three tabs are **sibling routes inside the role graph**, never a nested `NavHost` — a tab is not a flow. The role switcher lives in the menu now, still only for accounts that have both roles. See `docs/adr/0016`.
  - The tab frame itself (`AppTabScaffold`, `AppBottomBar`, `AppBottomBarItem`) is in `:core`: it is layout without domain. Bottom-bar **labels are always visible**, including on unselected tabs — the Material default hides them, which leaves icon and colour highlight as the only channels, and those fail together.
  - **The student onboarding writes `students/{uid}` once, at the end** (`OnboardingViewModel`). Every step can be skipped — skipping writes nothing for that field — and the document's *existence* is what marks "onboarding happened", which is why `MainViewModel` routes a student with no document there and never re-asks someone who skipped everything. What is still missing becomes the home banner, via `StudentProfileCompletion` (a failed read answers "nothing missing", like `ProfileCompletion`). The edit screen reuses the onboarding's step composables rather than copying them, and reads the email from the account as **read-only** — changing it is a credential change, which is another flow.
  - **Injuries are picked from a list of body regions, not typed.** That's how the market's anamnesis asks (PAR-Q+: "a bone, joint, or soft tissue problem — for example, back, knee or hip") and it's what maps to exercise selection: the prescriber doesn't need to know it's tendinitis to drop the overhead press, they need to know it's the shoulder. The nine regions run head to toe, as a physical exam does. Free text survives as the "Outra" chip, which reveals a field — the list gets the region right and loses "hurts when I lift overhead", which is half of what a trainer needs. Chips rather than a separate screen (eleven options need no search) or a dropdown (**the list is the reminder** — reading "knee" is what makes someone remember their knee).
  - **"Nenhuma" is an answer, and `injuries` is therefore a nullable set**: `null` is "hasn't answered", empty is "answered none". Without that distinction a person with no injuries could never clear the home banner, and "declared none" — which is clinical information — would be indistinguishable from silence. The exclusivity between "Nenhuma" and the regions lives in `TrainingFormState`, not in the screen. The pre-list free-text field (`restrictions`) is still read as a fallback into `injuryNotes` and deleted on the first write, so nothing anyone typed disappears because the format changed.
  - **Health data is gated by its own consent.** Weight, height and injuries are only written once `HealthDataConsent` exists, and that rule lives in `FirestoreStudentRepository`, not in a screen — a new screen is the most likely place to forget it. Withdrawing consent clears them from memory too: an authorization taken back with the weight still in the form is the authorization still in force. The consent step sits mid-flow and *adds* the two health steps when accepted: asking for sensitive data before the person knows what the app is would be the wrong order. `students/{uid}` is readable by the student and by a trainer with an **active link**, and writable only by the student.
  - Both modules duplicate `MainDispatcherRule` and the repository fakes in their test source sets, because test source sets are not shared between modules. Thirty repeated lines still cost less than a `:test-fixtures` module; the trigger to extract them is the third module that needs them.
- `:app` — `MainActivity`, `MainViewModel`, `AppContainer` (manual DI), and the root `NavHost` that stitches the three graphs together. It no longer names any screen of either role: `RoleRoutes` is one function translating role to graph.

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

`./gradlew spotlessApply` formats (ktlint), `spotlessCheck` verifies without writing, `detekt` runs static analysis. CI runs `spotlessCheck detekt lint test`, then `:koverXmlReport` and `:koverVerify`; there is deliberately **no detekt baseline**, so the violation count must stay at zero — fix the cause rather than suppress.

**Coverage has two floors, and they answer different questions**: the project stays at **≥ 60% of lines** (`koverVerify`, so it fails on your machine before it fails in CI), and a PR's **diff stays at ≥ 80%** — measured over the lines the PR changed, commented on the PR, and failing the `verify` job when it doesn't. Aggregation covers all six modules, and the report **excludes `@Composable`, Room-generated `*_Impl`, `*PreviewFixtures*` and the design-system tokens**: without those cuts the percentage measures documented decisions (there are no UI tests by choice) rather than gaps. What is *not* excluded is the Firebase adapter layer in `:data` — it holds the health-data consent gate, which is where the largest real gap is. 75% is the declared target, one work item away: the three Firestore repositories are the 190 lines that get there. The PR comment comes from `madrapps/jacoco-report` pinned **by SHA, not by tag** — the released v1.8.0 gates on whole changed *files*, which would fail a one-line PR touching an old untested file. See `docs/adr/0018`.

`main` is protected by a GitHub ruleset: work enters through a branch and a Pull Request, the `verify` and `firestore-rules` checks must pass before merging, history is linear (squash only), and force-push and deletion are blocked. Those two check names are the **job ids** in `ci.yml` — renaming a job without updating the ruleset stalls every later PR. See `docs/adr/0015`.

The ruleset also requires **one approving review from a code owner**, and `.github/CODEOWNERS` makes the repository owner own every path. That rule exists for the PRs nobody is watching — Dependabot's — which arrive with no reviewer at all. GitHub will not request a review from a PR's own author, and nobody can approve their own PR, so on the owner's own PRs the requirement stays unmet by design and the merge goes through the admin bypass the ruleset grants. Don't "fix" that by dropping the requirement; the bot PRs are the point.

## Opening a Pull Request

The full cycle is `gh`-driven, and `.claude/settings.json` allowlists it so it runs without a prompt per command (force-push, hard reset, `pr merge` and `pr close` stay denied — merging is the author's call, not the agent's).

```bash
gh pr create --base main --head <branch> \
  --title "<title>" --body-file <file.md> \
  --assignee @me --label refactor --label tests
```

Two details that are easy to get wrong:

- **`--body-file`, never `--body`.** PR descriptions here carry markdown, accents and blank lines; passing that inline gets mangled by the shell. Write it to a scratchpad file first.
- **`--assignee @me`**, not the handle spelled out — it keeps working for whoever runs the command.
- **Reviewers are not added by hand.** `.github/CODEOWNERS` already requests the owner on every PR that isn't theirs; `--add-reviewer` with the author's own handle is a 422 from the API.

**After the PR is open, go back to `main`:**

```bash
git switch main && git fetch origin && git merge --ff-only origin/main
```

The branch stays on the remote for review, and the working copy is left where the next piece of work starts — on an up-to-date default branch, ready to branch again. Merging the PR is the author's call, so never wait on it; the next branch is cut from `main` and not stacked on top of unmerged work.

Labels are a fixed vocabulary, and a PR takes as many as apply: `refactor` (reorganisation, no behaviour change), `feature`, `fix`, `tests`, `docs`, `build` (Gradle, CI, dependencies), `security` (Firestore rules, LGPD, auth). Create a missing one with `gh label create <name> --color <hex>` rather than inventing a synonym.

Commit messages and PR bodies are written in **Portuguese**, like the KDoc; code, identifiers and this file stay in English.

Write new code inside these limits instead of discovering them at commit time: **6 parameters** per function and **7** per constructor (defaults don't count, so Compose slot APIs are fine), **11 functions** per file and per class, **60 lines** per function, **120 columns**.

Rule exceptions live in `config/detekt/detekt.yml` with the reason in a comment — mostly Compose frictions (`@Composable` PascalCase, unused private `@Preview`, colour tokens as magic numbers).

Compose-specific checks come from `compose-lint-checks` (Slack), wired through `lintChecks` and run by `./gradlew lint`. Its exceptions live in `core/lint.xml`, scoped by path rather than disabled globally — a rule that gets in the way inside the design system is usually still right in screen code. Every module holding Compose code needs its own `lintChecks(libs.compose.lint.checks)`.

ktlint settings live in `.editorconfig` and nowhere else. Note the plumbing: Spotless hands ktlint an in-memory string, so ktlint can't discover `.editorconfig` on disk and `setEditorConfigPath` doesn't help either. `editorConfigProperties()` in `build.gradle.kts` therefore parses the `[*]` and `[*.{kt,kts}]` sections and feeds them to `editorConfigOverride`, which is the only channel ktlint honours. Don't add ktlint keys directly to that map — put them in `.editorconfig` so the IDE and the build stay on one source.

## One context per file

The project prefers **many small files over few mixed ones**. File count is not a cost; opening three files to answer one question is. Four rules follow from that:

- **An extension goes in the file of the type it extends.** Each form-error enum in `feature/auth/validation/` carries its own `@Composable message()`, so adding a case breaks the `when` on the next line instead of in a distant messages file. The exceptions are the `:data` enums, which can't follow the rule — that module has no string resources and no Compose — so `AuthFailure.message()` and every `ActiveRole`-to-text mapping live in `feature/auth/text/`, the closest package to the enum that may know `R.string`.
- **One top-level type per file**, named after it — detekt's `MatchingDeclarationName` enforces this on files that hold exactly one. A UI state, its ViewModel, and its screen are three files, not one.
- **One composable per file, each with its own `@Preview`.** A shared gallery preview hides which component broke; a per-file preview is what the "every file holding Compose layout carries a `@Preview`" rule is actually for.
- **Screen-agnostic auth UI lives in `feature/auth/component/`**, not inside `credentials/`. The layout frame, headline, role chip, failure banner and legal links are used by sign-in, sign-up, recovery *and* complete-profile; owning them from one screen's package is how the recovery screen ended up not using them at all.

## Data layer rules

Four rules every repository follows — they are what makes offline real rather than "a cache that sometimes works" (`OfflineFirstExerciseRepository` is the reference implementation):

1. **Reads never touch the network.** `observe*` comes from Room and re-emits on table change.
2. **Sync is an explicit call**; its result arrives through the same `Flow`, not the return value.
3. **Network failure is a return value, not an exception** — the app keeps working on disk data.
4. **Every network call states its Firestore read cost** in KDoc, and avoids the call when it can.

Room schemas are exported to `data/schemas/` and committed — migrations (E0-13) have nothing to migrate from otherwise. Never add `fallbackToDestructiveMigration`. Tests use hand-written fakes, not MockK.

## Testing

There are **no UI tests** — a screen's layout is verified by opening its `@Preview`, which is why every Compose file has one. What does get a test is everything a preview cannot show: ViewModel state transitions, validation rules, route building, and form-to-storage mapping.

The shared fakes live in `feature/auth/src/test/…/fake/` (`FakeAuthRepository`, `FakeUserRepository`, `MainDispatcherRule`) and are reused by every ViewModel test in the module — write a new one there rather than a private class inside a test. `MainDispatcherRule` uses `StandardTestDispatcher`, not `UnconfinedTestDispatcher`, so `advanceUntilIdle()` is what runs the coroutine; an unconfined dispatcher would finish everything before the first assertion and hide the `submitting` state.

The tests worth keeping honest are the ones for rules the UI can't show you: password recovery answering identically for an e-mail that exists and one that doesn't, `ProfileCompletion` answering "nothing missing" when the read fails, and `MainViewModel`'s four start destinations in order.

## Firebase

`google-services.json` is gitignored (public repo — see `docs/adr/0004`). The `google-services` and `crashlytics` plugins are therefore applied **conditionally** in `app/build.gradle.kts`: present file → Firebase on; absent → loud warning and a build that still succeeds. Never make that unconditional without also solving CI, which builds without the file. **Any `R.string` the plugin generates needs a placeholder for the plugin-absent path** — `default_web_client_id` is declared as an empty `resValue` in `defaultConfig` when the file is missing, because `RunAndLiftNavHost` references it and CI would otherwise fail to compile. That is also why `buildFeatures { resValues = true }` is on; AGP 9 disables it by default. Firestore and Auth belong to `:data`; Crashlytics, Analytics, Remote Config and Performance to `:app`. Debug builds disable Crashlytics/Analytics collection via `app/src/debug/AndroidManifest.xml` so development noise stays out of the crash-free rate and the funnel.

## Firestore Security Rules

`firestore/firestore.rules`, deny-by-default. Tests run against the emulator: `cd firestore && npm test` (needs `java` on PATH; CI has its own job). **Link documents must use the id `{trainerId}_{studentId}`** — rules can't query, only `get()` by exact path, so that format is what makes "trainer only reads students with an active link" expressible at all. Every `get()`/`exists()` in a rule costs a document read, so keep them off hot paths; see `docs/adr/0007`.

## Decision records

`docs/adr/` holds the decisions whose rationale the code can't carry — currently the quality-tooling stack and the JDK 21 pin. Before changing build tooling, versions, or theming fundamentals, check whether an ADR already covers it; if a decision gets reversed, add a new ADR rather than editing the old one.

## Design system (`:core`, package `core.designsystem`)

**Before writing a screen, read `docs/design-guidelines.md`** — it is the checklist (four states per
screen, which scaffold, motion, colour, a11y, UX writing), and `docs/adr/0017` carries the reasoning.
The one rule that summarises it: if you are hand-rolling a frame, a state or a spacing, a component
for it already exists. **Every screen owes four states** — `AppLoadingState`, `AppEmptyState`, its
content, and `AppMessageCard` — and `if (loading) return` is never one of them, because it draws a
blank screen, which reads as broken rather than as loading.

Four layers, and code should always consume the highest one:

- `Color.kt` — brand tonal ramps (Cobalto/Aço/Brasa + state families) as `internal` tokens. **Never referenced from a screen.**
- `ColorScheme.kt` — maps those tokens onto Material 3 roles for light and dark. The two schemes are mirrored (tone 40 in light ↔ tone 80 in dark); keep that symmetry or screens stop working in one theme.
- `ExtendedColorScheme.kt` — the roles M3 lacks: `ok` / `attention` / `critical` (the adherence semaphore) and `highlight` (records). Read via `MaterialTheme.extendedColors`. Each is a `ColorRole` (`ColorRole.kt`) with `color`/`onColor`/`container`/`onContainer`.
- `Type.kt`, `Shape.kt`, `Dimens.kt` — `AppTypography`, `AppShapes`, plus the spacing grid and `Dimens.MinTouchTarget` (48 dp) with the `Modifier.minimumTouchTarget()` helper.

`:core` has **no `strings.xml` by design** — every component takes its text as a parameter, so the design system never decides language. Preview sample copy therefore can't come from `stringResource`; it lives in `PreviewSamples.kt`, one file for the whole module. Modules that *do* have string resources (`:feature:auth`) must use `stringResource` in previews instead — a literal there is a second copy of a string that already exists.

`AppScreenScaffold` (title + back arrow) and `AppTabScaffold` (bottom bar) are the two screen frames;
both wire the top bar's `pinnedScrollBehavior` internally, which is what keeps a screen from ever
touching that experimental Material API. Scrolling content goes in `AppScreenColumn`, which applies
the 600 dp `Dimens.ContentMaxWidth` — the line that stops the app from being a stretched phone on a
tablet or foldable. `AppMotion` owns every duration and easing, including the navigation transitions
the root `NavHost` applies; a screen never writes `tween(300)` of its own.

`AppSearchablePicker` is the full-screen "choose one from a long list" — top bar, search box, list, and three distinct outcomes, because **an empty search and a list that failed to load are different screens**: one says there is nothing matching, the other offers to try again. It knows nothing about states or cities, which is what lets the same screen serve two feature modules that don't see each other; the filtering happens in the ViewModel, since a rule inside a composable can't be tested without booting a screen. `AppSelectField` is the field that opens it: it looks like `AppTextField` on purpose, is read-only rather than disabled (grey means "unavailable", and this field is perfectly available — just not by keyboard), and takes its taps on a layer above, because an `OutlinedTextField` swallows the touch to place a cursor even when read-only.

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
