# Vemorize Android Architecture

## Overview

Vemorize is a voice-based memorization app built with **Jetpack Compose**, **Hilt**, and **Supabase**. The architecture follows **Clean Architecture** principles with clear separation between domain, data, and UI layers.

## Key Architectural Principles

1. **Two Backend Types**: Clear separation between Supabase DB (Postgrest) and Vemorize API (Edge Functions)
2. **Three Domains**: auth, courses, chat - each with clear boundaries
3. **Repository Pattern**: All data access through repository interfaces
4. **Dependency Injection**: Hilt provides all dependencies
5. **Unidirectional Data Flow**: ViewModels expose StateFlow, UI observes and reacts

## Package Structure

```
app/src/main/java/com/example/vemorize/
├── VemorizeApplication.kt              # Application class
├── MainActivity.kt                     # Single activity host
│
├── domain/                             # Business logic & models
│   ├── auth/                           # Auth domain
│   │   └── User.kt                     # Domain model
│   ├── courses/                        # Courses domain
│   │   ├── Course.kt                   # Course model
│   │   ├── CourseNode.kt              # Tree node model
│   │   ├── CourseTree.kt              # Tree structure
│   │   ├── CourseTypes.kt             # Enums (NodeType, LeafType)
│   │   └── Annotation.kt              # Learning progress tracking
│   └── chat/                           # Chat domain
│       ├── ChatManager.kt              # Chat orchestration
│       ├── modes/                      # Mode handlers
│       │   ├── BaseModeHandler.kt     # Base handler
│       │   ├── IdleModeHandler.kt
│       │   ├── ReadingModeHandler.kt
│       │   └── QuizModeHandler.kt
│       ├── actions/                    # Tool execution
│       │   ├── Actions.kt             # Action implementations
│       │   └── ToolRegistry.kt        # Tool discovery
│       ├── commands/                   # Voice commands
│       │   ├── VoiceCommand.kt        # Command interface
│       │   └── CommandTypes.kt        # Command DTOs
│       ├── managers/                   # Sub-managers
│       │   ├── NavigationManager.kt
│       │   ├── UserMemoryManager.kt
│       │   └── UserPreferencesManager.kt
│       └── model/                      # Chat models
│           ├── ChatMode.kt            # Enum: IDLE, READING, QUIZ
│           ├── Conversation.kt        # DB entity
│           ├── Navigation.kt          # Current position
│           ├── UserMemory.kt          # Cross-course knowledge
│           ├── UserPreferences.kt     # TTS settings
│           └── ChatResponses.kt       # Domain response types
│
├── data/                               # Data layer
│   ├── auth/                           # Auth repositories
│   │   ├── AuthRepository.kt          # Interface
│   │   ├── SupabaseAuthRepositoryImpl.kt
│   │   ├── AuthState.kt               # Sealed class
│   │   └── di/AuthModule.kt           # Hilt bindings
│   │
│   ├── courses/                        # Courses repositories
│   │   ├── CoursesRepository.kt       # Interface
│   │   ├── SupabaseCoursesRepositoryImpl.kt
│   │   ├── CourseTreeRepository.kt    # Interface
│   │   ├── SupabaseCourseTreeRepositoryImpl.kt
│   │   ├── AnnotationRepository.kt    # Interface
│   │   ├── SupabaseAnnotationRepositoryImpl.kt
│   │   ├── CoursesUiState.kt          # UI state
│   │   └── di/CoursesModule.kt        # Hilt bindings
│   │
│   ├── chat/                           # Chat repositories
│   │   ├── ConversationRepository.kt  # Interface
│   │   ├── VemorizeApiConversationRepositoryImpl.kt
│   │   ├── NavigationRepository.kt    # Interface
│   │   ├── SupabaseNavigationRepositoryImpl.kt
│   │   ├── UserPreferencesRepository.kt  # Interface
│   │   ├── SupabaseUserPreferencesRepositoryImpl.kt
│   │   ├── UserMemoryRepository.kt    # Interface
│   │   ├── SupabaseUserMemoryRepositoryImpl.kt
│   │   └── di/ChatModule.kt           # Hilt bindings
│   │
│   └── clients/                        # Backend clients
│       └── vemorize_api/              # Vemorize API client
│           ├── VemorizeApiClient.kt   # HTTP client
│           └── dto/                    # API DTOs
│               └── ChatApiDtos.kt     # API payloads
│
├── ui/                                 # Presentation layer
│   ├── navigation/
│   │   └── VemorizeNavigation.kt      # Nav graph + drawer
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── auth/
│   │   └── LoginScreen.kt
│   ├── courses/
│   │   ├── CoursesScreen.kt
│   │   ├── CoursesViewModel.kt
│   │   ├── CoursesUiState.kt
│   │   ├── CourseDetailScreen.kt
│   │   └── CourseDetailViewModel.kt
│   └── chat/
│       ├── ChatScreen.kt
│       ├── ChatViewModel.kt
│       └── ChatUiState.kt
│
└── core/                               # Core utilities
    ├── network/
    │   └── di/NetworkModule.kt        # Supabase client
    ├── di/
    │   └── ApplicationModule.kt       # App-level deps
    └── util/                           # Utilities
```

## Domain Architecture

### Three Core Domains

#### 1. **Auth Domain**
- **Purpose**: User authentication and session management
- **Models**: `User`, `AuthState`
- **Repository**: `AuthRepository` → `SupabaseAuthRepositoryImpl`
- **Backend**: Supabase Auth

#### 2. **Courses Domain**
- **Purpose**: Course content, structure, and learning progress
- **Subdomains**:
  - **Courses**: Course metadata (`Course`)
  - **Course Trees**: Hierarchical content (`CourseNode`, `CourseTree`)
  - **Annotations**: Learning progress (`Annotation`, `MemorizationState`)
- **Repositories**:
  - `CoursesRepository` → `SupabaseCoursesRepositoryImpl`
  - `CourseTreeRepository` → `SupabaseCourseTreeRepositoryImpl`
  - `AnnotationRepository` → `SupabaseAnnotationRepositoryImpl`
- **Backend**: Supabase DB (Postgrest)

#### 3. **Chat Domain**
- **Purpose**: Voice-based chat interactions and LLM conversations
- **Subdomains**:
  - **Conversations**: LLM interactions (`ConversationRepository`)
  - **Navigation**: Current position tracking (`Navigation`)
  - **User Memory**: Cross-course knowledge (`UserMemory`)
  - **User Preferences**: TTS settings (`UserPreferences`)
- **Repositories**:
  - `ConversationRepository` → `VemorizeApiConversationRepositoryImpl` (uses Vemorize API)
  - `NavigationRepository` → `SupabaseNavigationRepositoryImpl` (uses Supabase DB)
  - `UserMemoryRepository` → `SupabaseUserMemoryRepositoryImpl` (uses Supabase DB)
  - `UserPreferencesRepository` → `SupabaseUserPreferencesRepositoryImpl` (uses Supabase DB)
- **Backend**: Mixed (Vemorize API for LLM, Supabase DB for state)

## Backend Integration

### Two Backend Types

#### 1. **Supabase DB (Postgrest)**
- **Purpose**: Direct database access for CRUD operations
- **Pattern**: Repository → Postgrest client → PostgreSQL
- **Repositories**:
  - `SupabaseAuthRepositoryImpl` (Auth)
  - `SupabaseCoursesRepositoryImpl` (Courses)
  - `SupabaseCourseTreeRepositoryImpl` (Course structure)
  - `SupabaseAnnotationRepositoryImpl` (Learning progress)
  - `SupabaseNavigationRepositoryImpl` (Current position)
  - `SupabaseUserMemoryRepositoryImpl` (User knowledge)
  - `SupabaseUserPreferencesRepositoryImpl` (TTS settings)
- **Naming Convention**: `Supabase*RepositoryImpl`
- **Security**: Row Level Security (RLS) policies

#### 2. **Vemorize API (Supabase Edge Functions)**
- **Purpose**: Business logic, LLM orchestration, complex operations
- **Pattern**: Repository → VemorizeApiClient → HTTP → Edge Function
- **Client**: `VemorizeApiClient` (in `data/clients/vemorize_api/`)
- **Repositories**:
  - `VemorizeApiConversationRepositoryImpl` (LLM conversations)
- **Naming Convention**: `VemorizeApi*RepositoryImpl`
- **DTOs**: Isolated in `data/clients/vemorize_api/dto/`

### Backend Selection Guide

| Use Case | Backend | Reason |
|----------|---------|--------|
| Simple CRUD | Supabase DB | Direct, fast, RLS security |
| LLM calls | Vemorize API | Complex orchestration, tool calls |
| Validation logic | Vemorize API | Centralized business rules |
| User data queries | Supabase DB | Simple filtering, RLS |
| Multi-step workflows | Vemorize API | Transaction control |

## Repository Naming Conventions

### Pattern: `{Backend}{Domain}RepositoryImpl`

Examples:
- ✅ `SupabaseCoursesRepositoryImpl` - Uses Supabase DB
- ✅ `VemorizeApiConversationRepositoryImpl` - Uses Vemorize API
- ❌ `CoursesRepositoryImpl` - Unclear which backend
- ❌ `ChatApiClient` - Not a repository (this is the HTTP client)

### File Organization

```
data/
├── {domain}/
│   ├── {Feature}Repository.kt                    # Interface
│   ├── Supabase{Feature}RepositoryImpl.kt       # Supabase DB impl
│   ├── VemorizeApi{Feature}RepositoryImpl.kt    # Vemorize API impl
│   └── di/{Domain}Module.kt                      # Hilt bindings
└── clients/
    └── vemorize_api/
        ├── VemorizeApiClient.kt                  # HTTP client
        └── dto/{Feature}Dtos.kt                  # API payloads
```

## Data Flow

### Typical Flow (Supabase DB)

```
UI (Composable)
    ↓ observes StateFlow
ViewModel
    ↓ calls repository
Repository Interface (e.g., CoursesRepository)
    ↓ implements
SupabaseCoursesRepositoryImpl
    ↓ uses
Postgrest Client (injected)
    ↓ HTTP
Supabase PostgreSQL (with RLS)
```

### LLM Flow (Vemorize API)

```
UI (Composable)
    ↓ user speaks
ChatViewModel
    ↓ delegates to
ChatManager
    ↓ uses current mode
ReadingModeHandler (extends BaseModeHandler)
    ↓ calls
ConversationRepository
    ↓ implements
VemorizeApiConversationRepositoryImpl
    ↓ delegates to
VemorizeApiClient
    ↓ HTTP POST
Supabase Edge Function (chat-llm)
    ↓ calls
OpenAI/Anthropic
    ↓ returns
Tool calls (ToolCall[])
    ↓ executed by
ToolRegistry + Actions
    ↓ updates
Navigation/Memory/Preferences (via repositories)
```

## Message Types (Critical Distinction!)

⚠️ **Three Different "Message" Concepts**:

1. **VoiceExchangeMessage** (`ui/chat/`)
   - **Where**: UI layer only
   - **Purpose**: Ephemeral UI display (chat bubbles)
   - **Lifecycle**: Cleared on screen rotation
   - **Example**: `VoiceExchangeMessage(text = "Hello", role = ASSISTANT)`

2. **Conversation** (`domain/chat/model/`)
   - **Where**: Both Android client and backend
   - **Purpose**: DB entity + API endpoint concept
   - **Lifecycle**: Persisted in Supabase DB
   - **Example**: `Conversation(id, userId, courseId, mode, createdAt)`

3. **LLM Messages** (Backend only)
   - **Where**: Backend edge functions ONLY
   - **Purpose**: Full LangChain message history
   - **Format**: `{role: 'user' | 'assistant' | 'tool', content: string, tool_calls?: []}`
   - **Android does NOT store or manipulate these** - they stay in backend

## Key Architectural Patterns

### 1. Repository Pattern
- All data access through interfaces
- Implementations are swappable
- Naming clearly indicates backend type

### 2. Dependency Inversion
- Domain layer defines repository interfaces
- Data layer provides implementations
- UI depends only on ViewModels
- ViewModels depend only on repository interfaces

### 3. Single Source of Truth
- ViewModels expose `StateFlow<UiState>`
- UI is read-only (no direct state mutation)
- All updates flow through repositories

### 4. Mode Handler Strategy Pattern
- `ChatManager` delegates to mode-specific handlers
- Each mode (IDLE, READING, QUIZ) has its own logic
- Handlers can invoke LLM with mode-specific tools

### 5. Hilt Scoping
- `@Singleton`: Repositories, API clients
- `@ViewModelScoped`: ChatManager, NavigationManager
- `@HiltViewModel`: All ViewModels

## Testing Strategy

### Unit Tests (`src/test/`)
- **ViewModels**: Mock repositories
- **Repositories**: Mock Supabase/HTTP clients
- **Domain Logic**: Pure Kotlin tests (e.g., `CourseTree.fromNodes()`)

### UI Tests (`src/androidTest/`)
- **Key Screens**: `ComposeTestRule` for critical flows
- **Navigation**: Verify routing works
- **State**: Verify UI reacts to ViewModel state

## Security

### Row Level Security (RLS)
- All Supabase tables have RLS policies
- Users can only access their own data
- `user_id` filtering is automatic

### Authentication
- JWT tokens managed by Supabase Auth
- Session refresh handled automatically
- No credentials stored locally

## Build Configuration

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34
- **Kotlin**: 1.9.x
- **Compose**: BOM-based versioning

## Dependencies

### Core
- Jetpack Compose (UI toolkit)
- Hilt (Dependency Injection)
- Coroutines + Flow (Async)
- Navigation Compose (Routing)

### Backend
- Supabase Kotlin SDK (postgrest-kt, gotrue-kt, realtime-kt)
- Ktor (HTTP client)
- Kotlinx Serialization (JSON)

### Testing
- JUnit 4 (Unit tests)
- Turbine (Flow testing)
- Compose UI Test (Integration tests)
- MockK (Mocking)

## Migration Notes

### From Old Structure
- ❌ `domain/model/` → ✅ `domain/{domain}/` (flattened)
- ❌ `ChatApiClient` → ✅ `VemorizeApiClient` (clearer naming)
- ❌ `*RepositoryImpl` → ✅ `Supabase*RepositoryImpl` | `VemorizeApi*RepositoryImpl` (backend-specific)
- ❌ Direct client injection → ✅ Repository interfaces (testability)

### Legacy Files Removed
- `ui/home/` (XML-based)
- `ui/gallery/` (XML-based)
- `ui/slideshow/` (XML-based)
- `ui/screens/HomeScreen.kt` (old Compose)

## Future Considerations

### Potential Additions
- **Offline Support**: Room database cache layer
- **Multi-module**: Split by feature when team grows
- **Compose Multiplatform**: Share UI with iOS/Desktop
- **WebSocket**: Real-time collaboration via Supabase Realtime

### Avoided Over-Engineering
- No clean architecture "use cases" layer (repositories are sufficient)
- No separate "data" and "domain" modules (single-module is simpler)
- No reactive DB (Room) - Supabase is source of truth
- No complex caching (online-only for initial development)

## References

- [Supabase Kotlin Docs](https://supabase.com/docs/reference/kotlin)
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Hilt Docs](https://dagger.dev/hilt/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
