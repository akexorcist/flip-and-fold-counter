# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this app does

Flip & Fold Counter counts how many times a Samsung Galaxy Z-series device is flipped or folded. It does not read sensors itself: Samsung's "Modes and Routines" app is configured by the user to launch `CounterActivity` on a fold/flip routine trigger, and this app just records that event. `MainActivity` (the com.samsung.android.app.routines package) is declared in `<queries>` in the manifest and its presence is checked at runtime (`isRoutinesAppAvailable` in `MainScreen.kt`) to warn the user if it's missing.

## Commands

Single Gradle module (`:app`), Kotlin + Jetpack Compose, built with the wrapper.

- Build debug APK: `./gradlew assembleDebug`
- Build release APK: `./gradlew assembleRelease` (minified, uses `app/proguard-rules.pro`)
- Install debug build on a connected device/emulator: `./gradlew installDebug`
- Unit tests (`app/src/test`): `./gradlew testDebugUnitTest`
- Instrumented tests (`app/src/androidTest`): `./gradlew connectedDebugAndroidTest`
- Run a single test class: `./gradlew testDebugUnitTest --tests "dev.akexorcist.flipfoldcounter.SomeTest"`
- Lint: `./gradlew lint`

Note: `app/src/test` and `app/src/androidTest` currently contain no source files — there is no existing test suite/pattern to follow yet.

## Architecture

Single-Activity(ish)-plus-one Compose app, MVVM, DI via Koin. Two entry-point activities:

- **`MainActivity`** — the visible, launcher activity. Installs splash screen, hosts the entire Compose UI via `NavGraph()`.
- **`CounterActivity`** — exported, invisible (`Theme.InvisibleActivity`, `autoRemoveFromRecents`), launched externally by the Samsung Routines app. On `onCreate` it calls `CounterViewModel.addCounter()` then immediately `finish()`s. This is the actual "counting" trigger — treat any change here as changing the external contract with Samsung Routines, not just internal app logic.

### DI (Koin)

`MainApplication.onCreate` calls `startKoin { modules(appModule) }`. All bindings live in `AppModule.kt`: `AppDatabase` (single), `CounterDao` (factory), `AppSettingsDataSource`/`AppSettingsRepository` (factory), `CounterRepository`/`StatisticsRepository` (factory), and every ViewModel (`viewModelOf`). New repositories/ViewModels must be registered here or `koinViewModel()`/injection will fail at runtime, not compile time.

### Data layer

- Single Room table: `CounterEntity(dateTime: LocalDateTime PK, count: Int)` in `counter_database`, one row per **hour** (`CounterRepository.addCountForCurrentHour` truncates to `withMinute(0).withSecond(0).withNano(0)`). `CounterDao.upsertCountForHour` does insert-or-increment (`OnConflictStrategy.IGNORE` + fallback `UPDATE ... count = count + 1`) inside a `@Transaction`.
- `AppDatabase` is schema version 4, `exportSchema = false`, `fallbackToDestructiveMigration(false)` — there are no migration files checked in; bumping the entity currently means data loss on upgrade unless a real `Migration` is added.
- `CounterRepository` — totals/today/this-month counts, all as `Flow`, consumed by `MainViewModel`.
- `StatisticsRepository` — hourly (per day), daily (per month), and all-time monthly aggregates for the Statistics screen/charts (Vico). Each aggregate method fills in zero-count gaps for the full period (all 24 hours, all days in month, all months since first entry) so charts never have missing buckets.
- `AppSettingsRepository`/`AppSettingsDataSource` — SharedPreferences-backed flag for "don't show the before-using dialog again."

### UI / Navigation

Uses **Navigation3** (`androidx.navigation3`, alpha), not the classic Navigation component. `NavGraph.kt` defines a sealed `Screen : NavKey` (`Main`, `Instruction`, `Statistics(initialTab: StatisticsTab)`) and a `NavDisplay` with an `entryProvider` `when` — any new screen needs a case added there or it hits the `error(...)` branch.

Each feature package (`ui/main`, `ui/statistics`, `ui/instruction`) follows a `XxxRoute` (Composable that owns `koinViewModel()`, collects state, wires navigation callbacks) + `XxxScreen` (stateless, previewable Composable) split. `StatisticsViewModel` models UI state as a `sealed interface StatisticsUiState { Loading / Success / Error }` with a `GraphType` sealed class (`Hourly`/`Daily`/`Monthly`) carrying the data map plus precomputed `max`/`average` for the Vico chart.

Shared UI components live in `ui/component` (`AppCard`, `AnimatedCountText`, `BeforeUsingDialog`, `Button`). Theming in `ui/theme` (standard Material3 `Color`/`Theme`/`Type`).

### Version catalog

Dependencies are centralized in `gradle/libs.versions.toml` — add/bump libraries there, not as inline coordinates in `app/build.gradle.kts`. Kotlin 2.2.0 with `-XXLanguage:+PropertyParamAnnotationDefaultTargetMode` compiler flag enabled (see `app/build.gradle.kts`).
