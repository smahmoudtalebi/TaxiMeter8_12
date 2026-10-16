# Fix 0.8.6 — Kotlin refresh Runnable type inference

- Declared `refreshRunnable` explicitly as `Runnable`.
- Captured the active Runnable as a locally typed `scheduledTask` inside `run()` and used that local in `Handler.postDelayed`, avoiding a self-reference to the property during its initializer.
- The Room schema export message remains a warning and is unrelated to the reported Kotlin compilation failure.

Build status: source-level fix applied; a Gradle/Android build was not run in this environment because the project does not include a Gradle wrapper and a Gradle executable/Android SDK was not found. Verify with GitHub Actions.
