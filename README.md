# openStream

> *Every frame tells a story.*

**openStream** is a premium, cinematic Android streaming application dedicated to legally accessible public-domain cinema. Designed with a timeless aesthetic, openStream brings historical motion picture masterworks—from early silent film innovations to German Expressionism, Film Noir, and classic golden-age comedies—to film enthusiasts worldwide.

---

## 📽️ Phase 1: Foundation & Design System

Phase 1 establishes the rock-solid architectural foundation, design tokens, multilingual capabilities, and core navigation infrastructure for the platform.

### ✨ Key Features in Phase 1

1. **Cinematic Design System**:
   - Distinctive dark palette: **Midnight Navy** (`#101827`), **Surface** (`#182233`), **Elevated Surface** (`#252F40`), **Warm Ivory** (`#F2EFE8`), and **Muted Champagne** (`#C6AD7A`).
   - Elegant typography pairing classic Serif headings for cinematic titles with clean Sans-Serif for modern UI metadata and controls.
   - Comprehensive tokens for spacing, elevation, animation, and shapes.
   - Ten core reusable components:
     - `OpenStreamButton` (Primary, Outlined, Text variants)
     - `OpenStreamIconButton` (Accessible 48dp minimum touch target)
     - `OpenStreamTopBar` (Brand & destination headers with back-navigation)
     - `OpenStreamBottomNavigation` (5-tab navigation with active indicators)
     - `OpenStreamMovieCard` (2:3 aspect ratio, subtle gradients, and typography)
     - `OpenStreamSectionHeader` (Section titles with optional actions)
     - `OpenStreamLoadingIndicator` (Cinematic circular progress indicator)
     - `OpenStreamErrorState` (Styled error graphic, message, and retry button)
     - `OpenStreamEmptyState` (Curated empty states with actionable guidance)
     - `OpenStreamPosterPlaceholder` (Graceful textured film-frame fallback)

2. **Multilingual Architecture**:
   - Full tri-lingual resource coverage:
     - **English (`en`)** — Default
     - **O‘zbekcha (`uz`)** — Latin script
     - **Русский (`ru`)** — Cyrillic script
   - Dynamic in-app language switching updating strings immediately without app restart.

3. **Application Navigation (Navigation Compose)**:
   - **Splash**: Minimal cinematic logo entrance animation with smart route evaluation.
   - **Onboarding**: 3-step illustrated introduction to timeless public-domain cinema with Skip, Next, and Get Started actions.
   - **Home**: Spotlight hero film (*Metropolis*), Continue Watching rail, Curated Collections, Recently Added, and Popular Classics.
   - **Discover**: Category/genre chips (Silent Era, Film Noir, Sci-Fi & Horror, Comedy, Drama) with responsive adaptive grid.
   - **Search**: Debounced live search with clear button, recent search history, and empty states.
   - **Library**: Segmented tabs for Watchlist and Favorites with local state synchronization.
   - **Settings**: Dynamic language selection, theme information, privacy pledge, and app details.

4. **Persistence & Data**:
   - Preferences DataStore: Onboarding completion state, active language, and theme preference.
   - Curated preview dataset of historical public-domain cinema (*Metropolis*, *Nosferatu*, *The General*, *Charade*, *His Girl Friday*, *A Trip to the Moon*, *The Cabinet of Dr. Caligari*, *Night of the Living Dead*, *The Gold Rush*, *Sherlock Jr.*).

---

## 🎬 Phase 2: Complete Premium OTT UI/UX Redesign

Phase 2 elevates **openStream** to commercial streaming standards inspired by Apple TV+, Max, and Netflix, while maintaining strict adherence to legal public-domain historical archiving.

### 🌟 What's New in Phase 2

1. **Movie Detail Screen**:
   - Immersive cinematic backdrop with multi-stop dark gradient scrim and floating circular back button.
   - Comprehensive metadata badges: Public Domain Status, 4K Restored, Release Year, Audio, Runtime.
   - Interactive Watchlist and Favorite toggles with reactive feedback and persistent DataStore synchronization.
   - Archival preservation notes and territory availability information.
   - Rich horizontal carousel of related historical masterworks.

2. **Cinematic Home Screen Redesign**:
   - Full-bleed edge-to-edge Hero Spotlight banner featuring *Metropolis* (1927).
   - Landscape 16:9 Continue Watching rail with subtle gold progress bars.
   - Numbered gold rank badges (#1–#5) for Trending Masterpieces.
   - Curated thematic collections and recently restored masterworks.

3. **Discover & Search Elevation**:
   - Curated Movement Spotlight banner in Discover highlighting German Expressionism.
   - Interactive genre filter capsules with smooth selection states.
   - Luxury Search field with persistent recent search history chips and instant clear functionality.

4. **Library & Settings**:
   - Dual-tab Library (Watchlist & Favorites) with persistent storage via DataStore.
   - High-contrast empty states featuring direct "Explore Catalog" call-to-actions.
   - Trilingual Settings (English, O‘zbekcha, Русский) with immediate Compose context updates.
   - Public Domain archival pledge and version information cards.

5. **Expanded Design System**:
   - `OpenStreamBadge`: `Gold`, `Subtle`, and `Solid` variants.
   - `MovieCardStyle.Landscape` (16:9) and `MovieCardStyle.Portrait` (2:3) with duration and progress bar overlays.
   - Multi-stop gradients: `HeroOverlayGradient`, `DarkScrimGradient`, and `GoldAccentGradient`.

6. **True Persistence**:
   - Watchlist, Favorites, and Recent Searches persist across app restarts using Jetpack Preferences DataStore.

7. **Test Coverage**:
   - 44 comprehensive unit tests passing with 100% success rate across domain, data repositories, ViewModels, and navigation.

---

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| Language | **Kotlin 1.9.23** |
| UI Toolkit | **Jetpack Compose (BOM 2024.04.01)** with Material 3 |
| Navigation | **AndroidX Navigation Compose 2.7.7** |
| Architecture | **MVVM** with Clean Architecture & Unidirectional Data Flow |
| Asynchronous / Streams | **Kotlin Coroutines 1.8.0 & StateFlow** |
| Local Preferences | **Jetpack Preferences DataStore 1.0.0** |
| Image Loading | **Coil Compose 2.6.0** |
| Dependency Management | **Gradle Version Catalog (`libs.versions.toml`)** |
| Testing | **JUnit 4**, **Kotlinx Coroutines Test**, **Compose UI Test** |

---

## 📁 Project Structure

```text
app/src/main/java/com/openstream/app/
├── MainActivity.kt
├── OpenStreamApplication.kt
├── core/
│   ├── common/             # UiState sealed interface
│   ├── designsystem/
│   │   ├── colors/         # Midnight Navy, Warm Ivory, Muted Champagne
│   │   ├── typography/     # Serif display & Sans-serif UI scales
│   │   ├── shapes/         # Defined subtle radii tokens
│   │   ├── theme/          # Spacing, Elevation, Animation, OpenStreamTheme
│   │   └── component/      # 10 Reusable openStream UI components
│   ├── di/                 # AppContainer lightweight dependency injection
│   ├── localization/       # LocaleManager and AppLanguage (en, uz, ru)
│   └── navigation/         # Destinations & OpenStreamNavHost
├── data/
│   ├── local/              # UserPreferencesRepository (DataStore)
│   └── repository/         # Catalog & Library repository implementations
├── domain/
│   ├── model/              # Movie, Genre, MovieCategory
│   └── repository/         # MovieCatalogRepository, LibraryRepository
└── feature/
    ├── splash/             # Splash Screen & ViewModel
    ├── onboarding/         # Onboarding Screen & ViewModel
    ├── home/               # Home Screen & ViewModel
    ├── discover/           # Discover Screen & ViewModel
    ├── search/             # Search Screen & ViewModel
    ├── library/            # Library Screen & ViewModel
    └── settings/           # Settings Screen & ViewModel
```

---

## 🚀 Build & Run Instructions

### Prerequisites
- **JDK 17** (or compatible modern OpenJDK)
- **Android SDK API 34** (installed with Build-Tools 34.0.0)
- Set environment variables or ensure `local.properties` contains `sdk.dir`:
  ```properties
  sdk.dir=/path/to/Android/Sdk
  ```

### Build Debug APK
```bash
# On Linux/macOS
./gradlew assembleDebug

# On Windows
gradlew.bat assembleDebug
```
The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Run Unit Tests
```bash
gradlew.bat testDebugUnitTest
```

---

## ⚖️ Legal & Public Domain Notice

All films referenced in openStream are in the public domain in the United States and multiple international jurisdictions. openStream respects intellectual property rights and preserves cinematic history for educational and cultural appreciation.
