TaxiMeterCore 0.8.5

Compile fix: MainActivity.kt refresh runnable schedules `refreshRunnable` rather than `this` from inside `lifecycleScope.launch`. `this` there is a CoroutineScope and is not a Runnable.

Room schema export remains enabled and KSP is configured with room.schemaLocation. A Room schema warning is non-fatal and is separate from the Kotlin compile error.

Build verification: source-level check only; a full Android Gradle build was not run in this environment.
