# Student Registration App (Android)

Mobile Apps coursework, BSc in Computing, Dorset College Dublin (2026).

An Android app written in Kotlin with Jetpack Compose that registers students and finds them by ID, with data stored locally in a Room database.

## Features

- Register a student with name, course and year; input is validated and trimmed before saving.
- Search for a student by ID and show their details, or a clear "not found" message.
- Invalid input (empty names, non-numeric or out-of-range years, invalid IDs) shows an error and never reaches the database.
- Duplicate taps while saving are ignored, and database failures are reported without claiming success.

## Architecture

```
UI (Compose screens)  →  StudentViewModel  →  StudentDao  →  Room database (students table)
```

- `RegistrationScreen`, `StudentSearchScreen`, `HomeScreen` and `AppNavigation` (Navigation Compose).
- `StudentViewModel` holds UI state, validates input and calls the DAO with coroutines.
- `data/Student.kt` (entity), `StudentDao.kt` (insert, query by ID), `StudentDatabase.kt`.

## Tests

12 automated tests:

| Suite | Type | Covers |
|---|---|---|
| `StudentViewModelTest` | Unit | validation rules, boundary years, trimming, database errors, duplicate taps |
| `StudentDatabaseTest` | Instrumented | generated IDs, lookup by ID, data persisting after the database is reopened |
| `StudentAppTest` | Compose UI | full register-then-search flow and error messages |
| `MainActivityLifecycleTest` | Instrumented | state survives activity recreation |

## Run

Open the project in Android Studio (minSdk 24) and run the `app` configuration.

```bash
./gradlew testDebugUnitTest          # unit tests
./gradlew connectedDebugAndroidTest  # instrumented and UI tests (emulator or device)
```
