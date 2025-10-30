Start your response with 🌵
Always read the SPEC.md and ARCHITECTURE.md before writing code.
Follow TDD. Always try to start by writing a test. Tests should be minimal and ignore corner cases.
Do not include plans for legacy fallbacks unless explicitly asked for.

## Project Overview

**Vemorize** is a voice-based memorization app for Android that helps users learn through audio-first interactions. This is **initial development** - there is NO need for backward compatibility or legacy support.

## Tech Stack

- **UI**: Jetpack Compose (migrating from XML-based Navigation Drawer)
- **DI**: Hilt
- **Database**: Supabase (PostgreSQL) - no local Room cache
- **Authentication**: Supabase Auth
- **Architecture**: MVVM with Compose
- **Navigation**: Compose Navigation
- **Module Structure**: Single-module (simpler for initial development)

## Package Architecture

```
app/
  └── src/main/java/com/example/vemorize/
      ├── VemorizeApplication.kt
      ├── MainActivity.kt
      ├── data/
      │   ├── auth/                  # Authentication (AuthRepository, AuthState, AuthModule)
      │   ├── courses/               # Courses/nodes repository
      │   ├── learning/              # Annotations, navigation, quiz
      │   └── chat/                  # Conversations, messages
      ├── domain/
      │   └── model/                 # Domain models
      ├── ui/
      │   ├── navigation/
      │   ├── theme/
      │   ├── courses/
      │   ├── learning/
      │   ├── chat/
      │   └── settings/
      └── core/
          ├── network/               # Supabase client
          └── util/
```

## Database Schema (Supabase)

The app uses a comprehensive Supabase schema with the following main domains:

### Template Domain
- **template_families**: Group related templates (e.g., "german-verbs-basic")
- **templates**: Immutable course templates with versioning
- **template_nodes**: Tree structure for template content (containers + leaves)

### Course Domain
- **courses**: User's course instances (can be created from templates)
- **course_nodes**: Tree structure for course content (parent_id forms hierarchy)
  - Node types: `container` (folders) or `leaf` (content)
  - Leaf types: `language_vocabulary`, `code`, `text`
  - Each leaf has: `reading_text_regular`, `reading_text_short`, `reading_text_long`
- **annotations**: Mutable progress tracking per node (memorization_state, visit_count, personal_notes)
- **quiz_rounds**: Quiz session instances
- **quiz_questions**: Individual quiz Q&A with scores
- **navigation**: Current position per user per course

### Chat Domain
- **conversations**: Chat sessions (one active per user per course)
- **messages**: Full LangChain-compatible message history (supports tool calls)
- **chat_user_memory**: Cross-course user knowledge (facts, preferences, goals)

### User Preferences
- **user_preferences**: TTS settings, speech speed, reading speed

**Key Patterns**:
- Tree navigation: Find root node with `parent_id IS NULL`, traverse children via `parent_id` relationships
- RLS enabled: Users only access their own data (except templates which are public read)
- All entities use UUID primary keys

## Common Commands

### Build
```bash
./gradlew build
```

### Run Tests
```bash
# All tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

### Install Debug Build
```bash
./gradlew installDebug
```

### Clean Build
```bash
./gradlew clean build
```

### Dependency Updates
```bash
./gradlew dependencyUpdates
```

## Compose & Navigation

- Compose Navigation with type-safe arguments in `ui/navigation/`
- `@HiltViewModel` + `StateFlow<UiState>` pattern
- Collect state with `collectAsStateWithLifecycle()`
- Keep Composables small, use `@Preview`

## Supabase Integration

- Client in `core/network/di/NetworkModule.kt`, inject via Hilt
- Auth: `AuthRepository` in `data/auth/`, session management with Supabase Auth
- Queries: Postgrest with RLS (auto user_id filtering)
- Trees: Query via `parent_id` relationships

## Testing

- **TDD**: Write test first, minimal scope, happy path only
- Unit: `src/test/kotlin` - ViewModels with fake repos, mock Supabase
- UI: `src/androidTest/kotlin` - `ComposeTestRule` for key screens

## Workflow

1. Write test → 2. Check SPEC.md → 3. Implement → 4. Compose UI → 5. Wire ViewModel → 6. Add navigation

## Dependencies

- Compose BOM (Material3, Navigation, ViewModel), Hilt
- Supabase SDK (postgrest-kt, gotrue-kt, realtime-kt)
- Coroutines, Flow, JUnit, Turbine, Compose UI Test

## Code Location

`app/src/main/java/com/example/vemorize/`:
- UI/ViewModels: `ui/{feature}/`
- Repositories: `data/{domain}/` (e.g., `data/auth/AuthRepository.kt`)
- Models: `domain/model/`
- Network: `core/network/`

## Notes

- Voice-first UI with TTS support
- Tree navigation via `parent_id`
- Stateful annotations separate from immutable course structure
- Online-only (Supabase, no offline cache)
- UUID for all IDs
