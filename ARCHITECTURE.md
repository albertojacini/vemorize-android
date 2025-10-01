## Guidelines

- Follow Domain-Driven Design in Bob Martin's Clean Architecture flavor (https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- Use Test-Driven Development (TDD) to develop features.
- Use a test-driven (TDD) approach to development. Write tests in `/app/src/test/` for unit tests and `/app/src/androidTest/` for instrumentation tests. Favor function-based tests over class-based tests when possible.

## Project Structure

### Android App Structure
- `/app/src/main/java/` - Main application code
  - `/data/` - Data layer implementation
    - `/database/` - Room database entities, DAOs, and database
    - `/repository/` - Repository implementations
    - `/remote/` - API services and network data sources
    - `/local/` - Local data sources and preferences
  - `/domain/` - Domain layer (business logic)
    - `/entity/` - Core business entities/models
    - `/usecase/` - Use cases (application business logic)
    - `/repository/` - Repository interfaces
  - `/presentation/` - Presentation layer (UI)
    - `/viewmodel/` - ViewModels for UI state management
    - `/ui/` - UI components (Activities, Fragments, Compose screens)
    - `/components/` - Reusable UI components
    - `/navigation/` - Navigation logic
  - `/di/` - Dependency injection modules (Hilt)
  - `/util/` - Utility classes and extensions

### Resource Structure
- `/app/src/main/res/` - Android resources
  - `/layout/` - XML layouts (if using traditional Views)
  - `/values/` - Strings, colors, dimensions, styles
  - `/drawable/` - Images and vector drawables
  - `/raw/` - Raw assets like audio files

### Test Structure
- `/app/src/test/` - Unit tests (JUnit)
- `/app/src/androidTest/` - Instrumentation tests (Espresso, Compose testing)

### Build Configuration
- `/app/build.gradle.kts` - App module build configuration
- `/build.gradle.kts` - Project-level build configuration
- `/gradle/` - Gradle wrapper and dependencies

This structure follows Android best practices while maintaining Clean Architecture principles with clear separation of concerns between data, domain, and presentation layers.