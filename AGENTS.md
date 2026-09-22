# AGENTS.md

Guidance for AI agents and contributors working in this repository. Keep it accurate and
concise; update it whenever you change build, tooling, or architectural conventions.

## Project Overview

- **رَبْوَة (Rabwa)** — an Arabic-first Android application.
- **Language / UI:** Kotlin + Jetpack Compose (Material 3), RTL by default.
- **Build system:** Gradle (Kotlin DSL) with version catalog `gradle/libs.versions.toml`.
- **Single module:** `:app`. Root project name is `رَبْوَة`.
- **Architecture:** layered Clean Architecture (core / data / domain / presentation / ui).
- **Backend:** Supabase (Auth, PostgREST, Storage, Realtime) + Firebase Cloud Messaging.
- **Min / target / compile SDK:** 24 / 36 / 36. `buildToolsVersion = "36.1.0"`.
- **Namespace:** `com.royaraqamia.rabwa` · **Application ID:** `com.royaraqamia.rabwa`.

## Toolchain & Prerequisites

- **JDK 17 or 21.** AGP 9.1.1 does **not** support JDK 25/26. Point `JAVA_HOME` at JDK 21 or set
  `org.gradle.java.home` in `~/.gradle/gradle.properties`.
- **Android SDK** with `platforms;android-36` and `build-tools;36.1.0`. `local.properties` must
  contain `sdk.dir=...`.
- **Never commit** `.env`, `local.properties`, `*.jks`, or `debug.keystore` — all are git-ignored.

## Commands

Run from the repository root (`D:\Projects\rabwa`). On Windows use `gradlew.bat`; on
macOS/Linux use `./gradlew`.

| Task | Command |
| --- | --- |
| Debug build | `.\gradlew.bat assembleDebug` |
| Release build | `.\gradlew.bat assembleRelease` |
| JVM unit tests | `.\gradlew.bat testDebugUnitTest` |
| Full check | `.\gradlew.bat check` |
| Lint | `.\gradlew.bat lint` (or `lintDebug`) |
| Install on device | `.\gradlew.bat installDebug` |
| Clean | `.\gradlew.bat clean` |
| Record screenshots | `.\gradlew.bat recordRoborazziDebug` |
| Verify screenshots | `.\gradlew.bat verifyRoborazziDebug` |

Outputs: `app\build\outputs\apk\debug\app-debug.apk` and `...\release\app-release.apk`.

### Run with live logs

`run.ps1` is the CLI equivalent of Android Studio's Run button — it builds, installs, launches,
and streams logcat (no hot reload; rebuild to apply changes):

```powershell
.\run.ps1                      # build + install + launch + follow logs
.\run.ps1 -NoBuild             # relaunch installed app only
.\run.ps1 -ErrorsOnly          # only error-level log lines
.\run.ps1 -Snapshot -Tail 100  # print recent logs and exit
.\run.ps1 -Clean               # gradlew clean, then build/install/launch
```

The device is auto-detected; if none is connected it tries BlueStacks on `127.0.0.1:5555`.
Override with `-Device <serial>`.

### Release signing

`release.ps1` is the supported path: it prompts for the upload keystore password
(never echoed), exports the signing env vars `app/build.gradle.kts` reads, builds,
and verifies the signature and package.

```powershell
.\release.ps1                          # signed APK
.\release.ps1 -Bundle                  # signed .aab for Play
.\release.ps1 -Bump -VersionName 1.1.0   # bump versionCode + set versionName, then build
.\release.ps1 -Clean                   # clean first
```

The equivalent manual invocation:

```powershell
$env:KEYSTORE_PATH="D:\keys\my-upload-key.jks"   # defaults to <rootDir>\my-upload-key.jks
$env:STORE_PASSWORD="..."
$env:KEY_PASSWORD="..."
.\gradlew.bat assembleRelease
```

`versionCode` must increase on every Play upload (`release.ps1 -Bump` does this; commit the
bump before tagging). Back up the keystore, and separately its password, or the app can never
be updated: `.\backup-keystore.ps1 -To E:\backups\rabwa`. CI mirrors this — `.github/workflows/release.yml`
builds the APK and AAB on `v*` tags using repository secrets and publishes a GitHub Release
carrying both plus the R8 `mapping.txt`, so artifacts outlive the 90-day workflow-artifact
retention. All actions there are pinned to commit SHAs; releases are deliberate (a `v*` tag or a
manual `workflow_dispatch`, with a `dry_run` input), never per-merge — a Play `versionCode` can
never be reused. `.\set-ci-secrets.ps1` uploads the two keystore passwords as secrets (prompts
securely; requires Python + PyNaCl).

Debug builds use the committed-in-working-tree `debug.keystore` (git-ignored; regenerate if missing).

## Configuration

Secrets are injected into `BuildConfig` by the **Secrets Gradle Plugin** (`secrets {
propertiesFileName = ".env"; defaultPropertiesFileName = ".env.example" }`).

- Create `.env` in the project root from `.env.example`:
  `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `GOOGLE_WEB_CLIENT_ID`.
- If `.env` is absent the build falls back to `.env.example`; **authentication fails at runtime**
  without real values.
- `FIREBASE_APPCHECK_DEBUG_TOKEN` is on the plugin `ignoreList`.
- Access config only through `core/config/SupabaseConfig.kt` — do not read `BuildConfig` directly
  outside that file.
- `google-services.json` is optional (`MissingGoogleServicesStrategy.WARN`), so FCM is inert until
  it is supplied.

## Architecture & Directory Layout

Code lives under `app/src/main/java/com/example/`. Dependencies point **inward**:
`ui → presentation → domain ← data`. The domain layer must not import Android or framework types.

```
core/           Cross-cutting primitives (no feature logic)
  result/       AppResult<T> monad + AppError sealed hierarchy
  dispatcher/   CoroutineDispatchers abstraction (io/default/main)
  config/       SupabaseConfig (validated BuildConfig reader)
  network/      SupabaseClientProvider singleton
  validation/   InputValidator, AuthValidator (zero-trust input checks)
  avatar|notification|qr/  Small focused utilities
data/           Implementation of domain contracts
  local/        Room entities/DAOs (AppDatabase), SharedPreferences managers
  remote/       Supabase data sources, DTOs, DTO mappers
  mapper/       Entity ↔ domain mappers
  network/      ConnectivityMonitorImpl
  repository/   *RepositoryImpl (one per domain repository)
  service/      AppFirebaseMessagingService
domain/         Pure Kotlin business layer
  model/        Immutable domain models
  repository/   Repository interfaces (contracts only)
  usecase/      Single-responsibility use cases (invoke operator)
presentation/   ViewModels + UI state (no Compose imports)
  common/UiState.kt, auth/, notification/, qr/, storage/
ui/             Compose UI
  screen/       Full screens (LoginScreen, MainAppScreen, ProfileScreen, ...)
  component/    Reusable composables
  theme/        RabwaTheme, Color, Type, Shape, Spacing (LocalSpacing)
  icon/         Bundled Lucide icon sets
keyboard/       ArabicEnglishKeyboardService + KeyboardView (IME)
```

### Dependency injection

There is **no DI framework**. Wiring is manual in
`presentation/ArchitectureViewModelFactory.kt`, a thread-safe singleton
(`getInstance(context)`) that constructs repositories, use cases, and ViewModels. When adding a
ViewModel, create its use cases here and register it in `create(...)`.

### Conventions

- **Result handling:** never throw across layer boundaries. Return `AppResult<T>`
  (`Success` / `Failure(AppError)`); map exceptions with `AppResult.runCatching { }`. Use `map`/`fold`
  extension helpers.
- **Errors:** use the existing `AppError` variants (`Network`, `Validation`, `Database`,
  `NotFound`, `Unauthorized`, `Auth`, `RemoteDatabase`, `Storage`, `Unknown`). Do not invent new
  ad-hoc exception types. User-facing messages in `presentation`/`ui` are Arabic.
- **Validation:** all input entering the domain passes through `InputValidator` / `AuthValidator`
  (see `AddArchitectureRecordUseCase`).
- **Dispatchers:** inject `CoroutineDispatchers`; never reference `Dispatchers.IO` directly in
  repositories/usecases. Default parameter is `DefaultDispatchers()`.
- **Use cases:** one class per action, `suspend operator fun invoke(...)`, thin orchestration over a
  repository.
- **ViewModels:** expose `StateFlow` via private `MutableStateFlow` + `asStateFlow()`; launch in
  `viewModelScope`. Keep them free of Android UI imports.
- **Compose:** use `ui/theme` design tokens (`MaterialTheme`, `LocalSpacing`) instead of hardcoded
  colors/dimensions. Screens receive pre-wired ViewModels; composables stay stateless where
  possible. Use `collectAsStateWithLifecycle`.
- **Persistence:** Room for structured data (`data/local/*Entity`, `*Dao`), SharedPreferences for
  session/theme preferences.
- **Networking:** go through `SupabaseClientProvider` (never construct a client ad hoc) and the
  `data/remote` data sources. Map Supabase DTOs to domain models via `data/remote/mapper`.
- **Localization:** UI is RTL (`LocalLayoutDirection = Rtl`) and Arabic-first. `resourceConfigurations`
  is limited to `en`, `ar`. Use IBM Plex Sans Arabic fonts under `res/font`.
- **Style:** follow existing Kotlin/Compose formatting (2-space indent, trailing commas). KDoc on
  public/core types is welcome. Do **not** add comments that merely restate the code.

## Testing

- **Unit tests:** `app/src/test/java/com/example/` — JUnit 4, Robolectric, Roborazzi, Compose UI test.
- **Instrumented tests:** `app/src/androidTest/java/com/example/` — Espresso / AndroidJUnitRunner.
- **Patterns to follow:**
  - Use hand-written fakes for DAOs/repositories (see `UserRepositoryImplTest.FakeUserDao`).
  - Inject `testutil/TestCoroutineDispatchers` (`UnconfinedTestDispatcher`) for deterministic
    coroutine tests.
  - Compose screenshot tests use `@RunWith(RobolectricTestRunner::class)`,
    `@GraphicsMode(NATIVE)`, and `captureRoboImage(...)`; golden images live in
    `app/src/test/screenshots/`. Regenerate intentionally with `recordRoborazziDebug`.
  - Test names may use backtick descriptions: `` `cacheUser persists user profile` ``.
- `testOptions { unitTests { isIncludeAndroidResources = true } }` is enabled, so Robolectric can
  read resources.

Run the full suite before considering work done: `.\gradlew.bat testDebugUnitTest`.

## Backend

- `supabase_schema.sql` is the source of truth for the remote schema and RLS policies. Apply it in
  the Supabase SQL editor. Keep DTOs, Room entities, and this file in sync.
- Tables: `architecture_records`, `profiles`, `user_fcm_tokens`; storage bucket `documents`.
- All tables are protected by RLS. Preserve the owner-scoped policies and the
  `set_architecture_user_id` / `handle_new_user` triggers when editing.

## Guardrails

- **Do not commit** secrets, keystores, `.env`, or `local.properties`.
- **Do not** upgrade the JDK requirement or AGP/Kotlin versions without checking JDK compatibility.
- **Do not** read `BuildConfig` outside `SupabaseConfig`.
- **Do not** add a DI framework or new networking stack without explicit instruction; extend the
  existing factory and `SupabaseClientProvider`.
- **Do not** bypass `InputValidator`/`AppResult` at domain boundaries.
- **Do not** remove or weaken RLS policies in `supabase_schema.sql`.
- Do not create documentation files unless requested.

## Environment Quirks

- This machine's JVM defaults to the `ar_SA` locale, which caused Room/KSP to emit Arabic-Indic
  numerals (`٣`, `١`, `٠`) into generated Kotlin source and break compilation. `gradle.properties`
  therefore forces `-Duser.language=en -Duser.country=US` for build JVMs. If generated code appears
  corrupted, purge the poisoned build cache:
  `Remove-Item "$env:USERPROFILE\.gradle\caches\build-cache-1" -Recurse -Force; .\gradlew.bat clean assembleDebug`.
- On restricted networks that cannot reach `dl.google.com`, artifact resolution is redirected to
  mirrors via a machine-level `~/.gradle/init.gradle`. Do not commit machine-specific Gradle config.
- `kotlin.compiler.execution.strategy=in-process` is set to avoid Kotlin compile-daemon
  connection errors.

## Definition of Done

1. Code compiles: `.\gradlew.bat assembleDebug`.
2. Unit tests pass: `.\gradlew.bat testDebugUnitTest`.
3. Lint is clean (or findings are justified): `.\gradlew.bat lint`.
4. New behavior has tests following the patterns above.
5. Architecture boundaries and the guardrails in this file are respected.
6. Do not commit unless the user explicitly asks you to.

## Agent skills

### Issue tracker

Issues live in this repo's GitHub Issues, driven by the `gh` CLI. See `docs/agents/issue-tracker.md`.

### Triage labels

The five canonical triage roles use their default label strings. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `CONTEXT.md` + `docs/adr/` at the repo root. See `docs/agents/domain.md`.
