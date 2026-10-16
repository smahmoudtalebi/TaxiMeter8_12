# Build fix: Kotlin EditText singleLine

Fixed the Kotlin compile error at `MainActivity.kt` by replacing the unsupported synthetic property `singleLine = true` with the Android API call `setSingleLine(true)`. This affects only the single-line merchant-code field in payment QR settings.

The archive contains source projects only. It has not been compiled or tested on physical Android 6 devices in this environment.
