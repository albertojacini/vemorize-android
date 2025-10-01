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
- **Module Structure**: Multi-module

## Module Architecture

The app follows a multi-module structure:

```
app/                    # Main application module, Hilt setup, MainActivity
feature/
  ├── auth/            # Authentication screens (login, signup)
  ├── courses/         # Course listing and management
  ├── learning/        # Learning sessions, reading, quiz
  ├── chat/            # Chat/conversation features
  └── settings/        # User preferences
core/
  ├── ui/              # Shared Compose components, theme
  ├── network/         # Supabase client setup
  ├── model/           # Domain models matching database schema
  └── common/          # Utilities, extensions
data/
  ├── auth/            # Authentication repository
  ├── courses/         # Courses/nodes repository
  ├── learning/        # Annotations, navigation, quiz repository
  └── chat/            # Conversations, messages repository
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

# Specific module
./gradlew :feature:auth:test

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

## Compose & Navigation Patterns

### Navigation Structure
- Use Compose Navigation with type-safe arguments
- Define navigation graphs per feature module
- Main navigation graph in `app` module composes feature graphs

### ViewModel Pattern
```kotlin
@HiltViewModel
class CourseListViewModel @Inject constructor(
    private val coursesRepository: CoursesRepository
) : ViewModel() {
    val uiState: StateFlow<UiState> = ...
}
```

### UI State Management
- Use `StateFlow` for UI state in ViewModels
- Collect state in Composables using `collectAsStateWithLifecycle()`
- Model UI state as sealed classes/data classes

### Composable Structure
- Keep Composables small and focused
- Extract preview-friendly components
- Use `@Preview` annotations liberally

## Supabase Integration

### Client Setup
- Supabase client configured in `core:network` module
- Inject `SupabaseClient` via Hilt
- Use Supabase Kotlin SDK for all database operations

### Authentication Flow
- Supabase Auth handles login/signup/session management
- Store auth state in `AuthRepository`
- Protect routes with auth checks in navigation

### Database Queries
- Use Supabase Postgrest for queries
- Leverage RLS - no need to filter by user_id (handled automatically)
- Handle tree structures by querying nodes with parent relationships

### Real-time (Future)
- Supabase real-time can be added for live updates
- Not required for initial implementation

## Testing Strategy

### Unit Tests (Required)
- Test ViewModels with fake repositories
- Test repositories with mock Supabase client
- Test domain logic in isolation
- Place in `src/test/kotlin`

### Integration Tests
- Test Supabase queries with test database (optional for now)
- Focus on critical paths

### UI Tests
- Compose UI tests for key screens
- Use `ComposeTestRule` and semantics
- Place in `src/androidTest/kotlin`

### Test Approach
- **TDD**: Write test first when possible
- **Minimal scope**: Test happy path, ignore edge cases initially
- **Fast feedback**: Prioritize unit tests over instrumented tests

## Development Workflow

1. **Start with test**: Write a failing test for the feature
2. **Check schema**: Reference SPEC.md and database schema
3. **Implement**: Build minimal implementation to pass test
4. **Compose UI**: Build Composable with preview
5. **Wire ViewModel**: Connect UI state to ViewModel
6. **Integrate navigation**: Add route to navigation graph

## Migration from XML to Compose

The current codebase has XML-based UI with Navigation Drawer. Migration strategy:
1. Keep `MainActivity` but replace content with Compose
2. Convert fragments to Composable screens
3. Replace Navigation Component XML with Compose Navigation
4. Remove View Binding after full migration
5. Convert drawer to Compose ModalNavigationDrawer or NavigationRail

## Key Dependencies (Expected)

Add these to `gradle/libs.versions.toml`:
- Compose BOM (Material3, Navigation, ViewModel)
- Hilt (Android, Compose Navigation integration)
- Supabase Kotlin SDK (postgrest-kt, gotrue-kt, realtime-kt)
- Coil for image loading (if needed)
- Kotlin Coroutines & Flow
- JUnit, Turbine (for testing Flows)
- Compose UI Test

## Code Location

- **Feature code**: `feature/{feature-name}/src/main/kotlin/com/example/vemorize/{feature}`
- **Repositories**: `data/{domain}/src/main/kotlin/com/example/vemorize/data/{domain}`
- **Models**: `core/model/src/main/kotlin/com/example/vemorize/core/model`
- **Shared UI**: `core/ui/src/main/kotlin/com/example/vemorize/core/ui`

## Important Notes

- **Voice-first**: UI should support TTS interaction patterns
- **Tree navigation**: Course nodes form trees - always traverse via parent_id
- **Stateful annotations**: Progress tracking is mutable and separate from immutable course structure
- **No offline**: Initial version requires network connection (Supabase only)
- **UUID everywhere**: All IDs are UUIDs, generate with `UUID.randomUUID()` or use Supabase defaults
